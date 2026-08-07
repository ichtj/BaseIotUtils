package com.ichtj.basetools.reboot;

/**
 * Pure decision logic for confirming that a pending timer request caused this boot.
 */
final class RebootConfirmationPolicy {
    static final long CONFIRM_WINDOW_MS = 3 * 60 * 1000L;

    enum Status {
        SUCCESS,
        NO_PENDING_TASK,
        INVALID_REQUEST_TIME,
        CONFIRM_TIMEOUT,
        BOOT_MARKER_UNAVAILABLE,
        BOOT_NOT_CHANGED
    }

    static final class Result {
        final Status status;
        final long elapsedMs;
        final String detail;

        Result(Status status, long elapsedMs, String detail) {
            this.status = status;
            this.elapsedMs = elapsedMs;
            this.detail = detail;
        }

        boolean isSuccess() {
            return status == Status.SUCCESS;
        }
    }

    private RebootConfirmationPolicy() {
    }

    static Result evaluate(RebootStateStore.PendingTask task, long nowWallTimeMs,
                           int currentBootCount, String currentBootId) {
        if (task == null || isEmpty(task.taskId)) {
            return new Result(Status.NO_PENDING_TASK, -1L, "No pending timer reboot task");
        }

        long elapsedMs = nowWallTimeMs - task.requestWallTimeMs;
        if (task.requestWallTimeMs <= 0L || elapsedMs < 0L) {
            return new Result(Status.INVALID_REQUEST_TIME, elapsedMs,
                    "System time moved backwards or request time is invalid");
        }
        if (elapsedMs > CONFIRM_WINDOW_MS) {
            return new Result(Status.CONFIRM_TIMEOUT, elapsedMs,
                    "BOOT_COMPLETED arrived outside the 3 minute window");
        }

        boolean comparedMarker = false;
        if (task.requestBootCount >= 0 && currentBootCount >= 0) {
            comparedMarker = true;
            if (currentBootCount <= task.requestBootCount) {
                return new Result(Status.BOOT_NOT_CHANGED, elapsedMs,
                        "BOOT_COUNT did not increase");
            }
        }

        if (!isEmpty(task.requestBootId) && !isEmpty(currentBootId)) {
            comparedMarker = true;
            if (task.requestBootId.equals(currentBootId)) {
                return new Result(Status.BOOT_NOT_CHANGED, elapsedMs,
                        "Kernel boot_id did not change");
            }
        }

        if (!comparedMarker) {
            return new Result(Status.BOOT_MARKER_UNAVAILABLE, elapsedMs,
                    "Neither BOOT_COUNT nor kernel boot_id could be compared");
        }
        return new Result(Status.SUCCESS, elapsedMs,
                "Pending timer reboot was confirmed by BOOT_COMPLETED");
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
