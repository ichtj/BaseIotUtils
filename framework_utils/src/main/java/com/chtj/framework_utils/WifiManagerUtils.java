package com.chtj.framework_utils;


import android.content.Context;
import android.net.wifi.WifiManager;

import com.chtj.framework_utils.entity.DeviceType;

/**
 * wifi工具类
 */
public class WifiManagerUtils {
    /**
     * 开启以太网
     */
    public static void openWifi() {
        WifiManager wifiManager = (WifiManager) BaseSystemUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (BaseSystemUtils.deviceType == DeviceType.DEVICE_FC5330) {
            wifiManager.setWifiEnabled(true);
        } else if (BaseSystemUtils.deviceType == DeviceType.DEVICE_RK3288) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭以太网
     */
    public static void closeWifi() {
        WifiManager wifiManager = (WifiManager) BaseSystemUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (BaseSystemUtils.deviceType == DeviceType.DEVICE_FC5330) {
            wifiManager.setWifiEnabled(false);
        } else if (BaseSystemUtils.deviceType == DeviceType.DEVICE_RK3288) {
            wifiManager.setWifiEnabled(false);
        }
    }

}