package com.ichtj.basetools.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ichtj.basetools.BuildConfig;
import com.ichtj.basetools.MainActivity;
import com.ichtj.basetools.network.NetMonitorAty;
import com.ichtj.basetools.network.NetRecordAty;
import com.ichtj.basetools.reboot.RebootAty;
import com.ichtj.basetools.reboot.RebootCustomService;
import com.ichtj.basetools.test.ReadImeiAty;

/** Receives boot completion for the selected utility application variant. */
public class BaseIotReceiver extends BroadcastReceiver {
    private static final String TAG = BaseIotReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        Log.d(TAG, "onReceive: action=" + action);
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            return;
        }

        String packageName = context.getPackageName();
        Log.d(TAG, "onReceive: packageName=" + packageName
                + ", appChoose=" + BuildConfig.APP_CHOOSE);
        switch (packageName) {
            case PACKAGES.PKG_REBOOT:
                handleRebootBootCompleted(context);
                if (!RebootCustomService.isBackgroundModeEnabled(context)) {
                    startActivity(context, RebootAty.class);
                }
                break;
            case PACKAGES.PKG_SERIALPORT:
                break;
            case PACKAGES.PKG_NETMONITOR:
                startActivity(context, NetMonitorAty.class);
                break;
            case PACKAGES.PKG_NETTIMER:
                startActivity(context, NetRecordAty.class);
                break;
            case PACKAGES.PKG_EXAMPLE:
                startActivity(context, MainActivity.class);
                break;
            case PACKAGES.PKG_READIMEI:
                startActivity(context, ReadImeiAty.class);
                break;
            default:
                Log.d(TAG, "onReceive: no boot action for package " + packageName);
                break;
        }
    }

    private void handleRebootBootCompleted(Context context) {
        PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                RebootCustomService.handleBootCompleted(appContext);
            } finally {
                pendingResult.finish();
            }
        }, "reboot-boot-handler").start();
    }

    private static void startActivity(Context context, Class<?> activityClass) {
        Intent activityIntent = new Intent(context, activityClass);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }
}
