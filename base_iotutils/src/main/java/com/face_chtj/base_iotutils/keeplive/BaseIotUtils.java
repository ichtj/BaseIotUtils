package com.face_chtj.base_iotutils.keeplive;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.SCREEN_TYPE;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.ActivityLifecycleImp;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.DefaultAutoAdaptStrategy;
import com.face_chtj.base_iotutils.screen_adapta.AdaptScreenUtils;

import java.util.HashMap;
import java.util.Map;

public final class BaseIotUtils {
    private static final String TAG = BaseIotUtils.class.getSimpleName();
    //全局上下文
    static Context sApp;
    //宽度
    private int defaultWidth = 1080;
    //高度
    private int defaultHeight = 1920;
    //是否开启适配
    private boolean mAutoScreenAdaptation = false;
    //屏幕适配类型 高度|宽度
    private SCREEN_TYPE screen_type = SCREEN_TYPE.HEIGHT;
    //activity生命周期监控及适配屏幕
    private ActivityLifecycleImp mActivityLifecycleImp;
    //唤醒的周期
    //6分钟
    public static final int DEFAULT_WAKE_UP_INTERVAL = 6 * 60 * 1000;
    //3分钟
    private static final int MINIMAL_WAKE_UP_INTERVAL = 3 * 60 * 1000;
    //唤醒的间隔时间
    static int sWakeUpInterval = DEFAULT_WAKE_UP_INTERVAL;
    //保存需要启动的Service
    static Class<? extends AbsWorkService> sServiceClass;
    //是否已经初始化过
    static boolean sInitialized;
    private static final Map<Class<? extends Service>, ServiceConnection> BIND_STATE_MAP = new HashMap<>();

    private static volatile BaseIotUtils sInstance;

    //单例模式
    public static BaseIotUtils instance() {
        if (sInstance == null) {
            synchronized (BaseIotUtils.class) {
                if (sInstance == null) {
                    sInstance = new BaseIotUtils();
                }
            }
        }
        return sInstance;
    }


    /**
     * 设置屏幕宽度 dp
     *
     * @param defaultWidth          dp 宽度
     * @param defaultHeight         dp 高度
     * @param mAutoScreenAdaptation 是否开启适配
     * @return
     */
    public BaseIotUtils setBaseScreenParam(int defaultWidth, int defaultHeight, boolean mAutoScreenAdaptation) {
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.mAutoScreenAdaptation = mAutoScreenAdaptation;
        return this;
    }


    /**
     * 设置以宽度或者高度来配置
     * WIDTH 按宽度适配
     * HEIGHT 按高度适配
     *
     * @param screen_type dp
     * @return
     */
    public BaseIotUtils setCreenType(SCREEN_TYPE screen_type) {
        this.screen_type = screen_type;
        return this;
    }

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配 (AndroidManifest 的 Meta 属性)
     */
    public static void autoConvertDensityOfGlobal(Activity activity) {
        if (BaseIotUtils.instance().mAutoScreenAdaptation) {
            if (BaseIotUtils.instance().screen_type == SCREEN_TYPE.WIDTH) {
                KLog.d(TAG, "Adaptation is open and adapting to width");
                AdaptScreenUtils.adaptWidth(activity.getResources(), BaseIotUtils.instance().defaultWidth);
            } else {
                KLog.d(TAG, "Adaptation is open and adapting to height");
                AdaptScreenUtils.adaptHeight(activity.getResources(), BaseIotUtils.instance().defaultHeight);
            }
        } else {
            KLog.d(TAG, "Adaptation is not open adapting please at application BaseIotUtils.setAutoScreenAdaptation, set value equal true");
        }
    }

    /**
     * 初始化上下文，注册interface
     *
     * @param application 全局上下文
     */
    public void create(Application application) {
        BaseIotUtils.sApp = application.getApplicationContext();
        if (mAutoScreenAdaptation) {
            //activity生命周期监听
            mActivityLifecycleImp = new ActivityLifecycleImp(new DefaultAutoAdaptStrategy());
            application.registerActivityLifecycleCallbacks(mActivityLifecycleImp);
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (sApp != null) {
            return sApp;
        }
        throw new NullPointerException("should be initialized in application");
    }


    /**
     * 后台保活而进行的初始化
     *
     * @param wakeUpInterval 定时唤醒的时间间隔(ms).
     */
    public static void initSerice(@NonNull Class<? extends AbsWorkService> serviceClass, @Nullable Integer wakeUpInterval) {
        sServiceClass = serviceClass;
        if (wakeUpInterval != null) sWakeUpInterval = wakeUpInterval;
        sInitialized = true;
    }

    /**
     * 开启后台保活服务
     *
     * @param serviceClass
     */
    public static void startServiceMayBind(@NonNull final Class<? extends Service> serviceClass) {
        KLog.e(TAG,"startServiceMayBind");
        //如果已经初始化过后台保活的Service
        if (!sInitialized) {
            return;
        }
        //否者重新启动
        final Intent i = new Intent(sApp, serviceClass);
        startServiceSafely(i);
        ServiceConnection bound = BIND_STATE_MAP.get(serviceClass);
        if (bound == null) {
            sApp.bindService(i, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    BIND_STATE_MAP.put(serviceClass, this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    //连接断开时重新启动
                    BIND_STATE_MAP.remove(serviceClass);
                    startServiceSafely(i);
                    if (!sInitialized) {
                        return;
                    } else {
                        sApp.bindService(i, this, Context.BIND_AUTO_CREATE);
                    }
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    //服务死掉时执行onServiceDisconnected
                    onServiceDisconnected(name);
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }

    static void startServiceSafely(Intent i) {
        if (!sInitialized) return;
        try {
            sApp.startService(i);
        } catch (Exception ignored) {
        }
    }

    static int getWakeUpInterval() {
        return Math.max(sWakeUpInterval, MINIMAL_WAKE_UP_INTERVAL);
    }
}
