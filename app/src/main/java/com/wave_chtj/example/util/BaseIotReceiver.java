package com.wave_chtj.example.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.face_chtj.base_iotutils.KLog;
import com.wave_chtj.example.FeaturesOptionAty;
import com.wave_chtj.example.network.NetResetMonitorAty;
import com.wave_chtj.example.network.NetResetMonitorService;
import com.wave_chtj.example.reboot.RebootCustomService;

/**
 * Create on 2020/6/4
 * author chtj
 * desc
 */
public class BaseIotReceiver extends BroadcastReceiver {
    private static final String TAG = "BaseIotReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: action>>>" + intent.getAction());
        //开机启动
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            String pkgName = context.getPackageName();
            KLog.d(TAG, "onReceive pkgName=" + pkgName);
            if (pkgName.contains(SwitchUtils.FLAG_REBOOT_PKG)) {
                context.startService(new Intent(context, RebootCustomService.class));
            } else if (pkgName.contains(SwitchUtils.FLAG_SERIALPORT_PKG)) {

            } else if (pkgName.contains(SwitchUtils.FLAG_NETMONITOR_PKG)) {
                Intent intent1 = new Intent(context, NetResetMonitorAty.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            } else if (pkgName.contains(SwitchUtils.FLAG_EXAMPLE_PKG)) {
                Intent intent1 = new Intent(context, FeaturesOptionAty.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }
    }
}
