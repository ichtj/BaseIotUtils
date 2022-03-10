package com.face_chtj.base_iotutils.notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationManagerCompat;

import android.view.View;
import android.widget.RemoteViews;

import com.face_chtj.base_iotutils.R;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.BaseIotUtils;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Create on 2019/12/27
 * author chtj
 * desc ：NotifyUtils 工具类
 * {@link #getInstance(int)} ()}-----------初始化相关参数
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
    private Notification.Builder builder = null;
    //自定义的系统通知视图
    private RemoteViews contentView = null;
    private int notifyId = -1; //notification标识
    private boolean mSlideOff = true;//滑动时是否可以删除
    private boolean mAutoCancel = false;//点击的时候是否消失
    private OnNotifyLinstener mOnNotifyLinstener;
    private static volatile NotifyUtils notifyUtils;
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
    public static NotifyUtils getInstance(int notifyId) {
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
                    notifyUtils.notifyId = /*Integer.valueOf(*/notifyId/*)*/;
                    notifyUtils.manager = (NotificationManager) BaseIotUtils.getContext().getSystemService(NOTIFICATION_SERVICE);
                    //自定义视图
                    notifyUtils.contentView = new RemoteViews(BaseIotUtils.getContext().getPackageName(), R.layout.activity_notification);
                    //点击关闭按钮时效果
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(BaseIotUtils.getContext(),
                            1, new Intent(ACTION_CLOSE_NOTIFY), PendingIntent.FLAG_UPDATE_CURRENT);
                    //设置点击关闭按钮"X"时的操作
                    notifyUtils.contentView.setOnClickPendingIntent(R.id.ivClose, pendingIntent);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        //这里是android8.0以上
                        NotificationChannel channel = new NotificationChannel("channel_1", "channel_name_1", NotificationManager.IMPORTANCE_HIGH);
                        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
                        notifyUtils.manager.createNotificationChannel(channel);
                        notifyUtils.builder = new Notification.Builder(BaseIotUtils.getContext(), "channel_1");
                        notifyUtils.builder.setCustomContentView(notifyUtils.contentView);
                    } else {
                        //8.0以下
                        notifyUtils.builder = new Notification.Builder(BaseIotUtils.getContext());
                        //设置自定义View
                        notifyUtils.builder.setContent(notifyUtils.contentView);
                        //设置点击通知时的操作
                        //notifyUtils.builder.setContentIntent(pendingIntent);
                        //app通知栏图标
                        //notifyUtils.builder.setSmallIcon(R.drawable.ic_launcher);  //小图标，在大图标右下角
                        notifyUtils.builder.setLargeIcon(BitmapFactory.decodeResource(BaseIotUtils.getContext().getResources(), R.drawable.app_img)); //大图标，没有设置时小图标就是大图标
                    }
                }
            }
        }
        return notifyUtils;
    }


    public NotificationManager getManager() {
        return manager;
    }


    public Notification.Builder getBuilder() {
        return builder;
    }


    /**
     * 设置监听notification是否点击关闭的接口
     *
     * @param onNotifyLinstener 注册接口
     * @return this
     */
    public NotifyUtils setOnNotifyLinstener(OnNotifyLinstener onNotifyLinstener) {
        notifyUtils.mOnNotifyLinstener = onNotifyLinstener;
        return notifyUtils;
    }

    /**
     * 设置关闭按钮是否可见
     *
     * @param isEnable 是否可见
     * @return this
     */
    public NotifyUtils setEnableCloseButton(boolean isEnable) {
        notifyUtils.contentView.setViewVisibility(R.id.ivClose, isEnable ? View.VISIBLE : View.GONE);
        return notifyUtils;
    }


    /**
     * 设置logo
     *
     * @param ivLogo setImageViewResource
     */
    private NotifyUtils setIvLogo(int ivLogo) {
        notifyUtils.contentView.setImageViewResource(R.id.iv_logo, ivLogo);
        return notifyUtils;
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewBitmap
     */
    private NotifyUtils setIvLogo(Bitmap ivLogo) {
        if (ivLogo != null) {
            notifyUtils.contentView.setImageViewBitmap(R.id.iv_logo, ivLogo);
        }
        return notifyUtils;
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewUri
     * @return this
     */
    public NotifyUtils setIvLogo(Uri ivLogo) {
        if (ivLogo != null) {
            notifyUtils.contentView.setImageViewUri(R.id.iv_logo, ivLogo);
        }
        return notifyUtils;
    }

    /**
     * 设置进度
     * 外部调用此方法时，请先调用{@link #getInstance(int)} }
     *
     * @param progress 进度
     * @return this
     */
    public NotifyUtils setProgress(String progress) {
        String appendStr = "";
        if (progress != null && !progress.equals("")) {
            appendStr = progress;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvProgress, appendStr);
        return notifyUtils;
    }

    /**
     * 设置APP名称
     * 外部调用此方法时，请先调用{@link #getInstance(int)} }
     *
     * @param appName APP名称
     * @return this
     */
    public NotifyUtils setAppName(String appName) {
        String appendStr = "";
        if (appName != null && !appName.equals("")) {
            appendStr = appName;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvAppName, appendStr);
        return notifyUtils;
    }

    /**
     * 右上角字符串
     * 外部调用此方法时，请先调用{@link #getInstance(int)} }
     *
     * @param str topright str
     * @return this
     */
    public NotifyUtils setTopRight(String str) {
        String appendStr = "";
        if (str != null && !str.equals("")) {
            appendStr = str;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvTopRight, appendStr);
        return notifyUtils;
    }

    /**
     * 设置APP about
     * 外部调用此方法时，请先调用{@link #getInstance(int)}
     *
     * @param appAbout APP about
     * @return this
     */
    public NotifyUtils setAppAbout(String appAbout) {
        String appendStr = "";
        if (appAbout != null && !appAbout.equals("")) {
            appendStr = appAbout;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvAppAbout, appendStr);
        return notifyUtils;
    }

    /**
     * 设置需要显示备注信息
     * 外部调用此方法时，请先调用{@link #getInstance(int)}
     *
     * @param remarks 需要显示备注信息
     * @return this
     */
    public NotifyUtils setRemarks(String remarks) {
        String appendStr = "";
        if (remarks != null && !remarks.equals("")) {
            appendStr = "remarks:" + remarks;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvRemarks, appendStr);
        return notifyUtils;
    }

    /**
     * 设置需要提示的消息
     * 外部调用此方法时，请先调用{@link #getInstance(int)}
     *
     * @param prompt 需要提示的消息
     * @return this
     */
    public NotifyUtils setPrompt(String prompt) {
        String appendStr = "";
        if (prompt != null && !prompt.equals("")) {
            appendStr = "prompt:" + prompt;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvPrompt, appendStr);
        return notifyUtils;
    }

    /**
     * 时间
     * 外部调用此方法时，请先调用{@link #getInstance(int)}
     *
     * @param dataTime 时间
     * @return this
     */
    public NotifyUtils setDataTime(String dataTime) {
        String appendStr = "";
        if (dataTime != null && !dataTime.equals("")) {
            appendStr = dataTime;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvDataTime, appendStr);
        return notifyUtils;
    }


    /**
     * 设置滑动是否删除
     * 外部调用此方法时，请先调用{@link #getInstance(int)}
     *
     * @param mSlideOff true|false
     * @return this
     */
    public NotifyUtils setSlideOff(boolean mSlideOff) {
        notifyUtils.mSlideOff = !mSlideOff;
        return notifyUtils;
    }

    /**
     * 设置点击是否消失
     * 外部调用此方法时，请先调用{@link #getInstance(int)}
     *
     * @param mAutoCancel true|false
     * @return this
     */
    public NotifyUtils setmAutoCancel(boolean mAutoCancel) {
        notifyUtils.mAutoCancel = mAutoCancel;
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
     * @param topRight    右上字符串
     * @param mSlideOff   滑动是否删除
     * @param mAutoCancel 点击是否消失
     *                    该步骤执行完成之后需要执行下一个方法{@link #exeuNotify()}
     * @return this
     */
    public NotifyUtils setNotifyParam(@DrawableRes int icon, int ivLogo, String appName, String appAbout, String remarks
            , String prompt, String dataTime, String topRight, String progress, boolean mSlideOff, boolean mAutoCancel) {
        setIvLogo(ivLogo);
        addParam(icon, mSlideOff, mAutoCancel, appName, appAbout, remarks, prompt, dataTime, topRight, progress);
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
     * @param topRight    右上字符串
     * @param mSlideOff   滑动是否删除
     * @param mAutoCancel 点击是否消失
     *                    该步骤执行完成之后需要执行下一个方法{@link #exeuNotify()}
     * @return this
     */
    public NotifyUtils setNotifyParam(@DrawableRes int icon, Bitmap ivLogo, String appName, String appAbout, String remarks
            , String prompt, String dataTime, boolean mSlideOff, boolean mAutoCancel, String topRight, String progress) {
        setIvLogo(ivLogo);
        addParam(icon, mSlideOff, mAutoCancel, appName, appAbout, remarks, prompt, dataTime, topRight, progress);
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
     * @param topRight    右上字符串
     * @param mSlideOff   滑动是否删除
     * @param mAutoCancel 点击是否消失
     *                    该步骤执行完成之后需要执行下一个方法{@link #exeuNotify()}
     * @return this
     */
    public NotifyUtils setNotifyParam(@DrawableRes int icon, Uri ivLogo, String appName, String appAbout, String remarks
            , String prompt, String dataTime, String topRight, String progress, boolean mSlideOff, boolean mAutoCancel) {
        setIvLogo(ivLogo);
        addParam(icon, mSlideOff, mAutoCancel, appName, appAbout, remarks, prompt, dataTime, topRight, progress);
        return notifyUtils;
    }

    /**
     * 参数的统一整理
     */
    private void addParam(@DrawableRes int icon, boolean mSlideOff, boolean mAutoCancel, String appName, String appAbout, String remarks, String prompt, String dataTime, String topRight, String progress) {
        notifyUtils.mSlideOff = mSlideOff;
        notifyUtils.mAutoCancel = mAutoCancel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //app通知栏图标
            notifyUtils.builder.setSmallIcon(icon);  //小图标，在大图标右下角
        } else {
            //app通知栏图标
            notifyUtils.builder.setSmallIcon(icon);  //小图标，在大图标右下角
        }
        String appendStr = "";
        if (appName != null && !appName.equals("")) {
            appendStr = appName;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvAppName, appendStr);

        if (appAbout != null && !appAbout.equals("")) {
            appendStr = appAbout;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvAppAbout, appendStr);


        if (remarks != null && !remarks.equals("")) {
            appendStr = "remarks:" + remarks;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvRemarks, appendStr);

        if (prompt != null && !prompt.equals("")) {
            appendStr = "prompt:" + prompt;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvPrompt, appendStr);

        if (dataTime != null && !dataTime.equals("")) {
            appendStr = dataTime;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvDataTime, appendStr);

        if (topRight != null && !topRight.equals("")) {
            appendStr = topRight;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvTopRight, appendStr);

        if (progress != null && !progress.equals("")) {
            appendStr = progress;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(R.id.tvProgress, appendStr);
    }

    /**
     * 显示notifacation时执行
     * 更改参数时执行
     */
    public void exeuNotify() {
        if (notifyUtils.manager != null) {
            if (notifyUtils.notifyId != -1) {
                if (notifyUtils.builder != null) {
                    notifyUtils.builder.setOngoing(!notifyUtils.mSlideOff);//滑动不能清除
                    notifyUtils.builder.setAutoCancel(notifyUtils.mAutoCancel);//点击的时候消失
                    notifyUtils.manager.notify(notifyUtils.notifyId, notifyUtils.builder.build());  //参数一为ID，用来区分不同APP的Notification
                }
                SPUtils.putBoolean("needClose", notifyUtils.mAutoCancel);
                if (notifyUtils.mOnNotifyLinstener != null) {
                    notifyUtils.mOnNotifyLinstener.enableStatus(true);
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
     * 关闭消息通知
     */
    public static void closeNotify() {
        if (notifyUtils != null && notifyUtils.manager != null && notifyUtils.notifyId != -1) {
            notifyUtils.manager.cancel(notifyUtils.notifyId);//参数一为ID，用来区分不同APP的Notification
            if (notifyUtils.mOnNotifyLinstener != null) {
                //通知监听对象
                notifyUtils.mOnNotifyLinstener.enableStatus(false);
                notifyUtils.mOnNotifyLinstener = null;
            }
            //销毁广播
            if (notifyUtils.mNotifyBroadcastReceiver != null) {
                BaseIotUtils.getContext().unregisterReceiver(notifyUtils.mNotifyBroadcastReceiver);
            }
            notifyUtils = null;
        }
    }
}
