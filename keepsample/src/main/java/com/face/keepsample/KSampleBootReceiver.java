package com.face.keepsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KSampleBootReceiver extends BroadcastReceiver {
    private static final String TAG = "KeepAliveBootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        //开机启动
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.d(TAG, "onReceive: ACTION_BOOT_COMPLETED");
            context.startService(new Intent(context,KSampleService.class));
        }
    }
}
