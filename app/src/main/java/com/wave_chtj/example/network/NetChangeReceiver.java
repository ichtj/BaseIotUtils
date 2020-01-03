package com.wave_chtj.example.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.chtj.base_iotutils.KLog;
import com.chtj.base_iotutils.NetUtils;
import com.chtj.base_iotutils.keeplive.BaseIotUtils;

/**
 * Create on 2020/1/3
 * author chtj
 * desc 网络变化广播
 */
public class NetChangeReceiver extends BroadcastReceiver {
    private static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String TAG="NetChangeReceiver";
    private OnNetChangeLinstener mOnNetChangeLinstener;
    private static NetChangeReceiver netChangeReceiver;

    //单例模式
    public static NetChangeReceiver getInstance() {
        if (netChangeReceiver == null) {
            synchronized (NetChangeReceiver.class) {
                if (netChangeReceiver == null) {
                    netChangeReceiver = new NetChangeReceiver();
                }
            }
        }
        return netChangeReceiver;
    }

    /**
     * 设置回调
     */
    public void setOnNetChangeLinstener(OnNetChangeLinstener onNetChangeLinstener) {
        mOnNetChangeLinstener = onNetChangeLinstener;
    }

    /**
     * 注册广播
     */
    public void registerReceiver(){
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ANDROID_NET_CHANGE_ACTION);
        BaseIotUtils.getContext().registerReceiver(this,intentFilter);
    }

    /**
     * 销毁广播
     */
    public void unRegisterReceiver(){
        BaseIotUtils.getContext().unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        KLog.e(TAG,"action="+action);
        if(action.equals(ANDROID_NET_CHANGE_ACTION)){
            if(mOnNetChangeLinstener!=null){
                int type=NetUtils.getNetworkType();
                KLog.e(TAG,"type="+type);
                if(type== -1){//TYPE_NONE
                    mOnNetChangeLinstener.changed(NetTypeInfo.NONE,false);
                }else if(type==ConnectivityManager.TYPE_MOBILE){//0
                    mOnNetChangeLinstener.changed(NetTypeInfo.MOBILE,true);
                }else if(type==ConnectivityManager.TYPE_WIFI){//1
                    mOnNetChangeLinstener.changed(NetTypeInfo.WIFI,true);
                }else if(type==ConnectivityManager.TYPE_ETHERNET){//9
                    mOnNetChangeLinstener.changed(NetTypeInfo.ETH,true);
                }else{
                    mOnNetChangeLinstener.changed(NetTypeInfo.OTHER,true);
                }
            }
        }
    }
}
