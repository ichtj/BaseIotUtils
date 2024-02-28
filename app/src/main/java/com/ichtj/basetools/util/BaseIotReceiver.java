package com.ichtj.basetools.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.face_chtj.base_iotutils.KLog;
import com.ichtj.basetools.OptionAty;
import com.ichtj.basetools.network.NetMonitorAty;
import com.ichtj.basetools.network.NetRecordAty;
import com.ichtj.basetools.reboot.RebootCustomService;

/**
 * Create on 2020/6/4
 * author chtj
 * desc
 */
public class BaseIotReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        KLog.d("onReceive: action>>>" + intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            String pkgName = context.getPackageName();
            KLog.d("onReceive pkgName=" + pkgName);
            switch (pkgName) {
                case PACKAGES.PKG_REBOOT:
                    context.startService(new Intent(context, RebootCustomService.class));
                    break;
                case PACKAGES.PKG_SERIALPORT:
                    break;
                case PACKAGES.PKG_NETMONITOR:
                    intent = new Intent(context, NetMonitorAty.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    break;
                case PACKAGES.PKG_NETTIMER:
                    intent = new Intent(context, NetRecordAty.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    break;
                case PACKAGES.PKG_EXAMPLE:
                    intent = new Intent(context, OptionAty.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    break;
            }
        }
    }
}
