package com.face_chtj.base_iotutils.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Create on 2020/5/6
 * author chtj
 * desc
 */
public class NotifyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NotifyUtils.ACTION_CLOSE_NOTIFY)) {
            //关闭通知
            NotifyUtils.closeNotify();
        }
    }
}
