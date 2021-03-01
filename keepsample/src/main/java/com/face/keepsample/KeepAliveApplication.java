package com.face.keepsample;

import android.app.Application;
import android.content.Context;

import com.chtj.keepalive.FBaseDaemon;

public class KeepAliveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        FBaseDaemon.init(base);
    }
}
