package com.ichtj.basetools.reboot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RebootConfirmationPolicyTest {
    private static final long REQUEST_TIME_MS = 1_000_000L;

    @Test
    public void changedBootMarkersWithinThreeMinutesConfirmSuccess() {
        RebootStateStore.PendingTask task = task(12, "boot-a");

        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                task, REQUEST_TIME_MS + 179_999L, 13, "boot-b");

        assertTrue(result.isSuccess());
        assertEquals(RebootConfirmationPolicy.Status.SUCCESS, result.status);
    }

    @Test
    public void exactlyThreeMinutesStillConfirmsSuccess() {
        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                task(12, "boot-a"),
                REQUEST_TIME_MS + RebootConfirmationPolicy.CONFIRM_WINDOW_MS,
                13, "boot-b");

        assertTrue(result.isSuccess());
    }

    @Test
    public void bootAfterThreeMinutesIsNotCounted() {
        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                task(12, "boot-a"),
                REQUEST_TIME_MS + RebootConfirmationPolicy.CONFIRM_WINDOW_MS + 1L,
                13, "boot-b");

        assertFalse(result.isSuccess());
        assertEquals(RebootConfirmationPolicy.Status.CONFIRM_TIMEOUT, result.status);
    }

    @Test
    public void unchangedBootMarkerIsNotCounted() {
        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                task(12, "boot-a"), REQUEST_TIME_MS + 30_000L, 12, "boot-a");

        assertFalse(result.isSuccess());
        assertEquals(RebootConfirmationPolicy.Status.BOOT_NOT_CHANGED, result.status);
    }

    @Test
    public void noPendingTimerTaskIsNotCounted() {
        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                null, REQUEST_TIME_MS + 30_000L, 13, "boot-b");

        assertFalse(result.isSuccess());
        assertEquals(RebootConfirmationPolicy.Status.NO_PENDING_TASK, result.status);
    }

    @Test
    public void backwardsSystemTimeIsNotCounted() {
        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                task(12, "boot-a"), REQUEST_TIME_MS - 1L, 13, "boot-b");

        assertFalse(result.isSuccess());
        assertEquals(RebootConfirmationPolicy.Status.INVALID_REQUEST_TIME, result.status);
    }

    @Test
    public void bootIdCanConfirmWhenBootCountIsUnavailable() {
        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                task(-1, "boot-a"), REQUEST_TIME_MS + 30_000L, -1, "boot-b");

        assertTrue(result.isSuccess());
    }

    @Test
    public void unavailableBootMarkersAreNotCounted() {
        RebootConfirmationPolicy.Result result = RebootConfirmationPolicy.evaluate(
                task(-1, ""), REQUEST_TIME_MS + 30_000L, -1, "");

        assertFalse(result.isSuccess());
        assertEquals(RebootConfirmationPolicy.Status.BOOT_MARKER_UNAVAILABLE, result.status);
    }

    private static RebootStateStore.PendingTask task(int bootCount, String bootId) {
        return new RebootStateStore.PendingTask("task-1", REQUEST_TIME_MS, bootCount, bootId);
    }
}
