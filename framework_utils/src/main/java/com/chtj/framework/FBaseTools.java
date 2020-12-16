package com.chtj.framework;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.chtj.framework.entity.DeviceType;
import com.chtj.framework.receiver.NetworkReceiver;

import java.io.File;

public final class FBaseTools {


    private static final String TAG = "FBaseSystemTools";
    //全局上下文
    private static Context sApp;
    /**
     * 是否开启网络有关日志的记录
     */
    private static Boolean isOpenNetWorkRecord = false;
    /**
     * 当前操作的设备类型
     */
    static DeviceType deviceType;

    static volatile FBaseTools sInstance;


    //单例模式
    public static FBaseTools instance(DeviceType deviceType) {
        return instance(deviceType, false);
    }

    public FBaseTools() {
        createFileAndOpenReceiver();
    }
    /**
     * @param deviceType          设备类型
     * @param isOpenNetWorkRecord 网络变化后日志是否记录
     * @return
     */
    public static FBaseTools instance(DeviceType deviceType, boolean isOpenNetWorkRecord) {
        if (sInstance == null) {
            synchronized (FBaseTools.class) {
                if (sInstance == null) {
                    FBaseTools.deviceType = deviceType;
                    FBaseTools.isOpenNetWorkRecord = isOpenNetWorkRecord;
                    sInstance = new FBaseTools();
                }
            }
        }
        return sInstance;
    }

    /**
     * 创建记录网络变化后的日志文件创建
     */
    public  void createFileAndOpenReceiver() {
        if (FBaseTools.isOpenNetWorkRecord) {
            File file = new File(NetworkReceiver.SAVE_PATH);
            if (!file.exists()) {
                file.mkdir();
            }
            file = new File(NetworkReceiver.SAVE_PATH + NetworkReceiver.SAVE_FILE_NAME);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "errMeg:" + e.getMessage());
                }
            }
            /**开启广播监听网络**/
            NetworkReceiver networkReceiver = new NetworkReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NetworkReceiver.ANDROID_NET_CHANGE_ACTION);
            getContext().registerReceiver(networkReceiver, intentFilter);
        }
    }


    /**
     * 初始化上下文，注册interface
     *
     * @param application 全局上下文
     */
    public void create(Application application) {
        FBaseTools.sApp = application.getApplicationContext();
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
