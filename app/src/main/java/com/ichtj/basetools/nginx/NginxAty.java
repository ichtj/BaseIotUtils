package com.ichtj.basetools.nginx;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.R;

import java.net.Inet4Address;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class NginxAty extends BaseActivity implements NginxController.Listener {
    private static final int MAX_LOG_CHARS = 96 * 1024;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final SpannableStringBuilder log = new SpannableStringBuilder();
    private NginxController controller;
    private TextView status, feedback, result, lanAddress;
    private ScrollView logScroll;
    private View start, stop, reload, check, version, preview, logs, progress, send;
    private EditText commandInput;
    private AlertDialog previewDialog;
    private WebView previewWeb;
    private boolean destroyed;
    private final Runnable addressRefresh = new Runnable() {
        @Override public void run() {
            refreshAddresses();
            ui.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nginx);
        status = findViewById(R.id.nginxStatus);
        feedback = findViewById(R.id.nginxFeedback);
        result = findViewById(R.id.tvResult);
        lanAddress = findViewById(R.id.tvNowIp);
        logScroll = findViewById(R.id.nginxLogScroll);
        progress = findViewById(R.id.nginxBusy);
        start = findViewById(R.id.btnStart);
        stop = findViewById(R.id.btnStop);
        reload = findViewById(R.id.nginxReload);
        check = findViewById(R.id.nginxCheck);
        version = findViewById(R.id.btnVersion);
        preview = findViewById(R.id.nginxPreview);
        logs = findViewById(R.id.nginxLogs);
        send = findViewById(R.id.btnExeu);
        commandInput = findViewById(R.id.etNginxCmd);
        controller = new NginxController(this, this);
        ((TextView) findViewById(R.id.nginxLocalAddress)).setText(
                getString(R.string.nginx_local_address, controller.getLocalUrl()));
        bind(start, NginxController.Action.START);
        bind(stop, NginxController.Action.STOP);
        bind(reload, NginxController.Action.RELOAD);
        bind(check, NginxController.Action.CHECK);
        bind(version, NginxController.Action.VERSION);
        bind(logs, NginxController.Action.LOGS);
        preview.setOnClickListener(v -> openPreview());
        send.setOnClickListener(v -> sendCommand());
        commandInput.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEND || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                sendCommand();
                return true;
            }
            return false;
        });
        result.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom - top != oldBottom - oldTop) logScroll.scrollTo(0, result.getBottom());
        });
        findViewById(R.id.nginxBack).setOnClickListener(v -> finish());
        findViewById(R.id.nginxClear).setOnClickListener(v -> {
            log.clear();
            result.setText("");
            onLog(getString(R.string.nginx_cleared), false);
            showFeedback(getString(R.string.nginx_cleared), false);
        });
        adaptPanels();
        controller.execute(NginxController.Action.PREPARE);
    }

    private void bind(View view, NginxController.Action action) {
        view.setOnClickListener(v -> controller.execute(action));
    }

    private void sendCommand() {
        String input = commandInput.getText().toString();
        onLog(getString(R.string.nginx_command, input), false);
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(commandInput.getWindowToken(), 0);
        commandInput.clearFocus();
        controller.executeCommand(input);
    }

    @Override protected void onStart() {
        super.onStart();
        controller.onVisible();
        ui.post(addressRefresh);
    }

    @Override protected void onStop() {
        ui.removeCallbacks(addressRefresh);
        if (previewDialog != null) previewDialog.dismiss();
        controller.onHidden();
        super.onStop();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        ui.removeCallbacksAndMessages(null);
        if (previewDialog != null) previewDialog.dismiss();
        controller.release();
        super.onDestroy();
    }

    @Override public void onState(NginxController.State state) {
        if (destroyed) return;
        status.setText(state.title);
        status.setTextColor(color(state.running ? R.color.nginx_success : R.color.nginx_ink));
        progress.setVisibility(state.busy ? View.VISIBLE : View.GONE);
        boolean available = state.ready && !state.busy;
        start.setEnabled(available && !state.running);
        stop.setEnabled(available && state.running);
        reload.setEnabled(available && state.running);
        preview.setEnabled(available && state.previewReady);
        check.setEnabled(available);
        version.setEnabled(available);
        logs.setEnabled(available);
        send.setEnabled(available);
        commandInput.setEnabled(available);
        findViewById(R.id.nginxLocalAddress).setVisibility(state.running && !state.previewReady ? View.GONE : View.VISIBLE);
        refreshAddresses();
        showFeedback(state.feedback, state.error);
    }

    @Override public void onLog(String message, boolean error) {
        if (destroyed) return;
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        SpannableString stamp = new SpannableString(time + "  ");
        stamp.setSpan(new ForegroundColorSpan(color(R.color.nginx_console_time)), 0, stamp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        log.append(stamp);
        SpannableString body = new SpannableString(message + "\n");
        if (error) body.setSpan(new ForegroundColorSpan(Color.rgb(255, 183, 171)), 0, body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        log.append(body);
        if (log.length() > MAX_LOG_CHARS) log.delete(0, log.length() - MAX_LOG_CHARS);
        result.setText(log);
        logScroll.post(() -> logScroll.scrollTo(0, result.getBottom()));
    }

    private void showFeedback(String message, boolean error) {
        feedback.setText(message);
        feedback.setTextColor(color(error ? R.color.nginx_error : R.color.nginx_muted));
    }

    private void refreshAddresses() {
        if (controller.getState().running && !controller.getState().previewReady) {
            lanAddress.setText(R.string.nginx_native_address_hint);
            return;
        }
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (!network.isUp() || network.isLoopback()) continue;
                Enumeration<InetAddress> ips = network.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();
                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress() && !ip.isLinkLocalAddress()) {
                        addresses.add("http://" + ip.getHostAddress() + ":" + NginxController.PORT + "/");
                    }
                }
            }
        } catch (Exception exception) {
            android.util.Log.w("ichtj_nginx_ui", "Cannot read network interfaces", exception);
        }
        lanAddress.setText(addresses.isEmpty() ? getString(R.string.nginx_network_none) : TextUtils.join("\n", addresses));
    }

    private void adaptPanels() {
        if (getResources().getConfiguration().screenWidthDp >= 600) return;
        LinearLayout panels = findViewById(R.id.nginxTopPanels);
        panels.setOrientation(LinearLayout.VERTICAL);
        for (int id : new int[]{R.id.nginxServicePanel, R.id.nginxToolsPanel}) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            if (id == R.id.nginxToolsPanel) params.topMargin = dp(12);
            findViewById(id).setLayoutParams(params);
        }
        findViewById(R.id.nginxLogHint).setVisibility(View.GONE);
        // All controls remain reachable on a small display; the log keeps its own scroll.
        LinearLayout content = findViewById(R.id.nginxContent);
        View top = content.getChildAt(0);
        content.removeView(top);
        ScrollView controls = new ScrollView(this);
        controls.setFillViewport(false);
        controls.addView(top, new ScrollView.LayoutParams(-1, -2));
        content.addView(controls, 0, new LinearLayout.LayoutParams(-1,
                Math.min(dp(390), getResources().getDisplayMetrics().heightPixels / 2)));
    }

    @SuppressWarnings("deprecation")
    private void openPreview() {
        if (!controller.getState().running || previewDialog != null) return;
        if (android.os.Process.myUid() % 100000 == android.os.Process.SYSTEM_UID) {
            openHttpPreview();
            return;
        }
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        TextView loading = new TextView(this);
        loading.setPadding(dp(20), dp(8), dp(20), dp(12));
        loading.setTextColor(color(R.color.nginx_muted));
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setIndeterminate(true);
        content.addView(loading, new LinearLayout.LayoutParams(-1, -2));
        content.addView(bar, new LinearLayout.LayoutParams(-1, dp(3)));
        try {
            previewWeb = new WebView(this);
        } catch (RuntimeException error) {
            android.util.Log.w("ichtj_nginx_ui", "WebView unavailable; using HTTP preview", error);
            openHttpPreview();
            return;
        }
        WebView web = previewWeb;
        web.getSettings().setJavaScriptEnabled(false);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        content.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        final boolean[] failed = {false};
        Runnable timeout = () -> {
            if (previewWeb == web && bar.getVisibility() == View.VISIBLE) {
                failed[0] = true;
                web.stopLoading();
                String message = getString(R.string.nginx_command_timeout, 10);
                loading.setText(message);
                loading.setTextColor(color(R.color.nginx_error));
                bar.setVisibility(View.GONE);
                showFeedback(message, true);
                onLog(message, true);
            }
        };
        web.setWebViewClient(new WebViewClient() {
            private boolean permitted(String value) {
                Uri uri = Uri.parse(value);
                return "http".equals(uri.getScheme()) && "127.0.0.1".equals(uri.getHost())
                        && uri.getPort() == NginxController.PORT;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (permitted(url)) return false;
                report(getString(R.string.nginx_preview_blocked));
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                failed[0] = false;
                String message = getString(R.string.nginx_preview_loading, url);
                loading.setText(message);
                loading.setTextColor(color(R.color.nginx_muted));
                bar.setVisibility(View.VISIBLE);
                showFeedback(message, false);
                onLog(message, false);
                ui.removeCallbacks(timeout);
                ui.postDelayed(timeout, 10000);
            }
            @Override public void onPageFinished(WebView view, String url) {
                ui.removeCallbacks(timeout);
                bar.setVisibility(View.GONE);
                if (failed[0] || previewWeb != web) return;
                String message = getString(R.string.nginx_preview_ready);
                loading.setText(message);
                loading.setTextColor(color(R.color.nginx_success));
                showFeedback(message, false);
                onLog(message, false);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) report(getString(R.string.nginx_preview_error, error.getDescription()));
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame()) report(getString(R.string.nginx_preview_http_error, response.getStatusCode(), request.getUrl()));
            }
            private void report(String message) {
                failed[0] = true;
                ui.removeCallbacks(timeout);
                loading.setText(message);
                loading.setTextColor(color(R.color.nginx_error));
                bar.setVisibility(View.GONE);
                showFeedback(message, true);
                onLog(message, true);
            }
        });
        previewDialog = new AlertDialog.Builder(this).setTitle(R.string.nginx_preview)
                .setView(content).setPositiveButton(R.string.nginx_close, null).create();
        previewDialog.setOnDismissListener(dialog -> {
            ui.removeCallbacks(timeout);
            previewWeb = null;
            previewDialog = null;
            web.stopLoading();
            web.setWebViewClient(null);
            content.removeView(web);
            web.destroy();
            if (!destroyed) {
                String message = getString(R.string.nginx_preview_closed);
                showFeedback(message, false);
                onLog(message, false);
            }
        });
        previewDialog.show();
        Window window = previewDialog.getWindow();
        if (window != null) window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.85));
        web.loadUrl(controller.getLocalUrl());
    }

    @SuppressWarnings("deprecation")
    private void openHttpPreview() {
        String url = controller.getLocalUrl();
        String message = getString(R.string.nginx_preview_loading, url);
        onLog(message, false);
        showFeedback(message, false);
        AtomicBoolean dismissed = new AtomicBoolean();
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(12), dp(24), dp(20));
        TextView response = new TextView(this);
        response.setText(message);
        response.setTextColor(color(R.color.nginx_muted));
        response.setPadding(0, 0, 0, dp(16));
        ProgressBar busy = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        busy.setIndeterminate(true);
        TextView body = new TextView(this);
        body.setTextColor(color(R.color.nginx_ink));
        body.setTextSize(18);
        body.setLineSpacing(dp(6), 1);
        body.setPadding(dp(12), dp(16), dp(12), dp(16));
        body.setTextIsSelectable(true);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.nginx_address_background);
        scroll.addView(body, new ScrollView.LayoutParams(-1, -2));
        content.addView(response, new LinearLayout.LayoutParams(-1, -2));
        content.addView(busy, new LinearLayout.LayoutParams(-1, dp(3)));
        content.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        previewDialog = new AlertDialog.Builder(this).setTitle(R.string.nginx_preview)
                .setView(content).setPositiveButton(R.string.nginx_close, null).create();
        previewDialog.setOnDismissListener(dialog -> {
            dismissed.set(true);
            previewDialog = null;
            if (!destroyed) {
                String closed = getString(R.string.nginx_preview_closed);
                showFeedback(closed, false);
                onLog(closed, false);
            }
        });
        previewDialog.show();
        Window window = previewDialog.getWindow();
        if (window != null) window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.78));
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                long started = android.os.SystemClock.elapsedRealtime();
                connection = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(5000);
                connection.setInstanceFollowRedirects(false);
                connection.setUseCaches(false);
                connection.setRequestProperty("Connection", "close");
                int code = connection.getResponseCode();
                if (code != 200) throw new java.io.IOException(getString(R.string.nginx_preview_http_error, code, url));
                String html;
                try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = input.read(buffer)) != -1 && !dismissed.get()) {
                        if (output.size() + count > 64 * 1024) throw new java.io.IOException(getString(R.string.nginx_preview_too_large));
                        output.write(buffer, 0, count);
                    }
                    html = output.toString("UTF-8");
                }
                String summary = getString(R.string.nginx_preview_response, code,
                        android.os.SystemClock.elapsedRealtime() - started);
                String document = html.replaceAll("(?is)<head\\b[^>]*>.*?</head>", "");
                ui.post(() -> {
                    if (destroyed || dismissed.get()) return;
                    busy.setVisibility(View.GONE);
                    response.setText(summary);
                    response.setTextColor(color(R.color.nginx_success));
                    body.setText(Html.fromHtml(document));
                    showFeedback(getString(R.string.nginx_preview_ready), false);
                    onLog(getString(R.string.nginx_preview_ready) + " " + summary, false);
                });
            } catch (Exception error) {
                ui.post(() -> {
                    if (destroyed || dismissed.get()) return;
                    busy.setVisibility(View.GONE);
                    String failure = getString(R.string.nginx_preview_error, error.getMessage());
                    response.setText(failure);
                    response.setTextColor(color(R.color.nginx_error));
                    showFeedback(failure, true);
                    onLog(failure, true);
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, "ichtj_nginx_preview").start();
    }

    private int color(int id) { return ContextCompat.getColor(this, id); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
