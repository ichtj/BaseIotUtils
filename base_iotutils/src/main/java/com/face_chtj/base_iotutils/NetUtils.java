package com.face_chtj.base_iotutils;

import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.content.Context.WIFI_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.face_chtj.base_iotutils.entity.DnsBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:网络工具类
 * 网络类型如下：
 * > NETWORK_NO      = -1; 当前无网络连接
 * > NETWORK_WIFI    =  1; wifi的情况下
 * > NETWORK_2G      =  2; 切换到2G环境下
 * > NETWORK_3G      =  3; 切换到3G环境下
 * > NETWORK_4G      =  4; 切换到4G环境下
 * > NETWORK_UNKNOWN =  5; 未知网络
 * > NETWORK_ETH     =  9; ETH以太网
 */
public class NetUtils {
    public static final int NETWORK_NO = -1;//no network
    public static final int NETWORK_WIFI = 1;//wifi network
    public static final int NETWORK_2G = 2;//"2G" networks
    public static final int NETWORK_3G = 3;//"3G" networks
    public static final int NETWORK_4G = 4;//"4G" networks
    public static final int NETWORK_UNKNOWN = 5;//unknown network
    public static final int NETWORK_ETH = 9;//ETH networks
    private static final int NETWORK_TYPE_GSM = 16;//GSM
    private static final int NETWORK_TYPE_TD_SCDMA = 17;//TDSCDMA
    private static final int NETWORK_TYPE_IWLAN = 18;//IWLAN

    private static final int TIMERD_DNS_REFRESH = 7200;//预计多少秒后刷新dns列表
    public static final String[] DNS_LIST = new String[]{
            /*"114.114.114.114", "114.114.115.115", */"223.5.5.5",
            "223.6.6.6", "180.76.76.76", "119.29.29.29",
            "210.2.4.8", "182.254.116.116", "101.226.4.6",
            "1.2.4.8", "218.30.118.6", "123.125.81.6",
            "140.207.198.6", "47.106.129.104", "8.8.8.8", "8.8.4.4", "122.112.208.1",
            "139.9.23.90", "114.115.192.11", "116.205.5.1", "116.205.5.30",
            "122.112.208.175"
    };

    /**
     * 获取网络类型
     * 1 监视网络连接状态 包括（Wi-Fi, 2G, 3G, 4G，ETH）
     * 2 当网络状态改变时发送广播通知
     * 3 网络连接失败尝试连接其他网络
     * 4 提供API，允许应用程序获取可用的网络状态
     */
    public static int getNetWorkType() {
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
                            if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                    || subtypeName.equalsIgnoreCase("WCDMA")
                                    || subtypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORK_3G;
                            } else {
                                return NETWORK_UNKNOWN;//未知网络
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
    public static String getNetWorkTypeName() {
        switch (getNetWorkType()) {
            case NETWORK_WIFI:
                return "NETWORK_WIFI";
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
     * 根据网络内容转换为网络名称字符串
     */
    public static String convertNetTypeName(int netType) {
        switch (netType) {
            case NETWORK_WIFI:
                return "NETWORK_WIFI";
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
     * 判断网络连接是否可用
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断网络是否可用
     */
    public static boolean isAvailable() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    /**
     * 判断网络是否连接
     */
    public static boolean isConnected() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }


    /**
     * 尝试重新刷新可用的dns列表
     * 并非每次都去筛选可用列表,而是按照定时机制,例如多少个小时后
     */
    private static boolean tryRefreshDns() {
        String[] defList = NetUtils.DNS_LIST;
        boolean isExistPass = false;
        CopyOnWriteArrayList<DnsBean> dnsBeanList = new CopyOnWriteArrayList<>();
        for (int i = 0; i < defList.length; i++) {
            String nowDns = defList[i];
            boolean isPing = NetUtils.ping(nowDns, 1, 1);
            if (!isExistPass && isPing) {
                isExistPass = true;
            }
            dnsBeanList.add(new DnsBean(nowDns, isPing));
        }
        BaseIotUtils.instance().dnsBeans = dnsBeanList;
        KLog.d("tryRefreshDns() >> dnsBeans >> " + dnsBeanList.toString());
        return isExistPass;
    }

    private static String[] getConvertDns() {
        List<String> dnsList = new ArrayList<>();
        for (int i = 0; i < BaseIotUtils.instance().dnsBeans.size(); i++) {
            DnsBean dnsBean = BaseIotUtils.instance().dnsBeans.get(i);
            if (dnsBean.isPass) {
                dnsList.add(dnsBean.dns);
            }
        }
        return dnsList.toArray(new String[dnsList.size()]);
    }

    /**
     * 调用此方法会自动刷新可用的dns列表并进行ping操作
     * 请在子线程调用此方法
     * 2小时刷新一次可用的dns列表
     */
    public static boolean reloadDnsPing() {
        //先判断本机是否网络API返回正常
        if (getNetWorkType() != NETWORK_NO) {
            long beforeTime = BaseIotUtils.instance().dnsRefreshTime;
            if (beforeTime <= 0) {
                //如果没有记录过时间 那么证明第一次加载DNS列表 如果在通过的列表中 有网络正常通过的那么直接返回true ,因为的重新加载的列表中会对所有的列表做检测
                BaseIotUtils.instance().dnsRefreshTime = System.currentTimeMillis();
                KLog.d("dnsRefreshTime beforeTime <= 0 " + BaseIotUtils.instance().dnsRefreshTime);
                return tryRefreshDns();
            } else {
                long nowTime = System.currentTimeMillis() / 1000;
                long diffNum = nowTime - (beforeTime / 1000);
                if (diffNum > TIMERD_DNS_REFRESH) {//大于两小时刷新一次 7200秒等于2小时
                    KLog.d("reloadDnsList() time >> " + TIMERD_DNS_REFRESH + " diffNum >> " + diffNum);
                    BaseIotUtils.instance().dnsRefreshTime = System.currentTimeMillis();//记录这一次操作的时间
                    KLog.d("dnsRefreshTime diffNum > TIMERD_DNS_REFRESH " + BaseIotUtils.instance().dnsRefreshTime);
                    //如果在通过的列表中 有网络正常通过的那么直接返回true ,因为的重新加载的列表中会对所有的列表做检测
                    return tryRefreshDns();
                } else {
                    //未达到指定刷新dns的时间 那么使用前一次获取的列表
                    return checkNetWork(TypeDataUtils.getRandomList(getConvertDns(), 3), 1, 1);
                }
            }
        } else {
            //由于网络出现问题 重置下一次刷新时间
            BaseIotUtils.instance().dnsRefreshTime = 0;
            return false;
        }
    }

    /**
     * 根据输入的dns列表循环判断网络是否异常
     * dns中只要有一个通过 那么证明网络正常
     */
    public static boolean checkNetWork(String[] dnsList, int count, int w) {
        KLog.d("checkNetWork() dnsList >> " + Arrays.toString(dnsList));
        for (String pingAddr : dnsList) {
            boolean isPing = NetUtils.ping(pingAddr, count, w);
            //KLog.d("checkNetWork() isPing >> "+isPing);
            if (isPing) {
                //If it is abnormal when entering the program network at the beginning, then only prompt once
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     */
    public static final boolean ping(String ip) {
        return ping(ip, 0);
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     */
    public static final boolean ping(String ip, int count) {
        return ping(ip, count, 0);
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     */
    public static final boolean ping(String ip, int count, int w) {
        return ping(ip, count, w, 0);
    }


    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     * c 设置完成要求回应的次数
     * w 指定超时，单位为秒
     * W 等待一个响应的时间，单位为秒
     */
    public static final boolean ping(String ip, int count, int w, int W) {
        try {
            StringBuffer cbstr = new StringBuffer("ping");
            cbstr.append(" -c " + (count==0?1:count));
            if (w != 0) {
                cbstr.append(" -w " + w);
            }
            if (W != 0) {
                cbstr.append(" -W " + W);
            }
            cbstr.append(" " + ip);
            String cmd = cbstr.toString();
            KLog.d("ping cmd >> " + cmd);
            Process p = Runtime.getRuntime().exec(cmd);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = "";
            int connectedCount = 0;
            while ((line = in.readLine()) != null) {
                connectedCount += getCheckResult(line);
            }
            return connectedCount >= 1 ? true : false;
        } catch (Throwable e) {
            return false;
        }
    }

    // 若line含有=18 ms ttl=64字样,说明已经ping通,返回1,否則返回0.
    private static int getCheckResult(String line) {
        //KLog.d("getCheckResult : line >> "+line);
        return line.toUpperCase().contains("TTL=")?1:0;
    }

    /**
     * 判断WIFI是否打开
     */
    public static boolean isWifiEnabled() {
        ConnectivityManager mgrConn = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) BaseIotUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return ((mgrConn.getActiveNetworkInfo() != null
                && mgrConn.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED)
                || mgrTel.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    /**
     * 判断网络连接方式是否为WIFI
     */
    public static boolean isWifi() {
        NetworkInfo networkINfo = getActiveNetworkInfo();
        return networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 判断网络连接方式是否为ETH
     */
    public static boolean isEth() {
        NetworkInfo networkINfo = getActiveNetworkInfo();
        return networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_ETHERNET;
    }

    /**
     * 判断wifi是否连接状态
     */
    public static boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 判断是否为3G网络
     */
    public static boolean is3rd() {
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        return networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 判断网络是否是4G
     */
    public static boolean is4G() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE;
    }

    /**
     * GPS是否打开
     */
    public static boolean isGpsEnabled() {
        LocationManager lm = ((LocationManager) BaseIotUtils.getContext().getSystemService(Context.LOCATION_SERVICE));
        List<String> accessibleProviders = lm.getProviders(true);
        return accessibleProviders != null && accessibleProviders.size() > 0;
    }

    /**
     * 打开网络设置界面
     * <p>3.0以下打开设置界面</p>
     */
    public static void openWirelessSettings() {
        if (Build.VERSION.SDK_INT > 10) {
            BaseIotUtils.getContext().startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        } else {
            BaseIotUtils.getContext().startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    /**
     * 获取活动网络信息
     */
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 获取移动网络运营商名称
     * <p>如中国联通、中国移动、中国电信</p>
     *
     * @return 移动网络运营商名称
     */
    public static String getNetworkOperatorName() {
        TelephonyManager tm = (TelephonyManager) BaseIotUtils.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : null;
    }

    /**
     * 获取移动终端类型|手机制式
     * {@link TelephonyManager#PHONE_TYPE_NONE } : 0 手机制式未知</li>
     * {@link TelephonyManager#PHONE_TYPE_GSM  } : 1 手机制式为GSM，移动和联通</li>
     * {@link TelephonyManager#PHONE_TYPE_CDMA } : 2 手机制式为CDMA，电信</li>
     * {@link TelephonyManager#PHONE_TYPE_SIP  } : 3</li>
     */
    public static int getPhoneType() {
        TelephonyManager tm = (TelephonyManager) BaseIotUtils.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getPhoneType() : -1;
    }

    /**
     * Return the MAC address.
     * 获取设备 MAC 地址
     * <p>Must hold {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />},
     * {@code <uses-permission android:name="android.permission.INTERNET" />},
     * {@code <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />}</p>
     *
     * @return the MAC address
     */
    @RequiresPermission(allOf = {ACCESS_WIFI_STATE, INTERNET, CHANGE_WIFI_STATE})
    public static String getMacAddress() {
        String macAddress = getMacAddress((String[]) null);
        if (!macAddress.equals("") || getWifiEnabled()) return macAddress;
        setWifiEnabled(true);
        setWifiEnabled(false);
        return getMacAddress((String[]) null);
    }

    private static boolean getWifiEnabled() {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) BaseIotUtils.getContext().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        return manager.isWifiEnabled();
    }

    /**
     * Enable or disable wifi.
     * <p>Must hold {@code <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />}</p>
     *
     * @param enabled True to enabled, false otherwise.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    private static void setWifiEnabled(final boolean enabled) {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) BaseIotUtils.getContext().getSystemService(WIFI_SERVICE);
        if (manager == null) return;
        if (enabled == manager.isWifiEnabled()) return;
        manager.setWifiEnabled(enabled);
    }

    /**
     * Return the MAC address.
     * <p>Must hold {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />},
     * {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @return the MAC address
     */
    @RequiresPermission(allOf = {ACCESS_WIFI_STATE, INTERNET})
    public static String getMacAddress(final String... excepts) {
        String macAddress = getMacAddressByNetworkInterface();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        macAddress = getMacAddressByInetAddress();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        macAddress = getMacAddressByWifiInfo();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        macAddress = getMacAddressByFile();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        return "";
    }

    private static boolean isAddressNotInExcepts(final String address, final String... excepts) {
        if (excepts == null || excepts.length == 0) {
            return !"02:00:00:00:00:00".equals(address);
        }
        for (String filter : excepts) {
            if (address.equals(filter)) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private static String getMacAddressByWifiInfo() {
        try {
            final WifiManager wifi = (WifiManager) BaseIotUtils.getContext()
                    .getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifi != null) {
                final WifiInfo info = wifi.getConnectionInfo();
                if (info != null) return info.getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String getMacAddressByNetworkInterface() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni == null || !ni.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = ni.getHardwareAddress();
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : macBytes) {
                        sb.append(String.format("%02x:", b));
                    }
                    return sb.substring(0, sb.length() - 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String getMacAddressByInetAddress() {
        try {
            InetAddress inetAddress = getInetAddress();
            if (inetAddress != null) {
                NetworkInterface ni = NetworkInterface.getByInetAddress(inetAddress);
                if (ni != null) {
                    byte[] macBytes = ni.getHardwareAddress();
                    if (macBytes != null && macBytes.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (byte b : macBytes) {
                            sb.append(String.format("%02x:", b));
                        }
                        return sb.substring(0, sb.length() - 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static InetAddress getInetAddress() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                // To prevent phone of xiaomi return "10.0.2.15"
                if (!ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (hostAddress.indexOf(':') < 0) return inetAddress;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getMacAddressByFile() {
        ShellUtils.CommandResult result = ShellUtils.execCommand("getprop wifi.interface", false);
        if (result.result == 0) {
            String name = result.successMsg;
            if (name != null) {
                result = ShellUtils.execCommand("cat /sys/class/net/" + name + "/address", false);
                if (result.result == 0) {
                    String address = result.successMsg;
                    if (address != null && address.length() > 0) {
                        return address;
                    }
                }
            }
        }
        return "02:00:00:00:00:00";
    }
}
