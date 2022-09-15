package com.face_chtj.base_iotutils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.face_chtj.base_iotutils.network.callback.IDefaultUrlCallback;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.ActivityLifecycleImp;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.DefaultAutoAdaptStrategy;
import com.face_chtj.base_iotutils.screen_adapta.AdaptScreenUtils;

public final class BaseIotUtils {
    private static final String TAG = "BaseIotUtils";
    //全局上下文
    static Context sApp;
    //宽度
    private int defaultWidth = 1080;
    //高度
    private int defaultHeight = 1920;
    //是否开启适配
    private boolean mAutoScreenAdaptation = false;
    /**以高度适配 {@link #setCreenType(int)}*/
    public static final int SCREEN_HEIGHT=0x1010;
    /**以宽度适配  {@link #setCreenType(int)}*/
    public static final int SCREEN_WIDTH=0x1011;
    //屏幕适配类型 高度|宽度
    private int screen_type = SCREEN_HEIGHT;
    //activity生命周期监控及适配屏幕
    private ActivityLifecycleImp mActivityLifecycleImp;
    private IDefaultUrlCallback iDefaultUrlCallback;
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

    public static BaseIotUtils initBaseUrlMap(IDefaultUrlCallback iDefaultUrlCallback) {
        instance().iDefaultUrlCallback = iDefaultUrlCallback;
        return instance();
    }

    public static IDefaultUrlCallback getiBaseUrlCallback() {
        return instance().iDefaultUrlCallback;
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
        return instance();
    }


    /**
     * 设置以宽度或者高度来配置
     * {@link #SCREEN_WIDTH} 按宽度适配
     * {@link #SCREEN_HEIGHT} 按高度适配
     * @param screen_type {@link #SCREEN_WIDTH}{@link #SCREEN_HEIGHT}
     */
    public BaseIotUtils setCreenType(int screen_type) {
        this.screen_type = screen_type;
        return instance();
    }

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配 (AndroidManifest 的 Meta 属性)
     */
    public static void autoConvertDensityOfGlobal(Activity activity) {
        if (BaseIotUtils.instance().mAutoScreenAdaptation) {
            if (BaseIotUtils.instance().screen_type == SCREEN_WIDTH) {
                //KLog.d(TAG, "Adaptation is open and adapting to width");
                AdaptScreenUtils.adaptWidth(activity.getResources(), BaseIotUtils.instance().defaultWidth);
            } else {
                //KLog.d(TAG, "Adaptation is open and adapting to height");
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
}
