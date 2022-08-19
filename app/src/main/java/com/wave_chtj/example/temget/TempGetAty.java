package com.wave_chtj.example.temget;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ShellUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.util.AppManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TempGetAty extends BaseActivity {
    private static final String TAG = TempGetAty.class.getSimpleName();
    TextView tvTemp1;
    TextView tvTemp2;
    TextView tvTemp3;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_get);
        tvTemp1=findViewById(R.id.tvTemp1);
        tvTemp2=findViewById(R.id.tvTemp2);
        tvTemp3=findViewById(R.id.tvTemp3);
        getTemp();
    }
    public void getTemp(){
        Observable
                .interval(0, 30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())//调用切换之前的线程。
                .observeOn(AndroidSchedulers.mainThread())//调用切换之后的线程。observeOn之后，不可再调用subscribeOn 切换线程
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        ShellUtils.CommandResult getTemp1=ShellUtils.execCommand("cat /sys/class/thermal/thermal_zone0/temp",true);
                        KLog.d(TAG,"accept:>getTemp1.err="+getTemp1.errorMsg+",result="+getTemp1.result+",success="+getTemp1.successMsg);
                        tvTemp1.setText(((getTemp1.result==0)?"CPU温度获取成功：":"CPU温度获取失败：")+getTemp1.successMsg);
                        ShellUtils.CommandResult getTemp2=ShellUtils.execCommand("cat /sys/class/aht21/TempHum/temp",true);
                        KLog.d(TAG,"accept:>getTemp2.err="+getTemp2.errorMsg+",result="+getTemp2.result+",success="+getTemp2.successMsg);
                        tvTemp2.setText(((getTemp2.result==0)?"环境温度获取成功：":"环境温度获取失败：")+getTemp2.successMsg);
                        ShellUtils.CommandResult getTemp3=ShellUtils.execCommand("cat /sys/class/aht21/TempHum/hum",true);
                        KLog.d(TAG,"accept:>getTemp3.err="+getTemp3.errorMsg+",result="+getTemp3.result+",success="+getTemp3.successMsg);
                        tvTemp3.setText(((getTemp3.result==0)?"环境湿度获取成功：":"环境湿度获取失败：")+getTemp3.successMsg);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: ", throwable);
                        getTemp();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
