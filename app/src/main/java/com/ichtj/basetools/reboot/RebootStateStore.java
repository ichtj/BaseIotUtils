package com.ichtj.basetools.reboot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Durable state for the timer, pending reboot request and confirmed reboot count.
 */
final class RebootStateStore {
    static final int DEFAULT_CYCLE_SECONDS = 30;

    private static final String PREF_NAME = "config";
    private static final String KEY_CYCLE_SECONDS = "timeCycle";
    private static final String KEY_ENABLED = "rebootTaskEnabled";
    private static final String KEY_SUCCESS_COUNT = "rebootSuccessCount";
    private static final String KEY_LAST_SUCCESS_WALL_TIME = "rebootLastSuccessWallTime";
    private static final String KEY_PENDING_TASK_ID = "rebootPendingTaskId";
    private static final String KEY_PENDING_REQUEST_TIME = "rebootPendingRequestTime";
    private static final String KEY_PENDING_BOOT_COUNT = "rebootPendingBootCount";
    private static final String KEY_PENDING_BOOT_ID = "rebootPendingBootId";
    private static final String KEY_NEXT_TRIGGER_ELAPSED = "rebootNextTriggerElapsed";
    private static final String KEY_SCHEDULE_BOOT_MARKER = "rebootScheduleBootMarker";
    private static final String KEY_LAST_HANDLED_BOOT_ID = "rebootLastHandledBootId";
    private static final String KEY_LAST_HANDLED_BOOT_COUNT = "rebootLastHandledBootCount";
    private static final String KEY_PAUSED = "rebootTaskPaused";
    private static final String KEY_PAUSE_REASON = "rebootTaskPauseReason";
    private static final String KEY_PAUSED_AT = "rebootTaskPausedAt";
    private static final String BOOT_ID_PATH = "/proc/sys/kernel/random/boot_id";

    static final class PendingTask {
        final String taskId;
        final long requestWallTimeMs;
        final int requestBootCount;
        final String requestBootId;

        PendingTask(String taskId, long requestWallTimeMs, int requestBootCount,
                    String requestBootId) {
            this.taskId = taskId;
            this.requestWallTimeMs = requestWallTimeMs;
            this.requestBootCount = requestBootCount;
            this.requestBootId = requestBootId;
        }
    }

    static final class ScheduleResult {
        final boolean persisted;
        final long nextTriggerElapsedMs;
        final boolean created;

        ScheduleResult(boolean persisted, long nextTriggerElapsedMs, boolean created) {
            this.persisted = persisted;
            this.nextTriggerElapsedMs = nextTriggerElapsedMs;
            this.created = created;
        }
    }

    static final class BootResult {
        final boolean persisted;
        final boolean duplicate;
        final PendingTask pendingTask;
        final RebootConfirmationPolicy.Result confirmation;
        final long successCount;
        final long nextTriggerElapsedMs;

        BootResult(boolean persisted, boolean duplicate, PendingTask pendingTask,
                   RebootConfirmationPolicy.Result confirmation, long successCount,
                   long nextTriggerElapsedMs) {
            this.persisted = persisted;
            this.duplicate = duplicate;
            this.pendingTask = pendingTask;
            this.confirmation = confirmation;
            this.successCount = successCount;
            this.nextTriggerElapsedMs = nextTriggerElapsedMs;
        }
    }

    private RebootStateStore() {
    }

    static synchronized int getCycleSeconds(Context context) {
        Object value = preferences(context).getAll().get(KEY_CYCLE_SECONDS);
        if (value instanceof Number) {
            int seconds = ((Number) value).intValue();
            return seconds > 0 ? seconds : DEFAULT_CYCLE_SECONDS;
        }
        return DEFAULT_CYCLE_SECONDS;
    }

    static synchronized long getSuccessCount(Context context) {
        Object value = preferences(context).getAll().get(KEY_SUCCESS_COUNT);
        return value instanceof Number ? Math.max(0L, ((Number) value).longValue()) : 0L;
    }

    static synchronized long getLastSuccessWallTime(Context context) {
        return Math.max(0L, preferences(context).getLong(KEY_LAST_SUCCESS_WALL_TIME, 0L));
    }

    static synchronized boolean isEnabled(Context context) {
        return preferences(context).getBoolean(KEY_ENABLED, true);
    }

    static synchronized long getNextTriggerElapsedMs(Context context) {
        return Math.max(0L, preferences(context).getLong(KEY_NEXT_TRIGGER_ELAPSED, 0L));
    }

    static synchronized boolean isPaused(Context context) {
        return preferences(context).getBoolean(KEY_PAUSED, false);
    }

    static synchronized String getPauseReason(Context context) {
        return preferences(context).getString(KEY_PAUSE_REASON, "");
    }

    static synchronized boolean disable(Context context) {
        if (getPendingTask(context) != null) {
            return false;
        }
        return preferences(context).edit()
                .putBoolean(KEY_ENABLED, false)
                .remove(KEY_NEXT_TRIGGER_ELAPSED)
                .commit();
    }

    static synchronized boolean enable(Context context, String currentBootMarker,
                                       long nowElapsedMs) {
        if (getPendingTask(context) != null) {
            return false;
        }
        long nextTrigger = safeAddCycle(nowElapsedMs, getCycleSeconds(context));
        return clearPending(preferences(context).edit())
                .putBoolean(KEY_ENABLED, true)
                .remove(KEY_PAUSED)
                .remove(KEY_PAUSE_REASON)
                .remove(KEY_PAUSED_AT)
                .putString(KEY_SCHEDULE_BOOT_MARKER, currentBootMarker)
                .putLong(KEY_NEXT_TRIGGER_ELAPSED, nextTrigger)
                .commit();
    }

    static synchronized boolean pause(Context context, String reason, long nowWallTimeMs) {
        if (!isEnabled(context)) {
            return false;
        }
        return clearPending(preferences(context).edit())
                .putBoolean(KEY_PAUSED, true)
                .putString(KEY_PAUSE_REASON, nullToEmpty(reason))
                .putLong(KEY_PAUSED_AT, nowWallTimeMs)
                .remove(KEY_NEXT_TRIGGER_ELAPSED)
                .commit();
    }

    static synchronized boolean resume(Context context, String currentBootMarker,
                                       long nowElapsedMs) {
        if (!isEnabled(context) || getPendingTask(context) != null) {
            return false;
        }
        long nextTrigger = safeAddCycle(nowElapsedMs, getCycleSeconds(context));
        return clearPending(preferences(context).edit())
                .remove(KEY_PAUSED)
                .remove(KEY_PAUSE_REASON)
                .remove(KEY_PAUSED_AT)
                .putString(KEY_SCHEDULE_BOOT_MARKER, currentBootMarker)
                .putLong(KEY_NEXT_TRIGGER_ELAPSED, nextTrigger)
                .commit();
    }

    static synchronized PendingTask getPendingTask(Context context) {
        SharedPreferences preferences = preferences(context);
        String taskId = preferences.getString(KEY_PENDING_TASK_ID, "");
        if (isEmpty(taskId)) {
            return null;
        }
        return new PendingTask(taskId,
                preferences.getLong(KEY_PENDING_REQUEST_TIME, 0L),
                preferences.getInt(KEY_PENDING_BOOT_COUNT, -1),
                preferences.getString(KEY_PENDING_BOOT_ID, ""));
    }

    static synchronized boolean savePendingTask(Context context, PendingTask task) {
        if (!isEnabled(context)) {
            return false;
        }
        return preferences(context).edit()
                .putString(KEY_PENDING_TASK_ID, task.taskId)
                .putLong(KEY_PENDING_REQUEST_TIME, task.requestWallTimeMs)
                .putInt(KEY_PENDING_BOOT_COUNT, task.requestBootCount)
                .putString(KEY_PENDING_BOOT_ID, nullToEmpty(task.requestBootId))
                .remove(KEY_NEXT_TRIGGER_ELAPSED)
                .commit();
    }

    static synchronized ScheduleResult getOrCreateSchedule(Context context,
                                                           String currentBootMarker,
                                                           long nowElapsedMs) {
        SharedPreferences preferences = preferences(context);
        String storedMarker = preferences.getString(KEY_SCHEDULE_BOOT_MARKER, "");
        long storedTrigger = preferences.getLong(KEY_NEXT_TRIGGER_ELAPSED, 0L);
        if (currentBootMarker.equals(storedMarker) && storedTrigger > 0L) {
            return new ScheduleResult(true, storedTrigger, false);
        }

        long nextTrigger = safeAddCycle(nowElapsedMs, getCycleSeconds(context));
        boolean persisted = preferences.edit()
                .putString(KEY_SCHEDULE_BOOT_MARKER, currentBootMarker)
                .putLong(KEY_NEXT_TRIGGER_ELAPSED, nextTrigger)
                .commit();
        return new ScheduleResult(persisted, nextTrigger, true);
    }

    static synchronized boolean updateCycleAndSchedule(Context context, int cycleSeconds,
                                                       String currentBootMarker,
                                                       long nowElapsedMs) {
        if (cycleSeconds <= 0) {
            return false;
        }
        SharedPreferences.Editor editor = preferences(context).edit()
                .putInt(KEY_CYCLE_SECONDS, cycleSeconds)
                .putString(KEY_SCHEDULE_BOOT_MARKER, currentBootMarker);
        if (isEnabled(context) && !isPaused(context)) {
            editor.putLong(KEY_NEXT_TRIGGER_ELAPSED,
                    safeAddCycle(nowElapsedMs, cycleSeconds));
        } else {
            editor.remove(KEY_NEXT_TRIGGER_ELAPSED);
        }
        return editor.commit();
    }

    static synchronized boolean clearPendingAndResetSchedule(Context context, String taskId,
                                                             String currentBootMarker,
                                                             long nowElapsedMs) {
        PendingTask current = getPendingTask(context);
        if (current != null && !isEmpty(taskId) && !taskId.equals(current.taskId)) {
            return false;
        }
        SharedPreferences.Editor editor = clearPending(preferences(context).edit())
                .putString(KEY_SCHEDULE_BOOT_MARKER, currentBootMarker);
        if (isEnabled(context) && !isPaused(context)) {
            editor.putLong(KEY_NEXT_TRIGGER_ELAPSED,
                    safeAddCycle(nowElapsedMs, getCycleSeconds(context)));
        } else {
            editor.remove(KEY_NEXT_TRIGGER_ELAPSED);
        }
        return editor.commit();
    }

    static synchronized BootResult handleBootCompleted(Context context, long nowWallTimeMs,
                                                       long nowElapsedMs, int currentBootCount,
                                                       String currentBootId) {
        SharedPreferences preferences = preferences(context);
        PendingTask pendingTask = getPendingTask(context);
        long successCount = getSuccessCount(context);
        int lastBootCount = preferences.getInt(KEY_LAST_HANDLED_BOOT_COUNT, -1);
        String lastBootId = preferences.getString(KEY_LAST_HANDLED_BOOT_ID, "");
        boolean duplicate = isSameBoot(lastBootCount, lastBootId, currentBootCount, currentBootId);
        if (duplicate) {
            RebootConfirmationPolicy.Result noAction = new RebootConfirmationPolicy.Result(
                    RebootConfirmationPolicy.Status.NO_PENDING_TASK, -1L,
                    "Duplicate BOOT_COMPLETED for the current boot");
            return new BootResult(true, true, pendingTask, noAction, successCount,
                    preferences.getLong(KEY_NEXT_TRIGGER_ELAPSED, 0L));
        }

        RebootConfirmationPolicy.Result confirmation = RebootConfirmationPolicy.evaluate(
                pendingTask, nowWallTimeMs, currentBootCount, currentBootId);
        if (confirmation.isSuccess()) {
            successCount++;
        }

        boolean shouldSchedule = isEnabled(context) && !isPaused(context);
        long nextTrigger = shouldSchedule
                ? safeAddCycle(nowElapsedMs, getCycleSeconds(context)) : 0L;
        SharedPreferences.Editor editor = clearPending(preferences.edit())
                .putLong(KEY_SUCCESS_COUNT, successCount)
                .putString(KEY_SCHEDULE_BOOT_MARKER,
                        createBootMarker(currentBootCount, currentBootId))
                .putInt(KEY_LAST_HANDLED_BOOT_COUNT, currentBootCount)
                .putString(KEY_LAST_HANDLED_BOOT_ID, nullToEmpty(currentBootId));
        if (confirmation.isSuccess()) {
            editor.putLong(KEY_LAST_SUCCESS_WALL_TIME, nowWallTimeMs);
        }
        if (shouldSchedule) {
            editor.putLong(KEY_NEXT_TRIGGER_ELAPSED, nextTrigger);
        } else {
            editor.remove(KEY_NEXT_TRIGGER_ELAPSED);
        }
        boolean persisted = editor.commit();
        return new BootResult(persisted, false, pendingTask, confirmation, successCount,
                nextTrigger);
    }

    static int readBootCount(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return -1;
        }
        try {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.BOOT_COUNT, -1);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    static String readBootId() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(BOOT_ID_PATH), "UTF-8"));
            return nullToEmpty(reader.readLine()).trim();
        } catch (Throwable ignored) {
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable ignored) {
                    // Nothing useful can be done while reading a best-effort boot marker.
                }
            }
        }
    }

    static String createBootMarker(int bootCount, String bootId) {
        if (!isEmpty(bootId)) {
            return "id:" + bootId;
        }
        return bootCount >= 0 ? "count:" + bootCount : "unknown";
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor clearPending(SharedPreferences.Editor editor) {
        return editor.remove(KEY_PENDING_TASK_ID)
                .remove(KEY_PENDING_REQUEST_TIME)
                .remove(KEY_PENDING_BOOT_COUNT)
                .remove(KEY_PENDING_BOOT_ID);
    }

    private static long safeAddCycle(long nowElapsedMs, int cycleSeconds) {
        long cycleMs = cycleSeconds * 1000L;
        if (Long.MAX_VALUE - nowElapsedMs < cycleMs) {
            return Long.MAX_VALUE;
        }
        return nowElapsedMs + cycleMs;
    }

    private static boolean isSameBoot(int lastBootCount, String lastBootId,
                                      int currentBootCount, String currentBootId) {
        if (!isEmpty(lastBootId) && !isEmpty(currentBootId)) {
            return lastBootId.equals(currentBootId);
        }
        return lastBootCount >= 0 && currentBootCount >= 0 && lastBootCount == currentBootCount;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
