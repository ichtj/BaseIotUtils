package com.wave_chtj.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.chtj.base_iotutils.KLog;
import com.chtj.base_iotutils.PackagesUtils;
import com.chtj.base_iotutils.ServiceUtils;
import com.chtj.base_iotutils.SystemLoadDialog;
import com.chtj.base_iotutils.notify.INotifyLinstener;
import com.chtj.base_iotutils.notify.NotifyUtils;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.download.DownLoadAty;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.network.NetChangeAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.keepservice.KeepServiceActivity;
import com.wave_chtj.example.socket.SocketAty;
import com.wave_chtj.example.util.MD5;

import java.io.File;

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
        try{
            String strMd5=MD5.getFileMD5(new File("sdcard/3E96F92738EA6CFAEB9BB00DF1D7FAFB"));
            Log.e(TAG, "onCreate: "+strMd5);
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"errMeg:"+e.getMessage());
        }
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
            case R.id.btn_notification_open://notification display
                NotifyUtils.getInstance()
                        .setINotifyLinstener(new INotifyLinstener() {
                            @Override
                            public void enableStatus(boolean isEnable) {
                                KLog.e(TAG,"isEnable="+isEnable);
                            }
                        })
                        .setNotifyId(10)
                        .setNotifyParam(R.drawable.ic_launcher,R.drawable.app_img
                                ,"BaseIotUtils"
                                ,"工具类"
                                ,"文件压缩，文件下载，日志管理，时间管理，网络判断。。。"
                                ,"this is a library ..."
                                ,"2020-3-18"
                                ,false
                                ,true)
                        .exeuNotify();
                //NotifyUtils.getInstance().setAppName("");
                //NotifyUtils.getInstance().setAppAbout("");
                //NotifyUtils.getInstance().setRemarks("");
                //NotifyUtils.getInstance().setPrompt("");
                //NotifyUtils.getInstance().setDataTime("");
                break;
            case R.id.btn_notification_close://关闭notification
                NotifyUtils.getInstance().closeNotify();
                break;
            case R.id.btn_network://网络监听
                startActivity(new Intent(mContext, NetChangeAty.class));
                break;
            case R.id.btn_sysDialogShow://显示SystemDialog
                SystemLoadDialog.getInstance().show("hello world");
                break;
            case R.id.btn_sysDialogHide://关闭SystemDialog
                SystemLoadDialog.getInstance().dismiss();
                break;
            case R.id.btn_replaceContent://关闭SystemDialog
                SystemLoadDialog.getInstance().show("dsfsdfjlsd");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotifyUtils.getInstance().closeNotify();
        SystemLoadDialog.getInstance().dismiss();
    }
}
