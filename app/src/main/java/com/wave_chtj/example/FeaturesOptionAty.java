package com.wave_chtj.example;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;

import com.face_chtj.base_iotutils.SurfaceLoadDialog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.notify.OnNotifyLinstener;
import com.face_chtj.base_iotutils.notify.NotifyUtils;
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

import java.io.File;
import java.io.FileOutputStream;

import io.reactivex.functions.Consumer;


/**
 * 功能选择
 */
public class FeaturesOptionAty extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "FeaturesOptionAty";
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
                            ToastUtils.success("已通过权限");
                        } else {
                            // Oups permission denied
                            ToastUtils.error("未通过权限");
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
                    NotifyUtils.getInstance("111")
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
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(5000);
                            NotifyUtils.getInstance("111").setAppName("");
                            NotifyUtils.getInstance("111").setAppAbout("");
                            NotifyUtils.getInstance("111").setRemarks("");
                            NotifyUtils.getInstance("111").setPrompt("");
                            NotifyUtils.getInstance("111").setDataTime("");
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                }.start();
                break;
            case R.id.btn_notification_close://关闭notification
                NotifyUtils.getInstance("111").closeNotify();
                break;
            case R.id.btn_network://网络监听
                startActivity(new Intent(mContext, NetChangeAty.class));
                break;
            case R.id.btn_sysDialogShow://显示SystemDialog
                SurfaceLoadDialog.getInstance().show("hello world");
                break;
            case R.id.btn_sysDialogHide://关闭SystemDialog
                SurfaceLoadDialog.getInstance().dismiss();
                break;
            case R.id.btn_generalToast://普通吐司
                ToastUtils.showShort("Hello Worold!");
                break;
            case R.id.btn_showToast://图形化吐司
                ToastUtils.success("Hello Worold!");
                break;
            case R.id.btn_test_crash://测试anr
                stopService(new Intent(FeaturesOptionAty.this, MyService.class));
                startService(new Intent(FeaturesOptionAty.this, MyService.class));
                break;
            case R.id.btn_test_exception://测试其他异常
                int i = 1/0;
                break;
            case R.id.btn_gc_test://GC测试
                System.gc();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotifyUtils.getInstance("10").closeNotify();
        SurfaceLoadDialog.getInstance().dismiss();
    }
}
