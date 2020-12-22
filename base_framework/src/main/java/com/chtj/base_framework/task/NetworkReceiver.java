package com.chtj.base_framework.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.chtj.base_framework.FBaseTools;
import com.chtj.base_framework.FCommonTools;
import com.chtj.base_framework.network.FNetworkTools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NetworkReceiver extends BroadcastReceiver {
    public static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String TAG = "NetworkReceiver";


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
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            boolean isPingSuccessful = FCommonTools.ping("114.114.114.114", 1, 2);
            String gateway = FCommonTools.getGateWay();
            boolean isPingGateway = FCommonTools.ping(gateway, 1, 2);
            stringBuffer.append("nowTime=" + simpleDateFormat.format(new Date())+"\n\r");
            stringBuffer.append("netType=" + type+"\n\r");
            stringBuffer.append("gateway=" + gateway+"\n\r");
            stringBuffer.append("localIp=" + FCommonTools.getLocalIp()+"\n\r");
            stringBuffer.append("ping114=" + isPingSuccessful+"\n\r");
            stringBuffer.append("pingGateway=" + isPingGateway+"\n\r");
            String[] dnsList= FNetworkTools.getNetWorkDns();
            StringBuffer dnsStr=new StringBuffer();
            for (int i = 0; i <dnsList.length ; i++) {
                dnsStr.append(dnsList[i]+" | ");
            }
            stringBuffer.append("getDns=" +dnsStr.toString()+"\n\r");
            stringBuffer.append("///////////////////////////////////");
            FCommonTools.writeFileData(FCommonTools.SAVE_PATH+FCommonTools.SAVE_FILE_NAME,stringBuffer.toString(),false);
        }
    }
}
