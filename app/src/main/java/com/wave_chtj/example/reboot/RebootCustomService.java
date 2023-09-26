package com.wave_chtj.example.reboot;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.NotifyUtils;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.wave_chtj.example.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RebootCustomService extends Service {
    private static final String TAG = RebootCustomService.class.getSimpleName();
    Disposable disposable;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(98, NotifyUtils.getBuilder().build());
        NotifyUtils.setNotifyId(98)
                .setEnableCloseButton(false)//设置是否显示关闭按钮
                .setSmallIcon(R.mipmap.reboot)
                .setIvLogo(R.mipmap.reboot)
                .setIvStatus(false)
                .setIvNetStatus(false)
                .setRemarks("服务运行中...")
                .setDataTime(getSn())
                .setAppName(getString(R.string.app_name) + " " + AppsUtils.getAppVersionName())
                .exeuNotify();
        Log.d(TAG, "onCreate: ");
        startCycle();
    }

    /**
     * 获取设备SN号 请使用此唯一入口
     */
    public static String getSn() {
        try {
            return Build.VERSION.SDK_INT >= 30 ? Build.getSerial() : Build.SERIAL;
        } catch (Throwable throwable) {
            return "";
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public class TestBinder extends Binder {
        public RebootCustomService getService() {
            return RebootCustomService.this;
        }
    }

    public void startCycle() {
        int cycleTime = SPUtils.getInt("timeCycle", 30);
        Log.d(TAG, "startCycle: cycleTime=" + cycleTime);
        disposable = Observable
                .interval(cycleTime, cycleTime, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())//调用切换之前的线程。
                .observeOn(AndroidSchedulers.mainThread())//调用切换之后的线程。observeOn之后，不可再调用subscribeOn 切换线程
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        int count = SPUtils.getInt("rebootCount", 0) + 1;
                        Log.d(TAG, "accept: nowCount=" + count);
                        SPUtils.putInt("rebootCount", count);
                        ShellUtils.CommandResult result = ShellUtils.execCommand("reboot", true);
                        Log.d(TAG, "accept: result=" + result.result + ",errMeg=" + result.errorMsg + ",successMeg=" + result.successMsg);
                        try {
                            //调用系统接口进行重启
                            Intent intent = new Intent(Intent.ACTION_REBOOT);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            BaseIotUtils.getContext().startActivity(intent);
                        }catch (Throwable e){
                            Log.d(TAG, "accept: ",e);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: ", throwable);
                    }
                });
    }

    public void stopCycle() {
        Log.d(TAG, "stopCycle: ");
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    //通过binder实现调用者client与Service之间的通信
    private TestBinder binder = new TestBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
