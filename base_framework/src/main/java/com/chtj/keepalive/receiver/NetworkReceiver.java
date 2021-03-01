package com.chtj.keepalive.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.chtj.keepalive.FCommonTools;
import com.chtj.keepalive.network.FEthTools;
import com.chtj.keepalive.network.FNetworkTools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 网络变化时记录网络状态
 */
public class NetworkReceiver extends BroadcastReceiver {
    public static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "action=" + action);
        if (action.equals(ANDROID_NET_CHANGE_ACTION)) {
            //获取当前网络类型
            String type = FCommonTools.getNetWorkTypeName();
            //判断网络是否连接正常，是否能够ping通
            StringBuffer stringBuffer = new StringBuffer("\n\r");
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            boolean isPingSuccessful = FCommonTools.ping("114.114.114.114", 1, 2);
            String gateway = FCommonTools.getGateWay();
            boolean isPingGateway = FCommonTools.ping(gateway, 1, 2);

            stringBuffer.append("nowTime=" + simpleDateFormat.format(new Date())+"\n\r");
            stringBuffer.append("FirmwareVersion= [" + Build.DISPLAY + " ]"+"\n\r");
            stringBuffer.append("EthIpMode= [" + FEthTools.getIpMode() + " ]"+"\n\r");
            stringBuffer.append("AndroidVersion= [" + Build.VERSION.RELEASE + " ]"+"\n\r");
            stringBuffer.append("AppVersionName= [" + FCommonTools.getAppVersionName() + " ]"+"\n\r");
            stringBuffer.append("AppVersionCode= [" + FCommonTools.getAppVersionCode() + " ]"+"\n\r");
            stringBuffer.append("Devices_isRoot= [" + FCommonTools.isRoot() + " ]"+"\n\r");
            stringBuffer.append("netType=" + type+"\n\r");
            stringBuffer.append("gateway=" + gateway+"\n\r");
            stringBuffer.append("localIp=" + FCommonTools.getLocalIp()+"\n\r");
            stringBuffer.append("ping114=" + isPingSuccessful+"\n\r");
            stringBuffer.append("pingGateway=" + isPingGateway+"\n\r");

            String[] dnsList= FNetworkTools.getNetWorkDns();
            StringBuffer dnsStr=new StringBuffer();
            if(dnsList!=null){
                for (int i = 0; i <dnsList.length ; i++) {
                    dnsStr.append(dnsList[i]+" | ");
                }
            }
            stringBuffer.append("getDns=" +dnsStr.toString()+"\n\r");
            stringBuffer.append("///////////////////////////////////");
            FCommonTools.writeFileData(FCommonTools.SAVE_NETERR_PATH+context.getPackageName()+"/"+FCommonTools.SAVE_NETERR_FILE_NAME,stringBuffer.toString(),false);
        }
    }
}
