package com.ichtj.basetools;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.chtj.base_framework.FScreentTools;
import com.chtj.base_framework.FStorageTools;
import com.chtj.base_framework.entity.CommonValue;
import com.chtj.base_framework.entity.IpConfigInfo;
import com.chtj.base_framework.entity.Space;
import com.chtj.base_framework.network.FEthTools;
import com.chtj.base_framework.network.FLteTools;
import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.AudioUtils;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.DeviceUtils;
import com.face_chtj.base_iotutils.GlobalDialogUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.NotifyUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.TPoolSingleUtils;
import com.face_chtj.base_iotutils.TPoolUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.face_chtj.base_iotutils.callback.INotifyStateCallback;
import com.face_chtj.base_iotutils.download.DownloadCallback;
import com.face_chtj.base_iotutils.download.DownloadManager;
import com.face_chtj.base_iotutils.download.DownloadStatus;
import com.ichtj.basetools.allapp.AllAppAty;
import com.ichtj.basetools.audio.AudioAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.bluetooth.BlueToothAty;
import com.ichtj.basetools.callback.IUsbHubListener;
import com.ichtj.basetools.crash.CrashTools;
import com.ichtj.basetools.crash.MyService;
import com.ichtj.basetools.dialog.DialogAty;
import com.ichtj.basetools.download.FileDownLoadAty;
import com.ichtj.basetools.entity.ExcelEntity;
import com.ichtj.basetools.file.FileOperatAty;
import com.ichtj.basetools.greendao.GreenDaoSqliteAty;
import com.ichtj.basetools.hid.HidMainDevAty;
import com.ichtj.basetools.hid.HidSubDevAty;
import com.ichtj.basetools.install.InstallAPkAty;
import com.ichtj.basetools.keeplive.KeepAliveAty;
import com.ichtj.basetools.network.NetChangeAty;
import com.ichtj.basetools.network.NetRecordAty;
import com.ichtj.basetools.nginx.NginxAty;
import com.ichtj.basetools.screen.ScreenActivity;
import com.ichtj.basetools.serialport.SerialPortAty;
import com.ichtj.basetools.sign.ApkSignSearchAty;
import com.ichtj.basetools.socket.SocketAty;
import com.ichtj.basetools.timer.TimerAty;
import com.ichtj.basetools.touch.TouchDetectAty;
import com.ichtj.basetools.util.CustomButtonGridView;
import com.ichtj.basetools.util.FKey;
import com.ichtj.basetools.util.JXLExcelUtils;
import com.ichtj.basetools.util.OptionTools;
import com.ichtj.basetools.util.PACKAGES;
import com.ichtj.basetools.util.POIExcelUtils;
import com.ichtj.basetools.util.TableFileUtils;
import com.ichtj.basetools.util.UsbHubTools;
import com.ichtj.basetools.video.PlayCacheVideoAty;
import com.ichtj.basetools.video.VideoPlayAty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Route(path = PACKAGES.BASE + "basetools")
public class MainActivity extends BaseActivity implements CustomButtonGridView.OnButtonClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private CustomButtonGridView customButtonGridView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_aty);
        Button button = findViewById(R.id.btnNext);
        button.setTextColor(ContextCompat.getColor(this, R.color.red));
        customButtonGridView = findViewById(R.id.customButtonGridView);
        customButtonGridView.setButtonMap(getDisplayBtn());
        customButtonGridView.setNumColumns(2); // 设置每列显示2个按钮
        customButtonGridView.setOnButtonClickListener(this);
//        DownloadManager downloadManager =new DownloadManager(this, new DownloadCallback() {
//            @Override
//            public void onDownloadStatusChanged(DownloadStatus status) {
//                Log.d(TAG, "onDownloadStatusChanged: "+status.toString());
//            }
//
//            @Override
//            public void onDownloadProgress(String url, int progress) {
//                Log.d(TAG, "onDownloadProgress: url>>"+url+",progress>>"+progress);
//            }
//        });
//        downloadManager.downloadFile("https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/test_file/ichtj_test/update.zip","/sdcard/testdownload/update.zip");
//        downloadManager.downloadFile("https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/test_file/ichtj_test/sabresd_6dq-ota-20211029155349.zip","/sdcard/testdownload/20211029155349.zip");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.zto.ztoexpresscabinet", "com.zto.ztoexpresscabinet.business.view.MainActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public Map<Integer, String> getDisplayBtn() {
        Map<Integer, String> btnList = new HashMap<>();
        Space ramSpace = null;
        try {
            ramSpace = FStorageTools.getRamSpace(FStorageTools.TYPE_MB);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        Space sdSpace = null;
        try {
            sdSpace = FStorageTools.getSdcardSpace(FStorageTools.TYPE_MB);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        btnList.put(FKey.KEY_IMEI, "IMEI：" + DeviceUtils.getImeiOrMeid());
        btnList.put(FKey.KEY_ICCID, "ICCID：" + NetUtils.getLteIccid());
        btnList.put(FKey.KEY_SERIAL, "序列号：" + OptionTools.getSerialNo());
        btnList.put(FKey.KEY_NET_TYPE, "网络类型：" + NetUtils.getNetWorkTypeName());
        btnList.put(FKey.KEY_APK_VERSION, "APK版本：v" + AppsUtils.getAppVersionName());
        btnList.put(FKey.KEY_IS_ROOT, "是否ROOT：" + AppsUtils.isRoot());
        btnList.put(FKey.KEY_LOCAL_IP, "本地IP：" + NetUtils.getLocalIp());
        btnList.put(FKey.KEY_FW_VERSION, "固件版本：" + DeviceUtils.getFwVersion());
        btnList.put(FKey.KEY_RAM, "运存：" + ramSpace.getTotalSize() + "MB/" + ramSpace.getUseSize() + "MB/" + ramSpace.getAvailableSize() + "MB");
        btnList.put(FKey.KEY_SD_SPACE, "SD：" + sdSpace.getTotalSize() + "MB/" + sdSpace.getUseSize() + "MB/" + sdSpace.getAvailableSize() + "MB");
        btnList.put(FKey.KEY_ETH_MODE, "ETH模式：" + FEthTools.getIpMode(BaseIotUtils.getContext()));
        try {
            btnList.put(FKey.KEY_DBM, "4G信号值：" + FLteTools.getDbm());
        } catch (Throwable throwable) {
            btnList.put(FKey.KEY_DBM, "4G信号值：0 dBm 0 asu");
        }
        btnList.put(FKey.KEY_SERIAL_PORT, "串口收发");
        btnList.put(FKey.KEY_TIMERD, "定时器");
        btnList.put(FKey.KEY_SCREEN, "屏幕相关");
        btnList.put(FKey.KEY_FILE_RW, "文件读写");
        btnList.put(FKey.KEY_NETWORK, "网络监听");
        btnList.put(FKey.KEY_RESET_MONITOR, "网络重置监听");
        btnList.put(FKey.KEY_FILEDOWN, "多文件下载");
        btnList.put(FKey.KEY_TCP_UDP, "TCP|UDP");
        btnList.put(FKey.KEY_NOTIFY_SHOW, "通知开启");
        btnList.put(FKey.KEY_NOTIFY_CLOSE, "通知关闭");
        btnList.put(FKey.KEY_SYS_DIALOG_SHOW, "系统弹窗");
        btnList.put(FKey.KEY_SYS_DIALOG_CLOSE, "关闭系统弹窗");
        btnList.put(FKey.KEY_DIALOG, "对话框");
        btnList.put(FKey.KEY_TOAST, "普通吐司");
        btnList.put(FKey.KEY_TOAST_BG, "图形吐司");
        btnList.put(FKey.KEY_ERR_ANR, "测试anr");
        btnList.put(FKey.KEY_ERR_OTHER, "测试其他异常");
        btnList.put(FKey.KEY_USB_HUB, "USB设备监听");
        btnList.put(FKey.KEY_USB_HUB_UNREGIST, "USB监听解除");
        btnList.put(FKey.KEY_GREEN_DAO, "数据库封装");
        btnList.put(FKey.KEY_JXL_OPEN, "JXL打开excel");
        btnList.put(FKey.KEY_JXL_EXPORT, "JXL导出excel");
        btnList.put(FKey.KEY_POI_OPEN, "POI打开excel");
        btnList.put(FKey.KEY_POI_EXPORT, "POI导出excel");
        btnList.put(FKey.KEY_APP_LIST, "应用列表");
        btnList.put(FKey.KEY_VIDEO, "视频播放");
        btnList.put(FKey.KEY_URL_CONVERT, "Uri转路径");
        btnList.put(FKey.KEY_ASSETS, "获取Assets文件");
        btnList.put(FKey.KEY_AUDIO, "播放音频");
        btnList.put(FKey.KEY_IP_SET_STATIC, "静态IP(ROOT)");
        btnList.put(FKey.KEY_IP_SET_DHCP, "动态IP(ROOT)");
        btnList.put(FKey.KEY_SCREENSHOT, "截屏(ROOT)");
        btnList.put(FKey.KEY_KEEPALIVE, "ATY/SERVICE保活");
        btnList.put(FKey.KEY_OTA, "ota升级(RK|FC)");
        btnList.put(FKey.KEY_INSTALL, "静默安装");
        btnList.put(FKey.KEY_BLUETOOTH, "蓝牙测试");
        btnList.put(FKey.VIDEO_CACHE, "视频录制");
        btnList.put(FKey.KEY_CRASH, "死机验证");
        btnList.put(FKey.KEY_NGINX, "nginx");
        btnList.put(FKey.KEY_SUB_DEV_HID, "HID(作为从)");
        btnList.put(FKey.KEY_MAIN_DEV_HID, "HID(作为主)");
        btnList.put(FKey.KEY_APK_SIGN, "APK签名");
        btnList.put(FKey.KEY_TOUCH_DETECT, "触摸检查");
        return btnList;
    }

    @Override
    public void onButtonClick(int position, String buttonText) {
        switch (position) {
            case FKey.KEY_NOTIFY_SHOW:
                //获取系统中是否已经通过 允许通知的权限
                if (NotifyUtils.notifyIsEnable()) {
                    NotifyUtils.setNotifyId(111)
                            .setEnableCloseButton(false)//设置是否显示关闭按钮
                            .setOnNotifyLinstener(new INotifyStateCallback() {
                                @Override
                                public void enableStatus(boolean isEnable) {
                                    KLog.e(TAG, "isEnable=" + isEnable);
                                }
                            })
                            .setAppName("BaseIotUtils")
                            .setAppAbout(AppsUtils.getAppVersionName())
                            .setPrompt("this a prompt")
                            .setProgress("this a progress")
                            .setTopRight("xxxx")
                            .setDataTime("2022-04-18")
                            .setRemarks("this is a remarks")
                            .exeuNotify();
                } else {
                    //去开启通知
                    NotifyUtils.toOpenNotify();
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotifyUtils.setAppName("");
                        NotifyUtils.setAppAbout("");
                        NotifyUtils.setRemarks("");
                        NotifyUtils.setPrompt("");
                        NotifyUtils.setDataTime("");
                        NotifyUtils.setTopRight("");
                        NotifyUtils.setIvStatus(true, R.drawable.failed);
                    }
                }, 5000);
                break;
            case FKey.KEY_NOTIFY_CLOSE:
                NotifyUtils.closeNotify();
                break;
            case FKey.KEY_SYS_DIALOG_SHOW:
                GlobalDialogUtils.getInstance().show("hello world");
                break;
            case FKey.KEY_SYS_DIALOG_CLOSE:
                GlobalDialogUtils.getInstance().dismiss();
                break;
            case FKey.KEY_TOAST:
                ShellUtils.CommandResult commandResult = ShellUtils.execCommand("am force-stop " +
                        "com.face.regularservice", true);
                KLog.d("result=" + commandResult.result + ",errMeg=" + commandResult.errorMsg);
                ToastUtils.showShort("Hello Worold!");
                break;
            case FKey.KEY_TOAST_BG:
                ToastUtils.success("Hello Worold!");
                break;
            case FKey.KEY_ERR_ANR:
                stopService(new Intent(this, MyService.class));
                startService(new Intent(this, MyService.class));
                break;
            case FKey.KEY_ERR_OTHER:
                int i = 1 / 0;
                break;
            case FKey.KEY_USB_HUB:
                ToastUtils.info("usb设备监听开始,插入或拔出将提示！");
                UsbHubTools.getInstance().registerReceiver();
                UsbHubTools.getInstance().setIUsbDeviceListener(new IUsbHubListener() {
                    @Override
                    public void deviceInfo(String action, String path, boolean isConn) {
                        ToastUtils.info("path:" + path + ",isConn=" + isConn);
                    }
                });
                break;
            case FKey.KEY_USB_HUB_UNREGIST:
                ToastUtils.info("解除usb设备监听注册");
                UsbHubTools.getInstance().unRegisterReceiver();
                break;
            case FKey.KEY_JXL_OPEN:
                ToastUtils.info("请查看日志确定读取结果");
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream input = getAssets().open("table.xls");
                            if (input != null) {
                                TableFileUtils.writeToLocal(Environment.getExternalStorageDirectory() + "/table.xls", input);
                            }
                            //第一种jxl.jar 只能读取xls
                            List<ExcelEntity> readExcelDatas =
                                    JXLExcelUtils.readExcelxlsx(Environment.getExternalStorageDirectory() + "/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                });
                break;
            case FKey.KEY_JXL_EXPORT:
                //第一种 jxl.jar导出
                JXLExcelUtils.exportExcel();
                ToastUtils.success("export successful!");
                break;
            case FKey.KEY_POI_OPEN:
                ToastUtils.info("请查看日志确定读取结果");
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream input = getAssets().open("table.xls");
                            if (input != null) {
                                TableFileUtils.writeToLocal(Environment.getExternalStorageDirectory() + "/table.xls", input);
                            }
                            //poi.jar 可以读取xls xlsx 两种
                            List<ExcelEntity> readExcelDatas =
                                    POIExcelUtils.readExcel(Environment.getExternalStorageDirectory() + "/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                });
                break;
            case FKey.KEY_POI_EXPORT:
                ToastUtils.info("请查看日志确定导出结果");
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        //poi.jar导出
                        boolean isOK = POIExcelUtils.createExcelFile();
                        KLog.d(TAG, "isOK: " + isOK);
                    }
                });
                break;
            case FKey.KEY_URL_CONVERT:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "请选择文件"), FILE_SELECT_CODE);
                break;
            case FKey.KEY_ASSETS:
                try {
                    InputStream input = this.getAssets().open("table.xls");
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
            case FKey.KEY_IP_SET_DHCP:
                CommonValue commonValue2 = FEthTools.setEthDhcp();
                if (commonValue2 == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("动态IP设置成功！");
                } else {
                    ToastUtils.error("动态IP设置失败！errMeg=" + commonValue2.getRemarks());
                }
                break;
            case FKey.KEY_IP_SET_STATIC:
                CommonValue commonValue = FEthTools.setStaticIp(new IpConfigInfo("192.168.1.155",
                        "8.8.8.8", "8.8.4.4", "192.168.1.1", "255.255.255.0"));
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("静态IP设置成功！");
                } else {
                    ToastUtils.error("静态IP设置失败！errMeg=" + commonValue.getRemarks());
                }
                break;
            case FKey.KEY_SCREENSHOT:
                String imgPath = FScreentTools.takeScreenshot("/sdcard/");
                if (imgPath != null && !imgPath.equals("")) {
                    ToastUtils.success("截屏成功,位置:/sdcard/目录下");
                } else {
                    ToastUtils.error("截屏失败！");
                }
                break;
            case FKey.KEY_CRASH:
                CrashTools.crashtest();
                break;
            case FKey.KEY_OTA:
                OptionTools.showOtaUpgrade();
                break;
            case FKey.KEY_SERIAL_PORT:
                startAty(SerialPortAty.class);
                break;
            case FKey.KEY_TIMERD:
                startAty(TimerAty.class);
                break;
            case FKey.KEY_SCREEN:
                startAty(ScreenActivity.class);
                break;
            case FKey.KEY_FILE_RW:
                startAty(FileOperatAty.class);
                break;
            case FKey.KEY_NETWORK:
                startAty(NetChangeAty.class);
                break;
            case FKey.KEY_RESET_MONITOR:
                startAty(NetRecordAty.class);
                break;
            case FKey.KEY_FILEDOWN:
                startAty(FileDownLoadAty.class);
                break;
            case FKey.KEY_TCP_UDP:
                startAty(SocketAty.class);
                break;
            case FKey.KEY_GREEN_DAO:
                startAty(GreenDaoSqliteAty.class);
                break;
            case FKey.KEY_APP_LIST:
                startAty(AllAppAty.class);
                break;
            case FKey.KEY_VIDEO:
                startAty(VideoPlayAty.class);
                break;
            case FKey.KEY_AUDIO:
                startAty(AudioAty.class);
                break;
            case FKey.KEY_KEEPALIVE:
                startAty(KeepAliveAty.class);
                break;
            case FKey.KEY_INSTALL:
                startAty(InstallAPkAty.class);
                break;
            case FKey.KEY_BLUETOOTH:
                startAty(BlueToothAty.class);
                break;
            case FKey.VIDEO_CACHE:
                startAty(PlayCacheVideoAty.class);
                break;
            case FKey.KEY_NGINX:
                startAty(NginxAty.class);
                break;
            case FKey.KEY_DIALOG:
                startAty(DialogAty.class);
                break;
            case FKey.KEY_ICCID:

                break;
            case FKey.KEY_IMEI:

                break;
            case FKey.KEY_SUB_DEV_HID:
                startAty(HidSubDevAty.class);
                break;
            case FKey.KEY_MAIN_DEV_HID:
                startAty(HidMainDevAty.class);
                break;
            case FKey.KEY_APK_SIGN:
                startAty(ApkSignSearchAty.class);
                break;
            case FKey.KEY_TOUCH_DETECT:
                startAty(TouchDetectAty.class);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            // 用户未选择任何文件，直接返回
            ToastUtils.error("未选择任何文件!");
            return;
        }
        if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData(); // 获取用户选择文件的URI
            String filePath = UriPathUtils.getPath(uri);
            KLog.d(TAG, "filePath=" + filePath + ",uri.getPath()=" + uri.getPath());
            ToastUtils.success("文件地址:" + filePath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotifyUtils.closeNotify();
        GlobalDialogUtils.getInstance().dismiss();
        UsbHubTools.getInstance().unRegisterReceiver();
        AudioUtils.getInstance().stopPlaying();
        TPoolSingleUtils.shutdown();
        FLteTools.cancel();
    }
}
