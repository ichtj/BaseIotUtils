package com.ichtj.basetools.reboot;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimerRebootPlanTest {
    private static final TimeZone SHANGHAI = TimeZone.getTimeZone("Asia/Shanghai");

    @Test
    public void defaultDailyAtTwoUsesTodayBeforeTwo() {
        TimerRebootPlan plan = plan(TimerRebootPlan.DAILY, TimerRebootStore.DEFAULT_HOUR, 0, 0, 1);
        assertEquals(at(2026, 11, 2, 2, 0),
                plan.nextAfter(at(2026, 11, 2, 1, 0), SHANGHAI, ""));
    }

    @Test
    public void defaultDailyAtTwoUsesTomorrowAfterTwo() {
        TimerRebootPlan plan = plan(TimerRebootPlan.DAILY, TimerRebootStore.DEFAULT_HOUR, 0, 0, 1);
        assertEquals(at(2026, 11, 3, 2, 0),
                plan.nextAfter(at(2026, 11, 2, 3, 0), SHANGHAI, ""));
    }

    @Test
    public void novemberThirdAtOneFiftyNineIsOneMinuteBeforeSameDayReboot() {
        TimerRebootPlan plan = plan(TimerRebootPlan.DAILY, TimerRebootStore.DEFAULT_HOUR, 0, 0, 1);
        long now = at(2026, 11, 3, 1, 59);
        long next = plan.nextAfter(now, SHANGHAI, "");
        assertEquals(at(2026, 11, 3, 2, 0), next);
        assertEquals(60_000L, next - now);
    }

    @Test
    public void dailyAtEighteenHasThreeHoursRemainingAtFifteen() {
        TimerRebootPlan plan = plan(TimerRebootPlan.DAILY, 18, 0, 0, 1);
        long now = at(2026, 11, 2, 15, 0);
        assertEquals(3 * 3600_000L, plan.nextAfter(now, SHANGHAI, "") - now);
    }

    @Test
    public void changingSystemTimeToSeventeenLeavesOneHour() {
        TimerRebootPlan plan = plan(TimerRebootPlan.DAILY, 18, 0, 0, 1);
        long now = at(2026, 11, 2, 17, 0);
        assertEquals(3600_000L, plan.nextAfter(now, SHANGHAI, "") - now);
    }

    @Test
    public void movingClockBackRecalculatesTheRemainingTime() {
        TimerRebootPlan plan = plan(TimerRebootPlan.DAILY, 18, 0, 0, 1);
        long now = at(2026, 11, 2, 16, 0);
        assertEquals(2 * 3600_000L, plan.nextAfter(now, SHANGHAI, "") - now);
    }

    @Test
    public void movingPastTriggerSkipsToTomorrow() {
        assertEquals(at(2026, 11, 3, 18, 0),
                plan(TimerRebootPlan.DAILY, 18, 0, 0, 1)
                        .nextAfter(at(2026, 11, 2, 18, 10), SHANGHAI, ""));
    }

    @Test
    public void exactCurrentTimeIsNotAnImmediateCatchUp() {
        assertEquals(at(2026, 11, 3, 18, 0),
                plan(TimerRebootPlan.DAILY, 18, 0, 0, 1)
                        .nextAfter(at(2026, 11, 2, 18, 0), SHANGHAI, ""));
    }

    @Test
    public void sameOccurrenceIsNotRepeatedWhenClockMovesBack() {
        assertEquals(at(2026, 11, 3, 18, 0),
                plan(TimerRebootPlan.DAILY, 18, 0, 0, 1)
                        .nextAfter(at(2026, 11, 2, 17, 0), SHANGHAI, "2026-11-02"));
    }

    @Test
    public void weeklySupportsMultipleDaysAndWrapsWeek() {
        int mask = (1 << (Calendar.MONDAY - 1)) | (1 << (Calendar.FRIDAY - 1));
        TimerRebootPlan plan = plan(TimerRebootPlan.WEEKLY, 3, 0, mask, 1);
        assertEquals(at(2026, 11, 6, 3, 0),
                plan.nextAfter(at(2026, 11, 2, 4, 0), SHANGHAI, ""));
        assertEquals(at(2026, 11, 9, 3, 0),
                plan.nextAfter(at(2026, 11, 6, 4, 0), SHANGHAI, ""));
    }

    @Test
    public void weeklyRequiresAtLeastOneDay() {
        assertFalse(plan(TimerRebootPlan.WEEKLY, 3, 0, 0, 1).isValid());
        assertFalse(plan(TimerRebootPlan.WEEKLY, 3, 0, 128, 1).isValid());
    }

    @Test
    public void monthlyThirtyFirstSkipsFebruaryAndApril() {
        TimerRebootPlan plan = plan(TimerRebootPlan.MONTHLY, 3, 0, 0, 31);
        assertEquals(at(2026, 3, 31, 3, 0),
                plan.nextAfter(at(2026, 2, 1, 0, 0), SHANGHAI, ""));
        assertEquals(at(2026, 5, 31, 3, 0),
                plan.nextAfter(at(2026, 4, 1, 0, 0), SHANGHAI, ""));
    }

    @Test
    public void lastDayUsesTwentyEightInCommonFebruary() {
        assertEquals(at(2026, 2, 28, 3, 0),
                plan(TimerRebootPlan.MONTHLY, 3, 0, 0, TimerRebootPlan.LAST_DAY)
                        .nextAfter(at(2026, 2, 1, 0, 0), SHANGHAI, ""));
    }

    @Test
    public void lastDayUsesTwentyNineInLeapFebruary() {
        assertEquals(at(2028, 2, 29, 3, 0),
                plan(TimerRebootPlan.MONTHLY, 3, 0, 0, TimerRebootPlan.LAST_DAY)
                        .nextAfter(at(2028, 2, 1, 0, 0), SHANGHAI, ""));
    }

    @Test
    public void monthlyCrossesYearBoundary() {
        assertEquals(at(2027, 1, 15, 3, 0),
                plan(TimerRebootPlan.MONTHLY, 3, 0, 0, 15)
                        .nextAfter(at(2026, 12, 20, 0, 0), SHANGHAI, ""));
    }

    @Test
    public void onceUsesSpecifiedDateAndTime() {
        TimerRebootPlan plan = new TimerRebootPlan(TimerRebootPlan.ONCE, 18, 0,
                2026, 11, 3, 0, 1);
        assertEquals(at(2026, 11, 3, 18, 0),
                plan.nextAfter(at(2026, 11, 2, 15, 0), SHANGHAI, ""));
    }

    @Test
    public void expiredOnceNeverCatchesUp() {
        TimerRebootPlan plan = new TimerRebootPlan(TimerRebootPlan.ONCE, 18, 0,
                2026, 11, 2, 0, 1);
        assertEquals(0L, plan.nextAfter(at(2026, 11, 2, 18, 10), SHANGHAI, ""));
    }

    @Test
    public void completedOnceDoesNotRunAgainAfterClockRollback() {
        TimerRebootPlan plan = new TimerRebootPlan(TimerRebootPlan.ONCE, 18, 0,
                2026, 11, 2, 0, 1);
        assertEquals(0L, plan.nextAfter(at(2026, 11, 2, 17, 0), SHANGHAI, "2026-11-02"));
    }

    @Test
    public void invalidDatesAndTimeAreRejected() {
        assertFalse(new TimerRebootPlan(TimerRebootPlan.ONCE, 3, 0,
                2026, 2, 30, 0, 1).isValid());
        assertFalse(plan(TimerRebootPlan.DAILY, 24, 0, 0, 1).isValid());
        assertFalse(plan(TimerRebootPlan.DAILY, 3, 60, 0, 1).isValid());
        assertFalse(plan(TimerRebootPlan.MONTHLY, 3, 0, 0, 32).isValid());
        assertTrue(plan(TimerRebootPlan.MONTHLY, 3, 0, 0, 0).isValid());
    }

    @Test
    public void timezoneChangeKeepsLocalEighteenRatherThanOldUtcInstant() {
        TimerRebootPlan plan = plan(TimerRebootPlan.DAILY, 18, 0, 0, 1);
        long now = at(2026, 11, 2, 17, 0);
        TimeZone tokyo = TimeZone.getTimeZone("Asia/Tokyo");
        long next = plan.nextAfter(now, tokyo, "");
        Calendar calendar = Calendar.getInstance(tokyo);
        calendar.setTimeInMillis(next);
        assertEquals(18, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(3, calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void nonexistentDaylightSavingTimeIsSkipped() {
        TimeZone newYork = TimeZone.getTimeZone("America/New_York");
        Calendar before = Calendar.getInstance(newYork);
        before.clear();
        before.set(2026, Calendar.MARCH, 8, 0, 0);
        long next = plan(TimerRebootPlan.DAILY, 2, 30, 0, 1)
                .nextAfter(before.getTimeInMillis(), newYork, "");
        Calendar result = Calendar.getInstance(newYork);
        result.setTimeInMillis(next);
        assertEquals(9, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(2, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, result.get(Calendar.MINUTE));
    }

    @Test
    public void sameScheduleIgnoresInactiveFieldsButDetectsActiveChanges() {
        TimerRebootPlan daily = plan(TimerRebootPlan.DAILY, 18, 0, 0, 1);
        assertTrue(daily.sameSchedule(new TimerRebootPlan(TimerRebootPlan.DAILY,
                18, 0, 2027, 12, 31, 127, 31)));
        assertFalse(daily.sameSchedule(plan(TimerRebootPlan.DAILY, 19, 0, 0, 1)));
        assertFalse(daily.sameSchedule(plan(TimerRebootPlan.WEEKLY, 18, 0, 2, 1)));
        assertFalse(plan(TimerRebootPlan.MONTHLY, 18, 0, 0, 1)
                .sameSchedule(plan(TimerRebootPlan.MONTHLY, 18, 0, 0, 31)));
    }

    private static TimerRebootPlan plan(int type, int hour, int minute, int weekdays, int monthDay) {
        return new TimerRebootPlan(type, hour, minute, 2026, 11, 2, weekdays, monthDay);
    }

    private static long at(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance(SHANGHAI);
        calendar.clear();
        calendar.set(year, month - 1, day, hour, minute, 0);
        return calendar.getTimeInMillis();
    }
}
