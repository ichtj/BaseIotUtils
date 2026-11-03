package com.face_chtj.base_iotutils.test;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Android 11 memory statistics for the custom userdebug build used by this project.
 *
 * <p>All callbacks run on the Android main thread. UID PSS collection runs in a single
 * background worker and requires the app package to be allowed by the firmware's
 * {@code /data/misc/rootWhiteList.txt}. See the accompanying README for setup details.</p>
 */
public final class MemoryStats {
    private static final String SU_PATH = "/system/xbin/su";
    private static final String SHELL_WITH_READPROC = "2000,2000,3009";
    private static final String TIMEOUT_PATH = "/system/bin/timeout";
    private static final long DEFAULT_COMMAND_TIMEOUT_MS = 30_000L;
    private static final long COMMAND_EXIT_GRACE_MS = 5_000L;
    private static final int MAX_COMMAND_OUTPUT_CHARS = 2 * 1024 * 1024;

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "uid-memory-stats");
                    thread.setDaemon(true);
                    return thread;
                }
            });
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static final String UID_COLLECTION_SCRIPT =
            "echo " + MemoryStatsParser.PACKAGES_BEGIN + "\n"
                    + "/system/bin/cmd package list packages -U --user all\n"
                    + "echo " + MemoryStatsParser.PACKAGES_END + "\n"
                    + "echo " + MemoryStatsParser.PS_BEGIN + "\n"
                    + "/system/bin/ps -A -o PID,UID,NAME\n"
                    + "echo " + MemoryStatsParser.PS_END + "\n"
                    + "echo " + MemoryStatsParser.MEMINFO_BEGIN + "\n"
                    + "exec /system/bin/dumpsys meminfo -c\n";

    private MemoryStats() {
    }

    /** Error categories returned by asynchronous operations. */
    public enum ErrorCode {
        SU_BINARY_NOT_FOUND,
        SU_NOT_AUTHORIZED,
        COMMAND_TIMEOUT,
        COMMAND_FAILED,
        OUTPUT_TOO_LARGE,
        UNSUPPORTED_OUTPUT,
        IO_ERROR,
        INTERRUPTED,
        INTERNAL_ERROR
    }

    /** Structured failure information returned to the caller. */
    public static final class MemoryError {
        private final ErrorCode code;
        private final String message;
        private final Throwable cause;

        private MemoryError(ErrorCode code, String message, Throwable cause) {
            this.code = code;
            this.message = message;
            this.cause = cause;
        }

        public ErrorCode getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "MemoryError{" + "code=" + code + ", message='" + message + "'" + '}';
        }
    }

    /** Callback for a complete memory snapshot. Runs on the main thread. */
    public interface SnapshotCallback {
        void onSuccess(MemorySnapshot snapshot);

        void onError(MemoryError error);
    }

    /** Callback for a single byte value. Runs on the main thread. */
    public interface BytesCallback {
        void onSuccess(long bytes);

        void onError(MemoryError error);
    }

    /** Handle returned by every asynchronous request. */
    public static final class MemoryRequest {
        private volatile Future<?> future;
        private volatile boolean cancelled;

        private MemoryRequest() {
        }

        private void attach(Future<?> value) {
            future = value;
            if (cancelled) {
                value.cancel(false);
            }
        }

        /**
         * Cancels work when possible. A cancelled request does not invoke its callback.
         */
        public void cancel() {
            cancelled = true;
            Future<?> current = future;
            if (current != null) {
                current.cancel(false);
            }
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    /** Memory attributed to one process at the snapshot time. */
    public static final class ProcessMemoryInfo {
        private final int pid;
        private final int uid;
        private final String processName;
        private final long pssBytes;

        private ProcessMemoryInfo(int pid, int uid, String processName, long pssBytes) {
            this.pid = pid;
            this.uid = uid;
            this.processName = processName;
            this.pssBytes = pssBytes;
        }

        public int getPid() {
            return pid;
        }

        public int getUid() {
            return uid;
        }

        public String getProcessName() {
            return processName;
        }

        /** Returns process PSS in bytes. */
        public long getPssBytes() {
            return pssBytes;
        }
    }

    /** Aggregated PSS for all processes running under one Linux UID. */
    public static final class UidMemoryInfo {
        private final int uid;
        private final long pssBytes;
        private final List<String> packageNames;
        private final List<ProcessMemoryInfo> processes;

        private UidMemoryInfo(int uid, long pssBytes, List<String> packageNames,
                List<ProcessMemoryInfo> processes) {
            this.uid = uid;
            this.pssBytes = pssBytes;
            this.packageNames = Collections.unmodifiableList(new ArrayList<>(packageNames));
            this.processes = Collections.unmodifiableList(new ArrayList<>(processes));
        }

        public int getUid() {
            return uid;
        }

        /** Returns the sum of PSS for all processes under this UID, in bytes. */
        public long getPssBytes() {
            return pssBytes;
        }

        public int getProcessCount() {
            return processes.size();
        }

        /**
         * Returns all installed package names assigned to this UID, sorted alphabetically.
         *
         * <p>A shared UID can own multiple packages. Native service UIDs that do not own an APK
         * return an empty list. The returned list is immutable.</p>
         */
        public List<String> getPackageNames() {
            return packageNames;
        }

        public boolean hasPackageNames() {
            return !packageNames.isEmpty();
        }

        public List<ProcessMemoryInfo> getProcesses() {
            return processes;
        }
    }

    /**
     * Immutable result containing global RAM values and per-UID process PSS values.
     */
    public static final class MemorySnapshot {
        private final long timestampMillis;
        private final long totalMemoryBytes;
        private final long availableMemoryBytes;
        private final long usedMemoryBytes;
        private final boolean lowMemory;
        private final long lowMemoryThresholdBytes;
        private final Map<Integer, UidMemoryInfo> uidMemoryInfo;
        private final Map<Integer, Long> uidPssBytes;
        private final List<ProcessMemoryInfo> unmappedProcesses;
        private final long unmappedPssBytes;

        private MemorySnapshot(long timestampMillis, SystemMemory systemMemory,
                MemoryStatsParser.ParsedResult parsedResult) {
            this.timestampMillis = timestampMillis;
            totalMemoryBytes = systemMemory.totalBytes;
            availableMemoryBytes = systemMemory.availableBytes;
            usedMemoryBytes = systemMemory.usedBytes;
            lowMemory = systemMemory.lowMemory;
            lowMemoryThresholdBytes = systemMemory.thresholdBytes;

            TreeMap<Integer, MutableUidMemory> grouped = new TreeMap<>();
            List<ProcessMemoryInfo> unmapped = new ArrayList<>();
            long missingPss = 0L;
            for (MemoryStatsParser.ParsedProcess process : parsedResult.processes) {
                ProcessMemoryInfo info = new ProcessMemoryInfo(process.pid, process.uid,
                        process.name, process.pssBytes);
                if (process.uid < 0) {
                    unmapped.add(info);
                    missingPss = safeAdd(missingPss, process.pssBytes);
                    continue;
                }
                MutableUidMemory mutable = grouped.get(process.uid);
                if (mutable == null) {
                    mutable = new MutableUidMemory(process.uid);
                    grouped.put(process.uid, mutable);
                }
                mutable.pssBytes = safeAdd(mutable.pssBytes, process.pssBytes);
                mutable.processes.add(info);
            }

            LinkedHashMap<Integer, UidMemoryInfo> infos = new LinkedHashMap<>();
            LinkedHashMap<Integer, Long> values = new LinkedHashMap<>();
            for (MutableUidMemory mutable : grouped.values()) {
                List<String> packageNames = parsedResult.packageNamesByUid.get(mutable.uid);
                if (packageNames == null) {
                    packageNames = Collections.emptyList();
                }
                UidMemoryInfo info = new UidMemoryInfo(mutable.uid, mutable.pssBytes,
                        packageNames, mutable.processes);
                infos.put(mutable.uid, info);
                values.put(mutable.uid, mutable.pssBytes);
            }
            uidMemoryInfo = Collections.unmodifiableMap(infos);
            uidPssBytes = Collections.unmodifiableMap(values);
            unmappedProcesses = Collections.unmodifiableList(unmapped);
            unmappedPssBytes = missingPss;
        }

        /** Wall-clock time at which collection completed. */
        public long getTimestampMillis() {
            return timestampMillis;
        }

        /** Returns total physical RAM in bytes. */
        public long getTotalMemoryBytes() {
            return totalMemoryBytes;
        }

        /** Returns RAM currently available to the system in bytes. */
        public long getAvailableMemoryBytes() {
            return availableMemoryBytes;
        }

        /** Returns {@code totalMemoryBytes - availableMemoryBytes}. */
        public long getUsedMemoryBytes() {
            return usedMemoryBytes;
        }

        public boolean isLowMemory() {
            return lowMemory;
        }

        public long getLowMemoryThresholdBytes() {
            return lowMemoryThresholdBytes;
        }

        /**
         * Returns the sampled PSS for {@code uid}, or zero when that UID had no sampled process.
         */
        public long getUidPssBytes(int uid) {
            Long value = uidPssBytes.get(uid);
            return value != null ? value : 0L;
        }

        public UidMemoryInfo getUidMemoryInfo(int uid) {
            return uidMemoryInfo.get(uid);
        }

        /**
         * Returns all installed package names assigned to {@code uid}.
         * Native UIDs and UIDs absent from this snapshot return an immutable empty list.
         */
        public List<String> getUidPackageNames(int uid) {
            UidMemoryInfo info = uidMemoryInfo.get(uid);
            return info != null ? info.getPackageNames() : Collections.<String>emptyList();
        }

        /** Returns an immutable map sorted by UID. Values are bytes. */
        public Map<Integer, Long> getAllUidPssBytes() {
            return uidPssBytes;
        }

        /** Returns process details for every mapped UID. */
        public Map<Integer, UidMemoryInfo> getAllUidMemoryInfo() {
            return uidMemoryInfo;
        }

        /**
         * Returns true when every process in meminfo was present in the concurrent PID/UID map.
         */
        public boolean isUidMappingComplete() {
            return unmappedProcesses.isEmpty();
        }

        /** PSS that could not be assigned because a process changed during sampling. */
        public long getUnmappedPssBytes() {
            return unmappedPssBytes;
        }

        public List<ProcessMemoryInfo> getUnmappedProcesses() {
            return unmappedProcesses;
        }
    }

    /**
     * Collects global RAM values and all running process PSS values grouped by Linux UID.
     *
     * <p>This is the preferred API when the caller needs more than one value. It performs one
     * expensive {@code dumpsys meminfo} operation. The callback runs on the main thread.</p>
     */
    public static MemoryRequest collectMemorySnapshotAsync(Context context,
            SnapshotCallback callback) {
        return collectMemorySnapshotAsync(context, DEFAULT_COMMAND_TIMEOUT_MS, callback);
    }

    /** Same as {@link #collectMemorySnapshotAsync(Context, SnapshotCallback)} with a timeout. */
    public static MemoryRequest collectMemorySnapshotAsync(Context context, long timeoutMillis,
            SnapshotCallback callback) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }
        if (timeoutMillis <= 0L) {
            throw new IllegalArgumentException("timeoutMillis must be > 0");
        }
        final Context appContext = getApplicationContext(context);
        final MemoryRequest request = new MemoryRequest();
        Future<?> future = WORKER.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    CommandResult command = runUidCollectionCommand(timeoutMillis);
                    MemoryStatsParser.ParsedResult parsed = MemoryStatsParser.parse(command.output);
                    SystemMemory systemMemory = readSystemMemory(appContext);
                    final MemorySnapshot snapshot = new MemorySnapshot(
                            System.currentTimeMillis(), systemMemory, parsed);
                    postSnapshotSuccess(request, callback, snapshot);
                } catch (StatsException error) {
                    postSnapshotError(request, callback, error.toMemoryError());
                } catch (RuntimeException error) {
                    postSnapshotError(request, callback, new MemoryError(
                            ErrorCode.INTERNAL_ERROR, "Unexpected collection failure", error));
                }
            }
        });
        request.attach(future);
        return request;
    }

    /** Asynchronously returns total physical RAM in bytes. Root is not required. */
    public static MemoryRequest getTotalMemoryBytesAsync(Context context, BytesCallback callback) {
        return submitSystemMemoryRequest(context, callback, SystemMemoryField.TOTAL);
    }

    /** Asynchronously returns RAM currently available to Android in bytes. Root is not required. */
    public static MemoryRequest getAvailableMemoryBytesAsync(Context context,
            BytesCallback callback) {
        return submitSystemMemoryRequest(context, callback, SystemMemoryField.AVAILABLE);
    }

    /** Asynchronously returns total RAM minus available RAM in bytes. Root is not required. */
    public static MemoryRequest getUsedMemoryBytesAsync(Context context, BytesCallback callback) {
        return submitSystemMemoryRequest(context, callback, SystemMemoryField.USED);
    }

    /**
     * Asynchronously samples all processes and returns the PSS sum for one UID in bytes.
     *
     * <p>This convenience method still performs a complete UID snapshot. Call
     * {@link #collectMemorySnapshotAsync(Context, SnapshotCallback)} once when querying multiple
     * UIDs.</p>
     */
    public static MemoryRequest getUidPssBytesAsync(Context context, final int uid,
            final BytesCallback callback) {
        if (uid < 0) {
            throw new IllegalArgumentException("uid must be >= 0");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }
        return collectMemorySnapshotAsync(context, new SnapshotCallback() {
            @Override
            public void onSuccess(MemorySnapshot snapshot) {
                callback.onSuccess(snapshot.getUidPssBytes(uid));
            }

            @Override
            public void onError(MemoryError error) {
                callback.onError(error);
            }
        });
    }

    /** Formats a byte value using Android's locale-aware file-size formatter. */
    public static String formatBytes(Context context, long bytes) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        return Formatter.formatFileSize(context, bytes);
    }

    private enum SystemMemoryField {
        TOTAL,
        AVAILABLE,
        USED
    }

    private static MemoryRequest submitSystemMemoryRequest(Context context,
            final BytesCallback callback, final SystemMemoryField field) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }
        final Context appContext = getApplicationContext(context);
        final MemoryRequest request = new MemoryRequest();
        Future<?> future = WORKER.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SystemMemory memory = readSystemMemory(appContext);
                    final long value;
                    switch (field) {
                        case TOTAL:
                            value = memory.totalBytes;
                            break;
                        case AVAILABLE:
                            value = memory.availableBytes;
                            break;
                        case USED:
                            value = memory.usedBytes;
                            break;
                        default:
                            throw new AssertionError(field);
                    }
                    postBytesSuccess(request, callback, value);
                } catch (StatsException error) {
                    postBytesError(request, callback, error.toMemoryError());
                } catch (RuntimeException error) {
                    postBytesError(request, callback, new MemoryError(
                            ErrorCode.INTERNAL_ERROR, "Unexpected memory query failure", error));
                }
            }
        });
        request.attach(future);
        return request;
    }

    private static Context getApplicationContext(Context context) {
        Context appContext = context.getApplicationContext();
        return appContext != null ? appContext : context;
    }

    private static SystemMemory readSystemMemory(Context context) throws StatsException {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        if (manager == null) {
            throw new StatsException(ErrorCode.INTERNAL_ERROR,
                    "ActivityManager is unavailable", null);
        }
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        long used = Math.max(0L, info.totalMem - info.availMem);
        return new SystemMemory(info.totalMem, info.availMem, used, info.lowMemory,
                info.threshold);
    }

    private static CommandResult runUidCollectionCommand(long timeoutMillis)
            throws StatsException {
        File su = new File(SU_PATH);
        if (!su.isFile() || !su.canExecute()) {
            throw new StatsException(ErrorCode.SU_BINARY_NOT_FOUND,
                    "Required binary is missing or not executable: " + SU_PATH, null);
        }
        File timeout = new File(TIMEOUT_PATH);
        if (!timeout.isFile() || !timeout.canExecute()) {
            throw new StatsException(ErrorCode.COMMAND_FAILED,
                    "Required binary is missing or not executable: " + TIMEOUT_PATH, null);
        }

        long timeoutSeconds = Math.max(1L, (timeoutMillis + 999L) / 1_000L);
        Process process = null;
        OutputCollector collector = null;
        Thread reader = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    SU_PATH,
                    SHELL_WITH_READPROC,
                    TIMEOUT_PATH,
                    "-s",
                    "KILL",
                    Long.toString(timeoutSeconds),
                    "/system/bin/sh",
                    "-c",
                    UID_COLLECTION_SCRIPT);
            builder.redirectErrorStream(true);
            process = builder.start();
            collector = new OutputCollector(process);
            reader = new Thread(collector, "uid-memory-output");
            reader.setDaemon(true);
            reader.start();

            boolean exited = process.waitFor(timeoutMillis + COMMAND_EXIT_GRACE_MS,
                    TimeUnit.MILLISECONDS);
            if (!exited) {
                process.destroy();
                throw new StatsException(ErrorCode.COMMAND_TIMEOUT,
                        "Memory collection exceeded " + timeoutMillis + " ms", null);
            }
            reader.join(COMMAND_EXIT_GRACE_MS);
            if (reader.isAlive()) {
                process.destroy();
                throw new StatsException(ErrorCode.COMMAND_TIMEOUT,
                        "Timed out while reading memory command output", null);
            }
            if (collector.error != null) {
                throw new StatsException(ErrorCode.IO_ERROR,
                        "Failed to read memory command output", collector.error);
            }
            if (collector.truncated) {
                throw new StatsException(ErrorCode.OUTPUT_TOO_LARGE,
                        "Memory command output exceeded " + MAX_COMMAND_OUTPUT_CHARS
                                + " characters", null);
            }

            String output = collector.output.toString();
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw classifyCommandFailure(exitCode, output, timeoutMillis);
            }
            return new CommandResult(output);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroy();
            }
            throw new StatsException(ErrorCode.INTERRUPTED,
                    "Memory collection was interrupted", error);
        } catch (IOException error) {
            throw new StatsException(ErrorCode.IO_ERROR,
                    "Unable to start memory collection command", error);
        }
    }

    private static StatsException classifyCommandFailure(int exitCode, String output,
            long timeoutMillis) {
        String lower = output.toLowerCase();
        if (lower.contains("rootwhitelist.txt not found")
                || lower.contains("packagename not found")
                || lower.contains("not in root")
                || lower.contains("permission denied")) {
            return new StatsException(ErrorCode.SU_NOT_AUTHORIZED,
                    "The installed su rejected this caller. This su build may enforce "
                            + "rootWhiteList.txt unconditionally; add the application process "
                            + "name to the whitelist, or rebuild and flash su with property-switch "
                            + "support. Command output: "
                            + abbreviate(output), null);
        }
        if (exitCode == 124 || exitCode == 137 || exitCode == 143) {
            return new StatsException(ErrorCode.COMMAND_TIMEOUT,
                    "Memory collection exceeded " + timeoutMillis + " ms", null);
        }
        return new StatsException(ErrorCode.COMMAND_FAILED,
                "Memory command failed with exit code " + exitCode + ": "
                        + abbreviate(output), null);
    }

    private static String abbreviate(String value) {
        String singleLine = value.replace('\n', ' ').replace('\r', ' ').trim();
        return singleLine.length() <= 400 ? singleLine : singleLine.substring(0, 400) + "...";
    }

    private static long safeAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    private static void postSnapshotSuccess(final MemoryRequest request,
            final SnapshotCallback callback, final MemorySnapshot snapshot) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (!request.isCancelled()) {
                    callback.onSuccess(snapshot);
                }
            }
        });
    }

    private static void postSnapshotError(final MemoryRequest request,
            final SnapshotCallback callback, final MemoryError error) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (!request.isCancelled()) {
                    callback.onError(error);
                }
            }
        });
    }

    private static void postBytesSuccess(final MemoryRequest request,
            final BytesCallback callback, final long value) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (!request.isCancelled()) {
                    callback.onSuccess(value);
                }
            }
        });
    }

    private static void postBytesError(final MemoryRequest request,
            final BytesCallback callback, final MemoryError error) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (!request.isCancelled()) {
                    callback.onError(error);
                }
            }
        });
    }

    private static final class MutableUidMemory {
        final int uid;
        long pssBytes;
        final List<ProcessMemoryInfo> processes = new ArrayList<>();

        MutableUidMemory(int uid) {
            this.uid = uid;
        }
    }

    private static final class SystemMemory {
        final long totalBytes;
        final long availableBytes;
        final long usedBytes;
        final boolean lowMemory;
        final long thresholdBytes;

        SystemMemory(long totalBytes, long availableBytes, long usedBytes, boolean lowMemory,
                long thresholdBytes) {
            this.totalBytes = totalBytes;
            this.availableBytes = availableBytes;
            this.usedBytes = usedBytes;
            this.lowMemory = lowMemory;
            this.thresholdBytes = thresholdBytes;
        }
    }

    private static final class CommandResult {
        final String output;

        CommandResult(String output) {
            this.output = output;
        }
    }

    private static final class OutputCollector implements Runnable {
        final Process process;
        final StringBuilder output = new StringBuilder();
        IOException error;
        boolean truncated;

        OutputCollector(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            char[] buffer = new char[8_192];
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), StandardCharsets.UTF_8))) {
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    int remaining = MAX_COMMAND_OUTPUT_CHARS - output.length();
                    if (remaining > 0) {
                        output.append(buffer, 0, Math.min(count, remaining));
                    }
                    if (count > remaining) {
                        truncated = true;
                    }
                }
            } catch (IOException value) {
                error = value;
            }
        }
    }

    static final class StatsException extends Exception {
        private static final long serialVersionUID = 1L;

        final ErrorCode code;

        StatsException(ErrorCode code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        MemoryError toMemoryError() {
            return new MemoryError(code, getMessage(), getCause());
        }
    }
}
