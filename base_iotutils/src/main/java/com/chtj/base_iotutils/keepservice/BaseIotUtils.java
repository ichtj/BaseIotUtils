package com.chtj.base_iotutils.keepservice;

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
import android.util.Log;

import com.chtj.base_iotutils.screen_adapta.activitylifecycle.SCREEN_TYPE;
import com.chtj.base_iotutils.screen_adapta.activitylifecycle.ActivityLifecycleImp;
import com.chtj.base_iotutils.screen_adapta.activitylifecycle.DefaultAutoAdaptStrategy;
import com.chtj.base_iotutils.screen_adapta.AdaptScreenUtils;

import java.util.HashMap;
import java.util.Map;

public final class BaseIotUtils {
    private static final String TAG = BaseIotUtils.class.getSimpleName() ;
    //宽度
    private int defaultWidth = 1080;
    //高度
    private int defaultHeight = 1920;
    //是否开启适配
    private boolean mAutoScreenAdaptation=false;
    //屏幕适配类型 高度|宽度
    private SCREEN_TYPE screen_type;
    //activity生命周期监控及适配屏幕
    private ActivityLifecycleImp mActivityLifecycleImp;

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
     * @param defaultWidth dp
     * @return
     */
    public BaseIotUtils setBaseWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
        return this;
    }
    /**
     * 设置屏幕宽度 dp
     *
     * @param defaultHeight dp
     * @return
     */
    public BaseIotUtils setBaseHeight(int defaultHeight) {
        this.defaultHeight = defaultHeight;
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
     * 是否开启适配
     * @param mAutoScreenAdaptation
     * @return
     */
    public BaseIotUtils setAutoScreenAdaptation(boolean mAutoScreenAdaptation) {
        this.mAutoScreenAdaptation = mAutoScreenAdaptation;
        return this;
    }

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配 (AndroidManifest 的 Meta 属性)
     */
    public static void autoConvertDensityOfGlobal(Activity activity){
        if(BaseIotUtils.instance().mAutoScreenAdaptation){
            if (BaseIotUtils.instance().screen_type==SCREEN_TYPE.WIDTH) {
                Log.e(TAG,"开启了适配 并且以宽度进行适配");
                AdaptScreenUtils.adaptWidth(activity.getResources(), BaseIotUtils.instance().defaultWidth );
            } else {
                Log.e(TAG,"开启了适配 并且以高度进行适配");
                AdaptScreenUtils.adaptHeight(activity.getResources(), BaseIotUtils.instance().defaultHeight );
            }
        }else{
            Log.e(TAG,"当前未开启适配 如需设置请在application中将setAutoScreenAdaptation 设置为true");
        }

    }

    /**
     * 初始化上下文，注册interface
     * @param application 全局上下文
     */
    public void create(Application application) {
        BaseIotUtils.sApp = application.getApplicationContext();
        mActivityLifecycleImp = new ActivityLifecycleImp(new DefaultAutoAdaptStrategy());
        application.registerActivityLifecycleCallbacks(mActivityLifecycleImp);
    }


    /**后台保活相关**/
    public static final int DEFAULT_WAKE_UP_INTERVAL = 6 * 60 * 1000;
    private static final int MINIMAL_WAKE_UP_INTERVAL = 3 * 60 * 1000;

    static Context sApp;
    static Class<? extends AbsWorkService> sServiceClass;
    private static int sWakeUpInterval = DEFAULT_WAKE_UP_INTERVAL;
    static boolean sInitialized;

    static final Map<Class<? extends Service>, ServiceConnection> BIND_STATE_MAP = new HashMap<>();


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
    public static void initSerice( @NonNull Class<? extends AbsWorkService> serviceClass, @Nullable Integer wakeUpInterval) {
        sServiceClass = serviceClass;
        if (wakeUpInterval != null) sWakeUpInterval = wakeUpInterval;
        sInitialized = true;
    }

    /**
     * 开启后台保活服务
     * @param serviceClass
     */
    public static void startServiceMayBind(@NonNull final Class<? extends Service> serviceClass) {
        if (!sInitialized) return;
        final Intent i = new Intent(sApp, serviceClass);
        startServiceSafely(i);
        ServiceConnection bound = BIND_STATE_MAP.get(serviceClass);
        if (bound == null) sApp.bindService(i, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BIND_STATE_MAP.put(serviceClass, this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                BIND_STATE_MAP.remove(serviceClass);
                startServiceSafely(i);
                if (!sInitialized) return;
                sApp.bindService(i, this, Context.BIND_AUTO_CREATE);
            }

            @Override
            public void onBindingDied(ComponentName name) {
                onServiceDisconnected(name);
            }
        }, Context.BIND_AUTO_CREATE);
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
