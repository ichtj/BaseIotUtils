package com.face_chtj.base_iotutils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

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
 * --获取本机IP {@link #getLocalIp()}
 * --获取系统运行时间 {@link #getSystemRunningTime()}
 * --获取IMEI 或者MEID {@link #getImeiOrMeid()}
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
     * 获取本机IP
     */
    public static String getLocalIp() {
        List<String> ipList = new ArrayList<>();
        try {
            Enumeration enNetI = NetworkInterface.getNetworkInterfaces();
            while (enNetI.hasMoreElements()) {
                NetworkInterface netI = (NetworkInterface) enNetI.nextElement();
                Enumeration enumIpAddr = netI.getInetAddresses();

                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        ipList.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException var4) {
            var4.printStackTrace();
            return "0.0.0.0";
        }
        if (ipList.size() > 0) {
            return ipList.get(ipList.size() - 1);
        } else {
            return "0.0.0.0";
        }
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
        }catch (Throwable throwable){
            return "";
        }
    }


    /**
     * sim卡ccid
     *
     * @return ccid列表
     */
    public static List<String> getLteIccid() {
        List<String> iccid = new ArrayList<>();
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                SubscriptionManager sm = SubscriptionManager.from(BaseIotUtils.getContext());
                List<SubscriptionInfo> sis = sm.getActiveSubscriptionInfoList();
                if (sis.size() >= 1) {
                    SubscriptionInfo si1 = sis.get(0);
                    iccid.add(si1.getIccId());
                    //String phoneNum1 = si1.getNumber();
                }
                if (sis.size() >= 2) {
                    SubscriptionInfo si2 = sis.get(1);
                    iccid.add(si2.getIccId());
                    //String phoneNum2 = si2.getNumber();
                }
                // 获取SIM卡数量相关信息：
                //int count = sm.getActiveSubscriptionInfoCount();//当前实际插卡数量
                //int max   = sm.getActiveSubscriptionInfoCountMax();//当前卡槽数量
                return iccid;
            } else {
                TelephonyManager tm = (TelephonyManager) BaseIotUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
                iccid.add(tm.getSimSerialNumber());
                return iccid;
            }
        } catch (Throwable e) {
            Log.e("getLteIccid",e.getMessage());
            return iccid;
        }
    }
}
