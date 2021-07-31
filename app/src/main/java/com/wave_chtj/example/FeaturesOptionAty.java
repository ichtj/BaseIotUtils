package com.wave_chtj.example;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chtj.base_framework.FStorageTools;
import com.chtj.base_framework.entity.Space;
import com.chtj.base_framework.network.FEthTools;
import com.chtj.base_framework.network.FNetworkTools;
import com.chtj.base_framework.network.NetDbmListener;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.DeviceUtils;
import com.face_chtj.base_iotutils.ISysDialog;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.app.AppsUtils;
import com.face_chtj.base_iotutils.audio.PlayUtils;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.notify.NotifyUtils;
import com.wave_chtj.example.base.BaseActivity;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.util.BaseIotRvAdapter;
import com.wave_chtj.example.util.FKey;
import com.wave_chtj.example.util.SingletonDisposable;
import com.wave_chtj.example.util.keyevent.UsbHubTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * 功能选择
 */
public class FeaturesOptionAty extends BaseActivity {
    private static final String TAG = "FeaturesOptionAty";
    public static final int FILE_SELECT_CODE = 10000;
    private RecyclerView rvinfo;
    private BaseIotRvAdapter adapterDome;//声明适配器
    private final static int FLAG_DEVICE_INFO = 0x100;
    private String dbm4G = 0 + " dBm " + 0 + " asu";
    LinkedHashMap<Integer, String[]> infoList = new LinkedHashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_re);
        AppManager.getAppManager().finishActivity(StartPageAty.class);
        rvinfo = findViewById(R.id.rvinfo);
        /**获取权限*/
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
        adapterDome = new BaseIotRvAdapter(FeaturesOptionAty.this,infoList);
        GridLayoutManager manager = new GridLayoutManager(BaseIotUtils.getContext(), 2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvinfo.setLayoutManager(manager);
        rvinfo.setAdapter(adapterDome);

        FNetworkTools.lteListener(BaseIotUtils.getContext(), new NetDbmListener() {
            @Override
            public void getDbm(String dbmAsu) {
                dbm4G = dbmAsu;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    public void initData() {
        infoList = new LinkedHashMap<>();
        String netType = NetUtils.getNetWorkTypeName();
        infoList.put(FKey.KEY_NET_TYPE, new String[]{"网络类型：" + netType});
        String appVersion = AppsUtils.getAppVersionName();
        infoList.put(FKey.KEY_APK_VERSION, new String[]{"APK版本：v" + appVersion});
        boolean isRoot = AppsUtils.isRoot();
        infoList.put(FKey.KEY_IS_ROOT, new String[]{"是否ROOT：" + isRoot});
        String localIp = DeviceUtils.getLocalIp();
        infoList.put(FKey.KEY_LOCAL_IP, new String[]{"本地IP：" + localIp});
        String fwVersion = DeviceUtils.getFwVersion();
        infoList.put(FKey.KEY_FW_VERSION, new String[]{"固件版本：" + fwVersion});
        Space ramSpace = FStorageTools.getRamSpace();
        infoList.put(FKey.KEY_RAM, new String[]{"运存：" + ramSpace.getTotalSize() + "M/" + ramSpace.getUseSize() + "M/" + ramSpace.getAvailableSize() + "M"});
        Space romSpace = FStorageTools.getRomSpace();
        infoList.put(FKey.KEY_ROM, new String[]{"内存：" + romSpace.getTotalSize() + "M/" + romSpace.getUseSize() + "M/" + romSpace.getAvailableSize() + "M"});
        Space sdSpace = FStorageTools.getSdcardSpace();
        infoList.put(FKey.KEY_SD_SPACE, new String[]{"SD：" + sdSpace.getTotalSize() + "M/" + sdSpace.getUseSize() + "M/" + sdSpace.getAvailableSize() + "M"});
        try {
            infoList.put(FKey.KEY_ETH_MODE, new String[]{"ETH模式：" + FEthTools.getIpMode(BaseIotUtils.getContext())});
        } catch (Throwable e) {
            infoList.put(FKey.KEY_ETH_MODE, new String[]{"ETH模式：NONE"});
        }
        infoList.put(FKey.KEY_DBM, new String[]{"4G信号值：" + dbm4G});
        infoList.put(FKey.KEY_SERIAL_PORT, new String[]{"串口收发"});
        infoList.put(FKey.KEY_TIMERD, new String[]{"定时器"});
        infoList.put(FKey.KEY_SCREEN, new String[]{"屏幕相关"});
        infoList.put(FKey.KEY_FILE_RW, new String[]{"文件读写"});
        infoList.put(FKey.KEY_NETWORK, new String[]{"网络监听"});
        infoList.put(FKey.KEY_FILEDOWN, new String[]{"多文件下载"});
        infoList.put(FKey.KEY_TCP_UDP, new String[]{"TCP|UDP"});
        infoList.put(FKey.KEY_NOTIFY_SHOW, new String[]{"通知开启"});
        infoList.put(FKey.KEY_NOTIFY_CLOSE, new String[]{"通知关闭"});
        infoList.put(FKey.KEY_SYS_DIALOG_SHOW, new String[]{"系统弹窗"});
        infoList.put(FKey.KEY_SYS_DIALOG_CLOSE, new String[]{"关闭系统弹窗"});
        infoList.put(FKey.KEY_TOAST, new String[]{"普通吐司"});
        infoList.put(FKey.KEY_TOAST_BG, new String[]{"图形吐司"});
        infoList.put(FKey.KEY_ERR_ANR, new String[]{"测试anr"});
        infoList.put(FKey.KEY_ERR_OTHER, new String[]{"测试其他异常"});
        infoList.put(FKey.KEY_USB_HUB, new String[]{"USB设备监听"});
        infoList.put(FKey.KEY_USB_HUB_UNREGIST, new String[]{"USB监听解除"});
        infoList.put(FKey.KEY_GREEN_DAO, new String[]{"数据库封装"});
        infoList.put(FKey.KEY_JXL_OPEN, new String[]{"JXL打开excel"});
        infoList.put(FKey.KEY_JXL_EXPORT, new String[]{"JXL导出excel"});
        infoList.put(FKey.KEY_POI_OPEN, new String[]{"POI打开excel"});
        infoList.put(FKey.KEY_POI_EXPORT, new String[]{"POI导出excel"});
        infoList.put(FKey.KEY_APP_LIST, new String[]{"应用列表"});
        infoList.put(FKey.KEY_VIDEO, new String[]{"视频播放"});
        infoList.put(FKey.KEY_URL_CONVERT, new String[]{"Uri转路径"});
        infoList.put(FKey.KEY_ASSETS, new String[]{"获取Assets文件"});
        infoList.put(FKey.KEY_AUDIO, new String[]{"播放音频"});
        infoList.put(FKey.KEY_IP_SET_STATIC, new String[]{"静态IP(ROOT)"});
        infoList.put(FKey.KEY_IP_SET_DHCP, new String[]{"动态IP(ROOT)"});
        infoList.put(FKey.KEY_SCREENSHOT, new String[]{"截屏(ROOT)"});
        infoList.put(FKey.KEY_KEEPALIVE, new String[]{"ATY/SERVICE保活"});
        infoList.put(FKey.KEY_OTA, new String[]{"ota升级(RK|FC)"});
        infoList.put(FKey.KEY_MORE, new String[]{"更多...."});
        adapterDome.setList(infoList);
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
        ISysDialog.getInstance().dismiss();
        ToastUtils.info("解除usb设备监听注册");
        UsbHubTools.getInstance().unRegisterReceiver();
        PlayUtils.getInstance().stopPlaying();
        SingletonDisposable.clearAll();
    }
}
