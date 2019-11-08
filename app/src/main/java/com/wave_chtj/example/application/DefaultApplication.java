package com.wave_chtj.example.application;

import android.app.Application;

/**
 * Create on 2019/11/5
 * author chtj
 * desc
 */
public class DefaultApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        InitializeService.start(this);
    }
}
