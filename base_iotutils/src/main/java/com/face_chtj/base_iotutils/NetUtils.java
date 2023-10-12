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
import androidx.annotation.RequiresPermission;

import com.face_chtj.base_iotutils.entity.DnsBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final int TIMERD_DNS_REFRESH = 15;//预计多少秒后刷新dns列表
    public static final String[] DNS_LIST = new String[]{"223.5.5.5", "223.6.6.6", "180.76.76.76", "119.29.29.29", "210.2.4.8", "182.254.116.116", "101.226.4.6", "1.2.4.8", "218.30.118.6", "123.125.81.6", "140.207.198.6", "47.106.129.104", "8.8.8.8", "8.8.4.4", "122.112.208.1", "139.9.23.90", "114.115.192.11", "116.205.5.1", "116.205.5.30", "122.112.208.175"};
//    public static final String[] DNS_LIST = new String[]{"192.168.11.0", "192.168.11.1", "192.168.11.2", "192.168.11.3", "192.168.11.4", "192.168.11.5", "192.168.11.6", "192.168.11.7", "192.168.11.8", "192.168.11.9", "47.106.129.104",};

    public CopyOnWriteArrayList<DnsBean> cacheDnsList = new CopyOnWriteArrayList<>();
    //用于定时刷新DNS列表的时间→每隔一定的周期进行DNS刷新,筛选正常的列表用于网络校验
    public long dnsRefreshTime;

    private static volatile NetUtils sInstance;

    //singleton pattern
    public static NetUtils instance() {
        if (sInstance == null) {
            synchronized (NetUtils.class) {
                if (sInstance == null) {
                    sInstance = new NetUtils();
                }
            }
        }
        return sInstance;
    }

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
                            if (subtypeName.equalsIgnoreCase("TD-SCDMA") || subtypeName.equalsIgnoreCase("WCDMA") || subtypeName.equalsIgnoreCase("CDMA2000")) {
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
        if (cm != null) {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo networkInfo : info) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
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
     * refresh dns list
     */
    private static void refreshDns(DnsBean iDnsBean) {
        boolean isFound = false;
        for (int i = 0; i < instance().cacheDnsList.size(); i++) {
            DnsBean dnsBean = instance().cacheDnsList.get(i);
            if (dnsBean.dns.equals(iDnsBean.dns)) {
                dnsBean.delay = iDnsBean.delay;
                dnsBean.ttl = iDnsBean.ttl;
                dnsBean.from = iDnsBean.from;
                dnsBean.isPass = iDnsBean.isPass;
                dnsBean.srcIndex = iDnsBean.srcIndex;
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            instance().cacheDnsList.add(iDnsBean);
        }
    }

    /**
     * 比较出来一个DNS进行ping操作
     */
    private static boolean comparePignDns() {
        String[] excludeDns = excludeDnsList();
        String[] uniquedns = ObjectUtils.getUniqueList(DNS_LIST, convertAllCache(), excludeDns);
        if (uniquedns.length <= 0 || DNS_LIST.length == instance().cacheDnsList.size()) {
            return cacheOrAllPing();
        } else {
            return checkAddCache(uniquedns);
        }
    }

    /**
     * 随机取一个dns去判断是否正常
     * 如果不正常不用添加到缓存
     * 随后检查缓存中是否有可用的dns列表 去随机选择一个ping并返回
     */
    private static boolean checkAddCache(String[] uniquedns) {
        String[] newDns = ObjectUtils.getRandomList(uniquedns, 1);
        DnsBean dnsBean = NetUtils.ping(newDns[0], 1, 1);
        return dnsBean.isPass ? true : checkNetWork(ObjectUtils.shuffleStringArray(DNS_LIST), 1, 1);
    }

    public static String[] convertAllCache() {
        List<String> dnsList = new ArrayList<>();
        for (DnsBean dnsBean : instance().cacheDnsList) {
            dnsList.add(dnsBean.dns);
        }
        return dnsList.toArray(new String[dnsList.size()]);
    }

    /**
     * 获取可用DNS列表
     */
    public static String[] availableDnsList() {
        List<String> dnsList = new ArrayList<>();
        for (DnsBean dnsBean : instance().cacheDnsList) {
            if (dnsBean.isPass) {
                dnsList.add(dnsBean.dns);
            }
        }
        return dnsList.toArray(new String[dnsList.size()]);
    }

    /**
     * 获取需要已经排除的DNS列表
     */
    public static String[] excludeDnsList() {
        List<String> excludeDnsList = new ArrayList<>();
        for (int i = 0; i < instance().cacheDnsList.size(); i++) {
            DnsBean dnsBean = instance().cacheDnsList.get(i);
            if (!dnsBean.isPass) {
                excludeDnsList.add(dnsBean.dns);
            }
        }
        return excludeDnsList.toArray(new String[0]);
    }

    /**
     * 调用此方法会自动刷新可用的dns列表并进行ping操作
     * 请在子线程调用此方法
     * 2小时刷新一次可用的dns列表
     */
    public static boolean reloadDnsPing() {
        if (getNetWorkType() != NETWORK_NO) {
            if (instance().cacheDnsList.size() <= 0) {
                instance().dnsRefreshTime = System.currentTimeMillis();
                return checkNetWork(ObjectUtils.shuffleStringArray(DNS_LIST), 1, 1);
            } else {
                long diff = System.currentTimeMillis() / 1000 - (instance().dnsRefreshTime / 1000);
                if (instance().dnsRefreshTime == 0 || diff >= TIMERD_DNS_REFRESH) {
                    instance().dnsRefreshTime = System.currentTimeMillis();
                    return comparePignDns();
                } else {
                    return cacheOrAllPing();
                }
            }
        } else {
            instance().dnsRefreshTime = 0;
            return false;
        }
    }

    /**
     * 缓存的一个DNS无法通过尝试 使用DNS_LIST逐一验证
     */
    private static boolean cacheOrAllPing() {
        boolean pingResult = checkNetWork(ObjectUtils.getRandomList(convertAllCache(), 1), 1, 1);
        return pingResult ? true : checkNetWork(ObjectUtils.shuffleStringArray(DNS_LIST), 1, 1);
    }

    /**
     * 根据输入的dns列表循环判断网络是否异常
     * dns中只要有一个通过 那么证明网络正常
     */
    public static boolean checkNetWork(String[] dnsList, int count, int w) {
        for (String pingAddr : dnsList) {
            DnsBean dnsBean = NetUtils.ping(pingAddr, count, w);
            if (dnsBean.isPass) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据输入的dns列表循环判断网络是否异常
     * dns中只要有一个通过 那么证明网络正常
     */
    public static List<DnsBean> checkNetWork(String... dnsList) {
        List<DnsBean> dnsBeans = new ArrayList<>();
        for (String pingAddr : dnsList) {
            DnsBean dnsBean = NetUtils.ping(pingAddr, 1, 1);
            dnsBeans.add(dnsBean);
        }
        return dnsBeans;
    }

    /**
     * 根据输入的dns列表循环判断网络是否异常
     * dns中只要有一个通过 那么证明网络正常
     */
    private static DnsBean checkNetWorkCallback() {
        for (String pingAddr : DNS_LIST) {
            DnsBean dnsBean = NetUtils.ping(pingAddr, 1, 1);
            if (dnsBean.isPass) {
                return dnsBean;
            }
        }
        String lastDns = DNS_LIST[DNS_LIST.length - 1];
        return new DnsBean("ping -c 1 -w 1 -W " + lastDns, lastDns, false, findStringIndex(lastDns));
    }

    /**
     * 根据输入的dns列表循环判断网络是否异常
     * dns中只要有一个通过 那么证明网络正常
     */
    public static DnsBean checkNetWork() {
        String[] availableDns = availableDnsList();
        boolean isCacheAvailable = availableDns != null && availableDns.length > 0;
        if (isCacheAvailable) {
            String[] dns = ObjectUtils.getRandomList(availableDns, 1);
            DnsBean dnsBean = NetUtils.ping(dns[0], 1, 1);
            if (dnsBean.isPass) {
                return dnsBean;
            }else{
                return checkNetWorkCallback();
            }
        } else {
            return checkNetWorkCallback();
        }
    }

    // 检查外部互联网连接是否正常
    public static boolean isInetAddressAvailable(int timeoutMillis) {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            if (address != null) {
                // 使用指定的超时时间进行 Ping 测试
                if (address.isReachable(timeoutMillis)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 检查外部互联网连接是否正常
    public static boolean isHttpConnectAvailable() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            int responseCode = urlConnection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     */
    public static DnsBean ping(String ip) {
        return ping(ip, 0);
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     */
    public static DnsBean ping(String ip, int count) {
        return ping(ip, count, 0);
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     */
    public static DnsBean ping(String ip, int count, int w) {
        return ping(ip, count, w, 0);
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     * c 指定要发送的 ICMP 回显请求的数量。默认情况下，ping 命令会持续发送请求，直到手动停止。使用 -c 参数可以指定发送的请求数量
     * s 指定每个 ICMP 回显请求的数据包大小（字节）。默认情况下，数据包大小为 56 字节（包括 IP 头部和 ICMP 头部）。使用 -s 参数可以自定义数据包大小。
     * t 指定等待每个回显请求的超时时间（以秒为单位）。如果在指定的时间内未收到回复，则该请求被视为超时。
     * i 指定发送 ICMP 回显请求之间的间隔时间（以秒为单位）。默认情况下，ping 命令会每秒发送一个请求。
     * w 指定总共持续发送 ICMP 回显请求的时间（以秒为单位）。ping 命令将在达到指定时间后停止发送请求。
     * W 等待一个响应的时间，单位为秒
     */
    public static DnsBean ping(String ip, int c, int w, int W) {
        InputStreamReader isr = null;
        StringBuffer cbstr = new StringBuffer();
        try {
            cbstr.append("ping");
            cbstr.append(c > 0 ? (" -c " + c) : (" -c 1"));
            cbstr.append(w > 0 ? (" -w " + w) : (" -w 1"));
            cbstr.append(W > 0 ? (" -W " + W) : (" -W 1"));
            //cbstr.append(s > 0 ? (" -s " + s) : (" -s 64"));
            cbstr.append(" " + ip);
            Process p = Runtime.getRuntime().exec(cbstr.toString());// ping网址3次
            // 读取ping的内容，可以不加
            isr = new InputStreamReader(p.getInputStream());
            BufferedReader bReader = new BufferedReader(isr);
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String from = extractIcmpSeq(RegularTools.REGULAR_IP, line, true).replaceAll("-1", "");
                int ttl = Integer.parseInt(extractIcmpSeq("ttl=(\\d+)", line, false));
                int delay = Integer.parseInt(extractIcmpSeq("time=(\\d+)", line, false));
                if (ttl != -1 && delay != -1) {
                    DnsBean dnsBean = new DnsBean(cbstr.toString(), ip, true, findStringIndex(ip), from, ttl, delay);
                    refreshDns(dnsBean);
                    return dnsBean;
                }
            }
            DnsBean dnsBean = new DnsBean(cbstr.toString(), ip, false, findStringIndex(ip));
            refreshDns(dnsBean);
            return dnsBean;
        } catch (Throwable e) {
            DnsBean dnsBean = new DnsBean(cbstr.toString(), ip, false, findStringIndex(ip));
            refreshDns(dnsBean);
            return dnsBean;
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    /**
     * 查找原DNS_LIST中对应dns的下标
     *
     * @param dns
     */
    private static int findStringIndex(String dns) {
        for (int i = 0; i < DNS_LIST.length; i++) {
            if (DNS_LIST[i].equals(dns)) {
                return i; // 返回找到的字符串的下标
            }
        }
        return -1; // 字符串未找到，返回 -1
    }

    /**
     * 使用正则表达式获取ttl time
     */
    private static String extractIcmpSeq(String pattern, String input, boolean isPatternAll) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(input);
        if (matcher.find()) {
            return isPatternAll ? matcher.group() : matcher.group(1);
        }
        return "-1"; // 如果未找到icmp_seq，则返回-1或其他适当的默认值
    }

    /**
     * 判断WIFI是否打开
     */
    public static boolean isWifiEnabled() {
        ConnectivityManager mgrConn = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) BaseIotUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return ((mgrConn.getActiveNetworkInfo() != null && mgrConn.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
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
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 判断是否为3G网络
     */
    public static boolean is3rd() {
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        return networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_MOBILE;
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
        TelephonyManager tm = (TelephonyManager) BaseIotUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
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
        TelephonyManager tm = (TelephonyManager) BaseIotUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
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
        @SuppressLint("WifiManagerLeak") WifiManager manager = (WifiManager) BaseIotUtils.getContext().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        return manager.isWifiEnabled();
    }

    /**
     * Enable or disable wifi.
     * <p>Must hold {@code <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"
     * />}</p>
     *
     * @param enabled True to enabled, false otherwise.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    private static void setWifiEnabled(final boolean enabled) {
        @SuppressLint("WifiManagerLeak") WifiManager manager = (WifiManager) BaseIotUtils.getContext().getSystemService(WIFI_SERVICE);
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
            final WifiManager wifi = (WifiManager) BaseIotUtils.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
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
