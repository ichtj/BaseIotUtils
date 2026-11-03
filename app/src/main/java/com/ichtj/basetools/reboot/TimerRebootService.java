package com.ichtj.basetools.reboot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ichtj.basetools.BuildConfig;
import com.ichtj.basetools.R;

/** Restores the plan after boot and executes alarms without opening an Activity. */
public class TimerRebootService extends Service {
    private static final String CHANNEL = "ichtj_timer_reboot";

    public static void handleBroadcast(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        boolean fire = TimerRebootScheduler.ACTION_FIRE.equals(action);
        boolean restore = Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action);
        if (!fire && !restore && !TimerRebootScheduler.ACTION_CONFIRM.equals(action)
                && !Intent.ACTION_TIME_CHANGED.equals(action)
                && !Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            return;
        }
        if (!"timerreboot".equals(BuildConfig.APP_CHOOSE)
                && !TimerRebootStore.prefs(context).contains("configured") && !fire) {
            return;
        }
        Intent service = new Intent(intent).setClass(context, TimerRebootService.class);
        if (restore) {
            service.setAction(TimerRebootScheduler.ACTION_RESTORE);
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        } catch (Exception error) {
            TimerRebootStore.pause(context, TimerRebootStore.ERROR_REBOOT, error.toString());
            Log.e(TimerRebootScheduler.TAG, "Cannot start reboot service", error);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean fire = intent != null
                && TimerRebootScheduler.ACTION_FIRE.equals(intent.getAction());
        NotificationManager notifications = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notifications != null) {
            notifications.createNotificationChannel(new NotificationChannel(CHANNEL,
                    getString(R.string.timer_reboot_title), NotificationManager.IMPORTANCE_LOW));
        }
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent open = PendingIntent.getActivity(this, 4103,
                new Intent(this, TimerRebootAty.class), pendingFlags);
        startForeground(4103, new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.mipmap.reboot).setContentTitle(getString(R.string.timer_reboot_title))
                .setContentText(getString(fire ? R.string.reboot_task_status_executing
                        : R.string.timer_reboot_restoring))
                .setContentIntent(open).setOngoing(true).build());
        PowerManager manager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock lock = manager == null ? null
                : manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ichtj_timer_reboot:execute");
        if (lock != null) {
            lock.acquire(60_000L);
        }
        TimerRebootScheduler.WORKER.execute(() -> {
            try {
                if (fire) {
                    TimerRebootScheduler.fire(this, intent);
                } else if (intent != null && (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()))) {
                    TimerRebootScheduler.reschedule(this);
                } else {
                    TimerRebootScheduler.restore(this);
                }
            } finally {
                if (lock != null && lock.isHeld()) {
                    lock.release();
                }
                stopForeground(true);
                stopSelf(startId);
            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
