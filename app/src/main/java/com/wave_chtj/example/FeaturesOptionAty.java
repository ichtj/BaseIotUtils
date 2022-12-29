package com.wave_chtj.example;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chtj.base_framework.FScreentTools;
import com.chtj.base_framework.FStorageTools;
import com.chtj.base_framework.entity.CommonValue;
import com.chtj.base_framework.entity.InstallStatus;
import com.chtj.base_framework.entity.IpConfigInfo;
import com.chtj.base_framework.entity.Space;
import com.chtj.base_framework.network.FEthTools;
import com.chtj.base_framework.network.FNetworkTools;
import com.chtj.base_framework.network.NetDbmListener;
import com.chtj.base_framework.upgrade.FUpgradeTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.display.DeviceUtils;
import com.face_chtj.base_iotutils.display.ISysDialog;
import com.face_chtj.base_iotutils.display.AppsUtils;
import com.face_chtj.base_iotutils.audio.PlayUtils;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.face_chtj.base_iotutils.notify.OnNotifyLinstener;
import com.face_chtj.base_iotutils.threadpool.SingleTPoolUtils;
import com.face_chtj.base_iotutils.threadpool.TPoolUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.face_chtj.base_iotutils.display.ToastUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.notify.NotifyUtils;
import com.wave_chtj.example.allapp.AllAppAty;
import com.wave_chtj.example.audio.PlayAudioAty;
import com.wave_chtj.example.base.BaseActivity;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.wave_chtj.example.bluetooth.BlueToothAty;
import com.wave_chtj.example.crash.CrashTools;
import com.wave_chtj.example.crash.MyService;
import com.wave_chtj.example.download.FileDownLoadAty;
import com.wave_chtj.example.entity.ExcelEntity;
import com.wave_chtj.example.entity.IndexBean;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.greendao.GreenDaoSqliteAty;
import com.wave_chtj.example.install.InstallAPkAty;
import com.wave_chtj.example.keeplive.KeepAliveAty;
import com.wave_chtj.example.network.NetChangeAty;
import com.wave_chtj.example.network.NetMonitorAty;
import com.wave_chtj.example.nginx.NginxAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.socket.SocketAty;
import com.wave_chtj.example.timer.TimerAty;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.util.IndexAdapter;
import com.wave_chtj.example.util.FKey;
import com.wave_chtj.example.util.TableFileUtils;
import com.wave_chtj.example.util.excel.JXLExcelUtils;
import com.wave_chtj.example.util.excel.POIExcelUtils;
import com.wave_chtj.example.util.keyevent.IUsbHubListener;
import com.wave_chtj.example.util.keyevent.UsbHubTools;
import com.wave_chtj.example.video.PlayCacheVideoAty;
import com.wave_chtj.example.video.VideoPlayAty;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * 功能选择
 */
public class FeaturesOptionAty extends BaseActivity {
    private static final String TAG = FeaturesOptionAty.class.getSimpleName() + "M";
    public static final int FILE_SELECT_CODE = 10000;
    private RecyclerView rvinfo;
    private IndexAdapter adapterDome;//声明适配器
    private String dbm4G = 0 + " dBm " + 0 + " asu";
    List<IndexBean> indexBeanList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_re);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppManager.getAppManager().finishActivity(StartPageAty.class);
        rvinfo = findViewById(R.id.rvinfo);
        /*获取权限*/
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
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
        //初始化数据
        initData();
        adapterDome = new IndexAdapter(indexBeanList);
        GridLayoutManager manager = new GridLayoutManager(BaseIotUtils.getContext(), 2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvinfo.setLayoutManager(manager);
        rvinfo.setAdapter(adapterDome);
        adapterDome.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                clickByPosition(position);
            }
        });
        FNetworkTools.lteListener(BaseIotUtils.getContext(), new NetDbmListener() {
            @Override
            public void getDbm(String dbmAsu) {
                dbm4G = dbmAsu;
            }
        });
    }

    public void initData() {
        indexBeanList = new ArrayList<>();
        String netType = NetUtils.getNetWorkTypeName();
        indexBeanList.add(new IndexBean(FKey.KEY_NET_TYPE, new String[]{"网络类型：" + netType}, IndexAdapter.LAYOUT_NO_BG));
        String appVersion = AppsUtils.getAppVersionName();
        indexBeanList.add(new IndexBean(FKey.KEY_APK_VERSION, new String[]{"APK版本：v" + appVersion}, IndexAdapter.LAYOUT_NO_BG));
        boolean isRoot = AppsUtils.isRoot();
        indexBeanList.add(new IndexBean(FKey.KEY_IS_ROOT, new String[]{"是否ROOT：" + isRoot}, IndexAdapter.LAYOUT_NO_BG));
        String localIp = DeviceUtils.getLocalIp();
        indexBeanList.add(new IndexBean(FKey.KEY_LOCAL_IP, new String[]{"本地IP：" + localIp}, IndexAdapter.LAYOUT_NO_BG));
        String fwVersion = DeviceUtils.getFwVersion();
        indexBeanList.add(new IndexBean(FKey.KEY_FW_VERSION, new String[]{"固件版本：" + fwVersion}, IndexAdapter.LAYOUT_NO_BG));
        Space ramSpace = FStorageTools.getRamSpace();
        indexBeanList.add(new IndexBean(FKey.KEY_RAM, new String[]{"运存：" + ramSpace.getTotalSize() + "M/" + ramSpace.getUseSize() + "M/" + ramSpace.getAvailableSize() + "M"}, IndexAdapter.LAYOUT_NO_BG));
        Space romSpace = FStorageTools.getRomSpace();
        indexBeanList.add(new IndexBean(FKey.KEY_ROM, new String[]{"内存：" + 0 + "M/" + 0 + "M/" + 0 + "M"}, IndexAdapter.LAYOUT_NO_BG));
        Space sdSpace = FStorageTools.getSdcardSpace();
        indexBeanList.add(new IndexBean(FKey.KEY_SD_SPACE, new String[]{"SD：" + sdSpace.getTotalSize() + "M/" + sdSpace.getUseSize() + "M/" + sdSpace.getAvailableSize() + "M"}, IndexAdapter.LAYOUT_NO_BG));
        try {
            indexBeanList.add(new IndexBean(FKey.KEY_ETH_MODE, new String[]{"ETH模式：" + FEthTools.getIpMode(BaseIotUtils.getContext())}, IndexAdapter.LAYOUT_NO_BG));
        } catch (Throwable e) {
            indexBeanList.add(new IndexBean(FKey.KEY_ETH_MODE, new String[]{"ETH模式：NONE"}, IndexAdapter.LAYOUT_NO_BG));
        }
        indexBeanList.add(new IndexBean(FKey.KEY_DBM, new String[]{"4G信号值：" + dbm4G}, IndexAdapter.LAYOUT_NO_BG));
        indexBeanList.add(new IndexBean(FKey.KEY_SERIAL_PORT, new String[]{"串口收发"}, IndexAdapter.LAYOUT_NO_BG));
        indexBeanList.add(new IndexBean(FKey.KEY_TIMERD, new String[]{"定时器"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_SCREEN, new String[]{"屏幕相关"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_FILE_RW, new String[]{"文件读写"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_NETWORK, new String[]{"网络监听"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_RESET_MONITOR, new String[]{"网络重置监听"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_FILEDOWN, new String[]{"多文件下载"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_TCP_UDP, new String[]{"TCP|UDP"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_NOTIFY_SHOW, new String[]{"通知开启"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_NOTIFY_CLOSE, new String[]{"通知关闭"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_SYS_DIALOG_SHOW, new String[]{"系统弹窗"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_SYS_DIALOG_CLOSE, new String[]{"关闭系统弹窗"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_TOAST, new String[]{"普通吐司"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_TOAST_BG, new String[]{"图形吐司"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_ERR_ANR, new String[]{"测试anr"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_ERR_OTHER, new String[]{"测试其他异常"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_USB_HUB, new String[]{"USB设备监听"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_USB_HUB_UNREGIST, new String[]{"USB监听解除"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_GREEN_DAO, new String[]{"数据库封装"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_JXL_OPEN, new String[]{"JXL打开excel"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_JXL_EXPORT, new String[]{"JXL导出excel"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_POI_OPEN, new String[]{"POI打开excel"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_POI_EXPORT, new String[]{"POI导出excel"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_APP_LIST, new String[]{"应用列表"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_VIDEO, new String[]{"视频播放"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_URL_CONVERT, new String[]{"Uri转路径"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_ASSETS, new String[]{"获取Assets文件"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_AUDIO, new String[]{"播放音频"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_IP_SET_STATIC, new String[]{"静态IP(ROOT)"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_IP_SET_DHCP, new String[]{"动态IP(ROOT)"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_SCREENSHOT, new String[]{"截屏(ROOT)"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_KEEPALIVE, new String[]{"ATY/SERVICE保活"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_OTA, new String[]{"ota升级(RK|FC)"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_INSTALL, new String[]{"静默安装"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_BLUETOOTH, new String[]{"蓝牙测试"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.VIDEO_CACHE, new String[]{"视频录制"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_CRASH, new String[]{"死机验证"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_NGINX, new String[]{"nginx"}, IndexAdapter.LAYOUT_ONE));
        indexBeanList.add(new IndexBean(FKey.KEY_MORE, new String[]{"更多...."}, IndexAdapter.LAYOUT_ONE));
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


    public void clickByPosition(int position) {
        switch (position) {
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
                startAty(NetMonitorAty.class);
                break;
            case FKey.KEY_FILEDOWN:
                startAty(FileDownLoadAty.class);
                break;
            case FKey.KEY_TCP_UDP:
                startAty(SocketAty.class);
                break;
            case FKey.KEY_NOTIFY_SHOW:
                //获取系统中是否已经通过 允许通知的权限
                if (NotifyUtils.notifyIsEnable()) {
                    NotifyUtils.getInstance()
                            .setNotifyId(111)
                            .setEnableCloseButton(false)//设置是否显示关闭按钮
                            .setOnNotifyLinstener(new OnNotifyLinstener() {
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        NotifyUtils.getInstance().setAppName("");
                        NotifyUtils.getInstance().setAppAbout("");
                        NotifyUtils.getInstance().setRemarks("");
                        NotifyUtils.getInstance().setPrompt("");
                        NotifyUtils.getInstance().setDataTime("");
                        NotifyUtils.getInstance().setTopRight("");
                        NotifyUtils.getInstance().setIvStatus(true, R.drawable.failed);
                    }
                });
                break;
            case FKey.KEY_NOTIFY_CLOSE:
                NotifyUtils.closeNotify();
                break;
            case FKey.KEY_SYS_DIALOG_SHOW:
                ISysDialog.getInstance().show("hello world");
                break;
            case FKey.KEY_SYS_DIALOG_CLOSE:
                ISysDialog.getInstance().dismiss();
                break;
            case FKey.KEY_TOAST:
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
            case FKey.KEY_GREEN_DAO:
                startAty(GreenDaoSqliteAty.class);
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
                            List<ExcelEntity> readExcelDatas = JXLExcelUtils.readExcelxlsx(Environment.getExternalStorageDirectory() + "/table.xls");
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
                            List<ExcelEntity> readExcelDatas = POIExcelUtils.readExcel(Environment.getExternalStorageDirectory() + "/table.xls");
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
            case FKey.KEY_APP_LIST:
                startAty(AllAppAty.class);
                break;
            case FKey.KEY_VIDEO:
                startAty(VideoPlayAty.class);
                break;
            case FKey.KEY_URL_CONVERT:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "请选择文件"), FeaturesOptionAty.FILE_SELECT_CODE);
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
            case FKey.KEY_AUDIO:
                startAty(PlayAudioAty.class);
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
                CommonValue commonValue = FEthTools.setStaticIp(new IpConfigInfo("192.168.1.155", "8.8.8.8", "8.8.4.4", "192.168.1.1", "255.255.255.0"));
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
            case FKey.KEY_KEEPALIVE:
                startAty(KeepAliveAty.class);
                break;
            case FKey.KEY_OTA:
                showOtaUpgrade();
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
            case FKey.KEY_MORE:
                ToastUtils.info("敬请期待！");
                break;
            case FKey.KEY_CRASH:
                CrashTools.crashtest();
                break;
            case FKey.KEY_NGINX:
                startAty(NginxAty.class);
                break;
        }
    }

    /**
     * ota升级 确保sdcard目录存在update.zip固件
     */
    public static void showOtaUpgrade() {
        File file = new File("/sdcard/update.zip");
        if (file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseIotUtils.getContext());
            builder.setTitle("提示:");
            builder.setMessage("进行固件升级吗？点击确认后请等待...");
            builder.setIcon(R.drawable.logo_splash);
            builder.setCancelable(true);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FUpgradeTools.firmwareUpgrade("/sdcard/update.zip", new FUpgradeTools.UpgradeInterface() {
                        @Override
                        public void operating(InstallStatus installStatus) {
                            Log.d(TAG, "operating: " + installStatus.name());
                        }

                        @Override
                        public void error(String errInfo) {
                            Log.d(TAG, "error: ");
                        }
                    });
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            ToastUtils.error("/sdcard/目录下未找到update.zip文件！");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotifyUtils.closeNotify();
        ISysDialog.getInstance().dismiss();
        ToastUtils.info("解除usb设备监听注册");
        UsbHubTools.getInstance().unRegisterReceiver();
        PlayUtils.getInstance().stopPlaying();
        SingleTPoolUtils.shutdown();
    }
}
