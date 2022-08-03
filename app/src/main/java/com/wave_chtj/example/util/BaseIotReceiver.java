package com.wave_chtj.example.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.face_chtj.base_iotutils.KLog;
import com.wave_chtj.example.FeaturesOptionAty;
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
        Log.d(TAG, "onReceive: action>>>"+intent.getAction());
        //开机启动
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            String pkgName=context.getPackageName();
            if(pkgName.contains("reboot")){
                context.startService(new Intent(context, RebootCustomService.class));
            }else if(pkgName.equals("serial")){

            }else{
                Intent intent1=new Intent(context,FeaturesOptionAty.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                context.startActivity(intent1);
            }
        }
    }
}
