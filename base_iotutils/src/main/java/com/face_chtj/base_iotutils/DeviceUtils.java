package com.face_chtj.base_iotutils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.face_chtj.base_iotutils.BaseIotUtils;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:得到设备的相关信息
 * --获取系统运行时间 {@link #getSystemRunningTime()}
 * --获取IMEI 或者MEID {@link #getImeiOrMeid()}
 * --获取系统内核版本 {@link #getKernelVersion()}
 */
public final class DeviceUtils {
    private DeviceUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Get the firmware version
     */
    public static String getFwVersion() {
        return Build.DISPLAY;
    }


    /**
     * 获取系统运行时间 时:分:秒
     */
    public static String getSystemRunningTime() {
        long ut = SystemClock.elapsedRealtime() / 1000;

        if (ut == 0) {
            ut = 1;
        }
        int s = (int) (ut % 60);
        int m = (int) ((ut / 60) % 60);
        int h = (int) ((ut / 3600));
        return h + ":" + pad(m) + ":" + pad(s);
    }

    /**
     * 时间转化 系统运行时间
     */
    private static String pad(int n) {
        return n >= 10 ? String.valueOf(n) : "0" + String.valueOf(n);
    }

    /**
     * 获取IMEI 或者MEID
     * android.permission.READ_PRIVILEGED_PHONE_STATE
     *
     * @return 手机IMEI
     */
    public static String getImeiOrMeid() {
        try {
            TelephonyManager manager = (TelephonyManager) BaseIotUtils.getContext().getSystemService(Activity.TELEPHONY_SERVICE);
            return manager != null ? manager.getDeviceId() : null;
        } catch (Throwable throwable) {
            return "";
        }
    }

    /**
     * 获取系统内核版本
     */
    public static String getKernelVersion() {
        String kernelVersion = "N/A";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/version"));
            String line;
            if ((line = reader.readLine()) != null) {
                kernelVersion = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return kernelVersion;
    }

}
