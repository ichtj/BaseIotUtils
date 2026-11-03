package com.ichtj.basetools.reboot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimerRebootTimingPolicyTest {
    @Test
    public void unchangedClockAllowsOnTimeAlarm() {
        assertTrue(TimerRebootTimingPolicy.isDue(3_600_000L, 3_601_000L,
                3_600_000L, 0L, 1000L));
    }

    @Test
    public void earlyOrLongDelayedAlarmsCannotReboot() {
        assertFalse(TimerRebootTimingPolicy.isDue(999L, 999L, 1000L, 0L, 0L));
        assertFalse(TimerRebootTimingPolicy.isDue(61_001L, 61_001L, 1000L, 0L, 0L));
    }

    @Test
    public void clockJumpToExecutionTimeCannotTriggerCatchUpReboot() {
        assertFalse(TimerRebootTimingPolicy.isDue(3_600_000L, 60_000L,
                3_600_000L, 0L, 0L));
    }

    @Test
    public void reschedulingAfterTimeChangeAllowsNextRealAlarm() {
        assertTrue(TimerRebootTimingPolicy.isDue(7_200_000L, 3_660_000L,
                7_200_000L, 3_600_000L, 60_000L));
    }

    @Test
    public void smallClockSlewDoesNotDiscardAlarm() {
        assertTrue(TimerRebootTimingPolicy.isDue(10_001L, 10_000L, 10_000L, 0L, 0L));
    }

    @Test
    public void sameBootWaitsUntilThreeMinutesThenStops() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.WAIT,
                confirm(1_010_000L, 20_000L, "boot-a", "boot-a", 12, 12));
        assertEquals(TimerRebootTimingPolicy.Confirmation.UNCONFIRMED,
                confirm(1_180_000L, 190_000L, "boot-a", "boot-a", 12, 12));
    }

    @Test
    public void bootEvidenceConfirmsSuccessfulRequest() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.SUCCESS,
                confirm(1_070_000L, 60_000L, "boot-a", "boot-b", 12, 13));
    }

    @Test
    public void slowBootCompletionStillUsesActualBootStart() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.SUCCESS,
                confirm(1_610_000L, 600_000L, "boot-a", "boot-b", 12, 13));
    }

    @Test
    public void bootHoursAfterRequestDoesNotCountAsSuccess() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.UNCONFIRMED,
                confirm(4_660_000L, 60_000L, "boot-a", "boot-b", 12, 13));
    }

    @Test
    public void missingBootMarkersNeverClaimSuccess() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.UNCONFIRMED,
                confirm(1_070_000L, 60_000L, "", "", -1, -1));
    }

    @Test
    public void eitherBootIdOrBootCountCanProvideEvidence() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.SUCCESS,
                confirm(1_070_000L, 60_000L, "boot-a", "boot-b", -1, -1));
        assertEquals(TimerRebootTimingPolicy.Confirmation.SUCCESS,
                confirm(1_070_000L, 60_000L, "", "", 12, 13));
    }

    @Test
    public void contradictoryBootCountRejectsSuccess() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.UNCONFIRMED,
                confirm(1_070_000L, 60_000L, "boot-a", "boot-b", 12, 12));
    }

    @Test
    public void clockRollbackDoesNotProduceFalseSuccess() {
        assertEquals(TimerRebootTimingPolicy.Confirmation.UNCONFIRMED,
                confirm(900_000L, 60_000L, "boot-a", "boot-b", 12, 13));
    }

    private static TimerRebootTimingPolicy.Confirmation confirm(long now, long elapsed,
                                                               String before, String after,
                                                               int beforeCount, int afterCount) {
        return TimerRebootTimingPolicy.confirm(now, elapsed, 1_000_000L, 10_000L,
                before, after, beforeCount, afterCount);
    }
}
