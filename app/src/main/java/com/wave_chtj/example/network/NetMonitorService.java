package com.wave_chtj.example.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.chtj.base_framework.network.FLteTools;
import com.chtj.base_framework.network.NetDbmListener;
import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.convert.TimeUtils;
import com.face_chtj.base_iotutils.convert.TypeDataUtils;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.wave_chtj.example.R;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NetMonitorService extends Service {
    private static final String TAG = NetMonitorService.class.getSimpleName();
    private Disposable iDis;
    private int initialDelay = 1 * 60;//任务每分钟执行一次
    private static final int RESET_CYCLE = 4;//重置周期为多少分钟
    private INetMonitor nCallback;
    private String nowFilePath;
    private String dbmInfo = 0 + " dBm " + 0 + " asu";
    private int nowCount=0;
    //默认检测模式
    private static final int DEFAULT_RESET_MODE = NetMtools.MODE_HARD;
    //默认循环
    private static final int DEFAULT_CYCLES_BREAK = 1;//0为无限次 1为1次
    private static boolean DEFAULT_TIMERD_ACHIEVE = false;//每15分钟到达时是否重启

    public void setInetMonitor(INetMonitor nCallback) {
        this.nCallback = nCallback;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new NetBinder();
    }

    public class NetBinder extends Binder {
        public NetMonitorService getService() {
            return NetMonitorService.this;
        }
    }

    public void setDefaultTimerdAchieve(boolean isEnable) {
        SPUtils.putBoolean(NetMtools.KEY_TIME_ACHIEVE, isEnable);
    }

    public boolean getTimerdAchieve() {
        return SPUtils.getBoolean(NetMtools.KEY_TIME_ACHIEVE, DEFAULT_TIMERD_ACHIEVE);
    }

    public void setCyclesCount(int count) {
        SPUtils.putInt(NetMtools.KEY_CYCLES_BREAK, count);
    }

    public int getCyclesCount() {
        return SPUtils.getInt(NetMtools.KEY_CYCLES_BREAK, DEFAULT_CYCLES_BREAK);
    }

    public void setErrCount(int count) {
        SPUtils.putInt(NetMtools.KEY_ERR_COUNT, count);
    }

    public int getErrCount() {
        return SPUtils.getInt(NetMtools.KEY_ERR_COUNT, 0);
    }

    public void setTotalCount(int count) {
        SPUtils.putInt(NetMtools.KEY_TOTAL_COUNT, count);
    }

    public int getTotalCount() {
        return SPUtils.getInt(NetMtools.KEY_TOTAL_COUNT, 0);
    }

    public void getDataCallBack() {
        if (nCallback == null) {
            return;
        }
        String[] pingList = TypeDataUtils.getRandomList(NetUtils.getDnsTable(),3);
        boolean isNetPing = NetUtils.checkNetWork(pingList, 1, 1);
        nCallback.getNetType(NetUtils.getNetWorkTypeName(), isNetPing);
        nCallback.getPingList(pingList);
        int errCount = getErrCount();
        nCallback.getResetErrCount(errCount);
        int totalClount = getTotalCount();
        nCallback.getTotalCount(totalClount);
        nCallback.getDbm(dbmInfo);
        nCallback.getNowTime(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        nCallback.taskStatus(true);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        KLog.d("onCreate() >> ");
        setModeRestartCallBack(getResetModeValue());
        FLteTools.instance().init4GDbm(new NetDbmListener() {
            @Override
            public void getDbm(String dbmAsu) {
                dbmInfo = dbmAsu;
            }
        });
    }

    /**
     * 设置当前复位模式
     */
    public void setResetMode(int mode) {
        SPUtils.putInt(NetMtools.KEY_RESET_MOED, mode);
    }

    /**
     * 获取当前复位模式
     */
    public String getResetMode() {
        @IResetModel int resetMode = SPUtils.getInt(NetMtools.KEY_RESET_MOED, DEFAULT_RESET_MODE);
        String resetModeStr = getString(R.string.net_reset_hard);
        switch (resetMode) {
            case NetMtools.MODE_HARD:
                resetModeStr = getString(R.string.net_reset_hard);
                break;
            case NetMtools.MODE_SOFT:
                resetModeStr = getString(R.string.net_reset_soft);
                break;
            case NetMtools.MODE_AIRPLANE:
                resetModeStr = getString(R.string.airplane_mode);
                break;
            default:
                resetModeStr = getString(R.string.reset_model_reboot);
                break;
        }
        return resetModeStr;
    }

    /**
     * 获取当前复位模式
     */
    @IResetModel
    public int getResetModeValue() {
        return SPUtils.getInt(NetMtools.KEY_RESET_MOED, DEFAULT_RESET_MODE);
    }

    /**
     * 重置之后重启任务
     */
    public void resetTaskRestart() {
        File file = new File(NetMtools.LOG_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        nowFilePath = NetMtools.LOG_PATH + TimeUtils.getTodayDateHms("yyyyMMddHHmmss") + ".log";
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
        try {
            closeDisposable();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(mReceiver, filter);
        } catch (Throwable e) {
        }
        FileUtils.writeFileData(nowFilePath, "now start task >> " + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + "\n", false);
        startTask();
    }

    /**
     * 重启服务并回调部分数据
     */
    public void setModeRestartCallBack(int mode) {
        setResetMode(mode);
        resetTaskRestart();
        getDataCallBack();
    }


    /**
     * 开启任务
     */
    public void startTask() {
        nowCount=0;
        if (iDis == null) {
            iDis = Observable
                    .interval(initialDelay, initialDelay, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())//调用切换之前的线程。
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (TextUtils.isEmpty(nowFilePath)) {
                                nowFilePath = NetMtools.LOG_PATH + TimeUtils.getTodayDateHms("yyyyMMddHHmmss") + ".log";
                            }
                            int resetMode = getResetModeValue();
                            if (nowCount % RESET_CYCLE == 0) {
                                FileUtils.writeFileData(nowFilePath, "--->Arrived at reset time >> "+TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss")+"\n", false);
                                String recordSetMode = "";
                                switch (resetMode) {
                                    case NetMtools.MODE_HARD:
                                    case NetMtools.MODE_SOFT:
                                        ShellUtils.CommandResult commandResult2 = ShellUtils.execCommand(NetMtools.CMD_STOP_RILL, true);
                                        KLog.d(TAG, "accept commandResult2=" + commandResult2.result + ",errMeg=" + commandResult2.errorMsg);

                                        FileUtils.writeFileData(nowFilePath, "--->停止Rill服务 stop ril-daemon stopRillTime=" + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + "\n", false);
                                        Thread.sleep(20000);

                                        if (resetMode == NetMtools.MODE_HARD) {
                                            ShellUtils.CommandResult commandResult0 = ShellUtils.execCommand(NetMtools.CMD_HARD_RESET, true);
                                            KLog.d(TAG, "accept commandResult0=" + commandResult0.result + ",errMeg=" + commandResult0.errorMsg);
                                            recordSetMode = "--->hard write-->" + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + ",nowCount=" + nowCount + "\n";
                                        } else {
                                            ShellUtils.CommandResult commandResult1 = ShellUtils.execCommand(NetMtools.CMD_SOFT_RESET, true);
                                            KLog.d(TAG, "accept commandResult1=" + commandResult1.result + ",errMeg=" + commandResult1.errorMsg);
                                            recordSetMode = "--->soft write-->" + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + ",nowCount=" + nowCount + "\n";
                                        }
                                        FileUtils.writeFileData(nowFilePath, recordSetMode, false);

                                        ShellUtils.CommandResult commandResult3 = ShellUtils.execCommand(NetMtools.CMD_START_RILL, true);
                                        KLog.d(TAG, "accept commandResult3=" + commandResult3.result + ",errMeg=" + commandResult3.errorMsg);
                                        FileUtils.writeFileData(nowFilePath, "--->start ril-daemon startRillTime=" + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + "\n", false);
                                        break;
                                    case NetMtools.MODE_AIRPLANE:
                                        recordSetMode = "--->airplane mode turn on and turn off-->" + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss") + ",nowCount=" + nowCount + "\n";
                                        //开启飞行模式
                                        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(NetMtools.CMD_ARIPLANEMODE_ON, true);
                                        KLog.d(TAG, "accept commandResult=" + commandResult.result + ",errMeg=" + commandResult.errorMsg);

                                        Thread.sleep(2000);

                                        //关闭飞行模式
                                        ShellUtils.CommandResult commandResult1 = ShellUtils.execCommand(NetMtools.CMD_ARIPLANEMODE_OFF, true);
                                        KLog.d(TAG, "accept commandResult1=" + commandResult1.result + ",errMeg=" + commandResult1.errorMsg);

                                        FileUtils.writeFileData(nowFilePath, recordSetMode, false);
                                        break;

                                }
                            }
                            String[] pingList = TypeDataUtils.getRandomList(NetUtils.getDnsTable(),3);
                            boolean isNetPing = NetUtils.checkNetWork(pingList, 1, 1);
                            //判断模块重置的下一次执行之前 判断网络是否成功
                            long converNum = nowCount + 1;
                            boolean isContinue = nowCount != 0 && converNum != 0 && converNum % RESET_CYCLE == 0;
                            KLog.d("accept() isContinue >> " + isContinue + " nowCount >> " + nowCount + " convert >> " + converNum);
                            if (isContinue) {
                                int errCount = getErrCount();
                                int totalCount = getTotalCount();
                                totalCount++;
                                setTotalCount(totalCount);
                                if (!isNetPing) {
                                    errCount++;
                                    setErrCount(errCount);
                                }
                                String netType = NetUtils.getNetWorkTypeName();
                                if (nCallback != null) {
                                    nCallback.getTotalCount(totalCount);
                                    nCallback.getPingList(pingList);
                                    nCallback.getResetErrCount(errCount);
                                    nCallback.getNowTime(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
                                    nCallback.getDbm(dbmInfo);
                                    nCallback.getNetType(netType, isNetPing);
                                }
                                int cyclesCount = getCyclesCount();
                                StringBuilder sbstr = new StringBuilder();
                                sbstr.append("\nrecord----------status nowTime：[" + TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss")+"]\n");
                                sbstr.append("<---pingList：[" + Arrays.toString(pingList) + "]\n");
                                sbstr.append("<---isPing：[>>>>>>>>" + isNetPing + "<<<<<<<<]\n");
                                sbstr.append("<---netType：[" + netType + "]\n");
                                sbstr.append("<---dBm：[" + dbmInfo + "]\n");
                                sbstr.append("<---resetMode：[" + getResetMode() + "]\n");
                                sbstr.append("<---errCount：[" + errCount + "]\n");
                                sbstr.append("<---totalCount：[" + totalCount + "]\n");
                                //sbstr.append("------" + (cyclesCount == 0 ? "继续执行" : "已结束") + "cyclesCount=[ " + cyclesCount + " ]\n");

                                FileUtils.writeFileData(nowFilePath, sbstr.toString(), false);
                                if (!isNetPing) {
                                    if (cyclesCount == 1) {
                                        KLog.d(TAG, "accept task end cyclesCount==1");
                                        FileUtils.writeFileData(nowFilePath,"任务将停止执行 >> 请检查网络相关日志情况\n",false);
                                        nCallback.taskStatus(false);
                                        closeDisposable();
                                    } else {
                                        KLog.d(TAG, "accept task cyclesCount==more");
                                    }
                                }
                            }
                            boolean isAdditional = nowCount != 0 && nowCount % 15 == 0 && getTimerdAchieve();
                            boolean isToRebootMode = nowCount != 0 && nowCount % 10 == 0;
                            if (resetMode == NetMtools.MODE_REBOOT) {
                                if (isToRebootMode) {
                                    KLog.d(TAG, "accept 10分钟到了 执行重启");
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("重启前检查网络------>>>\n");
                                    stringBuilder.append("pingList：[" + Arrays.toString(pingList) + "]\n");
                                    stringBuilder.append("isNetPing：[>>>>>>>>" + isNetPing + "<<<<<<<<]\n");
                                    stringBuilder.append(".......10分钟到了,执行重启.......");
                                    FileUtils.writeFileData(nowFilePath, stringBuilder.toString(), false);
                                    NetMtools.rebootSystem();
                                }
                            } else {
                                //重启模式不生效的时候默认启用 其他模式的附加重启判断
                                if (isAdditional) {
                                    KLog.d(TAG, "accept 15分钟到了 执行重启");
                                    FileUtils.writeFileData(nowFilePath, ".......15分钟到了,执行重启.......", false);
                                    NetMtools.rebootSystem();
                                }
                            }
                            nowCount++;
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
        if (iDis != null && !iDis.isDisposed()) {
            iDis.dispose();
            iDis = null;
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {//网络变化
                if (nCallback != null) {
                    String netType = NetUtils.getNetWorkTypeName();
                    String[] pingList = TypeDataUtils.getRandomList(NetUtils.getDnsTable(),3);
                    boolean isNetPing = NetUtils.checkNetWork(pingList, 1, 1);
                    KLog.d(TAG, "action==> " + action + ", netType==> " + netType + ",isNetPing==> " + isNetPing);
                    nCallback.getNetType(netType, isNetPing);
                }
            }
        }
    };
}
