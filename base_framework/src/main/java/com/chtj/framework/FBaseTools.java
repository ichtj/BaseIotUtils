package com.chtj.framework;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chtj.framework.entity.DeviceType;
import com.chtj.framework.keep.FKeepAliveService;

import java.io.File;

public final class FBaseTools {

    private static final String TAG = "FBaseTools";
    //全局上下文
    static Context sApp;

    /**
     * 是否开启保活
     */
    private   boolean keepAliveStatus = false;

    public FBaseTools setKeepAliveStatus(boolean isKeepAlive) {
        keepAliveStatus = isKeepAlive;
        return sInstance;
    }

    /**
     * 当前操作的设备类型
     */
    private DeviceType deviceType;

    public FBaseTools setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
        Log.d(TAG, "setDeviceType: " + deviceType.name());
        return sInstance;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }


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
     * 创建记录网络变化后的日志文件创建
     */
    public void otherOperations() {
        File file = new File(FCommonTools.SAVE_NETERR_PATH+FBaseTools.getContext().getPackageName()+"/");
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(FCommonTools.SAVE_NETERR_PATH+FBaseTools.getContext().getPackageName()+"/" + FCommonTools.SAVE_NETERR_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
        }
    }


    /**
     * 初始化上下文，注册interface
     *
     * @param application 全局上下文
     */
    public void create(Application application) {
        FBaseTools.sApp = application.getApplicationContext();
        otherOperations();
        if(FBaseTools.instance().keepAliveStatus){
            //用于执行任务 或是保活等操作
            getContext().startService(new Intent(getContext(), FKeepAliveService.class));
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (FBaseTools.sApp != null) {
            return FBaseTools.sApp;
        }
        throw new NullPointerException("should be initialized in application");
    }
}
