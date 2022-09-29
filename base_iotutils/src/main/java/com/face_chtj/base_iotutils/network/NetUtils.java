package com.face_chtj.base_iotutils.network;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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

    /*dns列表*/
    public static final String[] DNS_LIST = new String[]{
            "114.114.114.114", "223.5.5.5", "223.6.6.6", "180.76.76.76"/*, "8.8.8.8"*/,
            "114.114.115.115", "119.29.29.29", "210.2.4.8"/*, "9.9.9.9"*/, "182.254.116.116", "199.91.73.222",
            "101.226.4.6", "1.2.4.8"};
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
     * @param netType
     * @return
     */
    public static String convertNetTypeName(int netType){
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
     * 根据输入的dns列表循环判断网络是否异常
     * dns中只要有一个通过 那么证明网络正常
     */
    public static boolean checkNetWork(String[] dnsList, int count, int deadline) {
        for (String pingAddr : dnsList) {
            boolean isPing=NetUtils.ping(pingAddr, count, deadline);
            KLog.d("checkNetWork() isPing >> "+isPing);
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
    public static final boolean ping() {

        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 2 -w 1 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
//            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
//            Log.d("----result---", "result = " + result);
        }
        return false;
    }

    /**
     * 动态控制执行次数和时间
     *
     * @param count  执行次数
     * @param deadline 时间秒
     * @return 是否成功
     */
    public static final boolean ping(int count, int deadline) {
        return ping("www.baidu.com", count, deadline);
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * 不要在主线程使用，会阻塞线程
     */
    public static final boolean ping(String ip, int count, int deadline) {
        String result = null;
        try {
            Process p = Runtime.getRuntime().exec("ping -c " + count + " -w " + deadline + " " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
//            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
//            Log.d("----result---", "result = " + result);
        }
        return false;
    }

    /**
     * ping IP
     * 不要在主线程使用，会阻塞线程
     */
    public static final boolean ping(String ip) {

        String result = null;
        try {
            // ping网址3次
            Process p = Runtime.getRuntime().exec("ping -c 2 -w 1 " + ip);
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            //Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            //Log.d("----result---", "result = " + result);
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
