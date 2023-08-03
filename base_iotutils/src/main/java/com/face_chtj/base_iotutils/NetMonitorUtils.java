package com.face_chtj.base_iotutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.face_chtj.base_iotutils.callback.INetChangeCallBack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 网络监听工具类
 * 广播监听网络变化
 * 定时监听网络变化
 */
public class NetMonitorUtils {
    public static final String ACTION_NET_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private boolean pingResult = false;
    private NetChangeReceiver netReceiver;
    private int NET_INIT = 0;//0表示为初始状态
    private int NET_SUCC = 1;//1表示为网络正常
    private int NET_FAIL = 2;//2表示为网络错误
    private int nowType = NET_INIT;//当前网络状态值
    private final int PING_TIMER = 1 * 60;
    private Disposable disposable;
    private boolean isRunning=false;
    private static volatile NetMonitorUtils sInstance;
    private List<INetChangeCallBack> iNetList = new ArrayList<>();

    /**
     * add callback
     */
    public static void addCallBack(INetChangeCallBack iCallback) {
        if (!getInstance().iNetList.contains(iCallback)) {
            getInstance().iNetList.add(iCallback);
            //KLog.d("don't contains size >> " + getInstance().iNetList.size() + ", object >> " + iCallback);
        }
    }

    /**
     * remove callback
     */
    public static void removeCallback(INetChangeCallBack iCallback) {
        if (getInstance().iNetList.contains(iCallback)) {
            getInstance().iNetList.remove(iCallback);
            //KLog.d("contains size >> " + getInstance().iNetList.size() + ", object >> " + iCallback);
        }
    }

    /**
     * 非实时性的 因为网络检测断开或者重连的存在检测间隔
     */
    public static boolean isPingResult() {
        return getInstance().pingResult;
    }


    /**
     * Open the scheduled task detection network
     */
    private void startTask() {
        if (getInstance().disposable == null) {
            getInstance().disposable = Observable
                    .interval(0, getInstance().PING_TIMER, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())//调用切换之前的线程。
                    .observeOn(AndroidSchedulers.mainThread())//调用切换之后的线程。observeOn之后，不可再调用subscribeOn 切换线程
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            receiverNetStatus();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            stopTask();
                            startTask();
                        }
                    });
        }
    }

    /**
     * Stop the task of regularly checking whether the network is normal
     */
    private void stopTask() {
        KLog.d("closeDisposable");
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    /**
     * register some broadcast
     */
    public static void register() {
        getInstance().netReceiver = new NetChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NET_CHANGE);
        BaseIotUtils.getContext().registerReceiver(getInstance().netReceiver, filter);
        getInstance().startTask();
    }

    /**
     * 注销广播
     */
    public static void unRegister() {
        if (getInstance().netReceiver != null) {
            BaseIotUtils.getContext().unregisterReceiver(getInstance().netReceiver);
        }
        getInstance().stopTask();
        sInstance=null;
    }

    //单例模式
    private static NetMonitorUtils getInstance() {
        if (sInstance == null) {
            synchronized (NetMonitorUtils.class) {
                if (sInstance == null) {
                    sInstance = new NetMonitorUtils();
                }
            }
        }
        return sInstance;
    }

    static class NetChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_NET_CHANGE)) {
                receiverNetStatus();
            }
        }
    }

    /**
     * Determine network status
     */
    private static void receiverNetStatus() {
        if(!getInstance().isRunning){
            getInstance().isRunning=true;
            getInstance().pingResult = NetUtils.reloadDnsPing();
            if (getInstance().pingResult) {
                if (getInstance().nowType == getInstance().NET_INIT || getInstance().nowType == getInstance().NET_FAIL) {
                    getInstance().nowType = getInstance().NET_SUCC;
                    dispatchCallback();
                }
            } else {
                if (getInstance().nowType == getInstance().NET_INIT || getInstance().nowType == getInstance().NET_SUCC) {
                    getInstance().nowType = getInstance().NET_FAIL;
                    dispatchCallback();
                }
            }
            getInstance().isRunning=false;
        }
    }

    /**
     * all
     */
    private static void dispatchCallback() {
        int netType = NetUtils.getNetWorkType();
        if(getInstance().iNetList.size()>0){
            for (int i = 0; i < getInstance().iNetList.size(); i++) {
                getInstance().iNetList.get(i).netChange(netType, getInstance().pingResult);
            }
        }
    }
}
