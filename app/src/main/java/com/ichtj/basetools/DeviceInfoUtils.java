package com.ichtj.basetools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *
 * <!-- 获取 IMEI、ICCID 需要 -->
 * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 *
 * <!-- 尝试获取 WiFi / MAC 信息时可加 -->
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 */
public class DeviceInfoUtils {
    public static final int NETWORK_NO = -1;//no network
    public static final int NETWORK_WIFI = 1;//wifi network
    public static final int NETWORK_2G = 2;//"2G" networks
    public static final int NETWORK_3G = 3;//"3G" networks
    public static final int NETWORK_4G = 4;//"4G" networks
    public static final int NETWORK_MOBILE = 14;//Indicates that the mobile network is available, not that the network is unavailable
    public static final int NETWORK_UNKNOWN = 5;//unknown network
    public static final int NETWORK_ETH = 9;//ETH networks
    private static final int NETWORK_TYPE_GSM = 16;//GSM
    private static final int NETWORK_TYPE_TD_SCDMA = 17;//TDSCDMA
    private static final int NETWORK_TYPE_IWLAN = 18;//IWLAN
    private DeviceInfoUtils() {
    }

    /**
     * 获取设备型号
     */
    public static String getDeviceModel() {
        return Build.MODEL == null ? "" : Build.MODEL;
    }

    /**
     * 获取设备品牌
     */
    public static String getDeviceBrand() {
        return Build.BRAND == null ? "" : Build.BRAND;
    }

    /**
     * 获取设备厂商
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER == null ? "" : Build.MANUFACTURER;
    }

    /**
     * 获取 Android ID
     * 普通 App 推荐使用这个作为设备标识的一部分
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        try {
            String androidId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            return androidId == null ? "" : androidId;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取设备序列号
     *
     * Android 10+ 普通 App 基本拿不到真实序列号
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getSerialNumber() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                return Build.SERIAL == null ? "" : Build.SERIAL;
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 判断网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager == null) {
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = connectivityManager.getActiveNetwork();
                if (network == null) {
                    return false;
                }

                NetworkCapabilities capabilities =
                        connectivityManager.getNetworkCapabilities(network);

                return capabilities != null &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }
    private static int getNetWorkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();// 获取当前网络状态
        if (ni != null && ni.isConnectedOrConnecting()) {
            switch (ni.getType()) {//获取当前网络的状态
                case ConnectivityManager.TYPE_WIFI:// wifi的情况下
                    return NETWORK_WIFI;//切换到wifi环境下
                case ConnectivityManager.TYPE_ETHERNET:
                    return NETWORK_ETH;//切换到以太网环境下
                case ConnectivityManager.TYPE_MOBILE:
                    switch (ni.getSubtype()) {
                        case NETWORK_TYPE_GSM:
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORK_2G;//RxToast.info("切换到2G环境下");
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                        case NETWORK_TYPE_TD_SCDMA:
                            return NETWORK_3G;//切换到3G环境下
                        case TelephonyManager.NETWORK_TYPE_LTE:
                        case NETWORK_TYPE_IWLAN:
                            return NETWORK_4G;//切换到4G环境下
                        default:
                            String subtypeName = ni.getSubtypeName();
                            if (subtypeName.equalsIgnoreCase("TD-SCDMA") || subtypeName.equalsIgnoreCase("WCDMA") || subtypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORK_3G;
                            } else {
                                return NETWORK_MOBILE;//标识为可用网络
                            }
                    }
                default:
                    return NETWORK_UNKNOWN;//未知网络
            }
        } else {
            return NETWORK_NO;//当前无网络连接
        }
    }

    /**
     * 获取当前的网络类型(WIFI,2G,3G,4G,ETH)
     */
    public static String getNetWorkTypeName(Context context) {
        switch (getNetWorkType(context)) {
            case NETWORK_WIFI:
                return "NETWORK_WIFI";
            case NETWORK_MOBILE:
                return "NETWORK_MOBILE";
            case NETWORK_4G:
                return "NETWORK_4G";
            case NETWORK_3G:
                return "NETWORK_3G";
            case NETWORK_2G:
                return "NETWORK_2G";
            case NETWORK_ETH:
                return "NETWORK_ETH";
            case NETWORK_NO:
                return "NETWORK_NO";
            default:
                return "NETWORK_UNKNOWN";
        }
    }

    /**
     * sim卡ccid
     *
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     * @return ccid列表
     */
    public static List<String> getLteIccid(Context context) {
        List<String> iccid = new ArrayList<>();
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                SubscriptionManager sm = SubscriptionManager.from(context);
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
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                iccid.add(tm.getSimSerialNumber());
                return iccid;
            }
        } catch (Throwable e) {
            Log.e("getLteIccid", e.getMessage());
            return iccid;
        }
    }

    /**
     * 获取 IMEI，默认获取卡槽 0
     *
     * 需要权限：
     * android.permission.READ_PHONE_STATE
     *
     * Android 10+ 普通 App 通常无法获取真实 IMEI
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getImei(Context context) {
        return getImei(context, 0);
    }

    /**
     * 获取指定卡槽 IMEI
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getImei(Context context, int slotIndex) {
        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager == null) {
                return "";
            }

            String imei;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei(slotIndex);
            } else {
                imei = telephonyManager.getDeviceId();
            }

            return imei == null ? "" : imei;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取 MAC 地址
     *
     * Android 6.0+ 通过 WifiInfo 获取通常会返回 02:00:00:00:00:00
     * 这里通过 NetworkInterface 尝试获取 wlan0
     */
    public static String getMacAddress() {
        try {
            List<NetworkInterface> interfaces =
                    Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface networkInterface : interfaces) {
                if (!"wlan0".equalsIgnoreCase(networkInterface.getName())) {
                    continue;
                }

                byte[] macBytes = networkInterface.getHardwareAddress();

                if (macBytes == null || macBytes.length == 0) {
                    return "";
                }

                StringBuilder macBuilder = new StringBuilder();

                for (byte b : macBytes) {
                    macBuilder.append(String.format("%02X:", b));
                }

                if (macBuilder.length() > 0) {
                    macBuilder.deleteCharAt(macBuilder.length() - 1);
                }

                return macBuilder.toString();
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取完整设备信息
     */
    public static DeviceInfo getDeviceInfo(Context context) {
        DeviceInfo info = new DeviceInfo();

        info.brand = getDeviceBrand();
        info.manufacturer = getManufacturer();
        info.model = getDeviceModel();
        info.androidId = getAndroidId(context);
        info.serialNumber = getSerialNumber();
        info.networkAvailable = isNetworkAvailable(context);
        info.networkType = getNetWorkTypeName(context);
        info.iccid = getLteIccid(context);
        info.imei = getImei(context);
        info.macAddress = getMacAddress();

        return info;
    }

    public static class DeviceInfo {
        public String brand;
        public String manufacturer;
        public String model;
        public String androidId;
        public String serialNumber;
        public boolean networkAvailable;
        public String networkType;
        public List<String> iccid;
        public String imei;
        public String macAddress;

        @Override
        public String toString() {
            return "DeviceInfo{" +
                    "brand='" + brand + '\'' +
                    ", manufacturer='" + manufacturer + '\'' +
                    ", model='" + model + '\'' +
                    ", androidId='" + androidId + '\'' +
                    ", serialNumber='" + serialNumber + '\'' +
                    ", networkAvailable=" + networkAvailable +
                    ", networkType='" + networkType + '\'' +
                    ", iccid='" + iccid + '\'' +
                    ", imei='" + imei + '\'' +
                    ", macAddress='" + macAddress + '\'' +
                    '}';
        }
    }
}