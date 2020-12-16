package com.chtj.framework;


import android.content.Context;
import android.net.wifi.WifiManager;

import com.chtj.framework.entity.DeviceType;

/**
 * wifi工具类
 */
public class FWifiTools {
    /**
     * 开启以太网
     */
    public static void openWifi() {
        WifiManager wifiManager = (WifiManager) FBaseTools.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (FBaseTools.deviceType == DeviceType.DEVICE_FC5330) {
            wifiManager.setWifiEnabled(true);
        } else if (FBaseTools.deviceType == DeviceType.DEVICE_RK3288) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭以太网
     */
    public static void closeWifi() {
        WifiManager wifiManager = (WifiManager) FBaseTools.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (FBaseTools.deviceType == DeviceType.DEVICE_FC5330) {
            wifiManager.setWifiEnabled(false);
        } else if (FBaseTools.deviceType == DeviceType.DEVICE_RK3288) {
            wifiManager.setWifiEnabled(false);
        }
    }

}
