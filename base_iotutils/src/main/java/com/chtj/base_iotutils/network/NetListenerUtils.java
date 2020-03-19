package com.chtj.base_iotutils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.chtj.base_iotutils.KLog;
import com.chtj.base_iotutils.NetUtils;
import com.chtj.base_iotutils.keeplive.BaseIotUtils;

/**
 * Create on 2020/1/3
 * author chtj
 * desc 网络变化广播
 */
public class NetListenerUtils extends BroadcastReceiver {
    private static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String TAG="NetChangeReceiver";
    private OnNetChangeLinstener mOnNetChangeLinstener;
    private static NetListenerUtils netListenerUtils;

    //单例模式
    public static NetListenerUtils getInstance() {
        if (netListenerUtils == null) {
            synchronized (NetListenerUtils.class) {
                if (netListenerUtils == null) {
                    netListenerUtils = new NetListenerUtils();
                }
            }
        }
        return netListenerUtils;
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
        try{
            BaseIotUtils.getContext().unregisterReceiver(this);
        }catch(Exception e){
            e.printStackTrace();
            KLog.e(TAG,"errMeg:"+e.getMessage());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        KLog.e(TAG,"action="+action);
        if(action.equals(ANDROID_NET_CHANGE_ACTION)){
            if(mOnNetChangeLinstener!=null){
                int type=NetUtils.getNetWorkType();
                KLog.e(TAG,"type="+type);
                if(type== -1){//TYPE_NONE
                    mOnNetChangeLinstener.changed(NetTypeInfo.NETWORK_NO,false);
                }else if(type==ConnectivityManager.TYPE_WIFI){//1
                    mOnNetChangeLinstener.changed(NetTypeInfo.NETWORK_WIFI,true);
                }else if(type==NetUtils.NETWORK_2G){//2
                    mOnNetChangeLinstener.changed(NetTypeInfo.NETWORK_2G,true);
                }else if(type==NetUtils.NETWORK_3G){//3
                    mOnNetChangeLinstener.changed(NetTypeInfo.NETWORK_3G,true);
                }else if(type==NetUtils.NETWORK_4G){//4
                    mOnNetChangeLinstener.changed(NetTypeInfo.NETWORK_4G,true);
                }else if(type==ConnectivityManager.TYPE_ETHERNET){//9
                    mOnNetChangeLinstener.changed(NetTypeInfo.NETWORK_ETH,true);
                }else{
                    mOnNetChangeLinstener.changed(NetTypeInfo.NETWORK_UNKNOWN,true);
                }
            }
        }
    }
}
