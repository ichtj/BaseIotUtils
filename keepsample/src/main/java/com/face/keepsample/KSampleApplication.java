package com.face.keepsample;

import android.app.Application;
import android.content.Context;

import com.chtj.keepalive.FBaseDaemon;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.SCREEN_TYPE;

public class KSampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KLog.init(true);
        BaseIotUtils.instance().setBaseScreenParam(1080, 1920, true).setCreenType(SCREEN_TYPE.WIDTH).create(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        FBaseDaemon.init(base);
    }
}
