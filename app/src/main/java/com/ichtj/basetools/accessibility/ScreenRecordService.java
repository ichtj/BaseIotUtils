package com.ichtj.basetools.accessibility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ScreenRecordService extends Service {
    public static final String ACTION_START = "action_start_recording";
    public static final String ACTION_STOP = "action_stop_recording";

    private static ScreenRecorderUtil recorderUtil;

    public static void setRecorderUtil(ScreenRecorderUtil util) {
        recorderUtil = util;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && recorderUtil != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                ForegroundServiceCompat.startForegroundFromService(this, intent);
                recorderUtil.startRecordingInternal(); // 启动录屏核心逻辑
            } else if (ACTION_STOP.equals(action)) {
                recorderUtil.stopRecording();
                stopForeground(true);
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("screen_record", "录屏服务", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, "screen_record")
                .setContentTitle("正在录屏")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
