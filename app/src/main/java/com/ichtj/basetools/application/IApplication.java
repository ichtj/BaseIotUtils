package com.ichtj.basetools.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.chtj.base_framework.FBaseTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ScreenAdaptUtils;

/**
 * Create on 2019/11/5
 * author chtj
 * desc
 */
public class IApplication extends Application {
    private static final String TAG = IApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: IApplication");
        KLog.init(true);
//        CrashHandler.getInstance().init(getApplication());
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        BaseIotUtils.instance().create(this);
        ScreenAdaptUtils.init(this, ScreenAdaptUtils.AdaptBase.WIDTH, 1080f);
        FBaseTools.instance().create(this);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
