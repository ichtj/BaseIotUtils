package com.face_chtj.base_iotutils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Root shell command helper.
 * result == 0 means the whole task succeeded, result != 0 means failed.
 */
public class ShellUtils {
    private static final String TAG = ShellUtils.class.getSimpleName();

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_EMPTY_COMMAND = -1;
    public static final int RESULT_TIMEOUT = -2;
    public static final int RESULT_EXCEPTION = -4;
    public static final int RESULT_ROOT_SHELL_NOT_FOUND = -5;

    private static final String COMMAND_SU = "su";
    private static final String[] COMPATIBLE_SU_PATHS = {
            "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/sbin/su",
            "/vendor/bin/su"
    };
    private static final String[] COMPATIBLE_FSU_PATHS = {
            "/system/xbin/fsu", "/system/bin/fsu", "/vendor/bin/fsu", "/sbin/fsu"
    };
    private static final String COMMAND_EXIT = "exit $?\n";
    private static final String COMMAND_LINE_END = "\n";
    private static final long DEFAULT_TIMEOUT_MS = 15 * 1000L;
    private static final long ROOT_PROBE_TIMEOUT_MS = 5 * 1000L;

    /**
     * Find su path.
     */
    public static String findSuPath() {
        String suPath = null;
        Process process = null;
        BufferedReader in = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            suPath = in.readLine();
            process.waitFor();
        } catch (Exception e) {
            Log.d(TAG, "findSuPath error: " + e.getMessage());
        } finally {
            closeQuietly(in);
            if (process != null) {
                process.destroy();
            }
        }
        return suPath;
    }

    /**
     * Check whether su exists in common paths.
     */
    public static boolean isCheckRoot() {
        final String[] suSearchPaths = {"/system/bin/", "/system/xbin/", "/system/sbin/",
                "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < suSearchPaths.length; i++) {
                File file = new File(suSearchPaths[i] + "su");
                if (file.exists()) {
                    Log.d(TAG, "find su in : " + suSearchPaths[i]);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "isCheckRoot error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Escape one shell argument, especially for file paths.
     */
    public static String quote(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("'", "'\\''") + "'";
    }

    public static CommandResult exec(String command) {
        return exec(command, DEFAULT_TIMEOUT_MS);
    }

    public static CommandResult exec(String command, long timeoutMs) {
        timeoutMs = normalizeTimeout(timeoutMs);
        if (command == null || command.trim().length() == 0) {
            CommandResult result = new CommandResult();
            result.result = RESULT_EMPTY_COMMAND;
            result.errorMsg = "command is empty";
            return result;
        }
        return exec(new String[]{command}, null, timeoutMs);
    }

    /**
     * Executes a command with an explicitly detected root shell. Existing exec methods still use
     * the original "su" command and are intentionally unaffected by this compatibility path.
     */
    public static CommandResult execCompatibleRoot(String command) {
        return execCompatibleRoot(command, DEFAULT_TIMEOUT_MS);
    }

    public static CommandResult execCompatibleRoot(String command, long timeoutMs) {
        timeoutMs = normalizeTimeout(timeoutMs);
        if (command == null || command.trim().length() == 0) {
            CommandResult result = new CommandResult();
            result.result = RESULT_EMPTY_COMMAND;
            result.errorMsg = "command is empty";
            return result;
        }

        List<String> rootShells = findCompatibleRootShellPaths();
        if (rootShells.isEmpty()) {
            CommandResult result = new CommandResult();
            result.result = RESULT_ROOT_SHELL_NOT_FOUND;
            result.errorMsg = "No executable su or fsu root shell found";
            result.failedCommand = command;
            return result;
        }

        StringBuilder probeErrors = new StringBuilder();
        for (String rootShell : rootShells) {
            CommandResult probe = execOne("id", Math.min(timeoutMs, ROOT_PROBE_TIMEOUT_MS),
                    rootShell);
            if (isRootIdentity(probe)) {
                CommandResult result = execOne(command, timeoutMs, rootShell);
                result.shellPath = rootShell;
                return result;
            }
            appendProbeError(probeErrors, rootShell, probe);
        }

        CommandResult result = new CommandResult();
        result.result = RESULT_ROOT_SHELL_NOT_FOUND;
        result.errorMsg = "No compatible shell obtained uid=0. " + probeErrors;
        result.failedCommand = command;
        return result;
    }

    /** Returns an executable su path when available, otherwise a vendor fsu path. */
    public static String findCompatibleRootShellPath() {
        List<String> paths = findCompatibleRootShellPaths();
        return paths.isEmpty() ? null : paths.get(0);
    }

    public static CommandResult exec(String[] commands) {
        return exec(commands, null, DEFAULT_TIMEOUT_MS);
    }

    public static CommandResult exec(String[] commands, long timeoutMs) {
        return exec(commands, null, timeoutMs);
    }

    public static CommandResult execScript(String script) {
        return execScript(script, DEFAULT_TIMEOUT_MS);
    }

    public static CommandResult execScript(String script, long timeoutMs) {
        if (script == null || script.trim().length() == 0) {
            CommandResult result = new CommandResult();
            result.result = RESULT_EMPTY_COMMAND;
            result.errorMsg = "script is empty";
            return result;
        }
        return exec(splitScript(script), timeoutMs);
    }

    /**
     * Execute main commands in fail-fast mode. finallyCommands always run, but do not override
     * the main result.
     */
    public static CommandResult exec(String[] commands, String[] finallyCommands, long timeoutMs) {
        timeoutMs = normalizeTimeout(timeoutMs);
        long startTime = System.currentTimeMillis();
        CommandResult finalResult = new CommandResult();
        finalResult.result = RESULT_SUCCESS;

        if (commands == null || commands.length == 0) {
            finalResult.result = RESULT_EMPTY_COMMAND;
            finalResult.errorMsg = "commands is empty";
            finalResult.durationMs = System.currentTimeMillis() - startTime;
            return finalResult;
        }

        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            if (command == null || command.trim().length() == 0) {
                continue;
            }
            CommandResult stepResult = execOne(command, timeoutMs);
            appendMessage(finalResult, stepResult);
            if (stepResult.result != RESULT_SUCCESS) {
                finalResult.result = stepResult.result;
                finalResult.failedCommand = command;
                finalResult.timeout = stepResult.timeout;
                break;
            }
        }

        runFinallyCommands(finalResult, finallyCommands, timeoutMs);
        finalResult.durationMs = System.currentTimeMillis() - startTime;
        return finalResult;
    }

    private static void runFinallyCommands(CommandResult finalResult, String[] finallyCommands,
                                           long timeoutMs) {
        if (finallyCommands == null || finallyCommands.length == 0) {
            return;
        }
        for (int i = 0; i < finallyCommands.length; i++) {
            String command = finallyCommands[i];
            if (command == null || command.trim().length() == 0) {
                continue;
            }
            CommandResult cleanResult = execOne(command, timeoutMs);
            appendMessage(finalResult, cleanResult);
            // Cleanup errors are kept in errorMsg, but never replace the main command result.
            if (cleanResult.result != RESULT_SUCCESS) {
                appendError(finalResult, "finally command failed: " + command
                        + ", result=" + cleanResult.result);
            }
        }
    }

    private static CommandResult execOne(String command, long timeoutMs) {
        return execOne(command, timeoutMs, COMMAND_SU);
    }

    private static CommandResult execOne(String command, long timeoutMs, String shellPath) {
        long startTime = System.currentTimeMillis();
        CommandResult commandResult = new CommandResult();
        commandResult.result = RESULT_EXCEPTION;

        Process process = null;
        DataOutputStream os = null;
        StreamReaderThread successReader = null;
        StreamReaderThread errorReader = null;
        WaitProcessThread waitThread = null;
        try {
            process = Runtime.getRuntime().exec(shellPath);
            os = new DataOutputStream(process.getOutputStream());
            successReader = new StreamReaderThread(process.getInputStream());
            errorReader = new StreamReaderThread(process.getErrorStream());
            waitThread = new WaitProcessThread(process);

            successReader.start();
            errorReader.start();
            waitThread.start();

            os.write(command.getBytes());
            os.writeBytes(COMMAND_LINE_END);
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            closeQuietly(os);
            os = null;

            waitThread.join(timeoutMs);
            if (waitThread.isAlive()) {
                commandResult.result = RESULT_TIMEOUT;
                commandResult.timeout = true;
                commandResult.failedCommand = command;
                commandResult.errorMsg = "Command timeout after " + timeoutMs + " ms";
                process.destroy();
                waitThread.interrupt();
            } else if (waitThread.errorMsg != null) {
                commandResult.result = RESULT_EXCEPTION;
                commandResult.errorMsg = waitThread.errorMsg;
                commandResult.failedCommand = command;
            } else {
                commandResult.result = waitThread.exitCode;
                if (commandResult.result != RESULT_SUCCESS) {
                    commandResult.failedCommand = command;
                }
            }

            joinReader(successReader);
            joinReader(errorReader);
            commandResult.successMsg = successReader.getContent();
            commandResult.errorMsg = mergeError(commandResult.errorMsg, errorReader.getContent());
        } catch (Throwable e) {
            commandResult.result = RESULT_EXCEPTION;
            commandResult.failedCommand = command;
            commandResult.errorMsg = e.getMessage();
            if (process != null) {
                process.destroy();
            }
        } finally {
            closeQuietly(os);
            if (process != null) {
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
                process.destroy();
            }
            commandResult.durationMs = System.currentTimeMillis() - startTime;
        }
        return commandResult;
    }

    private static void appendMessage(CommandResult target, CommandResult source) {
        if (source == null) {
            return;
        }
        target.successMsg = append(target.successMsg, source.successMsg);
        target.errorMsg = append(target.errorMsg, source.errorMsg);
    }

    private static void appendError(CommandResult target, String error) {
        target.errorMsg = append(target.errorMsg, error);
    }

    private static String append(String oldValue, String newValue) {
        if (newValue == null || newValue.length() == 0) {
            return oldValue;
        }
        if (oldValue == null || oldValue.length() == 0) {
            return newValue;
        }
        return oldValue + "\n" + newValue;
    }

    private static String mergeError(String oldError, String streamError) {
        return append(oldError, streamError);
    }

    private static void joinReader(Thread thread) {
        if (thread == null) {
            return;
        }
        try {
            thread.join(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static long normalizeTimeout(long timeoutMs) {
        return timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT_MS;
    }

    private static List<String> findCompatibleRootShellPaths() {
        Set<String> paths = new LinkedHashSet<String>();
        addExecutables(paths, COMPATIBLE_SU_PATHS);
        String discoveredSu = findSuPath();
        if (isExecutable(discoveredSu)) {
            paths.add(discoveredSu.trim());
        }
        addExecutables(paths, COMPATIBLE_FSU_PATHS);
        return new ArrayList<String>(paths);
    }

    private static void addExecutables(Set<String> output, String[] paths) {
        for (String path : paths) {
            if (isExecutable(path)) {
                output.add(path);
            }
        }
    }

    private static boolean isExecutable(String path) {
        if (path == null || path.trim().length() == 0) {
            return false;
        }
        try {
            File file = new File(path.trim());
            return file.isFile() && file.canExecute();
        } catch (Throwable ignored) {
            return false;
        }
    }

    static boolean isRootIdentity(CommandResult probe) {
        if (probe == null || !probe.isSuccess() || probe.successMsg == null) {
            return false;
        }
        String identity = probe.successMsg.trim();
        return identity.contains("uid=0 ") || identity.contains("uid=0(")
                || "0".equals(identity);
    }

    private static void appendProbeError(StringBuilder output, String shellPath,
                                         CommandResult probe) {
        if (output.length() > 0) {
            output.append("; ");
        }
        output.append(shellPath).append(": ");
        if (probe == null) {
            output.append("no result");
            return;
        }
        output.append("result=").append(probe.result)
                .append(", timeout=").append(probe.timeout)
                .append(", output=").append(probe.successMsg)
                .append(", error=").append(probe.errorMsg);
    }

    private static String[] splitScript(String script) {
        List<String> commands = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                current.append(c);
                escaped = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                current.append(c);
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                current.append(c);
                continue;
            }

            // Only split simple command separators outside quotes.
            if (!inSingleQuote && !inDoubleQuote && (c == ';' || c == '\n' || c == '\r')) {
                addCommand(commands, current);
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        addCommand(commands, current);
        return commands.toArray(new String[commands.size()]);
    }

    private static void addCommand(List<String> commands, StringBuilder command) {
        String value = command.toString().trim();
        if (value.length() > 0) {
            commands.add(value);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Command execution result.
     */
    public static class CommandResult {
        public int result = RESULT_EXCEPTION;
        public String errorMsg;
        public String successMsg;
        public String failedCommand;
        public boolean timeout;
        public long durationMs;
        public String shellPath;

        public boolean isSuccess() {
            return result == RESULT_SUCCESS && !timeout;
        }

        @Override
        public String toString() {
            return "CommandResult{"
                    + "result=" + result
                    + ", errorMsg='" + errorMsg + '\''
                    + ", successMsg='" + successMsg + '\''
                    + ", failedCommand='" + failedCommand + '\''
                    + ", timeout=" + timeout
                    + ", durationMs=" + durationMs
                    + '}';
        }
    }

    private static class StreamReaderThread extends Thread {
        private final InputStream inputStream;
        private final StringBuilder content = new StringBuilder();

        StreamReaderThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (content.length() > 0) {
                        content.append('\n');
                    }
                    content.append(line);
                }
            } catch (IOException ignored) {
            } finally {
                closeQuietly(reader);
            }
        }

        String getContent() {
            return content.toString();
        }
    }

    private static class WaitProcessThread extends Thread {
        private final Process process;
        private int exitCode = RESULT_EXCEPTION;
        private String errorMsg;

        WaitProcessThread(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                errorMsg = e.getMessage();
                Thread.currentThread().interrupt();
            }
        }
    }
}
