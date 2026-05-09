package com.face_chtj.base_iotutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.face_chtj.base_iotutils.callback.INetChangeCallBack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 网络监听工具类。
 * 通过动态广播监听网络变化，并定时 ping 校验外网连通性。
 */
public class NetMonitorUtils {
    private static final String TAG = NetMonitorUtils.class.getSimpleName();
    public static final String ACTION_NET_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private static final int MSG_NET_CHANGE = 0x10;
    private static final int NET_INIT = 0;
    private static final int NET_SUCC = 1;
    private static final int NET_FAIL = 2;
    private static final int PING_TIMER_SECONDS = 60;

    private static volatile NetMonitorUtils sInstance;

    private final List<INetChangeCallBack> iNetList = new CopyOnWriteArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isRegistered = new AtomicBoolean(false);
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_NET_CHANGE) {
                dispatchCallback();
            }
        }
    };

    private volatile boolean pingResult = false;
    private volatile int nowType = NET_INIT;
    private NetChangeReceiver netReceiver;
    private Disposable disposable;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * add callback
     */
    public static void addCallBack(INetChangeCallBack iCallback) {
        if (iCallback == null) {
            return;
        }
        NetMonitorUtils instance = getInstance();
        if (!instance.iNetList.contains(iCallback)) {
            instance.iNetList.add(iCallback);
        }
    }

    /**
     * remove callback
     */
    public static void removeCallback(INetChangeCallBack iCallback) {
        if (iCallback == null) {
            return;
        }
        getInstance().iNetList.remove(iCallback);
    }

    /**
     * 非实时结果：定时检测或广播触发后才会更新。
     */
    public static boolean isPingResult() {
        return getInstance().pingResult;
    }

    /**
     * register some broadcast
     */
    public static synchronized void register() {
        NetMonitorUtils instance = getInstance();
        if (!instance.isRegistered.compareAndSet(false, true)) {
            return;
        }

        instance.ensureExecutor();
        instance.netReceiver = new NetChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NET_CHANGE);
        BaseIotUtils.getContext().registerReceiver(instance.netReceiver, filter);
        instance.startTask();
    }

    /**
     * 注销广播并停止定时任务。
     */
    public static synchronized void unRegister() {
        NetMonitorUtils instance = getInstance();
        if (!instance.isRegistered.compareAndSet(true, false)) {
            return;
        }

        if (instance.netReceiver != null) {
            try {
                BaseIotUtils.getContext().unregisterReceiver(instance.netReceiver);
            } catch (IllegalArgumentException e) {
                KLog.d(TAG + " receiver already unregistered: " + e.getMessage());
            }
            instance.netReceiver = null;
        }

        instance.stopTask();
        if (instance.executor != null) {
            instance.executor.shutdownNow();
        }
        instance.handler.removeCallbacksAndMessages(null);
        instance.iNetList.clear();
        instance.nowType = NET_INIT;
        instance.pingResult = false;
    }

    // 单例模式
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

    /**
     * Open the scheduled task detection network
     */
    private void startTask() {
        if (disposable != null && !disposable.isDisposed()) {
            return;
        }

        disposable = Observable
                .interval(0, PING_TIMER_SECONDS, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        receiverNetStatus();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        stopTask();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isRegistered.get()) {
                                    startTask();
                                }
                            }
                        }, 1000);
                    }
                });
    }

    /**
     * Stop the task of regularly checking whether the network is normal
     */
    private void stopTask() {
        KLog.d("closeDisposable");
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = null;
    }

    static class NetChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !ACTION_NET_CHANGE.equals(intent.getAction())) {
                return;
            }

            final NetMonitorUtils instance = getInstance();
            instance.ensureExecutor();
            instance.executor.execute(new Runnable() {
                @Override
                public void run() {
                    receiverNetStatus();
                }
            });
        }
    }

    /**
     * Determine network status
     */
    private static void receiverNetStatus() {
        NetMonitorUtils instance = getInstance();
        if (!instance.isRegistered.get()) {
            return;
        }
        if (!instance.isRunning.compareAndSet(false, true)) {
            return;
        }

        try {
            instance.pingResult = NetUtils.reloadDnsPing();
            if (instance.pingResult) {
                if (instance.nowType == NET_INIT || instance.nowType == NET_FAIL) {
                    instance.nowType = NET_SUCC;
                    instance.handler.sendEmptyMessage(MSG_NET_CHANGE);
                }
            } else {
                if (instance.nowType == NET_INIT || instance.nowType == NET_SUCC) {
                    instance.nowType = NET_FAIL;
                    instance.handler.sendEmptyMessage(MSG_NET_CHANGE);
                }
            }
        } finally {
            instance.isRunning.set(false);
        }
    }

    /**
     * all
     */
    private static void dispatchCallback() {
        NetMonitorUtils instance = getInstance();
        int netType = NetUtils.getNetWorkType();
        for (INetChangeCallBack callBack : instance.iNetList) {
            try {
                callBack.netChange(netType, instance.pingResult);
            } catch (Throwable e) {
                KLog.d(TAG + " dispatch callback error: " + e.getMessage());
            }
        }
    }

    private void ensureExecutor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
    }
}
