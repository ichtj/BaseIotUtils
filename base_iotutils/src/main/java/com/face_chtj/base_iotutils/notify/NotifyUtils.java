package com.face_chtj.base_iotutils.notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.R;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.keeplive.BaseIotUtils;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Create on 2019/12/27
 * author chtj
 * desc ：NotifyUtils 工具类
 * {@link #getInstance(String)} ()}-----------初始化相关参数
 * {@link #setIvLogo(int)}----------设置logo  setImageViewResource
 * {@link #setIvLogo(Uri)}----------设置logo  setImageViewUri
 * {@link #setIvLogo(Bitmap)}-------设置logo  setImageViewBitmap
 * {@link #setAppName(String)}------设置APP名称
 * {@link #setRemarks(String)}------设置需要显示备注信息
 * {@link #setPrompt(String)}-------设置需要提示的消息
 * {@link #setSlideOff(boolean)}----是否设置滑动删除
 * {@link #setmAutoCancel(boolean)}-是否点击时关闭
 * {@link #exeuNotify()}------------执行显示或是参数改变时需要重新设置的方法
 * {@link #closeNotify()}-----------关闭notification
 */
public class NotifyUtils {
    private static final String TAG = "NotifyUtils";
    //系统通知
    private NotificationManager manager = null;
    //android api 26 以下
    private Notification.Builder builder = null;
    //android api 26 以上
    private NotificationCompat.Builder nBuilder = null;
    //自定义的系统通知视图
    private RemoteViews contentView = null;
    private int notifyId = -1; //notification标识
    private boolean mSlideOff = true;//滑动时是否可以删除
    private boolean mAutoCancel = false;//点击的时候是否消失
    private OnNotifyLinstener mOnNotifyLinstener;
    private static NotifyUtils notifyUtils;
    private NotifyBroadcastReceiver mNotifyBroadcastReceiver;
    //停止该通知务的广播
    public static final String ACTION_CLOSE_NOTIFY = "com.close.service.and.notification";
    //跳转设置
    public static final String SETTINGS_ACTION = "android.settings.APPLICATION_DETAILS_SETTINGS";

    /**
     * 获取系统中是否已经通过 允许通知的权限
     *
     * @return 是否开启 true|false
     */
    public static boolean notifyIsEnable() {
        NotificationManagerCompat notification = NotificationManagerCompat.from(BaseIotUtils.getContext());
        return notification.areNotificationsEnabled();
    }

    /**
     * 去开启通知
     */
    public static void toOpenNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            Intent intent = new Intent()
                    .setAction(SETTINGS_ACTION)
                    .setData(Uri.fromParts("package",
                            BaseIotUtils.getContext().getPackageName(), null));
            BaseIotUtils.getContext().startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent()
                    .setAction(SETTINGS_ACTION)
                    .setData(Uri.fromParts("package",
                            BaseIotUtils.getContext().getPackageName(), null));
            BaseIotUtils.getContext().startActivity(intent);
            return;
        }
    }

    /**
     * 单例模式
     * Oreo不用Priority了，用importance
     * NotificationManager.IMPORTANCE_NONE 关闭通知
     * NotificationManager.IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
     * NotificationManager.IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
     * NotificationManager.IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
     * NotificationManager.IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
     */
    public static NotifyUtils getInstance(String notifyId) {
        if (notifyUtils == null) {
            synchronized (NotifyUtils.class) {
                if (notifyUtils == null) {
                    //初始化
                    notifyUtils = new NotifyUtils();
                    //注册广播
                    notifyUtils.mNotifyBroadcastReceiver = new NotifyBroadcastReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(ACTION_CLOSE_NOTIFY);
                    BaseIotUtils.getContext().registerReceiver(notifyUtils.mNotifyBroadcastReceiver, filter);
                    notifyUtils.notifyId = Integer.valueOf(notifyId);
                    notifyUtils.manager = (NotificationManager) BaseIotUtils.getContext().getSystemService(NOTIFICATION_SERVICE);
                    //自定义视图
                    notifyUtils.contentView = new RemoteViews(BaseIotUtils.getContext().getPackageName(), R.layout.activity_notification);
                    //点击关闭按钮时效果
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(BaseIotUtils.getContext(),
                            1, new Intent(ACTION_CLOSE_NOTIFY), PendingIntent.FLAG_UPDATE_CURRENT);
                    //设置点击关闭按钮"X"时的操作
                    notifyUtils.contentView.setOnClickPendingIntent(R.id.ivClose, pendingIntent);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationManagerCompat notification = NotificationManagerCompat.from(BaseIotUtils.getContext());
                        boolean isEnabled = notification.areNotificationsEnabled();
                        if (isEnabled) {
                            NotificationChannel notificationChannel =
                                    new NotificationChannel("1212", "hello", NotificationManager.IMPORTANCE_NONE);
                            String description = "";
                            //配置通知渠道的属性
                            notificationChannel.setDescription(description);
                            //设置通知出现时的闪光灯
                            notificationChannel.enableLights(true);
                            notificationChannel.setLightColor(Color.RED);
                            //设置通知出现时的震动
                            notificationChannel.enableVibration(true);
                            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100});
                            //在notificationManager中创建通知渠道
                            notifyUtils.manager.createNotificationChannel(notificationChannel);
                            notifyUtils.nBuilder = new NotificationCompat.Builder(BaseIotUtils.getContext(), "1212")
                                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                                    .setContentTitle("你有一条新的消息")
                                    .setContentText("this is normal notification style")
                                    .setTicker("notification ticker")
                                    .setContent(notifyUtils.contentView)
                                    .setPriority(1000)
                                    .setAutoCancel(true)
                                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                                    .setNumber(3)
                                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                    .setOngoing(true);
                        }
                    } else {
                        notifyUtils.builder = new Notification.Builder(BaseIotUtils.getContext());
                        //设置自定义View
                        notifyUtils.builder.setContent(notifyUtils.contentView);
                        //设置点击通知时的操作
                        notifyUtils.builder.setContentIntent(pendingIntent);
                        //app通知栏图标
                        //notifyUtils.builder.setSmallIcon(R.drawable.ic_launcher);  //小图标，在大图标右下角
                        notifyUtils.builder.setLargeIcon(BitmapFactory.decodeResource(BaseIotUtils.getContext().getResources(), R.drawable.app_img)); //大图标，没有设置时小图标就是大图标
                    }
                }
            }
        }
        return notifyUtils;
    }


    /**
     * 设置监听notification是否点击关闭的接口
     *
     * @param onNotifyLinstener 注册接口
     * @return this
     */
    public NotifyUtils setOnNotifyLinstener(OnNotifyLinstener onNotifyLinstener) {
        this.mOnNotifyLinstener = onNotifyLinstener;
        return notifyUtils;
    }

    /**
     * 设置关闭按钮是否可见
     *
     * @param isEnable 是否可见
     * @return this
     */
    public NotifyUtils setEnableCloseButton(boolean isEnable) {
        contentView.setViewVisibility(R.id.ivClose, isEnable ? View.VISIBLE : View.GONE);
        return notifyUtils;
    }


    /**
     * 设置logo
     *
     * @param ivLogo setImageViewResource
     */
    private void setIvLogo(int ivLogo) {
        contentView.setImageViewResource(R.id.iv_logo, ivLogo);
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewBitmap
     */
    private void setIvLogo(Bitmap ivLogo) {
        if (ivLogo != null) {
            contentView.setImageViewBitmap(R.id.iv_logo, ivLogo);
        }
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewUri
     * @return this
     */
    public void setIvLogo(Uri ivLogo) {
        if (ivLogo != null) {
            contentView.setImageViewUri(R.id.iv_logo, ivLogo);
        }
    }

    /**
     * 设置APP名称
     * 外部调用此方法时，请先调用{@link #getInstance(String)} }
     *
     * @param appName APP名称
     * @return this
     */
    public void setAppName(String appName) {
        String appendStr = "";
        if (appName != null && !appName.equals("")) {
            appendStr = appName;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvAppName, appendStr);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
        } else {
            manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
        }
    }

    /**
     * 设置APP about
     * 外部调用此方法时，请先调用{@link #getInstance(String)}
     *
     * @param appAbout APP about
     * @return this
     */
    public void setAppAbout(String appAbout) {
        String appendStr = "";
        if (appAbout != null && !appAbout.equals("")) {
            appendStr = appAbout;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvAppAbout, appendStr);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
        } else {
            manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
        }
    }

    /**
     * 设置需要显示备注信息
     * 外部调用此方法时，请先调用{@link #getInstance(String)}
     *
     * @param remarks 需要显示备注信息
     * @return this
     */
    public void setRemarks(String remarks) {
        String appendStr = "";
        if (remarks != null && !remarks.equals("")) {
            appendStr = "备注:" + remarks;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvRemarks, appendStr);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
        } else {
            manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
        }
    }

    /**
     * 设置需要提示的消息
     * 外部调用此方法时，请先调用{@link #getInstance(String)}
     *
     * @param prompt 需要提示的消息
     * @return this
     */
    public void setPrompt(String prompt) {
        String appendStr = "";
        if (prompt != null && !prompt.equals("")) {
            appendStr = "提示:" + prompt;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvPrompt, appendStr);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
        } else {
            manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
        }
    }

    /**
     * 时间
     * 外部调用此方法时，请先调用{@link #getInstance(String)}
     *
     * @param dataTime 时间
     * @return this
     */
    public void setDataTime(String dataTime) {
        String appendStr = "";
        if (dataTime != null && !dataTime.equals("")) {
            appendStr = dataTime;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvDataTime, appendStr);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
        } else {
            manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
        }
    }


    /**
     * 设置滑动是否删除
     * 外部调用此方法时，请先调用{@link #getInstance(String)}
     *
     * @param mSlideOff true|false
     * @return this
     */
    public void setSlideOff(boolean mSlideOff) {
        this.mSlideOff = mSlideOff;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.nBuilder.setOngoing(!mSlideOff);
            this.manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
        } else {
            this.builder.setOngoing(!mSlideOff);
            this.manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
        }
    }

    /**
     * 设置点击是否消失
     * 外部调用此方法时，请先调用{@link #getInstance(String)}
     *
     * @param mAutoCancel true|false
     * @return this
     */
    public void setmAutoCancel(boolean mAutoCancel) {
        this.mAutoCancel = mAutoCancel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            nBuilder.setAutoCancel(mAutoCancel);
            manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
        } else {
            builder.setAutoCancel(mAutoCancel);
            manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
        }
    }

    /**
     * 为各个参数赋值
     *
     * @param icon        app通知栏图标
     * @param ivLogo      下拉通知栏图标
     * @param appName     app名称
     * @param remarks     备注
     * @param prompt      提示
     * @param dataTime    时间
     * @param mSlideOff   滑动是否删除
     * @param mAutoCancel 点击是否消失
     *                    该步骤执行完成之后需要执行下一个方法{@link #exeuNotify()}
     * @return this
     */
    public NotifyUtils setNotifyParam(@DrawableRes int icon, int ivLogo, String appName, String appAbout, String remarks
            , String prompt, String dataTime, boolean mSlideOff, boolean mAutoCancel) {
        setIvLogo(ivLogo);
        addParam(icon, mSlideOff, mAutoCancel, appName, appAbout, remarks, prompt, dataTime);
        return notifyUtils;
    }

    /**
     * 为各个参数赋值
     *
     * @param icon        app通知栏图标
     * @param ivLogo      下拉通知栏图标
     * @param appName     app名称
     * @param remarks     备注
     * @param prompt      提示
     * @param dataTime    时间
     * @param mSlideOff   滑动是否删除
     * @param mAutoCancel 点击是否消失
     *                    该步骤执行完成之后需要执行下一个方法{@link #exeuNotify()}
     * @return this
     */
    public NotifyUtils setNotifyParam(@DrawableRes int icon, Bitmap ivLogo, String appName, String appAbout, String remarks
            , String prompt, String dataTime, boolean mSlideOff, boolean mAutoCancel) {
        setIvLogo(ivLogo);
        addParam(icon, mSlideOff, mAutoCancel, appName, appAbout, remarks, prompt, dataTime);
        return notifyUtils;
    }

    /**
     * 为各个参数赋值
     *
     * @param icon        app通知栏图标
     * @param ivLogo      下拉通知栏图标
     * @param appName     app名称
     * @param remarks     备注
     * @param prompt      提示
     * @param dataTime    时间
     * @param mSlideOff   滑动是否删除
     * @param mAutoCancel 点击是否消失
     *                    该步骤执行完成之后需要执行下一个方法{@link #exeuNotify()}
     * @return this
     */
    public NotifyUtils setNotifyParam(@DrawableRes int icon, Uri ivLogo, String appName, String appAbout, String remarks
            , String prompt, String dataTime, boolean mSlideOff, boolean mAutoCancel) {
        setIvLogo(ivLogo);
        addParam(icon, mSlideOff, mAutoCancel, appName, appAbout, remarks, prompt, dataTime);
        return notifyUtils;
    }

    /**
     * 参数的统一整理
     */
    private void addParam(@DrawableRes int icon, boolean mSlideOff, boolean mAutoCancel, String appName, String appAbout, String remarks, String prompt, String dataTime) {
        this.mSlideOff = mSlideOff;
        this.mAutoCancel = mAutoCancel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //app通知栏图标
            nBuilder.setSmallIcon(icon);  //小图标，在大图标右下角
        } else {
            //app通知栏图标
            builder.setSmallIcon(icon);  //小图标，在大图标右下角
        }
        String appendStr = "";
        if (appName != null && !appName.equals("")) {
            appendStr = appName;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvAppName, appendStr);

        if (appAbout != null && !appAbout.equals("")) {
            appendStr = appAbout;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvAppAbout, appendStr);


        if (remarks != null && !remarks.equals("")) {
            appendStr = "备注:" + remarks;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvRemarks, appendStr);

        if (prompt != null && !prompt.equals("")) {
            appendStr = "提示:" + prompt;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvPrompt, appendStr);

        if (dataTime != null && !dataTime.equals("")) {
            appendStr = dataTime;
        } else {
            appendStr = "";
        }
        contentView.setTextViewText(R.id.tvDataTime, appendStr);
    }

    /**
     * 显示notifacation时执行
     * 更改参数时执行
     */
    public void exeuNotify() {
        if (manager != null) {
            if (notifyId != -1) {
                KLog.e(TAG, "notifyId=" + notifyId + ",mSlideOff=" + mSlideOff + ",mAutoCancel=" + mAutoCancel);
                int sdkInt = android.os.Build.VERSION.SDK_INT;
                KLog.d(TAG, "sdkInt: "+sdkInt);
                if (sdkInt >= android.os.Build.VERSION_CODES.O && nBuilder != null) {
                    KLog.e(TAG, "more than android api 26 ,nBuilder=" + nBuilder);
                    nBuilder.setOngoing(!mSlideOff);//滑动不能清除
                    nBuilder.setAutoCancel(mAutoCancel);//点击的时候消失
                    manager.notify(notifyId, nBuilder.build());  //参数一为ID，用来区分不同APP的Notification
                } else if (sdkInt < android.os.Build.VERSION_CODES.O && builder != null) {
                    KLog.e(TAG, "less than android api 26,builder=" + builder);
                    builder.setOngoing(!mSlideOff);//滑动不能清除
                    builder.setAutoCancel(mAutoCancel);//点击的时候消失
                    manager.notify(notifyId, builder.build());  //参数一为ID，用来区分不同APP的Notification
                }
                SPUtils.putBoolean("needClose", mAutoCancel);
                if (mOnNotifyLinstener != null) {
                    mOnNotifyLinstener.enableStatus(true);
                }
            } else {
                throw new NullPointerException("notifyId ==null:method > setNotifyId(int notifyId)");
            }
        } else {
            throw new NullPointerException("manager or builder ==null");
        }
    }

    /**
     * 调用此方法前，必须首先执行过{@link #exeuNotify()}
     * 外部调用此方法时，请先调用{@link #getInstance(String)}
     * 关闭消息通知
     */
    public static void closeNotify() {
        if (notifyUtils != null) {
            KLog.d(TAG, "notifyId=" + notifyUtils.notifyId);
            if (notifyUtils.manager != null) {
                if (notifyUtils.notifyId != -1) {
                    notifyUtils.manager.cancel(notifyUtils.notifyId);//参数一为ID，用来区分不同APP的Notification
                    if (notifyUtils.mOnNotifyLinstener != null) {
                        notifyUtils.mOnNotifyLinstener.enableStatus(false);
                        //销毁广播
                        if (notifyUtils.mNotifyBroadcastReceiver != null) {
                            BaseIotUtils.getContext().unregisterReceiver(notifyUtils.mNotifyBroadcastReceiver);
                        }
                        notifyUtils.mOnNotifyLinstener = null;
                        notifyUtils = null;
                    } else {
                        throw new NullPointerException("mOnNotifyLinstener ==null");
                    }
                }
            } else {
                throw new NullPointerException("manager or builder ==null");
            }
        }
    }
}
