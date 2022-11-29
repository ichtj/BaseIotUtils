package com.face_chtj.base_iotutils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.convert.TypeDataUtils;
import com.face_chtj.base_iotutils.network.callback.INetChangeCallback;

/**
 * Create on 2020/1/3
 * author chtj
 * desc 网络变化广播
 * 动态注册 无需将广播在AndroidManifest.xml中注册
 */
public class NetChangeMonitor extends BroadcastReceiver {
    private static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String TAG = NetChangeMonitor.class.getSimpleName();
    private INetChangeCallback mINetChangeCallback;
    private static NetChangeMonitor netChangeMonitor;

    //单例模式
    public static NetChangeMonitor instance() {
        if (netChangeMonitor == null) {
            synchronized (NetChangeMonitor.class) {
                if (netChangeMonitor == null) {
                    netChangeMonitor = new NetChangeMonitor();
                }
            }
        }
        return netChangeMonitor;
    }

    /**
     * 注册广播
     */
    public void registerReceiver(INetChangeCallback iNetChangeCallback) {
        this.mINetChangeCallback = iNetChangeCallback;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ANDROID_NET_CHANGE_ACTION);
        BaseIotUtils.getContext().registerReceiver(this, intentFilter);
    }

    /**
     * 销毁广播
     */
    public void unRegisterReceiver() {
        try {
            BaseIotUtils.getContext().unregisterReceiver(this);
        } catch (Throwable e) {
            e.printStackTrace();
            KLog.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        KLog.e(TAG, "action=" + action);
        if (action.equals(ANDROID_NET_CHANGE_ACTION)) {
            if (mINetChangeCallback != null) {
                //获取当前网络类型
                int type = NetUtils.getNetWorkType();
                //判断网络是否连接正常，是否能够ping通
                //KLog.e(TAG, "type=" + type);
                String [] pingList= TypeDataUtils.getRandomList(NetUtils.getDnsTable(),3);
                boolean isPing=NetUtils.checkNetWork(pingList,1, 1);
                //KLog.d("onReceive() isPing >> "+isPing+",dnsList >> "+ Arrays.toString(dns));
                mINetChangeCallback.changed(type, isPing);
            }
        }
    }
}
