package com.face_chtj.base_iotutils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.face_chtj.base_iotutils.callback.IAdaptation;
import com.face_chtj.base_iotutils.entity.DnsBean;
import com.face_chtj.base_iotutils.callback.IDefaultUrlCallback;
import com.face_chtj.base_iotutils.adaptation.activitylifecycle.ActivityLifecycleImp;
import com.face_chtj.base_iotutils.adaptation.activitylifecycle.DefaultAutoAdaptStrategy;
import com.face_chtj.base_iotutils.adaptation.AdaptScreenUtils;

import java.util.concurrent.CopyOnWriteArrayList;

public final class BaseIotUtils {
    private static final String TAG = BaseIotUtils.class.getSimpleName();
    //全局上下文
    static Context sApp;
    //以高度适配
    public static final int HEIGHT = 0x1010;
    //以宽度适配
    public static final int WIDTH = 0x1011;
    //屏幕适配类型 高度|宽度
    private int adaptation = HEIGHT;
    private int defaultWidth = 1080;//宽度
    private int defaultHeight = 1920;//高度
    private boolean autoAdaptation=false;

    //activity生命周期监控及适配屏幕
    private ActivityLifecycleImp mAtyLifecycle;
    private IDefaultUrlCallback iDefaultUrlCallback;
    private static volatile BaseIotUtils sInstance;
    public CopyOnWriteArrayList<DnsBean> dnsBeans;
    //用于定时刷新DNS列表的时间→每隔一定的周期进行DNS刷新,筛选正常的列表用于网络校验
    public long dnsRefreshTime;

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
     * 设置屏幕宽度 pt
     *
     * @param width          pt 宽度
     * @param height         pt 高度
     * @param adaptation 是否开启适配
     * @return
     */
    public BaseIotUtils setAdaptation(int width, int height, @IAdaptation int adaptation, boolean autoAdaptation) {
        instance().defaultWidth = width;
        instance().defaultHeight = height;
        instance().adaptation=adaptation;
        instance().autoAdaptation=autoAdaptation;
        return instance();
    }

    /**
     * 使用 AndroidAutoSize 初始化时设置的默认适配参数进行适配 (AndroidManifest 的 Meta 属性)
     */
    public static void autoConvertDensityOfGlobal(Activity activity) {
        if (instance().autoAdaptation) {
            if (instance().adaptation == WIDTH) {
                //KLog.d(TAG, "Adaptation is open and adapting to width");
                AdaptScreenUtils.adaptWidth(activity.getResources(), instance().defaultWidth);
            } else {
                //KLog.d(TAG, "Adaptation is open and adapting to height");
                AdaptScreenUtils.adaptHeight(activity.getResources(), instance().defaultHeight);
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
        sApp = application.getApplicationContext();
        if (instance().autoAdaptation) {
            //activity生命周期监听
            instance().mAtyLifecycle = new ActivityLifecycleImp(new DefaultAutoAdaptStrategy());
            application.registerActivityLifecycleCallbacks(instance().mAtyLifecycle);
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
