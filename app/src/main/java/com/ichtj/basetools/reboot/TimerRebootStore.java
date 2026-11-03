package com.ichtj.basetools.reboot;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.UUID;

final class TimerRebootStore {
    static final int DEFAULT_HOUR = 2;
    static final int ERROR_NONE = 0;
    static final int ERROR_STORAGE = 1;
    static final int ERROR_ALARM = 2;
    static final int ERROR_REBOOT = 3;
    static final int ERROR_CONFIRM = 4;
    static final int ERROR_STATE = 5;

    private TimerRebootStore() {
    }

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("ichtj_timer_reboot", Context.MODE_PRIVATE);
    }

    static TimerRebootPlan plan(Context context) {
        SharedPreferences p = prefs(context);
        Calendar now = Calendar.getInstance();
        return new TimerRebootPlan(p.getInt("type", TimerRebootPlan.DAILY),
                p.getInt("hour", DEFAULT_HOUR), p.getInt("minute", 0),
                p.getInt("year", now.get(Calendar.YEAR)),
                p.getInt("month", now.get(Calendar.MONTH) + 1),
                p.getInt("day", now.get(Calendar.DAY_OF_MONTH)),
                p.getInt("weekdays", 1 << (Calendar.MONDAY - 1)), p.getInt("monthDay", 1));
    }

    static boolean configured(Context context) {
        return prefs(context).getBoolean("configured", true);
    }

    static boolean enabled(Context context) {
        return prefs(context).getBoolean("enabled", true);
    }

    static boolean initializeDefaults(Context context) {
        SharedPreferences p = prefs(context);
        if (p.contains("configured") && p.contains("enabled") && p.contains("type")
                && p.contains("hour") && p.contains("minute")
                && !p.getString("generation", "").isEmpty()) {
            return true;
        }
        SharedPreferences.Editor editor = p.edit();
        if (!p.contains("configured")) {
            editor.putBoolean("configured", true);
        }
        if (!p.contains("enabled")) {
            editor.putBoolean("enabled", true);
        }
        if (!p.contains("type")) {
            editor.putInt("type", TimerRebootPlan.DAILY);
        }
        if (!p.contains("hour")) {
            editor.putInt("hour", DEFAULT_HOUR);
        }
        if (!p.contains("minute")) {
            editor.putInt("minute", 0);
        }
        if (p.getString("generation", "").isEmpty()) {
            editor.putString("generation", UUID.randomUUID().toString());
        }
        return editor.commit();
    }

    static boolean paused(Context context) {
        return prefs(context).getInt("error", ERROR_NONE) != ERROR_NONE;
    }

    static boolean pending(Context context) {
        return !prefs(context).getString("pendingId", "").isEmpty();
    }

    static long next(Context context) {
        return prefs(context).getLong("next", 0L);
    }

    static String generation(Context context) {
        return prefs(context).getString("generation", "");
    }

    static String lastOccurrence(Context context) {
        return prefs(context).getString("lastOccurrence", "");
    }

    static String lastOccurrenceFor(Context context, TimerRebootPlan plan) {
        return plan.sameSchedule(plan(context)) ? lastOccurrence(context) : "";
    }

    static boolean save(Context context, TimerRebootPlan plan) {
        String last = lastOccurrenceFor(context, plan);
        return prefs(context).edit().putBoolean("configured", true)
                .putInt("type", plan.type).putInt("hour", plan.hour).putInt("minute", plan.minute)
                .putInt("year", plan.year).putInt("month", plan.month).putInt("day", plan.day)
                .putInt("weekdays", plan.weekdays).putInt("monthDay", plan.monthDay)
                .putString("lastOccurrence", last)
                .putBoolean("expired", false).putBoolean("completed", false).putLong("next", 0L)
                .putString("generation", UUID.randomUUID().toString()).commit();
    }

    static boolean setEnabled(Context context, boolean enabled) {
        return prefs(context).edit().putBoolean("enabled", enabled)
                .putBoolean("expired", false).putBoolean("completed", false)
                .putInt("error", ERROR_NONE)
                .putString("detail", "").putLong("next", 0L)
                .putString("generation", UUID.randomUUID().toString()).commit();
    }

    static boolean schedule(Context context, long next, long now, long elapsed) {
        return prefs(context).edit().putLong("next", next)
                .putLong("scheduledAtWall", now).putLong("scheduledAtElapsed", elapsed).commit();
    }

    static boolean expire(Context context) {
        return prefs(context).edit().putBoolean("enabled", false)
                .putBoolean("expired", true).putLong("next", 0L).commit();
    }

    static boolean pause(Context context, int error, String detail) {
        return prefs(context).edit().putInt("error", error)
                .putString("detail", detail == null ? "" : detail).putLong("next", 0L).commit();
    }

    static boolean begin(Context context, String id, long now, long elapsed, String bootId,
                         int bootCount, String occurrence) {
        return prefs(context).edit().putString("pendingId", id)
                .putLong("requestWall", now).putLong("requestElapsed", elapsed)
                .putString("requestBootId", bootId).putInt("requestBootCount", bootCount)
                .putString("lastOccurrence", occurrence).putLong("next", 0L).commit();
    }

    static boolean finish(Context context, String result, long now, int error, String detail) {
        SharedPreferences.Editor editor = prefs(context).edit().remove("pendingId")
                .putString("lastResult", result).putLong("lastResultTime", now)
                .putInt("error", error).putString("detail", detail == null ? "" : detail);
        if (error == ERROR_NONE && plan(context).type == TimerRebootPlan.ONCE) {
            editor.putBoolean("enabled", false).putBoolean("completed", true);
        }
        return editor.commit();
    }
}
