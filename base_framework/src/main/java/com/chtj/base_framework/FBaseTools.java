package com.chtj.base_framework;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.chtj.base_framework.receiver.NetworkReceiver;

import java.io.File;

public final class FBaseTools {

    private static final String TAG = "FBaseTools";
    //全局上下文
    static Context sApp;
    static boolean isOpenLogRecord;
    private static volatile FBaseTools sInstance;

    /**
     * 单例模式
     *
     * @return
     */
    public static FBaseTools instance() {
        if (sInstance == null) {
            synchronized (FBaseTools.class) {
                if (sInstance == null) {
                    sInstance = new FBaseTools();
                }
            }
        }
        return sInstance;
    }

    /**
     * 开网络异常日志记录
     *
     * @return
     */
    public FBaseTools setRecordLog(boolean isLogRecord) {
        isOpenLogRecord = isLogRecord;
        return sInstance;
    }

    /**
     * 初始化上下文，注册interface
     *
     * @param application 全局上下文
     */
    public void create(Application application) {
        FBaseTools.sApp = application.getApplicationContext();
        otherOperations();
    }

    /**
     * 创建记录网络变化后的日志文件创建
     */
    public void otherOperations() {
        File file = new File(FCommonTools.SAVE_NETERR_PATH + FBaseTools.sApp.getPackageName() + "/");
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(FCommonTools.SAVE_NETERR_PATH + FBaseTools.sApp.getPackageName() + "/" + FCommonTools.SAVE_NETERR_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
        }
        if (isOpenLogRecord) {
            //开启网络异常日志记录
            NetworkReceiver networkReceiver = new NetworkReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NetworkReceiver.ANDROID_NET_CHANGE_ACTION);
            getContext().registerReceiver(networkReceiver, intentFilter);
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
