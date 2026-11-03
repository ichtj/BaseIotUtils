package com.ichtj.basetools.reboot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.face_chtj.base_iotutils.ShellUtils;
import com.ichtj.basetools.util.BaseIotReceiver;

import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class TimerRebootScheduler {
    static final String ACTION_FIRE = "com.ichtj.basetools.reboot.TIMER_FIRE";
    static final String ACTION_CONFIRM = "com.ichtj.basetools.reboot.TIMER_CONFIRM";
    static final String ACTION_RESTORE = "com.ichtj.basetools.reboot.TIMER_RESTORE";
    static final String TAG = "ichtj_timer_reboot";
    static final ExecutorService WORKER = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "ichtj_timer_worker"));
    private static final long CONFIRM_MS = TimerRebootTimingPolicy.CONFIRM_MS;
    private static final Object LOCK = new Object();

    private TimerRebootScheduler() {
    }

    static boolean save(Context context, TimerRebootPlan plan) {
        synchronized (LOCK) {
            if (!plan.isValid() || TimerRebootStore.pending(context)) {
                return false;
            }
            if (plan.type == TimerRebootPlan.ONCE && plan.nextAfter(System.currentTimeMillis(),
                    TimeZone.getDefault(), TimerRebootStore.lastOccurrenceFor(context, plan)) == 0L) {
                return false;
            }
            if (!TimerRebootStore.save(context, plan)) {
                return false;
            }
            return reschedule(context);
        }
    }

    static boolean enable(Context context, boolean enabled) {
        synchronized (LOCK) {
            if (TimerRebootStore.pending(context)
                    || (enabled && !TimerRebootStore.configured(context))) {
                return false;
            }
            TimerRebootPlan plan = TimerRebootStore.plan(context);
            if (enabled && (!plan.isValid() || (plan.type == TimerRebootPlan.ONCE
                    && plan.nextAfter(System.currentTimeMillis(), TimeZone.getDefault(),
                    TimerRebootStore.lastOccurrence(context)) == 0L))) {
                return false;
            }
            if (!TimerRebootStore.setEnabled(context, enabled)) {
                return false;
            }
            return reschedule(context);
        }
    }

    static boolean reschedule(Context context) {
        synchronized (LOCK) {
            if (!TimerRebootStore.initializeDefaults(context)) {
                pause(context, TimerRebootStore.ERROR_STATE, "Default plan could not be persisted");
                return false;
            }
            cancel(context, ACTION_FIRE);
            if (TimerRebootStore.pending(context)) {
                return true;
            }
            if (!TimerRebootStore.enabled(context) || TimerRebootStore.paused(context)) {
                return true;
            }
            TimerRebootPlan plan = TimerRebootStore.plan(context);
            long now = System.currentTimeMillis();
            TimeZone zone = TimeZone.getDefault();
            long next = plan.nextAfter(now, zone, TimerRebootStore.lastOccurrence(context));
            if (next == 0L) {
                if (plan.isValid() && plan.type == TimerRebootPlan.ONCE) {
                    if (TimerRebootStore.expire(context)) {
                        return true;
                    }
                    pause(context, TimerRebootStore.ERROR_STATE, "Expired state could not be persisted");
                    return false;
                }
                pause(context, TimerRebootStore.ERROR_STATE, "Invalid calendar schedule");
                return false;
            }
            if (!TimerRebootStore.schedule(context, next, now, SystemClock.elapsedRealtime())) {
                pause(context, TimerRebootStore.ERROR_STATE, "Schedule could not be persisted");
                return false;
            }
            try {
                Intent intent = new Intent(context, BaseIotReceiver.class).setAction(ACTION_FIRE)
                        .putExtra("generation", TimerRebootStore.generation(context))
                        .putExtra("trigger", next);
                setExact(context, AlarmManager.RTC_WAKEUP, next, pending(context, intent, 4101));
                Log.i(TAG, "Scheduled local-calendar reboot at " + next + " zone=" + zone.getID());
                return true;
            } catch (Exception error) {
                pause(context, TimerRebootStore.ERROR_ALARM, error.toString());
                return false;
            }
        }
    }

    static void fire(Context context, Intent intent) {
        String taskId;
        synchronized (LOCK) {
            if (!TimerRebootStore.enabled(context) || TimerRebootStore.paused(context)
                    || TimerRebootStore.pending(context)
                    || !TimerRebootStore.generation(context)
                    .equals(intent.getStringExtra("generation"))
                    || TimerRebootStore.next(context) != intent.getLongExtra("trigger", -1L)) {
                return;
            }
            long now = System.currentTimeMillis();
            long elapsed = SystemClock.elapsedRealtime();
            long trigger = TimerRebootStore.next(context);
            SharedPreferences state = TimerRebootStore.prefs(context);
            // A stale alarm must not turn a clock jump or a long outage into a catch-up reboot.
            if (!TimerRebootTimingPolicy.isDue(now, elapsed, trigger,
                    state.getLong("scheduledAtWall", now),
                    state.getLong("scheduledAtElapsed", elapsed))) {
                reschedule(context);
                return;
            }
            TimerRebootPlan plan = TimerRebootStore.plan(context);
            if (!plan.isValid()) {
                pause(context, TimerRebootStore.ERROR_STATE, "Invalid calendar schedule");
                return;
            }
            String occurrence = plan.occurrenceAt(trigger, TimeZone.getDefault());
            if (occurrence.equals(TimerRebootStore.lastOccurrence(context))) {
                reschedule(context);
                return;
            }
            taskId = UUID.randomUUID().toString();
            String bootId = RebootStateStore.readBootId();
            int bootCount = RebootStateStore.readBootCount(context);
            try {
                TimerRebootLog.start(context, taskId, plan, trigger, now, bootId, bootCount);
            } catch (Exception error) {
                pause(context, TimerRebootStore.ERROR_STORAGE, error.toString());
                return;
            }
            if (!TimerRebootStore.begin(context, taskId, now, elapsed, bootId, bootCount,
                    occurrence)) {
                fail(context, taskId, TimerRebootStore.ERROR_STATE,
                        "Pending request could not be persisted");
                return;
            }
            try {
                setExact(context, AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsed + CONFIRM_MS,
                        pending(context, new Intent(context, BaseIotReceiver.class)
                                .setAction(ACTION_CONFIRM), 4102));
            } catch (Exception error) {
                fail(context, taskId, TimerRebootStore.ERROR_ALARM, error.toString());
                return;
            }
        }
        dispatch(context, taskId);
    }

    private static void dispatch(Context context, String taskId) {
        try {
            PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (manager == null) {
                throw new IllegalStateException("PowerManager unavailable");
            }
            manager.reboot(null);
        } catch (Exception powerError) {
            Log.w(TAG, "PowerManager reboot failed; trying existing root-shell helper", powerError);
            ShellUtils.CommandResult result = ShellUtils.execCompatibleRoot("reboot");
            if (result == null || (result.result != ShellUtils.RESULT_SUCCESS
                    && result.result != ShellUtils.RESULT_TIMEOUT)) {
                synchronized (LOCK) {
                    fail(context, taskId, TimerRebootStore.ERROR_REBOOT,
                            powerError + "; root=" + (result == null ? "no result"
                                    : result.result + ": " + result.errorMsg));
                }
            }
        }
    }

    static void restore(Context context) {
        synchronized (LOCK) {
            if (TimerRebootStore.pending(context)) {
                resolvePending(context);
            }
            reschedule(context);
        }
    }

    private static void resolvePending(Context context) {
        SharedPreferences state = TimerRebootStore.prefs(context);
        String id = state.getString("pendingId", "");
        String before = state.getString("requestBootId", "");
        String after = RebootStateStore.readBootId();
        int beforeCount = state.getInt("requestBootCount", -1);
        int afterCount = RebootStateStore.readBootCount(context);
        long now = System.currentTimeMillis();
        long uptime = SystemClock.elapsedRealtime();
        TimerRebootTimingPolicy.Confirmation confirmation = TimerRebootTimingPolicy.confirm(
                now, uptime, state.getLong("requestWall", 0L),
                state.getLong("requestElapsed", 0L), before, after, beforeCount, afterCount);
        if (confirmation == TimerRebootTimingPolicy.Confirmation.WAIT) {
            long waited = uptime - state.getLong("requestElapsed", 0L);
            try {
                setExact(context, AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        uptime + CONFIRM_MS - waited,
                        pending(context, new Intent(context, BaseIotReceiver.class)
                                .setAction(ACTION_CONFIRM), 4102));
            } catch (Exception alarmError) {
                fail(context, id, TimerRebootStore.ERROR_ALARM, alarmError.toString());
            }
            return;
        }
        if (confirmation == TimerRebootTimingPolicy.Confirmation.SUCCESS) {
            cancel(context, ACTION_CONFIRM);
            if (finishLog(context, id, "SUCCESS", "Boot marker changed after reboot request")) {
                if (!TimerRebootStore.finish(context, "SUCCESS", now,
                        TimerRebootStore.ERROR_NONE, "")) {
                    pause(context, TimerRebootStore.ERROR_STATE,
                            "Confirmed reboot state could not be persisted");
                }
            } else if (!TimerRebootStore.finish(context, "UNCONFIRMED", now,
                    TimerRebootStore.ERROR_STORAGE, "Cannot update latest reboot log")) {
                pause(context, TimerRebootStore.ERROR_STATE,
                        "Confirmed reboot state could not be persisted");
            }
            return;
        }
        fail(context, id, TimerRebootStore.ERROR_CONFIRM,
                "Reboot was not confirmed within 3 minutes, or boot evidence is unavailable");
    }

    private static boolean finishLog(Context context, String id, String result, String detail) {
        try {
            TimerRebootLog.finish(context, id, result, detail);
            return true;
        } catch (Exception error) {
            Log.e(TAG, "Latest result could not be persisted", error);
            return false;
        }
    }

    private static void fail(Context context, String id, int error, String detail) {
        cancel(context, ACTION_CONFIRM);
        boolean logged = finishLog(context, id, "FAILED", detail);
        if (!TimerRebootStore.finish(context, "FAILED", System.currentTimeMillis(),
                logged ? error : TimerRebootStore.ERROR_STORAGE, detail)) {
            pause(context, TimerRebootStore.ERROR_STATE, "Failure state could not be persisted");
        }
        cancel(context, ACTION_FIRE);
        Log.e(TAG, "Reboot plan paused: " + detail);
    }

    private static boolean pause(Context context, int error, String detail) {
        cancel(context, ACTION_FIRE);
        TimerRebootStore.pause(context, error, detail);
        Log.e(TAG, "Reboot plan paused: " + detail);
        return false;
    }

    private static PendingIntent pending(Context context, Intent intent, int requestCode) {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    private static void cancel(Context context, String action) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pending(context,
                    new Intent(context, BaseIotReceiver.class).setAction(action),
                    ACTION_FIRE.equals(action) ? 4101 : 4102));
        }
    }

    private static void setExact(Context context, int type, long trigger, PendingIntent intent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            throw new IllegalStateException("AlarmManager unavailable");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(type, trigger, intent);
        } else {
            manager.setExact(type, trigger, intent);
        }
    }
}
