package com.face_chtj.base_iotutils;

import android.app.Application;
import android.content.Context;

import com.face_chtj.base_iotutils.callback.IDefaultUrlCallback;

public final class BaseIotUtils {
    private static final String TAG = BaseIotUtils.class.getSimpleName();
    //全局上下文
    static Context sApp;
    private IDefaultUrlCallback iDefaultUrlCallback;
    private static volatile BaseIotUtils sInstance;

    //singleton pattern
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
     * 初始化上下文，注册interface
     *
     * @param application 全局上下文
     */
    public void create(Application application) {
        sApp = application.getApplicationContext();
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
