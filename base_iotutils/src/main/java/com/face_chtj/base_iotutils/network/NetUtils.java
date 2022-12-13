package com.face_chtj.base_iotutils.network;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.convert.TypeDataUtils;
import com.face_chtj.base_iotutils.entity.DnsBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
    /**
     * no network
     */
    public static final int NETWORK_NO = -1;
    /**
     * wifi network
     */
    public static final int NETWORK_WIFI = 1;
    /**
     * "2G" networks
     */
    public static final int NETWORK_2G = 2;
    /**
     * "3G" networks
     */
    public static final int NETWORK_3G = 3;
    /**
     * "4G" networks
     */
    public static final int NETWORK_4G = 4;
    /**
     * unknown network
     */
    public static final int NETWORK_UNKNOWN = 5;
    /**
     * ETH networks
     */
    public static final int NETWORK_ETH = 9;

    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN = 18;

    /**
     * 预计多少秒后刷新dns列表
     */
    private static final int TIMERD_DNS_REFRESH = 240;

    /**
     * 建议自己去ping一个自己的服务地址
     * 这里只是提供一些公共的，可能会被屏蔽，请留意
     * 也不要长时间的用一个dns地址去ping ,最好每次随机获取一个两个地址去ping
     */
    public static final String[] DNS_LIST = new String[]{
            "114.114.114.114", "114.114.115.115", "223.5.5.5",
            "223.6.6.6", "180.76.76.76", "119.29.29.29",
            "210.2.4.8", "182.254.116.116", "101.226.4.6",
            "1.2.4.8", "218.30.118.6", "123.125.81.6",
            "140.207.198.6", "47.106.129.104", "8.8.8.8", "8.8.4.4"};

    /**
     * 需添加权限
     *
     * @return 网络类型
     * @code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     * <p>
     * 它主要负责的是
     * 1 监视网络连接状态 包括（Wi-Fi, 2G, 3G, 4G，ETH）
     * 2 当网络状态改变时发送广播通知
     * 3 网络连接失败尝试连接其他网络
     * 4 提供API，允许应用程序获取可用的网络状态
     * <p>
     * netTyped 的结果
     * @link #NETWORK_NO      = -1; 当前无网络连接
     * @link #NETWORK_WIFI    =  1; wifi的情况下
     * @link #NETWORK_2G      =  2; 切换到2G环境下
     * @link #NETWORK_3G      =  3; 切换到3G环境下
     * @link #NETWORK_4G      =  4; 切换到4G环境下
     * @link #NETWORK_UNKNOWN =  5; 未知网络
     * @link #NETWORK_ETH     =  9; ETH以太网
     */
    public static int getNetWorkType() {
        // 获取ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager) BaseIotUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();// 获取当前网络状态

        int netType = NETWORK_NO;

        if (ni != null && ni.isConnectedOrConnecting()) {
            switch (ni.getType()) {//获取当前网络的状态
                case ConnectivityManager.TYPE_WIFI:// wifi的情况下
                    netType = NETWORK_WIFI;
                    //切换到wifi环境下
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    //切换到以太网环境下
                    netType = NETWORK_ETH;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    switch (ni.getSubtype()) {
                        case NETWORK_TYPE_GSM:
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            netType = NETWORK_2G;
                            //RxToast.info("切换到2G环境下");
                            break;
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
                            netType = NETWORK_3G;
                            //切换到3G环境下
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:

                        case NETWORK_TYPE_IWLAN:
                            netType = NETWORK_4G;
                            //切换到4G环境下
                            break;
                        default:

                            String subtypeName = ni.getSubtypeName();
                            if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                    || subtypeName.equalsIgnoreCase("WCDMA")
                                    || subtypeName.equalsIgnoreCase("CDMA2000")) {
                                netType = NETWORK_3G;
                            } else {
                                netType = NETWORK_UNKNOWN;
                            }
                            //未知网络
                    }
                    break;
                default:
                    netType = NETWORK_UNKNOWN;
                    //未知网络
            }

        } else {
            netType = NETWORK_NO;
            //当前无网络连接
        }
        return netType;
    }

    /**
     * 获取当前的网络类型(WIFI,2G,3G,4G,ETH)
     * <p>依赖上面的方法</p>
     *
     * @return 网络类型名称
     * <ul>
     * <li>NETWORK_ETH   </li>
     * <li>NETWORK_WIFI   </li>
     * <li>NETWORK_4G     </li>
     * <li>NETWORK_3G     </li>
     * <li>NETWORK_2G     </li>
     * <li>NETWORK_UNKNOWN</li>
     * <li>NETWORK_NO     </li>
     * </ul>
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
     *
     * @param netType
     * @return
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
     * 需添加权限
     *
     * @code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     */
    public static boolean isAvailable() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    /**
     * 判断网络是否连接
     * 需添加权限
     *
     * @code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
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
        KLog.d("tryRefreshDns() >> ");
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
                //如果没有记录过时间 那么证明第一次加载DNS列表
                BaseIotUtils.instance().dnsRefreshTime = System.currentTimeMillis();//记录这一次操作的时间
                //如果在通过的列表中 有网络正常通过的那么直接返回true ,因为的重新加载的列表中会对所有的列表做检测
                return tryRefreshDns();
            } else {
                long nowTime = System.currentTimeMillis() / 1000;
                long diffNum = nowTime - (beforeTime / 1000);
                if (diffNum > TIMERD_DNS_REFRESH) {//大于两小时刷新一次 7200秒等于2小时
                    KLog.d("reloadDnsList() time >> " + TIMERD_DNS_REFRESH + " diffNum >> " + diffNum);
                    BaseIotUtils.instance().dnsRefreshTime = System.currentTimeMillis();//记录这一次操作的时间
                    //如果在通过的列表中 有网络正常通过的那么直接返回true ,因为的重新加载的列表中会对所有的列表做检测
                    return tryRefreshDns();
                }
            }
            //即使在上面经过刷新dns列表的情况下都没有ping那么还有这次
            return checkNetWork(TypeDataUtils.getRandomList(getConvertDns(), 3), 1, 1);
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
     */
    public static final boolean ping(String ip, int count, int w, int W) {
        try {
            StringBuffer cbstr = new StringBuffer("ping");
            if (count != 0) {
                cbstr.append(" -c " + count);
            }
            if (w != 0) {
                cbstr.append(" -w " + w);
            }
            if (W != 0) {
                cbstr.append(" -W " + W);
            }
            cbstr.append(" " + ip);
            String cmd = cbstr.toString();
            Log.d("------ping-----", "ping cmd >> " + cmd);
            Process p = Runtime.getRuntime().exec(cmd);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            // ping的状态
            int status = p.waitFor();
            Log.d("------ping-----", "result content : " + stringBuffer.toString() + " >> status =" + (status == 0 ? true : false));
            if (status == 0) {
                return true;
            }
        } catch (Throwable e) {
            Log.e("------ping-----", "ping: ", e);
        } finally {
        }
        return false;
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
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return {@code true}: 连接<br>{@code false}: 未连接
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
     * 需添加权限
     *
     * @code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     */
    public static boolean is4G() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE;
    }

    /**
     * GPS是否打开
     *
     * @return
     */
    public static boolean isGpsEnabled() {
        LocationManager lm = ((LocationManager) BaseIotUtils.getContext().getSystemService(Context.LOCATION_SERVICE));
        List<String> accessibleProviders = lm.getProviders(true);
        return accessibleProviders != null && accessibleProviders.size() > 0;
    }

    /*
     * 下面列举几个可直接跳到联网设置的意图,供大家学习
     *
     *      startActivity(new Intent(android.provider.Settings.ACTION_APN_SETTINGS));
     *      startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
     *
     *  用下面两种方式设置网络
     *
     *      startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
     *      startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
     */

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
     *
     * @return NetworkInfo
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
     * 获取移动终端类型
     *
     * @return 手机制式
     * <ul>
     * <li>{@link TelephonyManager#PHONE_TYPE_NONE } : 0 手机制式未知</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_GSM  } : 1 手机制式为GSM，移动和联通</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_CDMA } : 2 手机制式为CDMA，电信</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_SIP  } : 3</li>
     * </ul>
     */
    public static int getPhoneType() {
        TelephonyManager tm = (TelephonyManager) BaseIotUtils.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getPhoneType() : -1;
    }
}
