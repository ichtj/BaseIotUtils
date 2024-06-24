package com.face_chtj.base_iotutils.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NotifyUtils;
import com.face_chtj.base_iotutils.SPUtils;

/**
 * Create on 2020/5/6
 * author chtj
 * desc
 */
public class NotifyReceiver extends BroadcastReceiver {
    private static final String TAG=NotifyReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NotifyUtils.ACTION_CLOSE_NOTIFY)) {
            KLog.d(TAG,"NotifyBroadcastReceiver action="+intent.getAction());
            NotifyUtils.closeNotify();
        }
    }
}
