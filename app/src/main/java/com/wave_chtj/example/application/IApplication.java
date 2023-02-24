package com.wave_chtj.example.application;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.face_chtj.base_iotutils.NetMonitorUtils;

/**
 * Create on 2019/11/5
 * author chtj
 * desc
 */
public class IApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        InitializeService.start(this);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
