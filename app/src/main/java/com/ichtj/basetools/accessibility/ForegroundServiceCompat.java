package com.ichtj.basetools.accessibility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class ForegroundServiceCompat {

    /**
     * 启动前台服务（兼容 Android 5.0 - 11）
     *
     * @param context 应用上下文
     * @param serviceIntent 启动服务的 Intent
     * @param notificationId 通知 ID
     * @param channelId 通知渠道 ID
     * @param channelName 通知渠道名称
     * @param notificationTitle 通知标题
     * @param notificationContent 通知内容
     * @param smallIcon 小图标资源 ID
     * @param foregroundServiceType 例如：ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION（Android 10+）
     */
    public static void startForegroundServiceCompat(Context context,
                                                    Intent serviceIntent,
                                                    int notificationId,
                                                    String channelId,
                                                    String channelName,
                                                    String notificationTitle,
                                                    String notificationContent,
                                                    int smallIcon,
                                                    int foregroundServiceType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // 传参数给 Service 进行前台启动
        serviceIntent.putExtra("notification_id", notificationId);
        serviceIntent.putExtra("channel_id", channelId);
        serviceIntent.putExtra("channel_name", channelName);
        serviceIntent.putExtra("title", notificationTitle);
        serviceIntent.putExtra("content", notificationContent);
        serviceIntent.putExtra("icon", smallIcon);
        serviceIntent.putExtra("type", foregroundServiceType);
    }

    /**
     * 在服务中调用：从 Intent 构建通知并启动前台服务
     */
    public static void startForegroundFromService(Service service, Intent intent) {
        int notificationId = intent.getIntExtra("notification_id", 1);
        String channelId = intent.getStringExtra("channel_id");
        String channelName = intent.getStringExtra("channel_name");
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        int icon = intent.getIntExtra("icon", android.R.drawable.ic_media_play);
        int type = intent.getIntExtra("type", 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(service, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            service.startForeground(notificationId, notification, type);
        } else {
            service.startForeground(notificationId, notification);
        }
    }
}

