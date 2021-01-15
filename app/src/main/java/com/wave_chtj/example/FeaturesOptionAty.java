package com.wave_chtj.example;

import android.Manifest;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.chtj.framework.FBaseTools;
import com.chtj.framework.FStorageTools;
import com.chtj.framework.entity.CommonValue;
import com.chtj.framework.network.FEthTools;
import com.chtj.framework.FScreentTools;
import com.chtj.framework.entity.IpConfigInfo;
import com.chtj.framework.network.FNetworkTools;
import com.face_chtj.base_iotutils.audio.PlayUtils;
import com.face_chtj.base_iotutils.threadpool.TPoolUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.face_chtj.base_iotutils.SurfaceLoadDialog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.notify.OnNotifyLinstener;
import com.face_chtj.base_iotutils.notify.NotifyUtils;
import com.wave_chtj.example.allapp.AllAppAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.crash.MyService;
import com.wave_chtj.example.download.DownLoadAty;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.greendao.GreenDaoSqliteAty;
import com.wave_chtj.example.keeplive.KeepLiveAty;
import com.wave_chtj.example.network.NetChangeAty;
import com.wave_chtj.example.play.VideoPlayAty;
import com.wave_chtj.example.playmedia.PlayMediaAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.socket.SocketAty;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.wave_chtj.example.timer.TimerAty;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.entity.ExcelEntity;
import com.wave_chtj.example.util.excel.JXLExcelUtils;
import com.wave_chtj.example.util.excel.POIExcelUtils;
import com.wave_chtj.example.util.keyevent.IUsbDeviceListener;
import com.wave_chtj.example.util.keyevent.KeyEventUtils;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * 功能选择
 */
public class FeaturesOptionAty extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "FeaturesOptionAty";
    private static final int FILE_SELECT_CODE = 10000;
    private TextView tvTruePath;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_re);
        context=FeaturesOptionAty.this;
        tvTruePath = findViewById(R.id.tvTruePath);
        /**获取权限*/
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.PACKAGE_USAGE_STATS,
                Manifest.permission.READ_PHONE_STATE,
        }).subscribe(new Consumer<Boolean>() {
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

        AppManager.getAppManager().finishActivity(StartPageAty.class);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSeialPortNormal://串口测试
                startActivity(new Intent(context, SerialPortAty.class));
                break;
            case R.id.btnScreen://屏幕适配相关
                startActivity(new Intent(context, ScreenActivity.class));
                break;
            case R.id.btn_write_read://文件读写
                startActivity(new Intent(context, FileOperatAty.class));
                break;
            case R.id.btn_download://文件下载
                startActivity(new Intent(context, DownLoadAty.class));
                break;
            case R.id.btn_socket://Socket Tcp/upd
                startActivity(new Intent(context, SocketAty.class));
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
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
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
                });
                break;
            case R.id.btn_notification_close://关闭notification
                NotifyUtils.closeNotify();
                break;
            case R.id.btn_network://网络监听
                startActivity(new Intent(context, NetChangeAty.class));
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
                stopService(new Intent(context, MyService.class));
                startService(new Intent(context, MyService.class));
                break;
            case R.id.btn_test_exception://测试其他异常
                int i = 1 / 0;
                break;
            case R.id.btn_key_reg://usb设备监听注册
                KeyEventUtils.getInstance().registerReceiver();
                KeyEventUtils.getInstance().setIUsbDeviceListener(new IUsbDeviceListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                startActivity(new Intent(context, GreenDaoSqliteAty.class));
                break;
            case R.id.btn_jxl_open://打开Excel JXL版本 table.xls 可以在项目的File文件夹下找到
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //第一种jxl.jar 只能读取xls
                            List<ExcelEntity> readExcelDatas = JXLExcelUtils.readExcelxlsx(Environment.getExternalStorageDirectory() + "/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                            ToastUtils.success("readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                            ToastUtils.success("read failed!");
                        }
                    }
                });
                break;
            case R.id.btn_jxl_export://导出Excel JXL版本
                //第一种 jxl.jar导出
                JXLExcelUtils.exportExcel();
                ToastUtils.success("export successful!");
                break;
            case R.id.btn_poi_open://打开Excel POI版本 table.xls 可以在项目的File文件夹下找到
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //poi.jar 可以读取xls xlsx 两种
                            List<ExcelEntity> readExcelDatas = POIExcelUtils.readExcel(Environment.getExternalStorageDirectory() + "/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                            ToastUtils.success("readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                            ToastUtils.success("read failed!");
                        }
                    }
                });
                break;
            case R.id.btn_poi_export://导出Excel POI版本
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        //poi.jar导出
                        boolean isOK = POIExcelUtils.createExcelFile();
                        KLog.d(TAG, "isOK: " + isOK);
                        ToastUtils.success("export successful!");
                    }
                });
                break;
            case R.id.btn_all_app://应用列表
                startActivity(new Intent(context, AllAppAty.class));
                break;
            case R.id.btn_play://视频播放
                startActivity(new Intent(context, VideoPlayAty.class));
                break;
            case R.id.btn_open_file://打开文件
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "请选择文件"), FILE_SELECT_CODE);
                break;
            case R.id.btn_getAssets://获取Assets目录下的文件
                try {
                    InputStream input = getAssets().open("table.xls");
                    if (input != null) {
                        ToastUtils.success("found table.xls");
                    } else {
                        ToastUtils.success("not found table.xls");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
                break;
            case R.id.btn_timereboot://定时器
                startActivity(new Intent(context, TimerAty.class));
                break;
            case R.id.btn_play_media://音频播放
                startActivity(new Intent(context, PlayMediaAty.class));
                break;
            case R.id.btn_set_ip://设置静态IP
                CommonValue commonValue = FEthTools.setStaticIp(new IpConfigInfo("192.168.1.155", "8.8.8.8", "8.8.4.4", "192.168.1.1", "255.255.255.0"));
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("静态IP设置成功！");
                } else {
                    ToastUtils.error("静态IP设置失败！errMeg=" + commonValue.getRemarks());
                }
                break;
            case R.id.btn_dhcp://设置动态IP
                CommonValue commonValue2 =FEthTools.setEthDhcp();
                if (commonValue2 == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("动态IP设置成功！");
                } else {
                    ToastUtils.error("动态IP设置失败！errMeg=" + commonValue2.getRemarks());
                }
                break;
            case R.id.btn_screent://截屏
                boolean isOk = FScreentTools.takeScreenshot("/sdcard/");
                if (isOk) {
                    ToastUtils.success("截屏成功,位置:/sdcard/目录下");
                } else {
                    ToastUtils.error("截屏失败！");
                }
                break;
            case R.id.btn_keeplive://保活Activity
                startActivity(new Intent(context, KeepLiveAty.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            // 用户未选择任何文件，直接返回
            ToastUtils.success("未选择任何文件!");
            return;
        }
        if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData(); // 获取用户选择文件的URI
            String filePath = UriPathUtils.getPath(uri);
            KLog.d(TAG, "filePath=" + filePath + ",uri.getPath()=" + uri.getPath());
            ToastUtils.success("文件地址:" + filePath);
            tvTruePath.setText("Uri转换后的真实路径：" + filePath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotifyUtils.closeNotify();
        SurfaceLoadDialog.getInstance().dismiss();
        KeyEventUtils.getInstance().unRegisterReceiver();
        PlayUtils.getInstance().stopPlaying();
    }
}
