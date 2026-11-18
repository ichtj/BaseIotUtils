package com.ichtj.basetools.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ichtj.basetools.BuildConfig;

/** 按 config.gradle 中的 APP_CHOOSE 选择启动页面。 */
public final class AppNavigator {
    private static final String TAG = "ichtj_app_navigator";

    private AppNavigator() {
    }

    /** 在调用方 Activity 的任务栈中打开入口页面，由调用方决定是否结束自身。 */
    public static void open(Activity activity) {
        Class<? extends Activity> target = BuildConfig.ENTRY_ACTIVITY;
        Log.i(TAG, "Opening " + BuildConfig.APP_CHOOSE + " -> " + target.getName());
        activity.startActivity(new Intent(activity, target));
    }
}
