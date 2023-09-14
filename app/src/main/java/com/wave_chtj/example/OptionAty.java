package com.wave_chtj.example;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;

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
import com.chtj.base_framework.entity.IpConfigInfo;
import com.chtj.base_framework.entity.Space;
import com.chtj.base_framework.network.FEthTools;
import com.chtj.base_framework.network.FLteTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.DeviceUtils;
import com.face_chtj.base_iotutils.GlobalDialogUtils;
import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.AudioUtils;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.callback.INotifyStateCallback;
import com.face_chtj.base_iotutils.TPoolSingleUtils;
import com.face_chtj.base_iotutils.TPoolUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NotifyUtils;
import com.wave_chtj.example.allapp.AllAppAty;
import com.wave_chtj.example.audio.AudioAty;
import com.wave_chtj.example.base.BaseActivity;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.wave_chtj.example.bluetooth.BlueToothAty;
import com.wave_chtj.example.crash.CrashTools;
import com.wave_chtj.example.crash.MyService;
import com.wave_chtj.example.dialog.DialogAty;
import com.wave_chtj.example.download.FileDownLoadAty;
import com.wave_chtj.example.entity.ExcelEntity;
import com.wave_chtj.example.entity.Dbean;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.greendao.GreenDaoSqliteAty;
import com.wave_chtj.example.install.InstallAPkAty;
import com.wave_chtj.example.keeplive.KeepAliveAty;
import com.wave_chtj.example.network.NetChangeAty;
import com.wave_chtj.example.network.NetTimerAty;
import com.wave_chtj.example.nginx.NginxAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.socket.SocketAty;
import com.wave_chtj.example.timer.TimerAty;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.util.DocumentsUtils;
import com.wave_chtj.example.util.IndexItemAdapter;
import com.wave_chtj.example.util.FKey;
import com.wave_chtj.example.util.OptionTools;
import com.wave_chtj.example.util.TableFileUtils;
import com.wave_chtj.example.util.JXLExcelUtils;
import com.wave_chtj.example.util.POIExcelUtils;
import com.wave_chtj.example.callback.IUsbHubListener;
import com.wave_chtj.example.util.UsbHubTools;
import com.wave_chtj.example.video.PlayCacheVideoAty;
import com.wave_chtj.example.video.VideoPlayAty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能选择
 */
public class OptionAty extends BaseActivity implements OnItemClickListener {
    private static final String TAG = OptionAty.class.getSimpleName() + "M";
    private RecyclerView rvinfo;
    private IndexItemAdapter itemAdapter;//声明适配器
    private GridLayoutManager manager;
    private List<Dbean> dataList = new ArrayList<>();
    String rootPath = "/storage/3936-121D";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        FLteTools.init();
        initView();
        AppManager.finishActivity(StartPageAty.class);
        //初始化数据
        initData();
    }


    public void initView() {
        rvinfo = findViewById(R.id.rvinfo);
        itemAdapter = new IndexItemAdapter(dataList);
        manager = new GridLayoutManager(BaseIotUtils.getContext(), 2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        itemAdapter.setOnItemClickListener(this);
        rvinfo.setLayoutManager(manager);
        rvinfo.setAdapter(itemAdapter);
    }

    public void initData() {
        Space ramSpace = FStorageTools.getRamSpace();
        Space sdSpace = FStorageTools.getSdcardSpace();
        dataList.add(new Dbean(FKey.KEY_IMEI, "IMEI：" + DeviceUtils.getImeiOrMeid(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_ICCID, "ICCID：" + DeviceUtils.getLteIccid(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_SERIAL, "序列号：" + OptionTools.getSerialNo(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_NET_TYPE, "网络类型：" + NetUtils.getNetWorkTypeName(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_APK_VERSION, "APK版本：v" + AppsUtils.getAppVersionName(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_IS_ROOT, "是否ROOT：" + AppsUtils.isRoot(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_LOCAL_IP, "本地IP：" + DeviceUtils.getLocalIp(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_FW_VERSION, "固件版本：" + DeviceUtils.getFwVersion(),
                IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_RAM,
                "运存：" + ramSpace.getTotalSize() + "M/" + ramSpace.getUseSize() + "M/" + ramSpace.getAvailableSize() + "M", IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_SD_SPACE,
                "SD：" + sdSpace.getTotalSize() + "M/" + sdSpace.getUseSize() + "M/" + sdSpace.getAvailableSize() + "M", IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_ETH_MODE,
                "ETH模式：" + FEthTools.getIpMode(BaseIotUtils.getContext()),
                IndexItemAdapter.L_NO_BG));
        try {
            dataList.add(new Dbean(FKey.KEY_DBM, "4G信号值：" + FLteTools.getDbm(),
                    IndexItemAdapter.L_NO_BG));
        }catch (Throwable throwable){
            dataList.add(new Dbean(FKey.KEY_DBM, "4G信号值：0 dBm 0 asu" ,
                    IndexItemAdapter.L_NO_BG));
        }
        dataList.add(new Dbean(FKey.KEY_SERIAL_PORT, "串口收发", IndexItemAdapter.L_NO_BG));
        dataList.add(new Dbean(FKey.KEY_TIMERD, "定时器", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_SCREEN, "屏幕相关", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_FILE_RW, "文件读写", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_NETWORK, "网络监听", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_RESET_MONITOR, "网络重置监听", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_FILEDOWN, "多文件下载", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_TCP_UDP, "TCP|UDP", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_NOTIFY_SHOW, "通知开启", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_NOTIFY_CLOSE, "通知关闭", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_SYS_DIALOG_SHOW, "系统弹窗", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_SYS_DIALOG_CLOSE, "关闭系统弹窗", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_DIALOG, "对话框", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_TOAST, "普通吐司", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_TOAST_BG, "图形吐司", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_ERR_ANR, "测试anr", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_ERR_OTHER, "测试其他异常", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_USB_HUB, "USB设备监听", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_USB_HUB_UNREGIST, "USB监听解除", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_GREEN_DAO, "数据库封装", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_JXL_OPEN, "JXL打开excel", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_JXL_EXPORT, "JXL导出excel", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_POI_OPEN, "POI打开excel", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_POI_EXPORT, "POI导出excel", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_APP_LIST, "应用列表", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_VIDEO, "视频播放", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_URL_CONVERT, "Uri转路径", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_ASSETS, "获取Assets文件", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_AUDIO, "播放音频", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_IP_SET_STATIC, "静态IP(ROOT)", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_IP_SET_DHCP, "动态IP(ROOT)", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_SCREENSHOT, "截屏(ROOT)", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_KEEPALIVE, "ATY/SERVICE保活", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_OTA, "ota升级(RK|FC)", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_INSTALL, "静默安装", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_BLUETOOTH, "蓝牙测试", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.VIDEO_CACHE, "视频录制", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_CRASH, "死机验证", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_NGINX, "nginx", IndexItemAdapter.L_ONE));
        dataList.add(new Dbean(FKey.KEY_MORE, "更多....", IndexItemAdapter.L_ONE));
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
        } else if (requestCode == DocumentsUtils.OPEN_DOCUMENT_TREE_CODE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                DocumentsUtils.saveTreeUri(this, rootPath, uri);
                KLog.d(TAG, "data=" + data.toString()+" ,url >>> "+uri.toString() );
            }
        }
    }


    public void clickByPosition(int position) {
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
                startAty(NetTimerAty.class);
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
            case FKey.KEY_MORE:
                ToastUtils.info("敬请期待！");
                break;
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

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> a, @NonNull View v, int p) {
        clickByPosition(dataList.get(p).getFlag());
    }
}
