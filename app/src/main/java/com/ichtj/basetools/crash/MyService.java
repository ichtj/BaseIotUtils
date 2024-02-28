package com.ichtj.basetools.crash;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Create on 2020/3/23
 * author chtj
 * desc 测试anr的服务类
 */
public class MyService extends Service {
    private static final String TAG="MyService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
        sleepTest();
    }
    public void sleepTest() {
        Log.e(TAG, "sleepTest: start");
        SystemClock.sleep(20000);
        Log.e(TAG, "sleepTest: end");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
    }
}
