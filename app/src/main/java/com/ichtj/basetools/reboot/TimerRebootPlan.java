package com.ichtj.basetools.reboot;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/** Local-calendar scheduling, independent of Android and of the settings screen. */
final class TimerRebootPlan {
    static final int ONCE = 0;
    static final int DAILY = 1;
    static final int WEEKLY = 2;
    static final int MONTHLY = 3;
    static final int LAST_DAY = 0;

    final int type;
    final int hour;
    final int minute;
    final int year;
    final int month;
    final int day;
    final int weekdays;
    final int monthDay;

    TimerRebootPlan(int type, int hour, int minute, int year, int month, int day,
                    int weekdays, int monthDay) {
        this.type = type;
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekdays = weekdays;
        this.monthDay = monthDay;
    }

    boolean isValid() {
        if (type < ONCE || type > MONTHLY || hour < 0 || hour > 23
                || minute < 0 || minute > 59) {
            return false;
        }
        if (type == WEEKLY) {
            return weekdays > 0 && (weekdays & ~127) == 0;
        }
        if (type == MONTHLY) {
            return monthDay >= LAST_DAY && monthDay <= 31;
        }
        return type != ONCE || (year >= 1970 && year <= 9999
                && localTime(year, month, day, TimeZone.getTimeZone("UTC")) != null);
    }

    boolean sameSchedule(TimerRebootPlan other) {
        if (other == null || type != other.type || hour != other.hour || minute != other.minute) {
            return false;
        }
        switch (type) {
            case ONCE:
                return year == other.year && month == other.month && day == other.day;
            case WEEKLY:
                return weekdays == other.weekdays;
            case MONTHLY:
                return monthDay == other.monthDay;
            default:
                return true;
        }
    }

    long nextAfter(long now, TimeZone zone, String lastOccurrence) {
        if (!isValid()) {
            return 0L;
        }
        if (type == ONCE) {
            Calendar candidate = localTime(year, month, day, zone);
            return isFuture(candidate, now, lastOccurrence) ? candidate.getTimeInMillis() : 0L;
        }
        Calendar date = Calendar.getInstance(zone, Locale.US);
        date.setTimeInMillis(now);
        // Noon keeps day iteration away from daylight-saving transitions at midnight.
        date.set(Calendar.HOUR_OF_DAY, 12);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        for (int offset = 0; offset < 800; offset++, date.add(Calendar.DAY_OF_MONTH, 1)) {
            int weekday = date.get(Calendar.DAY_OF_WEEK);
            boolean matches = type == DAILY
                    || (type == WEEKLY && (weekdays & (1 << (weekday - 1))) != 0)
                    || (type == MONTHLY && date.get(Calendar.DAY_OF_MONTH)
                    == (monthDay == LAST_DAY
                    ? date.getActualMaximum(Calendar.DAY_OF_MONTH) : monthDay));
            if (!matches) {
                continue;
            }
            Calendar candidate = localTime(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.DAY_OF_MONTH), zone);
            if (isFuture(candidate, now, lastOccurrence)) {
                return candidate.getTimeInMillis();
            }
        }
        return 0L;
    }

    String occurrenceAt(long wallTime, TimeZone zone) {
        Calendar date = Calendar.getInstance(zone, Locale.US);
        date.setTimeInMillis(wallTime);
        return String.format(Locale.US, "%04d-%02d-%02d", date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
    }

    private boolean isFuture(Calendar candidate, long now, String lastOccurrence) {
        return candidate != null && candidate.getTimeInMillis() > now
                && !occurrenceAt(candidate.getTimeInMillis(), candidate.getTimeZone())
                .equals(lastOccurrence);
    }

    private Calendar localTime(int year, int month, int day, TimeZone zone) {
        Calendar calendar = Calendar.getInstance(zone, Locale.US);
        calendar.clear();
        calendar.setLenient(false);
        calendar.set(year, month - 1, day, hour, minute, 0);
        try {
            calendar.getTimeInMillis();
            return calendar;
        } catch (IllegalArgumentException invalidDateOrDstGap) {
            return null;
        }
    }
}
