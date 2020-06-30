package com.wave_chtj.example;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import com.face_chtj.base_iotutils.SurfaceLoadDialog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.notify.OnNotifyLinstener;
import com.face_chtj.base_iotutils.notify.NotifyUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wave_chtj.example.allapp.AllAppAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.crash.MyService;
import com.wave_chtj.example.download.DownLoadAty;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.greendao.GreenDaoSqliteAty;
import com.wave_chtj.example.network.NetChangeAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.keepservice.KeepServiceActivity;
import com.wave_chtj.example.socket.SocketAty;
import com.wave_chtj.example.util.excel.ExcelEntity;
import com.wave_chtj.example.util.excel.JXLExcelUtils;
import com.wave_chtj.example.util.excel.POIExcelUtils;
import com.wave_chtj.example.util.keyevent.IUsbDeviceListener;
import com.wave_chtj.example.util.keyevent.KeyEventUtils;
import java.util.List;
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
        setContentView(R.layout.activity_switch);
        mContext = FeaturesOptionAty.this;
        /**获取权限*/
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

    /*接收到刚才选择的文件路径*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                KLog.d(TAG, ": url=" + uri.getPath().toString());
            }
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
                //获取系统中是否已经通过 允许通知的权限
                if (NotifyUtils.notifyIsEnable()) {
                    NotifyUtils.getInstance("111")
                            .setEnableCloseButton(false)//设置是否显示关闭按钮
                            .setOnNotifyLinstener(new OnNotifyLinstener() {
                                @Override
                                public void enableStatus(boolean isEnable) {
                                    KLog.e(TAG, "isEnable=" + isEnable);
                                }
                            })
                            .setNotifyParam(R.drawable.app_img, R.drawable.app_img
                                    , "BaseIotUtils"
                                    , "工具类"
                                    , "文件压缩，文件下载，日志管理，时间管理，网络判断。。。"
                                    , "this is a library ..."
                                    , "2020-3-18"
                                    , false
                                    , false)
                            .exeuNotify();
                } else {
                    //去开启通知
                    NotifyUtils.toOpenNotify();
                }
                /*new Thread() {
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
                }.start();*/
                break;
            case R.id.btn_notification_close://关闭notification
                NotifyUtils.closeNotify();
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
                int i = 1 / 0;
                break;
            case R.id.btn_key_reg://usb设备监听注册
                KeyEventUtils.getInstance().registerReceiver();
                KeyEventUtils.getInstance().setIUsbDeviceListener(new IUsbDeviceListener() {
                    @Override
                    public void deviceInfo(UsbDevice device, boolean isConn) {
                        KLog.d(TAG, "device: " + device.getProductName());
                        KLog.d(TAG, "isConn: " + isConn);
                    }
                });
                break;
            case R.id.btn_key_unreg://usb设备监听注册
                KeyEventUtils.getInstance().unRegisterReceiver();
                break;
            case R.id.btn_sql://数据库操作
                startActivity(new Intent(mContext, GreenDaoSqliteAty.class));
                break;
            case R.id.btn_jxl_open://打开Excel JXL版本
                final Handler handler2 = new Handler();
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //第一种jxl.jar 只能读取xls
                            List<ExcelEntity> readExcelDatas=JXLExcelUtils.readExcelxlsx( Environment.getExternalStorageDirectory()+"/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                });
                break;
            case R.id.btn_jxl_export://导出Excel JXL版本
                //第一种 jxl.jar导出
                JXLExcelUtils.exportExcel();
                break;
            case R.id.btn_poi_open://打开Excel POI版本
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //poi.jar 可以读取xls xlsx 两种
                            List<ExcelEntity> readExcelDatas = POIExcelUtils.readExcel(Environment.getExternalStorageDirectory() + "/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                });
                break;
            case R.id.btn_poi_export://导出Excel POI版本
                //poi.jar导出
                boolean isOK = POIExcelUtils.createExcelFile();
                KLog.d(TAG, "isOK: " + isOK);
                break;
            case R.id.btn_all_app://系统应用详情
                startActivity(new Intent(mContext, AllAppAty.class));
                break;
            case R.id.btn_more://查看APP详情
                ToastUtils.success("敬请期待");
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotifyUtils.closeNotify();
        SurfaceLoadDialog.getInstance().dismiss();
        KeyEventUtils.getInstance().unRegisterReceiver();
    }
}
