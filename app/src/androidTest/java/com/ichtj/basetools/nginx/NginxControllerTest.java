package com.ichtj.basetools.nginx;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class NginxControllerTest {
    private NginxController controller;
    private final List<String> output = new CopyOnWriteArrayList<>();
    private int port;
    private File nativeRoot;

    private void prepare(int selectedPort) throws Exception {
        assumeTrue(Build.VERSION.SDK_INT >= 30);
        port = selectedPort;
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        controller = new NginxController(context, new NginxController.Listener() {
            @Override public void onState(NginxController.State state) { }
            @Override public void onLog(String message, boolean error) { output.add(message); }
        }, port);
        run(NginxController.Action.PREPARE);
        assertTrue(controller.getState().feedback, controller.getState().ready);
        assertFalse("Opening a page must not start a server", controller.getState().running);
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        }
    }

    private void run(NginxController.Action action) throws Exception {
        assertTrue(controller.execute(action));
        awaitIdle();
    }

    private void awaitIdle() throws Exception {
        long deadline = SystemClock.elapsedRealtime() + 15000;
        while (controller.getState().busy && SystemClock.elapsedRealtime() < deadline) Thread.sleep(25);
        assertFalse("Operation did not finish", controller.getState().busy);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private boolean open() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 150);
            return true;
        } catch (IOException refused) { return false; }
    }

    private void awaitClosed() throws Exception {
        long deadline = SystemClock.elapsedRealtime() + 10000;
        while (open() && SystemClock.elapsedRealtime() < deadline) Thread.sleep(50);
        assertFalse("The owned server port remains open", open());
    }

    @After public void cleanup() throws Exception {
        if (controller != null) {
            controller.release();
            awaitClosed();
        }
        if (nativeRoot != null) remove(nativeRoot);
    }

    private void remove(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) remove(child);
        assertTrue("Cannot remove test file " + file, !file.exists() || file.delete());
    }

    private void raw(String command) throws Exception {
        assertTrue(controller.executeCommand(command));
        awaitIdle();
    }

    @Test public void commandsReportOutputAndReloadAndStopTheOwnedServer() throws Exception {
        prepare(freePort());
        assertFalse(open());
        run(NginxController.Action.CHECK);
        assertFalse(controller.getState().feedback, controller.getState().error);
        run(NginxController.Action.VERSION);
        assertTrue(output.stream().anyMatch(line -> line.contains("nginx version: nginx/")));
        run(NginxController.Action.START);
        assertTrue(controller.getState().feedback, controller.getState().running);
        assertFalse(controller.getState().feedback, controller.getState().error);
        run(NginxController.Action.RELOAD);
        assertTrue(controller.getState().running);
        assertFalse(controller.getState().feedback, controller.getState().error);
        run(NginxController.Action.LOGS);
        assertTrue(output.stream().anyMatch(line -> line.contains("start worker process")));
        run(NginxController.Action.STOP);
        assertFalse(controller.getState().running);
        awaitClosed();
    }

    @Test public void aBusyPortIsReportedAsFailureWithoutStoppingTheOtherListener() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))) {
            prepare(occupied.getLocalPort());
            run(NginxController.Action.START);
            assertTrue(controller.getState().error);
            assertFalse(controller.getState().running);
            assertFalse("The other listener must remain open", occupied.isClosed());
            assertTrue(output.stream().anyMatch(line -> line.contains("bind()")));
        }
    }

    @Test public void hidingThePageStopsAnAlreadyRunningServer() throws Exception {
        prepare(freePort());
        run(NginxController.Action.START);
        assertTrue(controller.getState().running);
        controller.onHidden();
        awaitClosed();
        controller.onVisible();
        Thread.sleep(150);
        assertFalse("Returning to the page must not restart it", open());
    }

    @Test public void hidingDuringStartupCancelsTheQueuedStart() throws Exception {
        prepare(freePort());
        assertTrue(controller.execute(NginxController.Action.START));
        controller.onHidden();
        awaitIdle();
        awaitClosed();
        assertFalse(controller.getState().running);
    }

    @Test public void nativeFlagsAndQuotedDirectivesArePassedThroughAndNativeErrorsAreReported() throws Exception {
        prepare(freePort());
        raw("nginx -T -g 'env ICHTJ_NGINX_TEST=value;'");
        assertFalse(controller.getState().feedback, controller.getState().error);
        assertTrue(output.stream().anyMatch(line -> line.contains("# configuration file")));
        raw("nginx -Z");
        assertTrue(controller.getState().error);
        assertTrue(output.stream().anyMatch(line -> line.contains("invalid option")));
        raw("-v");
        assertFalse("Failed native flags must not poison later commands", controller.getState().error);
    }

    @Test public void customPrefixConfigAndDaemonModesKeepTheirShutdownContext() throws Exception {
        prepare(freePort());
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        nativeRoot = new File(context.getFilesDir(), "ichtj native test " + System.nanoTime());
        assertTrue(new File(nativeRoot, "logs").mkdirs());
        assertTrue(new File(nativeRoot, "temp").mkdirs());
        File config = new File(nativeRoot, "custom configuration.conf");
        Files.write(config.toPath(), ("worker_processes 1; pid logs/native.pid; error_log logs/error.log info;\n"
                + "events { worker_connections 32; } http { access_log off; server { listen 127.0.0.1:"
                + port + "; location / { return 200 'raw-options'; } } }\n").getBytes(StandardCharsets.UTF_8));
        String command = "nginx -p '" + nativeRoot.getAbsolutePath() + "/' -c 'custom configuration.conf'";
        raw(command + " -g 'daemon off;'");
        assertTrue(controller.getState().feedback, controller.getState().running);
        assertFalse("A custom address must not be presented as the built-in preview", controller.getState().previewReady);
        HttpURLConnection connection = (HttpURLConnection) new URL(controller.getLocalUrl()).openConnection();
        try { assertEquals(200, connection.getResponseCode()); } finally { connection.disconnect(); }
        raw("nginx -s reload");
        assertFalse(controller.getState().feedback, controller.getState().error);
        raw("nginx -s quit");
        awaitClosed();
        raw(command + " -g 'daemon on;'");
        assertTrue(controller.getState().feedback, controller.getState().running);
        assertTrue(open());
        controller.onHidden();
        awaitClosed();
    }
}
