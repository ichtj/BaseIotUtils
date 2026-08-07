package com.ichtj.basetools.reboot;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Persistent reboot audit log. Files roll at 2 MB and are never deleted automatically.
 */
final class RebootLogStore {
    private static final String TAG = "RebootAudit";
    private static final Object LOCK = new Object();
    private static final long MAX_FILE_BYTES = 2L * 1024L * 1024L;
    private static final String DIRECTORY_NAME = "TestReboot/logs";
    private static final String FILE_PREFIX = "reboot_";
    private static final String FILE_SUFFIX = ".log";
    private static File currentFile;

    static final class WriteResult {
        final boolean success;
        final String filePath;
        final String errorMessage;

        WriteResult(boolean success, String filePath, String errorMessage) {
            this.success = success;
            this.filePath = filePath;
            this.errorMessage = errorMessage;
        }
    }

    static final class LogStats {
        final int fileCount;
        final long totalBytes;

        LogStats(int fileCount, long totalBytes) {
            this.fileCount = fileCount;
            this.totalBytes = totalBytes;
        }
    }

    static final class ClearResult {
        final int matchedFiles;
        final int deletedFiles;
        final int failedFiles;
        final long freedBytes;

        ClearResult(int matchedFiles, int deletedFiles, int failedFiles, long freedBytes) {
            this.matchedFiles = matchedFiles;
            this.deletedFiles = deletedFiles;
            this.failedFiles = failedFiles;
            this.freedBytes = freedBytes;
        }
    }

    private RebootLogStore() {
    }

    static WriteResult info(String event, String taskId, int bootCount, long successCount,
                            String detail) {
        return write("INFO", event, taskId, bootCount, successCount, detail, null);
    }

    static WriteResult error(String event, String taskId, int bootCount, long successCount,
                             String detail, Throwable throwable) {
        return write("ERROR", event, taskId, bootCount, successCount, detail, throwable);
    }

    static File getLogDirectory() {
        return new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
    }

    static LogStats getStats() {
        synchronized (LOCK) {
            File[] files = listLogFiles();
            long bytes = 0L;
            for (File file : files) {
                bytes += Math.max(0L, file.length());
            }
            return new LogStats(files.length, bytes);
        }
    }

    static ClearResult clearLogs() {
        synchronized (LOCK) {
            File[] files = listLogFiles();
            int deleted = 0;
            int failed = 0;
            long freedBytes = 0L;
            for (File file : files) {
                long size = Math.max(0L, file.length());
                if (file.delete()) {
                    deleted++;
                    freedBytes += size;
                    if (file.equals(currentFile)) {
                        currentFile = null;
                    }
                } else {
                    failed++;
                }
            }
            Log.i(TAG, "CLEAR_LOGS matched=" + files.length + ", deleted=" + deleted
                    + ", failed=" + failed + ", freedBytes=" + freedBytes);
            return new ClearResult(files.length, deleted, failed, freedBytes);
        }
    }

    private static WriteResult write(String level, String event, String taskId, int bootCount,
                                     long successCount, String detail, Throwable throwable) {
        String safeDetail = normalize(detail);
        String logcatMessage = "event=" + normalize(event)
                + ", taskId=" + normalize(taskId)
                + ", bootCount=" + bootCount
                + ", successCount=" + successCount
                + ", detail=" + safeDetail;
        if ("ERROR".equals(level)) {
            Log.e(TAG, logcatMessage, throwable);
        } else {
            Log.i(TAG, logcatMessage);
        }

        synchronized (LOCK) {
            BufferedWriter writer = null;
            FileOutputStream outputStream = null;
            try {
                if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    return new WriteResult(false, "", "External storage is not mounted");
                }
                File directory = getLogDirectory();
                if (!directory.exists() && !directory.mkdirs()) {
                    return new WriteResult(false, directory.getAbsolutePath(),
                            "Cannot create log directory");
                }

                String throwableText = throwable == null ? "" : normalize(Log.getStackTraceString(throwable));
                String line = formatTimestamp(System.currentTimeMillis())
                        + " | " + level
                        + " | event=" + normalize(event)
                        + " | taskId=" + normalize(taskId)
                        + " | bootCount=" + bootCount
                        + " | successCount=" + successCount
                        + " | detail=" + safeDetail
                        + (throwableText.length() == 0 ? "" : " | throwable=" + throwableText)
                        + System.lineSeparator();
                byte[] bytes = line.getBytes("UTF-8");
                File target = obtainTargetFile(directory, bytes.length);
                outputStream = new FileOutputStream(target, true);
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(line);
                writer.flush();
                outputStream.getFD().sync();
                currentFile = target;
                return new WriteResult(true, target.getAbsolutePath(), "");
            } catch (Throwable writeError) {
                String message = writeError.getClass().getSimpleName() + ": "
                        + normalize(writeError.getMessage());
                Log.e(TAG, "Persistent log write failed: " + message, writeError);
                return new WriteResult(false,
                        currentFile == null ? getLogDirectory().getAbsolutePath()
                                : currentFile.getAbsolutePath(),
                        message);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable ignored) {
                        // The write result above already reflects the durable sync operation.
                    }
                } else if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable ignored) {
                        // Nothing else can be recovered here.
                    }
                }
            }
        }
    }

    private static File obtainTargetFile(File directory, int incomingBytes) {
        if (currentFile != null && currentFile.exists()
                && currentFile.length() + incomingBytes <= MAX_FILE_BYTES) {
            return currentFile;
        }

        File latest = null;
        for (File file : listLogFiles()) {
            if (latest == null || file.lastModified() > latest.lastModified()) {
                latest = file;
            }
        }
        if (latest != null && latest.length() + incomingBytes <= MAX_FILE_BYTES) {
            currentFile = latest;
            return latest;
        }

        String baseName = FILE_PREFIX + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)
                .format(new Date());
        File candidate = new File(directory, baseName + FILE_SUFFIX);
        int suffix = 1;
        while (candidate.exists()) {
            candidate = new File(directory, baseName + "_" + suffix + FILE_SUFFIX);
            suffix++;
        }
        currentFile = candidate;
        return candidate;
    }

    private static File[] listLogFiles() {
        File directory = getLogDirectory();
        File[] files = directory.listFiles(file -> file.isFile()
                && file.getName().startsWith(FILE_PREFIX)
                && file.getName().endsWith(FILE_SUFFIX));
        return files == null ? new File[0] : files;
    }

    private static String formatTimestamp(long timeMs) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
                .format(new Date(timeMs));
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r", "\\r").replace("\n", "\\n").replace("|", "\\|");
    }
}
