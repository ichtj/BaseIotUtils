package com.chtj.framework;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chtj.framework.entity.DeviceType;
import com.chtj.framework.task.FBaseService;

import java.io.File;

public final class FBaseTools {

    private static final String TAG = "FBaseSystemTools";
    //全局上下文
    static Context sApp;


    /**
     * 是否开启网络有关日志的记录
     */
    private Boolean isOpenNetWorkRecord = false;
    public FBaseTools setOpenNetWorkRecord(Boolean openNetWorkRecord) {
        this.isOpenNetWorkRecord = openNetWorkRecord;
        return this;
    }

    public Boolean getOpenNetWorkRecord() {
        return isOpenNetWorkRecord;
    }


    /**
     * 当前操作的设备类型
     */
    private DeviceType deviceType;
    public FBaseTools setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }


    private static volatile FBaseTools sInstance;


    /**
     * 单例模式
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
    public void createFileAndOpenReceiver() {
        if (FBaseTools.instance().isOpenNetWorkRecord) {
            File file = new File(FCommonTools.SAVE_PATH);
            if (!file.exists()) {
                file.mkdir();
            }
            file = new File(FCommonTools.SAVE_PATH + FCommonTools.SAVE_FILE_NAME);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "errMeg:" + e.getMessage());
                }
            }
            getContext().startService(new Intent(FBaseTools.sApp, FBaseService.class));
        }
    }


    /**
     * 初始化上下文，注册interface
     *
     * @param application 全局上下文
     */
    public void create(Application application) {
        FBaseTools.sApp = application.getApplicationContext();
        createFileAndOpenReceiver();
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
