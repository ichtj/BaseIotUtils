package com.wave_chtj.example.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.wave_chtj.example.callback.INetTimerCallback;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NetTimerService extends Service {
    static Disposable disposable;
    static INetTimerCallback iNetTimerCallback;

    public static final String KEY_ERRCOUNT="errCount";
    public static final String KEY_SUCCCOUNT="succCount";

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
        return new NetTimerBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startNetCheck();
    }

    public void startNetCheck() {
        if(disposable==null){
            KLog.d("startNetCheck() >> ");
            disposable = Observable.interval(0, 5, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())//调用切换之前的线程。
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            boolean pingResult = NetUtils.reloadDnsPing();
                            if(pingResult){
                                SPUtils.putInt(KEY_SUCCCOUNT,SPUtils.getInt(KEY_SUCCCOUNT,0)+1);
                            }else{
                                SPUtils.putInt(KEY_ERRCOUNT,SPUtils.getInt(KEY_ERRCOUNT,0)+1);
                            }
                            String netType=NetUtils.getNetWorkTypeName();
                            String time= TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss");
                            boolean isNet4G=NetUtils.is4G();
                            KLog.d("accept() netType >> "+netType+",isNet4G >> "+isNet4G+", pingResult >> "+pingResult);
                            FileUtils.writeFileData("/sdcard/DCIM/nettimer.log","\ntime："+time+", netType："+netType+"isNet4G="+NetUtils.is4G()+", pingResult："+pingResult,false);
                            if(iNetTimerCallback!=null){
                                iNetTimerCallback.refreshNet(time,netType,isNet4G, pingResult);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            KLog.d("accept() >> "+throwable.getMessage());
                            cancel();
                            startNetCheck();
                        }
                    });
        }else{
            ToastUtils.success("已开启");
        }
    }

    public int getErrCount(){
        return SPUtils.getInt(KEY_ERRCOUNT,0);
    }

    public int getSuccCount(){
        return SPUtils.getInt(KEY_SUCCCOUNT,0);
    }

    public void cancel() {
        if (disposable != null) {
            KLog.d("cancel() >> ");
            try {
                disposable.dispose();
            }catch (Throwable e){
                e.printStackTrace();
                KLog.e("errMeg:"+e.getMessage());
            }
        }
        disposable = null;
        ToastUtils.success("关闭成功！");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
