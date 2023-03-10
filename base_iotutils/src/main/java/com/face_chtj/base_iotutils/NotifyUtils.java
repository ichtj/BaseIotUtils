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
import com.face_chtj.base_iotutils.notify.NotifyReceiver;

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
    private NotifyReceiver mNotifyReceiver;
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
    private static NotifyUtils getInstance() {
        if (notifyUtils == null) {
            synchronized (NotifyUtils.class) {
                if (notifyUtils == null) {
                    //初始化
                    notifyUtils = new NotifyUtils();
                    //注册广播
                    notifyUtils.mNotifyReceiver = new NotifyReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(ACTION_CLOSE_NOTIFY);
                    BaseIotUtils.getContext().registerReceiver(notifyUtils.mNotifyReceiver, filter);
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

    public static NotificationManager getManager() {
        return getInstance().manager;
    }


    public static Notification.Builder getBuilder() {
        return getInstance().builder;
    }


    /**
     * 设置监听notification是否点击关闭的接口
     *
     * @param INotifyStateCallback 注册接口
     * @return this
     */
    public NotifyUtils setOnNotifyLinstener(INotifyStateCallback INotifyStateCallback) {
        getInstance().mINotifyStateCallback = INotifyStateCallback;
        return getInstance();
    }

    /**
     * 设置关闭按钮是否可见
     *
     * @param isEnable 是否可见
     * @return this
     */
    public static NotifyUtils setEnableCloseButton(boolean isEnable) {
        getInstance().contentView.setViewVisibility(R.id.ivClose, isEnable ? View.VISIBLE : View.GONE);
        return getInstance();
    }

    /**
     * 设置notification Id
     *
     * @param notifyId int整型
     * @return this
     */
    public static NotifyUtils setNotifyId(int notifyId){
        getInstance().notifyId=notifyId;
        return getInstance();
    }

    /**
     * 设置logo
     *
     * @param icon setImageViewResource
     */
    public NotifyUtils setSmallIcon(@DrawableRes int icon) {
        getInstance().builder.setSmallIcon(icon);
        return getInstance();
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewResource
     */
    public static NotifyUtils setIvLogo(int ivLogo) {
        getInstance().contentView.setImageViewResource(R.id.iv_logo, ivLogo);
        return getInstance();
    }

    /**
     * 设置IvStatus
     *
     */
    public static NotifyUtils setIvStatus(boolean isShow) {
        return setIvStatus(isShow,R.drawable.success);
    }
    /**
     * 设置IvStatus
     *
     * @param ivDrawable setImageViewResource
     */
    public static NotifyUtils setIvStatus(boolean isShow,int ivDrawable) {
        getInstance().contentView.setViewVisibility(R.id.ivStatus,isShow?View.VISIBLE:View.GONE);
        getInstance().contentView.setImageViewResource(R.id.ivStatus, ivDrawable);
        return getInstance();
    }

    /**
     * setIvNetStatus
     *
     */
    public static NotifyUtils setIvNetStatus(boolean isShow) {
        return setIvNetStatus(isShow,R.drawable.success);
    }

    /**
     * setIvNetStatus
     *
     * @param ivDrawable setImageViewResource
     */
    public static NotifyUtils setIvNetStatus(boolean isShow,int ivDrawable) {
        getInstance().contentView.setViewVisibility(R.id.ivNetStatus,isShow?View.VISIBLE:View.GONE);
        getInstance().contentView.setImageViewResource(R.id.ivNetStatus, ivDrawable);
        return getInstance();
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewBitmap
     */
    public static NotifyUtils setIvLogo(Bitmap ivLogo) {
        if (ivLogo != null) {
            getInstance().contentView.setImageViewBitmap(R.id.iv_logo, ivLogo);
        }
        return getInstance();
    }

    /**
     * 设置logo
     *
     * @param ivLogo setImageViewUri
     * @return this
     */
    public static NotifyUtils setIvLogo(Uri ivLogo) {
        if (ivLogo != null) {
            getInstance().contentView.setImageViewUri(R.id.iv_logo, ivLogo);
        }
        return getInstance();
    }

    /**
     * 设置进度
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param progress 进度
     * @return this
     */
    public static NotifyUtils setProgress(String progress) {
        return setView(R.id.tvProgress,progress);
    }

    /**
     * 设置APP名称
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)} }
     *
     * @param appName APP名称
     * @return this
     */
    public static NotifyUtils setAppName(String appName) {
        return setView(R.id.tvAppName,appName);
    }

    /**
     * 右上角字符串
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)} }
     */
    public static NotifyUtils setTopRight(String topRight) {
        return setView(R.id.tvTopRight,topRight);
    }

    /**
     * 设置APP about
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param appAbout APP about
     * @return this
     */
    public static NotifyUtils setAppAbout(String appAbout) {
        getInstance().contentView.setTextViewText(R.id.tvAppAbout, appAbout);
        return setView(R.id.tvAppAbout,appAbout);
    }

    private static NotifyUtils setView(int viewId,String content) {
        String appendStr = "";
        if (!TypeDataUtils.isEmpty(content)) {
            appendStr = content;
        } else {
            appendStr = "";
        }
        getInstance().contentView.setTextViewText(viewId, appendStr);
        return getInstance();
    }

    /**
     * 设置需要显示备注信息
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param remarks 需要显示备注信息
     * @return this
     */
    public static NotifyUtils setRemarks(String remarks) {
        return setView(R.id.tvRemarks,remarks);
    }

    /**
     * 设置需要提示的消息
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param prompt 需要提示的消息
     * @return this
     */
    public static NotifyUtils setPrompt(String prompt) {
        return setView(R.id.tvPrompt,prompt);
    }

    /**
     * 时间
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param dataTime 时间
     * @return this
     */
    public static NotifyUtils setDataTime(String dataTime) {
        return setView(R.id.tvDataTime,dataTime);
    }


    /**
     * 设置滑动是否删除
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param mSlideOff true|false
     * @return this
     */
    public static NotifyUtils setSlideOff(boolean mSlideOff) {
        getInstance().mSlideOff = !mSlideOff;
        return getInstance();
    }

    /**
     * 设置点击是否消失
     * 外部调用此方法时，请先调用{@link #setNotifyId(int)}
     *
     * @param mAutoCancel true|false
     * @return this
     */
    public static NotifyUtils setmAutoCancel(boolean mAutoCancel) {
        getInstance().mAutoCancel = mAutoCancel;
        return getInstance();
    }

    /**
     * 显示notifacation时执行
     * 更改参数时执行
     */
    public void exeuNotify() {
        if (getInstance().manager != null) {
            if (getInstance().notifyId != -1) {
                if (getInstance().builder != null) {
                    getInstance().builder.setOngoing(!getInstance().mSlideOff);//滑动不能清除
                    getInstance().builder.setAutoCancel(getInstance().mAutoCancel);//点击的时候消失
                    getInstance().manager.notify(getInstance().notifyId, getInstance().builder.build());  //参数一为ID，用来区分不同APP的Notification
                }
                SPUtils.putBoolean("needClose", getInstance().mAutoCancel);
                if (getInstance().mINotifyStateCallback != null) {
                    getInstance().mINotifyStateCallback.enableStatus(true);
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
        if (getInstance() != null && getInstance().manager != null && getInstance().notifyId != -1) {
            getInstance().manager.cancel(getInstance().notifyId);//参数一为ID，用来区分不同APP的Notification
            if (getInstance().mINotifyStateCallback != null) {
                //通知监听对象
                getInstance().mINotifyStateCallback.enableStatus(false);
                getInstance().mINotifyStateCallback = null;
            }
            //销毁广播
            if (getInstance().mNotifyReceiver != null) {
                BaseIotUtils.getContext().unregisterReceiver(getInstance().mNotifyReceiver);
            }
            notifyUtils = null;
        }
    }
}
