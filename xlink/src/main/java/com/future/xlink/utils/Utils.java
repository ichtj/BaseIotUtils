package com.future.xlink.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.future.xlink.bean.InitParams;
import com.future.xlink.logs.Log4J;
import com.future.xlink.mqtt.MqttManager;

import java.util.UUID;

public class Utils {
    public static String getToken(InitParams params, String time) {
        String token = "Basic " + AESUtils.encrypt(params.key, params.sn + ":" + params.secret + ":" + time);
        //Log4J.info(Utils.class, "getToken", token);
        return token;
    }

    /**
     * 创建消息数据唯一编号
     */
    public static String createIID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String GetNetworkType(NetworkInfo networkInfo) {
        String strNetworkType = "";


        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();

                Log4J.info(MqttManager.class, "GetNetworkType", "Network getSubtypeName : " + _strSubTypeName);

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = _strSubTypeName;
                        }

                        break;
                }

                Log4J.info(MqttManager.class, "GetNetworkType", "Network getSubtype : " + Integer.valueOf(networkType).toString());
            }
        }
        return strNetworkType;
    }

    /**
     * 得到当前的手机蜂窝网络信号强度
     * 获取LTE网络和3G/2G网络的信号强度的方式有一点不同，
     * LTE网络强度是通过解析字符串获取的，
     * 3G/2G网络信号强度是通过API接口函数完成的。
     * asu 与 dbm 之间的换算关系是 dbm=-113 + 2*asu
     */
    public static void getCurrentNetDBM(Context context) {
        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener mylistener = new PhoneStateListener() {
            //            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String signalInfo = signalStrength.toString();
                String[] params = signalInfo.split(" ");

                String IMSI = tm.getSubscriberId();
                String ProvidersName = null;
                if (IMSI == null) {
                    ProvidersName = "unknow";
                }

                if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                    ProvidersName = "中国移动";
                } else if (IMSI.startsWith("46001")) {
                    ProvidersName = "中国联通";
                } else if (IMSI.startsWith("46003")) {
                    ProvidersName = "中国电信";
                }
                if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G网络 最佳范围   >-90dBm 越大越好
                    int Itedbm = Integer.parseInt(params[9]);
                    Log4J.info(Utils.class, "getCurrentNetDBM", ProvidersName + " itemdm 4G-->" + Itedbm);
//                    setDBM(Itedbm+"");

                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {
                    //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                    //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
                    String itedbm = "";
                    if ("中国移动".equalsIgnoreCase(ProvidersName)) {
                        itedbm = "0";
                    } else if ("中国联通".equalsIgnoreCase(ProvidersName)) {
                        itedbm = String.valueOf(signalStrength.getCdmaDbm());
                    } else if ("中国电信".equalsIgnoreCase(ProvidersName)) {
                        itedbm = String.valueOf(signalStrength.getEvdoDbm());
                    }
                    Log4J.info(Utils.class, "getCurrentNetDBM", ProvidersName + " itemdm 3G-->" + itedbm);
                } else {
                    //2G网络最佳范围>-90dBm 越大越好
                    int asu = signalStrength.getGsmSignalStrength();
                    int dbm = -113 + 2 * asu;
                    Log4J.info(Utils.class, "getCurrentNetDBM", ProvidersName + " itemdm 2G-->" + dbm);
                }
            }
        };
        //开始监听
        tm.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */

    public static synchronized String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 判断网络是否正常
     *
     * @param context
     * @return
     */
    public static boolean isNetNormal(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo.isAvailable() && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

}
