package com.face_chtj.base_iotutils.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.SPUtils;

/**
 * Create on 2020/5/6
 * author chtj
 * desc
 */
class NotifyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG="NotifyBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        KLog.d(TAG,"NotifyBroadcastReceiver action="+intent.getAction());
        if (intent.getAction().equals(NotifyUtils.ACTION_CLOSE_NOTIFY)) {
            boolean isNeedClose= SPUtils.getBoolean("needClose",true);
            if(isNeedClose){
                //关闭通知
                NotifyUtils.closeNotify();
            }
        }
    }
}
