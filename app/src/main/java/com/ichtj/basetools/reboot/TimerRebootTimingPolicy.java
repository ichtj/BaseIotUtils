package com.ichtj.basetools.reboot;

/** Rejects stale clock-based alarms and evaluates boot evidence without UI state. */
final class TimerRebootTimingPolicy {
    static final long CONFIRM_MS = 180_000L;

    enum Confirmation { WAIT, SUCCESS, UNCONFIRMED }

    private TimerRebootTimingPolicy() {
    }

    static boolean isDue(long now, long elapsed, long trigger, long scheduledWall,
                         long scheduledElapsed) {
        long clockShift = (now - scheduledWall) - (elapsed - scheduledElapsed);
        return trigger > 0L && now >= trigger && now - trigger <= 60_000L
                && Math.abs(clockShift) <= 2000L;
    }

    static Confirmation confirm(long now, long elapsed, long requestWall, long requestElapsed,
                                String beforeId, String afterId, int beforeCount, int afterCount) {
        boolean idsAvailable = beforeId != null && afterId != null
                && !beforeId.isEmpty() && !afterId.isEmpty();
        boolean countsAvailable = beforeCount >= 0 && afterCount >= 0;
        if (requestWall <= 0L || requestElapsed < 0L || (!idsAvailable && !countsAvailable)) {
            return Confirmation.UNCONFIRMED;
        }
        boolean sameBoot = idsAvailable ? beforeId.equals(afterId) : beforeCount == afterCount;
        if (sameBoot) {
            long wait = elapsed - requestElapsed;
            return wait >= 0L && wait < CONFIRM_MS ? Confirmation.WAIT : Confirmation.UNCONFIRMED;
        }
        if (countsAvailable && afterCount <= beforeCount) {
            return Confirmation.UNCONFIRMED;
        }
        long bootDelay = now - elapsed - requestWall;
        return bootDelay >= -2000L && bootDelay <= CONFIRM_MS
                ? Confirmation.SUCCESS : Confirmation.UNCONFIRMED;
    }
}
