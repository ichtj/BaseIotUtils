package com.chtj.framework.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.chtj.framework.FBaseTools;
import com.chtj.framework.FCommonTools;
import com.chtj.framework.FNetworkTools;

public class NetworkReceiver extends BroadcastReceiver {
    public static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String TAG = "NetListenerUtils";
    public static final String SAVE_PATH="/sdcard/LOGSAVE/network/";
    public static final String SAVE_FILE_NAME="netchange.txt";


    /**
     * 注册广播
     */
    public void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ANDROID_NET_CHANGE_ACTION);
        FBaseTools.getContext().registerReceiver(this, intentFilter);
    }

    /**
     * 销毁广播
     */
    public void unRegisterReceiver() {
        try {
            FBaseTools.getContext().unregisterReceiver(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "action=" + action);
        if (action.equals(ANDROID_NET_CHANGE_ACTION)) {
            //获取当前网络类型
            String type = FCommonTools.getNetWorkTypeName();
            //判断网络是否连接正常，是否能够ping通
            StringBuffer stringBuffer = new StringBuffer();
            boolean isPingSuccessful = FCommonTools.ping("114.114.114.114", 1, 2);
            String gateway = FCommonTools.getGateWay();
            boolean isPingGateway = FCommonTools.ping(gateway, 1, 2);
            stringBuffer.append("netType=" + type);
            stringBuffer.append("gateway=" + gateway);
            stringBuffer.append("ping114=" + isPingSuccessful);
            stringBuffer.append("pingGateway=" + isPingGateway);
            stringBuffer.append("getDns=" + FNetworkTools.getNetWorkDns());
            FCommonTools.writeFileData(SAVE_PATH+SAVE_FILE_NAME,stringBuffer.toString(),false);
        }
    }
}
