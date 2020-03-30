package com.wave_chtj.example;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.chtj.base_iotutils.GlobalLoadDialog;
import com.chtj.base_iotutils.GraphicalToastUtils;
import com.chtj.base_iotutils.KLog;
import com.chtj.base_iotutils.ToastUtils;
import com.chtj.base_iotutils.notify.OnNotifyLinstener;
import com.chtj.base_iotutils.notify.NotifyUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.crash.MyService;
import com.wave_chtj.example.download.DownLoadAty;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.network.NetChangeAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.keepservice.KeepServiceActivity;
import com.wave_chtj.example.socket.SocketAty;

import io.reactivex.functions.Consumer;


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
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}).
                subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) { // Always true pre-M
                            // I can control the camera now
                            ToastUtils.showShort("已通过权限");
                        } else {
                            // Oups permission denied
                            ToastUtils.showShort("未通过权限");
                        }
                    }
                });
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
                if (NotifyUtils.notifyIsEnable()) {
                    NotifyUtils.getInstance("10")
                            .setOnNotifyLinstener(new OnNotifyLinstener() {
                                @Override
                                public void enableStatus(boolean isEnable) {
                                    KLog.e(TAG, "isEnable=" + isEnable);
                                }
                            })
                            .setNotifyParam(R.drawable.ic_launcher, R.drawable.app_img
                                    , "BaseIotUtils"
                                    , "工具类"
                                    , "文件压缩，文件下载，日志管理，时间管理，网络判断。。。"
                                    , "this is a library ..."
                                    , "2020-3-18"
                                    , false
                                    , true)
                            .exeuNotify();
                } else {
                    NotifyUtils.toOpenNotify();
                }
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try{
                            Thread.sleep(5000);
                            NotifyUtils.getInstance("10").setAppName("");
                            NotifyUtils.getInstance("10").setAppAbout("");
                            NotifyUtils.getInstance("10").setRemarks("");
                            NotifyUtils.getInstance("10").setPrompt("");
                            NotifyUtils.getInstance("10").setDataTime("");
                        }catch(Exception e){
                            e.printStackTrace();
                            KLog.e(TAG,"errMeg:"+e.getMessage());
                        }
                    }
                }.start();
                break;
            case R.id.btn_notification_close://关闭notification
                NotifyUtils.getInstance("10").closeNotify();
                break;
            case R.id.btn_network://网络监听
                startActivity(new Intent(mContext, NetChangeAty.class));
                break;
            case R.id.btn_sysDialogShow://显示SystemDialog
                GlobalLoadDialog.getInstance().show("hello world");
                break;
            case R.id.btn_sysDialogHide://关闭SystemDialog
                GlobalLoadDialog.getInstance().dismiss();
                break;
            case R.id.btn_showToast://关闭SystemDialog
                GraphicalToastUtils.success("Hello Worold!");
                break;
            case R.id.btn_test_crash://关闭SystemDialog
                stopService(new Intent(FeaturesOptionAty.this, MyService.class));
                startActivity(new Intent(FeaturesOptionAty.this,MyService.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotifyUtils.getInstance("10").closeNotify();
        GlobalLoadDialog.getInstance().dismiss();
    }
}
