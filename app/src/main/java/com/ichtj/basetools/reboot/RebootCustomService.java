package com.ichtj.basetools.reboot;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;

import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.ichtj.basetools.R;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RebootCustomService extends Service {
    private static final int NOTIFICATION_ID = 98;
    private static final String NOTIFICATION_CHANNEL_ID = "scheduled_reboot";
    private static final String GPIO_COMMAND =
            "echo \"11\" > /sys/class/fib_gpio/gpio_state";

    private final Object timerLock = new Object();
    private final AtomicBoolean requestingReboot = new AtomicBoolean(false);
    private final TestBinder binder = new TestBinder();
    private Disposable cycleTimer;
    private Disposable confirmationTimer;
    private long cycleGeneration;
    private long scheduledTriggerElapsedMs;
    private boolean foregroundStarted;
    private boolean runtimePaused;
    private String runtimePauseReason = "";

    @Override
    public void onCreate() {
        super.onCreate();
        showNotification("服务运行中...", true);
        if (!RebootStateStore.isEnabled(this)) {
            stopForeground(true);
            foregroundStarted = false;
            stopSelf();
            return;
        }
        audit("SERVICE_CREATED", "", "cycleSeconds=" + cycleSeconds());
        startCycle();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!RebootStateStore.isEnabled(this)) {
            stopForeground(true);
            foregroundStarted = false;
            stopSelf();
            return START_NOT_STICKY;
        }
        startCycle();
        return START_STICKY;
    }

    public static void handleBootCompleted(Context context) {
        Context app = context.getApplicationContext();
        long receivedWallTime = System.currentTimeMillis();
        long receivedElapsedTime = SystemClock.elapsedRealtime();
        int bootCount = RebootStateStore.readBootCount(app);
        String bootId = RebootStateStore.readBootId();
        RebootStateStore.PendingTask task = RebootStateStore.getPendingTask(app);
        String taskId = task == null ? "" : task.taskId;
        long oldCount = RebootStateStore.getSuccessCount(app);
        RebootLogStore.info("BOOT_COMPLETED_RECEIVED", taskId, bootCount, oldCount,
                "bootId=" + bootId + ", hasPendingTask=" + (task != null));

        RebootStateStore.BootResult result = RebootStateStore.handleBootCompleted(
                app, receivedWallTime, receivedElapsedTime, bootCount, bootId);
        String detail = "reason=" + result.confirmation.status
                + ", elapsedMs=" + result.confirmation.elapsedMs
                + ", detail=" + result.confirmation.detail;
        if (!result.persisted) {
            RebootLogStore.error("BOOT_STATE_SAVE_FAILED", taskId, bootCount, oldCount,
                    detail, null);
        } else if (result.duplicate) {
            RebootLogStore.info("BOOT_COMPLETED_DUPLICATE", taskId, bootCount,
                    result.successCount, detail);
        } else if (result.confirmation.isSuccess()) {
            RebootLogStore.info("REBOOT_CONFIRMED", taskId, bootCount,
                    result.successCount, detail);
        } else {
            RebootLogStore.info("REBOOT_NOT_COUNTED", taskId, bootCount,
                    result.successCount, detail);
        }
        if (RebootStateStore.isEnabled(app)) {
            startServiceCompat(app);
        }
    }

    public static void startServiceCompat(Context context) {
        if (!RebootStateStore.isEnabled(context)) {
            return;
        }
        try {
            ContextCompat.startForegroundService(context,
                    new Intent(context, RebootCustomService.class));
        } catch (Throwable throwable) {
            RebootLogStore.error("SERVICE_START_FAILED", "",
                    RebootStateStore.readBootCount(context),
                    RebootStateStore.getSuccessCount(context),
                    throwable.getMessage(), throwable);
        }
    }

    public static boolean enableTask(Context context) {
        Context app = context.getApplicationContext();
        int bootCount = RebootStateStore.readBootCount(app);
        String marker = RebootStateStore.createBootMarker(
                bootCount, RebootStateStore.readBootId());
        boolean saved = RebootStateStore.enable(
                app, marker, SystemClock.elapsedRealtime());
        if (!saved) {
            return false;
        }
        RebootLogStore.info("TASK_ENABLED", "", bootCount,
                RebootStateStore.getSuccessCount(app), "Timer enabled by user");
        startServiceCompat(app);
        return true;
    }

    public static boolean disableTask(Context context) {
        Context app = context.getApplicationContext();
        if (!RebootStateStore.disable(app)) {
            return false;
        }
        app.stopService(new Intent(app, RebootCustomService.class));
        RebootLogStore.info("TASK_DISABLED", "", RebootStateStore.readBootCount(app),
                RebootStateStore.getSuccessCount(app), "Timer disabled by user");
        return true;
    }

    public void startCycle() {
        synchronized (timerLock) {
            if (!RebootStateStore.isEnabled(this)) {
                stopCycleLocked();
                stopForeground(true);
                foregroundStarted = false;
                stopSelf();
                return;
            }
            if (runtimePaused || RebootStateStore.isPaused(this)) {
                cycleGeneration++;
                dispose(cycleTimer);
                dispose(confirmationTimer);
                cycleTimer = null;
                confirmationTimer = null;
                scheduledTriggerElapsedMs = 0L;
                requestingReboot.set(false);
                String reason = runtimePaused ? runtimePauseReason
                        : RebootStateStore.getPauseReason(this);
                showNotification("任务已暂停：" + summarize(reason));
                return;
            }
            RebootStateStore.PendingTask pending = RebootStateStore.getPendingTask(this);
            if (pending == null && active(confirmationTimer)) {
                dispose(confirmationTimer);
                confirmationTimer = null;
                requestingReboot.set(false);
            }
            if (pending != null) {
                if (active(cycleTimer)) {
                    cycleGeneration++;
                    dispose(cycleTimer);
                    cycleTimer = null;
                    scheduledTriggerElapsedMs = 0L;
                }
                if (!active(confirmationTimer)) {
                    scheduleConfirmationTimeout(pending);
                }
                return;
            }

            int bootCount = RebootStateStore.readBootCount(this);
            String marker = RebootStateStore.createBootMarker(
                    bootCount, RebootStateStore.readBootId());
            long now = SystemClock.elapsedRealtime();
            RebootStateStore.ScheduleResult schedule = RebootStateStore.getOrCreateSchedule(
                    this, marker, now);
            if (!RebootStateStore.isEnabled(this)) {
                RebootStateStore.disable(this);
                stopCycleLocked();
                stopForeground(true);
                foregroundStarted = false;
                stopSelf();
                return;
            }
            if (!schedule.persisted) {
                showNotification("定时配置保存失败");
                error("SCHEDULE_SAVE_FAILED", "", "Timer was not started", null);
                return;
            }
            if (active(cycleTimer)
                    && scheduledTriggerElapsedMs == schedule.nextTriggerElapsedMs) {
                return;
            }
            if (active(cycleTimer)) {
                cycleGeneration++;
                dispose(cycleTimer);
                cycleTimer = null;
                audit("TIMER_RESCHEDULED", "",
                        "oldTriggerElapsedMs=" + scheduledTriggerElapsedMs
                                + ", newTriggerElapsedMs=" + schedule.nextTriggerElapsedMs);
            }
            long delay = Math.max(0L, schedule.nextTriggerElapsedMs - now);
            audit("TIMER_STARTED", "", "cycleSeconds=" + cycleSeconds()
                    + ", delayMs=" + delay + ", newSchedule=" + schedule.created);
            showNotification("下次重启倒计时 " + formatDuration(delay));
            final long generation = ++cycleGeneration;
            scheduledTriggerElapsedMs = schedule.nextTriggerElapsedMs;
            cycleTimer = Observable.timer(delay, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io())
                    .subscribe(ignored -> requestReboot(generation), throwable -> {
                        synchronized (timerLock) {
                            cycleTimer = null;
                            scheduledTriggerElapsedMs = 0L;
                        }
                        error("TIMER_ERROR", "", throwable.getMessage(), throwable);
                    });
        }
    }

    public boolean updateCycleSeconds(int seconds) {
        final int oldSeconds;
        final boolean saved;
        synchronized (timerLock) {
            if (seconds <= 0 || requestingReboot.get()
                    || RebootStateStore.getPendingTask(this) != null) {
                return false;
            }
            oldSeconds = cycleSeconds();
            cycleGeneration++;
            dispose(cycleTimer);
            cycleTimer = null;
            scheduledTriggerElapsedMs = 0L;
            int bootCount = RebootStateStore.readBootCount(this);
            String marker = RebootStateStore.createBootMarker(
                    bootCount, RebootStateStore.readBootId());
            saved = RebootStateStore.updateCycleAndSchedule(
                    this, seconds, marker, SystemClock.elapsedRealtime());
        }
        if (!saved) {
            error("CYCLE_UPDATE_FAILED", "", "requestedSeconds=" + seconds, null);
            startCycle();
            return false;
        }
        audit("CYCLE_UPDATED", "", "oldSeconds=" + oldSeconds + ", newSeconds=" + seconds);
        if (RebootStateStore.isEnabled(this)) {
            startCycle();
        }
        return true;
    }

    public boolean setTaskEnabled(boolean enabled) {
        if (enabled) {
            synchronized (timerLock) {
                if (requestingReboot.get()
                        || RebootStateStore.getPendingTask(this) != null) {
                    return false;
                }
                int bootCount = RebootStateStore.readBootCount(this);
                String marker = RebootStateStore.createBootMarker(
                        bootCount, RebootStateStore.readBootId());
                if (!RebootStateStore.enable(
                        this, marker, SystemClock.elapsedRealtime())) {
                    error("TASK_ENABLE_FAILED", "",
                            "Enabled state could not be persisted", null);
                    return false;
                }
                runtimePaused = false;
                runtimePauseReason = "";
                requestingReboot.set(false);
                stopCycleLocked();
            }
            audit("TASK_ENABLED", "", "Timer enabled by user");
            startCycle();
            return true;
        }

        synchronized (timerLock) {
            if (requestingReboot.get()
                    || RebootStateStore.getPendingTask(this) != null) {
                return false;
            }
            if (!RebootStateStore.disable(this)) {
                error("TASK_DISABLE_FAILED", "",
                        "Disabled state could not be persisted", null);
                return false;
            }
            runtimePaused = false;
            runtimePauseReason = "";
            stopCycleLocked();
            requestingReboot.set(false);
        }
        stopForeground(true);
        foregroundStarted = false;
        audit("TASK_DISABLED", "", "Timer disabled by user");
        stopSelf();
        return true;
    }

    public boolean resumeCycle() {
        int bootCount = RebootStateStore.readBootCount(this);
        String marker = RebootStateStore.createBootMarker(
                bootCount, RebootStateStore.readBootId());
        boolean saved = RebootStateStore.resume(
                this, marker, SystemClock.elapsedRealtime());
        if (!saved) {
            error("TASK_RESUME_FAILED", "", "Resume state could not be persisted", null);
            return false;
        }
        synchronized (timerLock) {
            runtimePaused = false;
            runtimePauseReason = "";
            requestingReboot.set(false);
            cycleGeneration++;
            dispose(cycleTimer);
            dispose(confirmationTimer);
            cycleTimer = null;
            confirmationTimer = null;
            scheduledTriggerElapsedMs = 0L;
        }
        audit("TASK_RESUMED", "", "Timer resumed by user");
        startCycle();
        return true;
    }

    public boolean isTaskPaused() {
        return runtimePaused || RebootStateStore.isPaused(this);
    }

    public boolean isRebootInProgress() {
        return requestingReboot.get() || RebootStateStore.getPendingTask(this) != null;
    }

    public String getTaskPauseReason() {
        return runtimePaused ? runtimePauseReason : RebootStateStore.getPauseReason(this);
    }

    public void stopCycle() {
        synchronized (timerLock) {
            stopCycleLocked();
        }
    }

    private void stopCycleLocked() {
        cycleGeneration++;
        dispose(cycleTimer);
        dispose(confirmationTimer);
        cycleTimer = null;
        scheduledTriggerElapsedMs = 0L;
        confirmationTimer = null;
    }

    private void requestReboot(long generation) {
        boolean acquired;
        synchronized (timerLock) {
            if (generation != cycleGeneration || !RebootStateStore.isEnabled(this)) {
                return;
            }
            cycleTimer = null;
            scheduledTriggerElapsedMs = 0L;
            acquired = requestingReboot.compareAndSet(false, true);
        }
        if (!acquired) {
            audit("REBOOT_REQUEST_SKIPPED", "", "Request already in progress");
            return;
        }

        int bootCount = RebootStateStore.readBootCount(this);
        String bootId = RebootStateStore.readBootId();
        long successCount = RebootStateStore.getSuccessCount(this);
        String taskId = UUID.randomUUID().toString();
        long requestTime = System.currentTimeMillis();
        RebootLogStore.WriteResult preflight = RebootLogStore.info(
                "TIMER_ELAPSED", taskId, bootCount, successCount,
                "requestTimeMs=" + requestTime + ", bootId=" + bootId);
        if (!preflight.success) {
            pauseTask(taskId, "Persistent log unavailable: " + preflight.errorMessage, null);
            return;
        }

        RebootStateStore.PendingTask task = new RebootStateStore.PendingTask(
                taskId, requestTime, bootCount, bootId);
        if (!RebootStateStore.savePendingTask(this, task)) {
            if (!RebootStateStore.isEnabled(this)) {
                requestingReboot.set(false);
                audit("REBOOT_REQUEST_CANCELLED", taskId,
                        "Task was disabled before the reboot request was persisted");
                stopForeground(true);
                foregroundStarted = false;
                stopSelf();
                return;
            }
            pauseTask(taskId, "Pending task could not be persisted", null);
            return;
        }
        RebootLogStore.WriteResult persisted = RebootLogStore.info(
                "REBOOT_REQUEST_PERSISTED", taskId, bootCount, successCount,
                "confirmWindowMs=" + RebootConfirmationPolicy.CONFIRM_WINDOW_MS
                        + ", logFile=" + preflight.filePath);
        if (!persisted.success) {
            pauseTask(taskId, "Request log unavailable: " + persisted.errorMessage, null);
            return;
        }

        synchronized (timerLock) {
            scheduleConfirmationTimeout(task);
        }
        showNotification("正在请求重启");
        audit("GPIO_COMMAND_STARTED", taskId, GPIO_COMMAND);
        ShellUtils.CommandResult gpio = ShellUtils.execCompatibleRoot(GPIO_COMMAND);
        audit("GPIO_COMMAND_FINISHED", taskId, commandDetail(gpio));

        RebootLogStore.WriteResult dispatch = RebootLogStore.info(
                "POWER_REBOOT_DISPATCH", taskId, bootCount, successCount,
                "Calling PowerManager.reboot");
        if (!dispatch.success) {
            pauseTask(taskId, "Dispatch log unavailable: " + dispatch.errorMessage, null);
            return;
        }
        dispatchReboot(taskId);
    }

    private void dispatchReboot(String taskId) {
        try {
            PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (manager == null) {
                throw new IllegalStateException("PowerManager unavailable");
            }
            manager.reboot(null);
            audit("POWER_REBOOT_RETURNED", taskId, "Waiting for BOOT_COMPLETED");
        } catch (Throwable powerError) {
            error("POWER_REBOOT_FAILED", taskId, powerError.getMessage(), powerError);
            RebootLogStore.WriteResult fallback = RebootLogStore.info(
                    "SHELL_REBOOT_DISPATCH", taskId, RebootStateStore.readBootCount(this),
                    RebootStateStore.getSuccessCount(this), "Executing root reboot command");
            if (!fallback.success) {
                pauseTask(taskId, "Fallback log unavailable: " + fallback.errorMessage,
                        powerError);
                return;
            }
            ShellUtils.CommandResult shell = ShellUtils.execCompatibleRoot("reboot");
            audit("SHELL_REBOOT_RETURNED", taskId, commandDetail(shell));
            if (shell != null && shell.result == ShellUtils.RESULT_TIMEOUT) {
                audit("SHELL_REBOOT_RESULT_UNKNOWN", taskId,
                        "Command timed out; keeping pending task for BOOT_COMPLETED");
            } else if (shell == null || shell.result != ShellUtils.RESULT_SUCCESS) {
                pauseTask(taskId, "Both reboot methods failed: " + commandDetail(shell),
                        powerError);
            }
        }
    }

    private void scheduleConfirmationTimeout(RebootStateStore.PendingTask task) {
        dispose(confirmationTimer);
        long elapsed = System.currentTimeMillis() - task.requestWallTimeMs;
        long delay = task.requestWallTimeMs <= 0L || elapsed < 0L
                || elapsed > RebootConfirmationPolicy.CONFIRM_WINDOW_MS
                ? 0L : RebootConfirmationPolicy.CONFIRM_WINDOW_MS - elapsed + 1L;
        requestingReboot.set(true);
        audit("REBOOT_CONFIRM_WAIT_STARTED", task.taskId, "remainingMs=" + delay);
        confirmationTimer = Observable.timer(delay, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(ignored -> confirmationTimedOut(task.taskId), throwable -> {
                    synchronized (timerLock) {
                        confirmationTimer = null;
                    }
                    error("CONFIRM_TIMER_ERROR", task.taskId,
                            throwable.getMessage(), throwable);
                });
    }

    private void confirmationTimedOut(String taskId) {
        synchronized (timerLock) {
            confirmationTimer = null;
        }
        RebootStateStore.PendingTask current = RebootStateStore.getPendingTask(this);
        if (current == null || !taskId.equals(current.taskId)) {
            requestingReboot.set(false);
            startCycle();
            return;
        }
        error("REBOOT_CONFIRM_TIMEOUT", taskId,
                "No BOOT_COMPLETED within 3 minutes", null);
        pauseTask(taskId, "重启请求后 3 分钟内未收到开机确认", null);
    }

    private void pauseTask(String taskId, String reason, Throwable throwable) {
        boolean persisted = RebootStateStore.pause(this, reason, System.currentTimeMillis());
        synchronized (timerLock) {
            runtimePaused = true;
            runtimePauseReason = reason == null ? "" : reason;
            cycleGeneration++;
            dispose(cycleTimer);
            dispose(confirmationTimer);
            cycleTimer = null;
            confirmationTimer = null;
            scheduledTriggerElapsedMs = 0L;
        }
        requestingReboot.set(false);
        error("TASK_PAUSED", taskId,
                "reason=" + reason + ", statePersisted=" + persisted, throwable);
        if (!RebootStateStore.isEnabled(this)) {
            stopForeground(true);
            foregroundStarted = false;
            stopSelf();
            return;
        }
        showNotification("任务已暂停：" + summarize(reason));
    }

    private void showNotification(String remarks) {
        showNotification(remarks, false);
    }

    private void showNotification(String remarks, boolean allowWhenDisabled) {
        if (!allowWhenDisabled && !RebootStateStore.isEnabled(this)) {
            stopForeground(true);
            foregroundStarted = false;
            return;
        }
        try {
            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                throw new IllegalStateException("NotificationManager unavailable");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID, getString(R.string.reboot_title),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(getString(R.string.reboot_notification_channel));
                manager.createNotificationChannel(channel);
            }

            Intent openIntent = new Intent(this, RebootAty.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent contentIntent = PendingIntent.getActivity(
                    this, 0, openIntent, pendingFlags);
            String title = getString(R.string.reboot_app_name) + " "
                    + AppsUtils.getAppVersionName();
            Notification notification = new NotificationCompat.Builder(
                    this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.reboot)
                    .setContentTitle(title)
                    .setContentText(remarks)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remarks))
                    .setContentIntent(contentIntent)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
            if (!foregroundStarted) {
                startForeground(NOTIFICATION_ID, notification);
                foregroundStarted = true;
            } else {
                manager.notify(NOTIFICATION_ID, notification);
            }
        } catch (Throwable throwable) {
            RebootLogStore.error("NOTIFICATION_UPDATE_FAILED", "",
                    RebootStateStore.readBootCount(this),
                    RebootStateStore.getSuccessCount(this), remarks, throwable);
        }
    }

    private void audit(String event, String taskId, String detail) {
        RebootLogStore.info(event, taskId, RebootStateStore.readBootCount(this),
                RebootStateStore.getSuccessCount(this), detail);
    }

    private void error(String event, String taskId, String detail, Throwable throwable) {
        RebootLogStore.error(event, taskId, RebootStateStore.readBootCount(this),
                RebootStateStore.getSuccessCount(this), detail, throwable);
    }

    private int cycleSeconds() {
        return RebootStateStore.getCycleSeconds(this);
    }

    private static String commandDetail(ShellUtils.CommandResult result) {
        return result == null ? "result=null" : "result=" + result.result
                + ", durationMs=" + result.durationMs + ", timeout=" + result.timeout
                + ", shellPath=" + result.shellPath
                + ", error=" + result.errorMsg + ", output=" + result.successMsg;
    }

    private static String formatDuration(long durationMs) {
        long total = Math.max(0L, (durationMs + 999L) / 1000L);
        long hours = total / 3600L;
        long minutes = (total % 3600L) / 60L;
        long seconds = total % 60L;
        if (hours > 0L) {
            return hours + "小时" + minutes + "分";
        }
        return minutes > 0L ? minutes + "分" + seconds + "秒" : seconds + "秒";
    }

    private static boolean active(Disposable disposable) {
        return disposable != null && !disposable.isDisposed();
    }

    private static void dispose(Disposable disposable) {
        if (active(disposable)) {
            disposable.dispose();
        }
    }

    private static String summarize(String value) {
        if (value == null || value.trim().length() == 0) {
            return "未知原因";
        }
        String normalized = value.replace('\n', ' ').replace('\r', ' ').trim();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120) + "...";
    }

    @SuppressLint("HardwareIds")
    public static String getSn() {
        try {
            return Build.VERSION.SDK_INT >= 30 ? Build.getSerial() : Build.SERIAL;
        } catch (Throwable throwable) {
            return "";
        }
    }

    @Override
    public void onDestroy() {
        stopCycle();
        stopForeground(true);
        foregroundStarted = false;
        audit("SERVICE_DESTROYED", "", "All timers disposed");
        super.onDestroy();
    }

    public class TestBinder extends Binder {
        public RebootCustomService getService() {
            return RebootCustomService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
