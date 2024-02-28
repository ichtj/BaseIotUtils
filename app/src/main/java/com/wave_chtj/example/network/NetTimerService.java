package com.wave_chtj.example.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chtj.base_framework.FBaseTools;
import com.chtj.base_framework.network.FLteTools;
import com.face_chtj.base_iotutils.DeviceUtils;
import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.entity.DnsBean;
import com.wave_chtj.example.callback.INetTimerCallback;
import com.wave_chtj.example.entity.NetBean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NetTimerService extends Service {
    static Disposable disposable;
    static INetTimerCallback iNetTimerCallback;
    public NetTimerBinder netTimerBinder = new NetTimerBinder();
    public static final String KEY_ERRCOUNT = "errCount";
    public static final String KEY_SUCCCOUNT = "succCount";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_DNSLIST = "dnslist";
    public static final String SAVE_PATH = "/sdcard/DCIM/nettimer.log";
    public String[] defaultDns = new String[]{"47.106.129.104"};
    private static String[] pingDns;
    public static int DEFAULT_INTERVAL = 1000;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (iNetTimerCallback != null) {
                iNetTimerCallback.refreshNet((NetBean) msg.obj);
            }
        }
    };

    public void setPingDns(String[] pingDns) {
        this.pingDns = pingDns;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < pingDns.length; i++) {
            stringBuilder.append(pingDns[i] + ((i != pingDns.length - 1) ? "/" : ""));
        }
        SPUtils.putString(KEY_DNSLIST, stringBuilder.toString());
    }

    public String[] getPingDns() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < defaultDns.length; i++) {
            stringBuilder.append(defaultDns[i] + ((i != defaultDns.length - 1) ? "/" : ""));
        }
        return SPUtils.getString(KEY_DNSLIST, stringBuilder.toString()).split("/");
    }

    public void setiNetTimerCallback(INetTimerCallback iNetTimerCallback) {
        this.iNetTimerCallback = iNetTimerCallback;
    }

    class NetTimerBinder extends Binder {
        public NetTimerService getService() {
            return NetTimerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return netTimerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FLteTools.init();
        startNetCheck();
    }

    public void startNetCheck() {
        if (disposable == null) {
            KLog.d("startNetCheck() >> ");
            disposable = Observable.interval(0, SPUtils.getInt(KEY_INTERVAL, DEFAULT_INTERVAL), TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())//调用切换之前的线程。
                    .observeOn(Schedulers.io())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (pingDns == null || pingDns.length == 0) {
                                pingDns = defaultDns;
                            }
                            List<DnsBean> dnsBeanList = NetUtils.checkNetWork(pingDns);
                            boolean[] pingResult = new boolean[dnsBeanList.size()];
                            boolean netConnect = false;
                            for (int i = 0; i < dnsBeanList.size(); i++) {
                                boolean listItemResult = dnsBeanList.get(i).isPass;
                                if (listItemResult) {
                                    if (!netConnect) {
                                        netConnect = true;
                                    }
                                }
                                pingResult[i] = listItemResult;
                            }
                            putCount(netConnect);
                            String netTypeName = NetUtils.getNetWorkTypeName();
                            int netType = NetUtils.getNetWorkType();
                            String time = TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss");
                            boolean isNet4G = NetUtils.is4G();
                            String dbm = "0 dbm 0 asu";
                            String localIp="0.0.0.0";
                            if (netType==NetUtils.NETWORK_4G){
                                localIp=NetUtils.getLteIpAddress();
                                dbm = FLteTools.getDbm();
                                KLog.d("dbm>>"+dbm);
                            }else if(netType==NetUtils.NETWORK_WIFI){
                                localIp=NetUtils.getWifiIpAddress();
                            }else if(netType==NetUtils.NETWORK_ETH){
                                localIp=NetUtils.getEthIPv4Address();
                            }
                            NetBean netBean = new NetBean(pingDns, dbm, localIp, netTypeName, isNet4G, pingResult, netConnect);
                            handler.sendMessage(handler.obtainMessage(0x10, netBean));
                            FileUtils.writeFileData(SAVE_PATH, "\ntime：" + time + ", dns：" + Arrays.toString(pingDns) + ", netType：" + netType + ", isNet4G=" + NetUtils.is4G() + ", pingResult：" + Arrays.toString(pingResult) + ", dbm：" + dbm + ", localIp：" + localIp + ",netConnect：" + netConnect, false);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            KLog.d("accept() >> " + throwable.getMessage());
                            cancel();
                            startNetCheck();
                        }
                    });
        } else {
            ToastUtils.success("已开启");
        }
    }

    public void putCount(boolean pingResult) {
        if (pingResult) {
            SPUtils.putInt(KEY_SUCCCOUNT, SPUtils.getInt(KEY_SUCCCOUNT, 0) + 1);
        } else {
            SPUtils.putInt(KEY_ERRCOUNT, SPUtils.getInt(KEY_ERRCOUNT, 0) + 1);
        }
    }

    public int getErrCount() {
        return SPUtils.getInt(KEY_ERRCOUNT, 0);
    }

    public int getSuccCount() {
        return SPUtils.getInt(KEY_SUCCCOUNT, 0);
    }

    public void clearCount() {
        SPUtils.putInt(KEY_ERRCOUNT, 0);
        SPUtils.putInt(KEY_SUCCCOUNT, 0);
        ToastUtils.success("清除成功！");
    }

    public void cancel() {
        KLog.d("cancel() >> ");
        if (disposable != null) {
            try {
                disposable.dispose();
            } catch (Throwable e) {
                KLog.e("errMeg:" + e.getMessage());
            }
        }
        disposable = null;
        ToastUtils.success("关闭成功！");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FLteTools.cancel();
    }
}
