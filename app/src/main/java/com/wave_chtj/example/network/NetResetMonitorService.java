package com.wave_chtj.example.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chtj.base_framework.network.NetDbmListener;
import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.wave_chtj.example.R;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NetResetMonitorService extends Service {
    private static final String TAG = NetResetMonitorService.class.getSimpleName();
    public static final String ACTION_NET_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private Disposable disposable;
    /**
     * 任务每分钟执行一次
     */
    private int initialDelay = 1*60;
    private NetMonitorCallBack netMonitorCallBack;
    public final static String NET_LOG_RECORD_PATH = "/sdcard/Documents/";
    public String nowFilePath;

    public String KEY_SAVE_ERR_COUNT = "netMonitorErrCount";
    public String KEY_RESET_MOED = "netResetMode";
    public String KEY_TOTAL_COUNT = "totalCount";

    public String dbmInfo=0 + " dBm " + 0 + " asu";
    /**
     * 0为硬复位
     * 1为软复位
     * 2为飞行模式
     */
    private static final int DEFAULT_RESET_MODE = 0;

    /**
     * 0为无限次
     * 1为1次
     */
    private static final int DEFAULT_CYCLES_COUNT = 0;

    public void setNetMonitorCallBack(NetMonitorCallBack netMonitorCallBack) {
        this.netMonitorCallBack = netMonitorCallBack;
    }

    private final static int FLAG_GET_TIME = 0x100;
    private final static int FLAG_PING_RESULT = 0x101;
    private final static int FLAG_PING_LIST = 0x102;
    private final static int FLAG_ERR_COUNT = 0x103;
    private final static int FLAG_GET_NETTYPE = 0x104;
    private final static int FLAG_LOAD_DATA = 0x105;
    private final static int FLAG_TOTAL_COUNT = 0x106;
    private final static int FLAG_GET_LTE_DBM = 0x107;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case FLAG_GET_TIME:
                    if (netMonitorCallBack != null) {
                        netMonitorCallBack.getNowTime(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
                    }
                    break;
                case FLAG_PING_RESULT:
                    if (netMonitorCallBack != null) {
                        netMonitorCallBack.getPingResult((Boolean) msg.obj);
                    }
                    break;
                case FLAG_PING_LIST:
                    if (netMonitorCallBack != null) {
                        netMonitorCallBack.getPingList((String[]) msg.obj);
                    }
                    break;
                case FLAG_ERR_COUNT:
                    if (netMonitorCallBack != null) {
                        netMonitorCallBack.getResetErrCount((Integer) msg.obj);
                    }
                    break;
                case FLAG_GET_NETTYPE:
                    if (netMonitorCallBack != null) {
                        netMonitorCallBack.getNetType((String) msg.obj);
                    }
                    break;
                case FLAG_TOTAL_COUNT:
                    if (netMonitorCallBack != null) {
                        netMonitorCallBack.getTotalCount((Integer) msg.obj);
                    }
                    break;
                case FLAG_GET_LTE_DBM:
                    if (netMonitorCallBack != null) {
                        netMonitorCallBack.getDbm((String) msg.obj);
                    }
                    break;
                case FLAG_LOAD_DATA:
                    getDataCallBack();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new NetMonitorBinder();
    }

    public class NetMonitorBinder extends Binder {
        public NetResetMonitorService getService() {
            return NetResetMonitorService.this;
        }
    }

    public void setCyclesCount(int count) {
        SPUtils.putInt("cyclesCount", count);
    }

    public int getCyclesCount() {
        return SPUtils.getInt("cyclesCount", DEFAULT_CYCLES_COUNT);
    }

    public void setErrCount(int count) {
        SPUtils.putInt(KEY_SAVE_ERR_COUNT, count);
    }

    public int getErrCount() {
        return SPUtils.getInt(KEY_SAVE_ERR_COUNT, 0);
    }

    public void setTotalCount(int count) {
        SPUtils.putInt(KEY_TOTAL_COUNT, count);
    }

    public int getTotalCount() {
        return SPUtils.getInt(KEY_TOTAL_COUNT, 0);
    }

    public void getDataCallBack() {
        KLog.d(TAG, "getDataCallBack ");
        if (netMonitorCallBack == null) {
            return;
        }
        netMonitorCallBack.getNetType(NetUtils.getNetWorkTypeName());
        String[] pingList = NetMonitorUtils.getPingDns(2, NetMonitorUtils.DNS_LIST);
        boolean isPing = NetMonitorUtils.checkNetWork(pingList, 2, 1);
        netMonitorCallBack.getPingResult(isPing);
        netMonitorCallBack.getPingList(pingList);
        int errCount = getErrCount();
        netMonitorCallBack.getResetErrCount(errCount);
        int totalClount = getTotalCount();
        netMonitorCallBack.getTotalCount(totalClount);
        netMonitorCallBack.getDbm(dbmInfo);
        netMonitorCallBack.getNowTime(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
    }


    @Override
    public void onCreate() {
        super.onCreate();
        resetTaskRestart();
        FLteTools.instance().init4GDbm(new NetDbmListener() {
            @Override
            public void getDbm(String dbmAsu) {
                dbmInfo=dbmAsu;
            }
        });
    }

    /**
     * 设置当前复位模式
     *
     * @param mode
     */
    public void setResetMode(int mode) {
        SPUtils.putInt(KEY_RESET_MOED, mode);
    }

    /**
     * 获取当前复位模式
     */
    public String getResetMode() {
        int resetMode = SPUtils.getInt(KEY_RESET_MOED, DEFAULT_RESET_MODE);
        String resetModeStr = getString(R.string.net_reset_hard);
        if (resetMode == 0) {
            resetModeStr = getString(R.string.net_reset_hard);
        }else if (resetMode == 1) {
            resetModeStr = getString(R.string.net_reset_soft);
        }else{
            resetModeStr = getString(R.string.airplane_mode);
        }
        return resetModeStr;
    }

    /**
     * 获取当前复位模式
     */
    public int getResetModeValue() {
        return SPUtils.getInt(KEY_RESET_MOED, DEFAULT_RESET_MODE);
    }

    /**
     * 重置之后重启任务
     */
    public void resetTaskRestart() {
        File file = new File(NET_LOG_RECORD_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        nowFilePath = NET_LOG_RECORD_PATH + TimeUtils.getTodayDateHms("yyyyMMddHHmmss") + ".log";
        File file1 = new File(nowFilePath);
        if (!file1.exists()) {
            try {
                file1.createNewFile();
            } catch (Throwable e) {
                KLog.e(TAG, "resetTaskRestart err=" + e.getMessage());
            }
        }
        try {
            unregisterReceiver(mReceiver);
        } catch (Throwable e) {
        }
        closeDisposable();
        try {
            registerDynamicReceiver();
        } catch (Throwable e) {
        }
        startTask();
    }

    /**
     * 重启服务并回调部分数据
     */
    public void setModeRestartCallBack(int mode) {
        setResetMode(mode);
        resetTaskRestart();
        handler.sendEmptyMessageDelayed(FLAG_LOAD_DATA, 2000);
    }

    /**
     * 注册网络变化监听
     */
    private void registerDynamicReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NET_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    /**
     * 开启任务
     */
    public void startTask() {
        KLog.d(TAG, "startTask ");
        if (disposable == null) {
            disposable = Observable
                    .interval(initialDelay, initialDelay, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())//调用切换之前的线程。
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            //String nowTime = TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss");
                            if (TextUtils.isEmpty(nowFilePath)) {
                                nowFilePath = NET_LOG_RECORD_PATH + TimeUtils.getTodayDateHms("yyyyMMddHHmmss") + ".log";
                            }
                            if (aLong != 0 && aLong % 5 == 0) {
                                int resetMode=getResetModeValue();
                                String recordSetMode="";
                                if ( resetMode== 0||resetMode==1) {
                                    ShellUtils.CommandResult commandResult2 = ShellUtils.execCommand(NetMonitorUtils.CMD_STOP_RILL, true);
                                    KLog.d(TAG, "accept commandResult2=" + commandResult2.result + ",errMeg=" + commandResult2.errorMsg);

                                    FileUtils.writeFileData(nowFilePath, "停止Rill服务 stop ril-daemon stopRillTime="+TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss")+"\n", false);
                                    Thread.sleep(20000);

                                    if(resetMode==0){
                                        ShellUtils.CommandResult commandResult0 = ShellUtils.execCommand(NetMonitorUtils.CMD_HARD_RESET, true);
                                        KLog.d(TAG, "accept commandResult0=" + commandResult0.result + ",errMeg=" + commandResult0.errorMsg);
                                        recordSetMode="硬复位指令 写入-->"+ TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + ",aLong=" + aLong + "\n";
                                    }else{
                                        ShellUtils.CommandResult commandResult1 = ShellUtils.execCommand(NetMonitorUtils.CMD_SOFT_RESET, true);
                                        KLog.d(TAG, "accept commandResult1=" + commandResult1.result + ",errMeg=" + commandResult1.errorMsg);
                                        recordSetMode="软复位指令 写入-->"+ TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + ",aLong=" + aLong + "\n";
                                    }
                                    FileUtils.writeFileData(nowFilePath, recordSetMode, false);

                                    ShellUtils.CommandResult commandResult3 = ShellUtils.execCommand(NetMonitorUtils.CMD_START_RILL, true);
                                    KLog.d(TAG, "accept commandResult3=" + commandResult3.result + ",errMeg=" + commandResult3.errorMsg);
                                    FileUtils.writeFileData(nowFilePath, "启动Rill服务 start ril-daemon startRillTime="+TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss")+"\n", false);
                                }else{
                                    recordSetMode="飞行模式 开启后关闭-->"+ TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + ",aLong=" + aLong + "\n";
                                    //开启飞行模式
                                    ShellUtils.CommandResult commandResult = ShellUtils.execCommand(NetMonitorUtils.CMD_ARIPLANEMODE_ON, true);
                                    KLog.d(TAG, "accept commandResult=" + commandResult.result + ",errMeg=" + commandResult.errorMsg);

                                    Thread.sleep(2000);

                                    //关闭飞行模式
                                    ShellUtils.CommandResult commandResult1 = ShellUtils.execCommand(NetMonitorUtils.CMD_ARIPLANEMODE_OFF, true);
                                    KLog.d(TAG, "accept commandResult1=" + commandResult1.result + ",errMeg=" + commandResult1.errorMsg);

                                    FileUtils.writeFileData(nowFilePath, recordSetMode, false);
                                }
                            }
                            // 789次都进行ping操作  并且判断结果是否正常
                            //如果有一次正常 那么
                            boolean isContinue = (aLong - 2) % 5 == 0 || (aLong - 3) % 5 == 0 || (aLong - 4) % 5 == 0;
                            if (aLong != 0 && aLong != 1 && aLong != 2 && aLong != 3 && aLong != 4 && isContinue) {
                                String[] pingList = NetMonitorUtils.getPingDns(2, NetMonitorUtils.DNS_LIST);
                                boolean isPing = NetMonitorUtils.checkNetWork(pingList, 2, 1);
                                int errCount = getErrCount();
                                int totalCount = getTotalCount();
                                if ((aLong - 3) % 5 == 0) {
                                    totalCount++;
                                    setTotalCount(totalCount);
                                    if (!isPing) {
                                        errCount++;
                                        setErrCount(errCount);
                                    }
                                }
                                handler.sendMessage(handler.obtainMessage(FLAG_TOTAL_COUNT, totalCount));
                                handler.sendMessage(handler.obtainMessage(FLAG_PING_RESULT, isPing));
                                handler.sendMessage(handler.obtainMessage(FLAG_PING_LIST, pingList));
                                handler.sendMessage(handler.obtainMessage(FLAG_ERR_COUNT, errCount));
                                handler.sendMessage(handler.obtainMessage(FLAG_GET_TIME, TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss")));
                                handler.sendMessage(handler.obtainMessage(FLAG_GET_LTE_DBM, dbmInfo));
                                String netType = NetUtils.getNetWorkTypeName();
                                handler.sendMessage(handler.obtainMessage(FLAG_GET_NETTYPE, netType));
                                int cyclesCount = getCyclesCount();
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("new Line----------aLong=" + aLong + "---------->");
                                stringBuilder.append("nowTime：[" + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + "]\n");
                                stringBuilder.append("pingList：[" + Arrays.toString(pingList) + "]\n");
                                stringBuilder.append("isPing：[>>>>>>>>" + isPing + "<<<<<<<<]\n");
                                stringBuilder.append("netType：[" + netType + "]\n");
                                stringBuilder.append("dBm：[" + dbmInfo + "]\n");
                                stringBuilder.append("resetMode：[" + getResetMode() + "]\n");
                                stringBuilder.append("errCount：[" + errCount + "]\n");
                                stringBuilder.append("totalCount：[" + totalCount + "]\n");
                                stringBuilder.append("------"+ (cyclesCount == 0 ? "继续执行" : "已结束") +"cyclesCount=[ "+cyclesCount+" ]\n");

                                FileUtils.writeFileData(nowFilePath, stringBuilder.toString(), false);
                                if (!isPing) {
                                    if (cyclesCount == 1) {
                                        KLog.d(TAG, "accept task end cyclesCount==1");
                                        closeDisposable();
                                    } else {
                                        KLog.d(TAG, "accept task cyclesCount==more");
                                    }
                                }
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            KLog.d(TAG, "task err=" + throwable.getMessage() + ",restart net check");
                            resetTaskRestart();
                        }
                    });
        }
    }

    /**
     * 关闭任务
     */
    public void closeDisposable() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_NET_CHANGE.equals(action)) {//网络变化
                if (netMonitorCallBack != null) {
                    String netType = NetUtils.getNetWorkTypeName();
                    boolean isPing = NetMonitorUtils.checkNetWork(NetMonitorUtils.getPingDns(2, NetMonitorUtils.DNS_LIST), 2, 1);
                    KLog.d(TAG, "action==> " + action + ", netType==> " + netType + ",isPing==> " + isPing);
                    handler.sendMessage(handler.obtainMessage(FLAG_GET_NETTYPE, netType));
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
