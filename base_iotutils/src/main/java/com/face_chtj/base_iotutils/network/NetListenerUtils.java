package com.face_chtj.base_iotutils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.enums.NET_TYPE;
import com.face_chtj.base_iotutils.network.callback.INetChangeCallback;

/**
 * Create on 2020/1/3
 * author chtj
 * desc 网络变化广播
 */
public class NetListenerUtils extends BroadcastReceiver {
    private static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String TAG="NetListenerUtils";
    private INetChangeCallback mINetChangeCallback;
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
    public void setOnNetChangeLinstener(INetChangeCallback iNetChangeCallback) {
        mINetChangeCallback = iNetChangeCallback;
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
            if(mINetChangeCallback !=null){
                //获取当前网络类型
                int type=NetUtils.getNetWorkType();
                //判断网络是否连接正常，是否能够ping通
                boolean isPingSuccessful=NetUtils.ping(2,1);
                KLog.e(TAG,"type="+type);
                if(type== -1){//TYPE_NONE
                    mINetChangeCallback.changed(NET_TYPE.NETWORK_NO,isPingSuccessful);
                }else if(type==ConnectivityManager.TYPE_WIFI){//1
                    mINetChangeCallback.changed(NET_TYPE.NETWORK_WIFI,isPingSuccessful);
                }else if(type==NetUtils.NETWORK_2G){//2
                    mINetChangeCallback.changed(NET_TYPE.NETWORK_2G,isPingSuccessful);
                }else if(type==NetUtils.NETWORK_3G){//3
                    mINetChangeCallback.changed(NET_TYPE.NETWORK_3G,isPingSuccessful);
                }else if(type==NetUtils.NETWORK_4G){//4
                    mINetChangeCallback.changed(NET_TYPE.NETWORK_4G,isPingSuccessful);
                }else if(type==ConnectivityManager.TYPE_ETHERNET){//9
                    mINetChangeCallback.changed(NET_TYPE.NETWORK_ETH,isPingSuccessful);
                }else{
                    mINetChangeCallback.changed(NET_TYPE.NETWORK_UNKNOWN,isPingSuccessful);
                }
            }
        }
    }
}
