package com.face_chtj.base_iotutils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationManagerCompat;

import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.face_chtj.base_iotutils.callback.INotifyStateCallback;
import com.face_chtj.base_iotutils.notify.NotifyBroadcastReceiver;

import static android.content.Context.NOTIFICATION_SERVICE;

import java.util.List;

/**
 * Create on 2019/12/27
 * author chtj
 * desc ：NotifyUtils 工具类
 * {@link #getInstance()} ()}-----------初始化相关参数
 * {@link #setNotifyId(int)} ()}-----------初始化相关参数
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
    private NotificationManager manager = null;
    private Notification.Builder builder = null;
    //自定义的系统通知视图
    private RemoteViews contentView = null;
    private int notifyId = -1; //notification标识
    private boolean mSlideOff = true;//滑动时是否可以删除
    private boolean mAutoCancel = false;//点击的时候是否消失
    private INotifyStateCallback mINotifyStateCallback;
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
    public static NotifyUtils getInstance() {
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
                    notifyUtils.manager = (NotificationManager) BaseIotUtils.getContext().getSystemService(NOTIFICATION_SERVICE);
                    //自定义视图
                    notifyUtils.contentView = new RemoteViews(BaseIotUtils.getContext().getPackageName(), R.layout.activity_notification);
                    //点击通知栏跳转应用
                    Intent toAtyintent=getAppOpenIntentByPackageName(BaseIotUtils.getContext(),BaseIotUtils.getContext().getPackageName());
                    //点击关闭按钮时效果
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(BaseIotUtils.getContext(),
                            1, new Intent(ACTION_CLOSE_NOTIFY), PendingIntent.FLAG_UPDATE_CURRENT);//点击关闭按钮时效果
                    PendingIntent pendingToIntent = PendingIntent.getActivity(BaseIotUtils.getContext(),
                            0, toAtyintent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //设置点击关闭按钮"X"时的操作
                    notifyUtils.contentView.setOnClickPendingIntent(R.id.ivClose, pendingIntent);
                    notifyUtils.contentView.setOnClickPendingIntent(R.id.rlBg, pendingToIntent);
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
                        notifyUtils.builder.setSmallIcon(R.drawable.app_img);  //小图标，在大图标右下角
                        notifyUtils.builder.setLargeIcon(BitmapFactory.decodeResource(BaseIotUtils.getContext().getResources(), R.drawable.app_img)); //大图标，没有设置时小图标就是大图标
                    }
                }
            }
        }
        return notifyUtils;
    }
    /**
     * 获取该包名中的主界面
     *
     * @param packageName
     * @return
     */
    private static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
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
     * @param INotifyStateCallback 注册接口
     * @return this
     */
    public NotifyUtils setOnNotifyLinstener(INotifyStateCallback INotifyStateCallback) {
        notifyUtils.mINotifyStateCallback = INotifyStateCallback;
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
     * 设置notification Id
     *
     * @param notifyId int整型
     * @return this
     */
    public NotifyUtils setNotifyId(int notifyId){
        notifyUtils.notifyId=notifyId;
        return notifyUtils;
    }

    /**
     * 设置logo
     *
     * @param icon setImageViewResource
     */
    public NotifyUtils setSmallIcon(@DrawableRes int icon) {
        notifyUtils.builder.setSmallIcon(icon);
        return notifyUtils;
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewResource
     */
    public NotifyUtils setIvLogo(int ivLogo) {
        notifyUtils.contentView.setImageViewResource(R.id.iv_logo, ivLogo);
        return notifyUtils;
    }

    /**
     * 设置IvStatus
     *
     */
    public NotifyUtils setIvStatus(boolean isShow) {
        return setIvStatus(isShow,R.drawable.success);
    }
    /**
     * 设置IvStatus
     *
     * @param ivDrawable setImageViewResource
     */
    public NotifyUtils setIvStatus(boolean isShow,int ivDrawable) {
        notifyUtils.contentView.setViewVisibility(R.id.ivStatus,isShow?View.VISIBLE:View.GONE);
        notifyUtils.contentView.setImageViewResource(R.id.ivStatus, ivDrawable);
        return notifyUtils;
    }

    /**
     * setIvNetStatus
     *
     */
    public NotifyUtils setIvNetStatus(boolean isShow) {
        return setIvNetStatus(isShow,R.drawable.success);
    }

    /**
     * setIvNetStatus
     *
     * @param ivDrawable setImageViewResource
     */
    public NotifyUtils setIvNetStatus(boolean isShow,int ivDrawable) {
        notifyUtils.contentView.setViewVisibility(R.id.ivNetStatus,isShow?View.VISIBLE:View.GONE);
        notifyUtils.contentView.setImageViewResource(R.id.ivNetStatus, ivDrawable);
        return notifyUtils;
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewBitmap
     */
    public NotifyUtils setIvLogo(Bitmap ivLogo) {
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
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param progress 进度
     * @return this
     */
    public NotifyUtils setProgress(String progress) {
        return setView(R.id.tvProgress,progress);
    }

    /**
     * 设置APP名称
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)} }
     *
     * @param appName APP名称
     * @return this
     */
    public NotifyUtils setAppName(String appName) {
        return setView(R.id.tvAppName,appName);
    }

    /**
     * 右上角字符串
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)} }
     */
    public NotifyUtils setTopRight(String topRight) {
        return setView(R.id.tvTopRight,topRight);
    }

    /**
     * 设置APP about
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param appAbout APP about
     * @return this
     */
    public NotifyUtils setAppAbout(String appAbout) {
        notifyUtils.contentView.setTextViewText(R.id.tvAppAbout, appAbout);
        return setView(R.id.tvAppAbout,appAbout);
    }

    private NotifyUtils setView(int viewId,String content) {
        String appendStr = "";
        if (!TypeDataUtils.isEmpty(content)) {
            appendStr = content;
        } else {
            appendStr = "";
        }
        notifyUtils.contentView.setTextViewText(viewId, appendStr);
        return notifyUtils;
    }

    /**
     * 设置需要显示备注信息
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param remarks 需要显示备注信息
     * @return this
     */
    public NotifyUtils setRemarks(String remarks) {
        return setView(R.id.tvRemarks,remarks);
    }

    /**
     * 设置需要提示的消息
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param prompt 需要提示的消息
     * @return this
     */
    public NotifyUtils setPrompt(String prompt) {
        return setView(R.id.tvPrompt,prompt);
    }

    /**
     * 时间
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param dataTime 时间
     * @return this
     */
    public NotifyUtils setDataTime(String dataTime) {
        return setView(R.id.tvDataTime,dataTime);
    }


    /**
     * 设置滑动是否删除
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
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
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param mAutoCancel true|false
     * @return this
     */
    public NotifyUtils setmAutoCancel(boolean mAutoCancel) {
        notifyUtils.mAutoCancel = mAutoCancel;
        return notifyUtils;
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
                if (notifyUtils.mINotifyStateCallback != null) {
                    notifyUtils.mINotifyStateCallback.enableStatus(true);
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
            if (notifyUtils.mINotifyStateCallback != null) {
                //通知监听对象
                notifyUtils.mINotifyStateCallback.enableStatus(false);
                notifyUtils.mINotifyStateCallback = null;
            }
            //销毁广播
            if (notifyUtils.mNotifyBroadcastReceiver != null) {
                BaseIotUtils.getContext().unregisterReceiver(notifyUtils.mNotifyBroadcastReceiver);
            }
            notifyUtils = null;
        }
    }
}
