package com.wave_chtj.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.chtj.base_iotutils.ShellUtils;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.download.DownLoadAty;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.keepservice.KeepServiceActivity;
import com.wave_chtj.example.socket.SocketAty;

/**
 * 功能选择
 */
public class FeaturesOptionAty extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "FeaturesOptionAty";
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme); //切换正常主题
        setContentView(R.layout.activity_switch);
        mContext = FeaturesOptionAty.this;
        new Thread(){
            @Override
            public void run() {
                super.run();
                ShellUtils.CommandResult result0=ShellUtils.execCommand("stop ril-daemon",true);
                if(result0.result==0){
                    Log.e(TAG,"执行成功0");
                }else{
                    Log.e(TAG,"errMeg0="+result0.errorMsg);
                }
                ShellUtils.CommandResult result=ShellUtils.execCommand("echo \"AT+CRESET\" > /dev/ttyUSB3",true);
                if(result.result==0){
                    Log.e(TAG,"执行成功1");
                }else{
                    Log.e(TAG,"errMeg1="+result.errorMsg);
                }
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSeialPortNormal://串口测试
                startActivity(new Intent(mContext, SerialPortAty.class));
                break;
            case R.id.btnServiceKeep://后台Service
                startActivity(new Intent(mContext, KeepServiceActivity.class));
                break;
            case R.id.btnScreen://屏幕适配相关
                startActivity(new Intent(mContext, ScreenActivity.class));
                break;
            case R.id.btn_write_read://文件读写
                startActivity(new Intent(mContext, FileOperatAty.class));
                break;
            case R.id.btn_download://文件下载
                startActivity(new Intent(mContext, DownLoadAty.class));
                break;
            case R.id.btn_socket://Socket Tcp/upd
                startActivity(new Intent(mContext, SocketAty.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
