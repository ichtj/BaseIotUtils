package com.ichtj.basetools.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class AccessibilityControlService extends AccessibilityService {
    private static final String TAG = AccessibilityControlService.class.getSimpleName();
    private Socket socket;
    private PrintWriter writer;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        new Thread(() -> {
            try {
                socket = new Socket("192.168.1.150", 8234);  // 替换为你的服务器地址
                writer = new PrintWriter(socket.getOutputStream(), true);
                Log.d(TAG, "onServiceConnected: start");
            } catch (Throwable e) {
                Log.e(TAG, "Connection failed", e);
            }
        }).start();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (writer == null) return;

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;

        Log.d(TAG, "onAccessibilityEvent: "+event);
        JSONObject json = new JSONObject();
        try {
            json.put("eventType", AccessibilityEvent.eventTypeToString(event.getEventType()));
            json.put("packageName", event.getPackageName());
            json.put("className", event.getClassName());
            json.put("text", event.getText().toString());
            json.put("contentDescription", source.getContentDescription());

            writer.println(json.toString());
        } catch (Exception e) {
            Log.e(TAG, "JSON error", e);
        }
    }

    @Override
    public void onInterrupt() {
        try {
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "onInterrupt: ", e);
        }
    }
}
