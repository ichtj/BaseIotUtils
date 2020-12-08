package com.chtj.framework_utils;

import android.app.Application;
import android.content.Context;

import com.chtj.framework_utils.entity.DeviceType;

public final class BaseSystemUtils {
    private static final String TAG = "BaseIotUtils";
    //全局上下文
    static Context sApp;
    static DeviceType deviceType;
    private static volatile BaseSystemUtils sInstance;

    //单例模式
    public static BaseSystemUtils instance(DeviceType deviceType) {
        if (sInstance == null) {
            synchronized (BaseSystemUtils.class) {
                if (sInstance == null) {
                    sInstance = new BaseSystemUtils();
                    BaseSystemUtils.deviceType = deviceType;
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化上下文，注册interface
     *
     * @param application 全局上下文
     */
    public void create(Application application) {
        BaseSystemUtils.sApp = application.getApplicationContext();
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
