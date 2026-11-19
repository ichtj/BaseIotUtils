package com.ichtj.basetools.nginx;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.ichtj.basetools.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Owns one foreground-page session. No shell, root, or system-wide process killing. */
public final class NginxController {
    private static final String TAG = "ichtj_nginx_controller";
    private static final int LOG_LIMIT = 32 * 1024;
    public static final int PORT = 8080;

    public enum Action {
        PREPARE(R.string.nginx_preparing), START(R.string.nginx_start),
        STOP(R.string.nginx_stop), RELOAD(R.string.nginx_reload),
        CHECK(R.string.nginx_check), VERSION(R.string.nginx_version), LOGS(R.string.nginx_logs),
        COMMAND(R.string.nginx_send);
        final int label;
        Action(int label) { this.label = label; }
    }

    public static final class State {
        public final int title;
        public final boolean ready, running, busy, error, previewReady;
        public final String feedback;
        State(int title, boolean ready, boolean running, boolean busy, boolean error, String feedback, boolean previewReady) {
            this.title = title;
            this.ready = ready;
            this.running = running;
            this.busy = busy;
            this.error = error;
            this.feedback = feedback;
            this.previewReady = previewReady;
        }
    }

    public interface Listener {
        void onState(State state);
        void onLog(String message, boolean error);
    }

    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r ->
            new Thread(r, "ichtj_nginx_commands"));
    private final AtomicBoolean busy = new AtomicBoolean();
    private final File prefix;
    private final File executable;
    private final String token = "ichtj_nginx_" + UUID.randomUUID().toString().replace("-", "");
    private final int port;
    private volatile Listener listener;
    private volatile Process server;
    private volatile boolean visible = true, stopRequested, released, prepared;
    private volatile boolean daemonSession, localHealthy;
    private String[] launchOptions = new String[0];
    private volatile State state;

    public NginxController(Context context, Listener listener) {
        this(context, listener, PORT);
    }

    NginxController(Context context, Listener listener, int port) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.port = port;
        prefix = new File(this.context.getFilesDir(), "nginx/session-" + token);
        executable = new File(this.context.getApplicationInfo().nativeLibraryDir, "libnginx.so");
        state = new State(R.string.nginx_preparing, false, false, false, false,
                text(R.string.nginx_ready_hint), false);
    }

    public State getState() { return state; }
    public String getLocalUrl() { return "http://127.0.0.1:" + port + "/"; }

    public boolean execute(Action action) {
        return execute(action, null);
    }

    public boolean executeCommand(String input) {
        if (released) return false;
        try {
            List<String> arguments = splitCommand(input);
            if (!arguments.isEmpty()) {
                String first = arguments.get(0);
                if ("nginx".equals(first) || "libnginx.so".equals(first) || executable.getAbsolutePath().equals(first)) arguments.remove(0);
            }
            return execute(Action.COMMAND, arguments.toArray(new String[0]));
        } catch (IllegalArgumentException error) {
            String feedback = text(R.string.nginx_command_parse_error);
            log(feedback, true);
            publish(restingTitle(), feedback, true);
            return false;
        }
    }

    private boolean execute(Action action, String[] nativeArguments) {
        if (released) return false;
        if (!busy.compareAndSet(false, true)) {
            log(text(R.string.nginx_busy), true);
            return false;
        }
        if (action == Action.START || action == Action.COMMAND) stopRequested = false;
        publish(titleFor(action), text(R.string.nginx_executing, text(action.label)), false);
        worker.execute(() -> {
            String label = text(action.label);
            log(text(R.string.nginx_executing, label), false);
            boolean error = false;
            String feedback;
            try {
                if (released) throw new IOException(text(R.string.nginx_detached));
                if (action != Action.PREPARE && !prepared) throw new IOException(text(R.string.nginx_missing_binary));
                switch (action) {
                    case PREPARE: prepare(); break;
                    case START: startServer(); break;
                    case STOP: stopServer(); break;
                    case RELOAD: reload(); break;
                    case CHECK: command(inContext("-t")); break;
                    case VERSION: command(inContext("-V")); break;
                    case LOGS: showRuntimeLogs(); break;
                    case COMMAND: nativeCommand(nativeArguments); break;
                }
                feedback = action == Action.PREPARE ? text(R.string.nginx_prepared)
                        : text(R.string.nginx_completed, label);
                log(feedback, false);
            } catch (Exception exception) {
                error = true;
                feedback = text(R.string.nginx_operation_failed, label, message(exception));
                Log.e(TAG, feedback, exception);
                log(feedback, true);
                showRuntimeLogs();
            } finally {
                busy.set(false);
            }
            publish(restingTitle(), feedback, error);
        });
        return true;
    }

    public void onVisible() {
        visible = true;
        publish(state.title, state.feedback, state.error);
    }

    public void onHidden() {
        visible = false;
        stopRequested = true;
        if (released) return;
        // Queue cleanup even if a start/check command is currently running.
        worker.execute(() -> {
            if (!active()) return;
            busy.set(true);
            String feedback = text(R.string.nginx_auto_stop);
            log(feedback, false);
            publish(R.string.nginx_stopping, feedback, false);
            boolean error = false;
            try {
                stopServer();
                feedback = text(R.string.nginx_completed, text(R.string.nginx_stop));
            } catch (Exception exception) {
                error = true;
                feedback = text(R.string.nginx_operation_failed, text(R.string.nginx_stop), message(exception));
                Log.e(TAG, feedback, exception);
            } finally {
                busy.set(false);
            }
            log(feedback, error);
            publish(restingTitle(), feedback, error);
        });
    }

    public void release() {
        if (released) return;
        released = true;
        visible = false;
        stopRequested = true;
        listener = null;
        worker.execute(() -> {
            try {
                stopServer();
                if (!active()) deleteSession(prefix);
            } catch (Exception error) {
                Log.e(TAG, "Session cleanup failed: " + prefix, error);
            }
        });
        worker.shutdown();
    }

    private void prepare() throws Exception {
        if (Build.VERSION.SDK_INT < 30) throw new IOException(text(R.string.nginx_unsupported));
        if (!executable.isFile() || !executable.canExecute()) throw new IOException(text(R.string.nginx_missing_binary));
        for (String directory : new String[]{"conf", "html", "logs", "temp/client_body", "temp/proxy",
                "temp/fastcgi", "temp/uwsgi", "temp/scgi"}) {
            File path = new File(prefix, directory);
            if (!path.isDirectory() && !path.mkdirs()) throw new IOException("mkdir: " + path);
        }
        String config = asset("nginx/conf/nginx.conf").replace("@@PORT@@", Integer.toString(port))
                .replace("@@TOKEN@@", token);
        write(new File(prefix, "conf/nginx.conf"), config);
        write(new File(prefix, "conf/mime.types"), asset("nginx/conf/mime.types"));
        write(new File(prefix, "html/index.html"), "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + html(R.string.nginx_page_title) + "</title><style>"
                + "body{margin:0;background:#f4f7f9;color:#1b3540;font-family:sans-serif;padding:40px 24px}"
                + "main{max-width:720px;margin:auto;background:white;padding:32px;border-radius:16px}"
                + "h1{color:#008577;font-size:28px}p{line-height:1.7}footer{color:#70858e;margin-top:32px}"
                + "</style></head><body><main><h1>" + html(R.string.nginx_page_title) + "</h1><p>"
                + html(R.string.nginx_page_description) + "</p><footer>" + html(R.string.nginx_page_footer)
                + "</footer></main></body></html>");
        command("-v");
        prepared = true;
    }

    @TargetApi(26)
    private void startServer() throws Exception {
        if (active()) throw new IOException(text(R.string.nginx_already_running));
        launchOptions = new String[0];
        if (!visible || stopRequested) throw new IOException(text(R.string.nginx_cancelled));
        command("-t");
        if (!visible || stopRequested) throw new IOException(text(R.string.nginx_cancelled));
        log(text(R.string.nginx_command, "libnginx.so -p <session>/ -c conf/nginx.conf"), false);
        Process launched = builder().redirectOutput(ProcessBuilder.Redirect.appendTo(new File(prefix, "logs/launcher.log"))).start();
        server = launched;
        Thread observer = new Thread(() -> observe(launched), "ichtj_nginx_process");
        observer.start();
        try {
            long deadline = SystemClock.elapsedRealtime() + 8000;
            while (SystemClock.elapsedRealtime() < deadline) {
                if (!visible || stopRequested) throw new IOException(text(R.string.nginx_cancelled));
                if (!alive(launched)) throw new IOException(text(R.string.nginx_start_exited, launched.exitValue()));
                if (healthy()) {
                    localHealthy = true;
                    log(text(R.string.nginx_health_ok), false);
                    return;
                }
                Thread.sleep(100);
            }
            throw new IOException(text(R.string.nginx_health_timeout));
        } catch (Exception failure) {
            try { stopServer(); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            if (!active()) launchOptions = new String[0];
            throw failure;
        }
    }

    private void reload() throws Exception {
        if (!active()) throw new IOException(text(R.string.nginx_not_running));
        command(inContext("-t"));
        File errorLog = new File(prefix, "logs/error.log");
        long offset = errorLog.length();
        command(inContext("-s", "reload"));
        if (launchOptions.length > 0 || daemonSession) {
            log(text(R.string.nginx_native_signal_sent), false);
            return;
        }
        long deadline = SystemClock.elapsedRealtime() + 4000;
        while (SystemClock.elapsedRealtime() < deadline && alive(server) && !stopRequested) {
            String output = tail(errorLog, offset);
            if (output.contains("reconfiguring") && output.matches("(?s).*start worker process [0-9]+.*") && healthy()) {
                log(output, false);
                log(text(R.string.nginx_health_ok), false);
                return;
            }
            Thread.sleep(100);
        }
        throw new IOException(text(R.string.nginx_reload_unconfirmed));
    }

    @TargetApi(26)
    private void stopServer() throws Exception {
        if (daemonSession) {
            command(inContext("-s", "quit"));
            daemonSession = false;
            localHealthy = false;
            launchOptions = new String[0];
            log(text(R.string.nginx_native_signal_sent), false);
            return;
        }
        Process owned = server;
        if (!alive(owned)) { server = null; localHealthy = false; return; }
        try { command(inContext("-s", "quit")); } catch (Exception signalError) { log(message(signalError), true); }
        if (!owned.waitFor(4, TimeUnit.SECONDS)) {
            log(text(R.string.nginx_stop_fallback), true);
            owned.destroy(); // SIGTERM to our master; nginx terminates its workers.
            if (!owned.waitFor(2, TimeUnit.SECONDS)) {
                owned.destroyForcibly();
                if (!owned.waitFor(2, TimeUnit.SECONDS)) throw new IOException(text(R.string.nginx_stop_timeout));
            }
        }
        log(text(R.string.nginx_server_exit, owned.exitValue()), false);
        if (server == owned) server = null;
        localHealthy = false;
        launchOptions = new String[0];
    }

    @TargetApi(26)
    private void nativeCommand(String[] arguments) throws Exception {
        if (arguments.length == 0) { startServer(); return; }
        if (isImmediate(arguments)) {
            command(inContext(arguments));
            String signal = signal(arguments);
            if (daemonSession && ("quit".equals(signal) || "stop".equals(signal))) {
                daemonSession = false;
                localHealthy = false;
            }
            return;
        }
        if (active()) throw new IOException(text(R.string.nginx_already_running));
        if (!visible || stopRequested) throw new IOException(text(R.string.nginx_cancelled));
        launchOptions = arguments.clone();
        localHealthy = false;
        log(text(R.string.nginx_command, "libnginx.so " + TextUtils.join(" ", arguments)), false);
        long started = SystemClock.elapsedRealtime();
        Process launched = builder(arguments).redirectOutput(ProcessBuilder.Redirect.appendTo(new File(prefix, "logs/launcher.log"))).start();
        server = launched;
        try {
            long deadline = started + 3000;
            while (alive(launched) && SystemClock.elapsedRealtime() < deadline) {
                if (!visible || stopRequested) throw new IOException(text(R.string.nginx_cancelled));
                if (healthy()) { localHealthy = true; break; }
                Thread.sleep(100);
            }
            String output = tail(new File(prefix, "logs/launcher.log"), 0);
            if (!output.isEmpty()) log(output, false);
            if (!alive(launched)) {
                int exit = launched.exitValue();
                server = null;
                log(text(R.string.nginx_command_result, exit, SystemClock.elapsedRealtime() - started), exit != 0);
                if (exit != 0) throw new IOException(text(R.string.nginx_command_failed, exit));
                daemonSession = true;
                log(text(R.string.nginx_native_background), false);
            } else {
                new Thread(() -> observe(launched), "ichtj_nginx_process").start();
                log(text(localHealthy ? R.string.nginx_health_ok : R.string.nginx_native_foreground), false);
            }
        } catch (Exception failure) {
            try { stopServer(); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            if (!active()) launchOptions = new String[0];
            throw failure;
        }
    }

    private String[] inContext(String... arguments) {
        String[] combined = Arrays.copyOf(launchOptions, launchOptions.length + arguments.length);
        System.arraycopy(arguments, 0, combined, launchOptions.length, arguments.length);
        return combined;
    }

    /** Identify execution lifetime, without accepting/rejecting native option names. */
    private static boolean isImmediate(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if (!argument.startsWith("-")) continue;
            for (int j = 1; j < argument.length(); j++) {
                char option = argument.charAt(j);
                if ("vVhtT?s".indexOf(option) >= 0) return true;
                if ("pceg".indexOf(option) >= 0) {
                    if (j == argument.length() - 1) i++;
                    break;
                }
            }
        }
        return false;
    }

    private static String signal(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if (!argument.startsWith("-")) continue;
            for (int j = 1; j < argument.length(); j++) {
                char option = argument.charAt(j);
                if (option == 's') return j + 1 < argument.length() ? argument.substring(j + 1)
                        : i + 1 < arguments.length ? arguments[i + 1] : "";
                if ("pceg".indexOf(option) >= 0) { if (j == argument.length() - 1) i++; break; }
            }
        }
        return "";
    }

    static List<String> splitCommand(String input) {
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException("Empty command");
        List<String> result = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        char quote = 0;
        boolean started = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && quote != '\'') {
                if (i + 1 == input.length()) throw new IllegalArgumentException("Trailing escape");
                char next = input.charAt(i + 1);
                if (quote == '"' && next != '"' && next != '\\') word.append(c);
                else { word.append(next); i++; }
                started = true;
            } else if (quote != 0) {
                if (c == quote) quote = 0; else word.append(c);
                started = true;
            } else if (c == '\'' || c == '"') {
                quote = c;
                started = true;
            } else if (Character.isWhitespace(c)) {
                if (started) { result.add(word.toString()); word.setLength(0); started = false; }
            } else { word.append(c); started = true; }
        }
        if (quote != 0) throw new IllegalArgumentException("Unclosed quote");
        if (started) result.add(word.toString());
        return result;
    }

    @TargetApi(26)
    private ProcessBuilder builder(String... args) {
        List<String> command = new ArrayList<>(Arrays.asList(executable.getAbsolutePath(), "-p",
                prefix.getAbsolutePath() + "/", "-c", "conf/nginx.conf"));
        command.addAll(Arrays.asList(args));
        return new ProcessBuilder(command).directory(prefix).redirectErrorStream(true);
    }

    @TargetApi(26)
    private void command(String... args) throws Exception {
        File output = new File(prefix, "logs/command-" + System.nanoTime() + ".log");
        log(text(R.string.nginx_command, "libnginx.so " + TextUtils.join(" ", args)), false);
        long start = SystemClock.elapsedRealtime();
        Process command = builder(args).redirectOutput(output).start();
        boolean finished = command.waitFor(5, TimeUnit.SECONDS);
        if (!finished) {
            command.destroyForcibly();
            command.waitFor(1, TimeUnit.SECONDS);
        }
        String result = tail(output, 0);
        if (!result.isEmpty()) log(result, !finished || command.exitValue() != 0);
        if (!output.delete()) Log.w(TAG, "Cannot remove command output: " + output);
        if (!finished) throw new IOException(text(R.string.nginx_command_timeout, 5));
        int exit = command.exitValue();
        log(text(R.string.nginx_command_result, exit, SystemClock.elapsedRealtime() - start), exit != 0);
        if (exit != 0) throw new IOException(text(R.string.nginx_command_failed, exit));
    }

    private boolean healthy() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(getLocalUrl() + "health").openConnection(Proxy.NO_PROXY);
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "close");
            if (connection.getResponseCode() != 200) return false;
            try (InputStream input = connection.getInputStream()) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[256];
                int count;
                while ((count = input.read(buffer)) != -1 && output.size() < 1024) output.write(buffer, 0, count);
                return token.equals(output.toString("UTF-8"));
            }
        } catch (IOException ignored) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void observe(Process observed) {
        try {
            int exit = observed.waitFor();
            worker.execute(() -> {
                if (server != observed) return;
                server = null;
                localHealthy = false;
                String feedback = text(exit == 0 ? R.string.nginx_server_exit : R.string.nginx_unexpected_exit, exit);
                log(feedback, exit != 0);
                showRuntimeLogs();
                publish(restingTitle(), feedback, exit != 0);
            });
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        } catch (RejectedExecutionException ignored) {
            // release() already owns the final cleanup.
        }
    }

    private void showRuntimeLogs() {
        boolean found = false;
        for (String name : new String[]{"error.log", "launcher.log", "access.log"}) {
            try {
                String content = tail(new File(prefix, "logs/" + name), 0);
                if (!content.isEmpty()) {
                    found = true;
                    log(text(R.string.nginx_log_file, name) + "\n" + content, false);
                }
            } catch (IOException error) { log(message(error), true); }
        }
        if (!found) log(text(R.string.nginx_logs_empty), false);
    }

    private String asset(String path) throws IOException {
        try (InputStream input = context.getAssets().open(path); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toString("UTF-8");
        }
    }

    private static void write(File file, String content) throws IOException {
        try (FileOutputStream output = new FileOutputStream(file)) { output.write(content.getBytes(StandardCharsets.UTF_8)); }
    }

    private static String tail(File file, long offset) throws IOException {
        if (!file.isFile()) return "";
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            long length = input.length();
            long start = Math.min(length, Math.max(offset, length - LOG_LIMIT));
            input.seek(start);
            byte[] bytes = new byte[(int) (length - start)];
            input.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8).trim();
        }
    }

    private void deleteSession(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) deleteSession(child);
        if (file.exists() && !file.delete()) Log.w(TAG, "Cannot remove session file: " + file);
    }

    private static boolean alive(Process process) {
        if (process == null) return false;
        try { process.exitValue(); return false; } catch (IllegalThreadStateException running) { return true; }
    }

    private int restingTitle() {
        if (daemonSession) return R.string.nginx_native_background_title;
        return alive(server) ? (localHealthy ? R.string.nginx_running : R.string.nginx_native_foreground_title)
                : prepared ? R.string.nginx_waiting : R.string.nginx_unavailable;
    }

    private boolean active() { return daemonSession || alive(server); }

    private int titleFor(Action action) {
        if (action == Action.PREPARE) return R.string.nginx_preparing;
        if (action == Action.START) return R.string.nginx_starting;
        if (action == Action.STOP) return R.string.nginx_stopping;
        return restingTitle();
    }

    private String text(int id, Object... args) { return context.getString(id, args); }
    private String html(int id) { return TextUtils.htmlEncode(text(id)); }
    private static String message(Exception error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private void log(String message, boolean error) {
        if (error) Log.w(TAG, message); else Log.i(TAG, message);
        main.post(() -> { Listener current = listener; if (current != null) current.onLog(message, error); });
    }

    private void publish(int title, String feedback, boolean error) {
        State snapshot = new State(title, prepared, active(), busy.get(), error, feedback, localHealthy && active());
        state = snapshot;
        main.post(() -> { Listener current = listener; if (current != null) current.onState(snapshot); });
    }
}
