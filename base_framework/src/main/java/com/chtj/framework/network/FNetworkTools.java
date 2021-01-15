package com.chtj.framework.network;

import android.content.Context;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

import com.chtj.framework.FBaseTools;
import com.chtj.framework.FCmdTools;
import com.chtj.framework.entity.DeviceType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.net.NetworkStats.SET_ALL;
import static android.net.NetworkStats.TAG_NONE;
import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;

public class FNetworkTools {
    private static final String TAG = "FNetworkTools";

    /**
     * 获取dns
     *
     * @return
     */
    public static String[] getNetWorkDns() {
        FCmdTools.CommandResult commandResult = FCmdTools.execCommand("getprop | grep net.dns", true);
        if (commandResult.result == 0 && commandResult.successMsg != null) {
            if (commandResult.successMsg.length() > 0) {
                String[] result = commandResult.successMsg.replace("]: [", ":").replace("][", "];[").split(";");
                Log.d(TAG, "getDns:>=" + result);
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取该uid的流量消耗
     *
     * @param uid 应用uid
     * @return
     */
    public static String getEthUsage(int uid) {
        if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_FC5330) {
            long receiveRx = TrafficStats.getUidRxBytes(uid);//获取某个网络UID的接受字节数 总接收量
            long sendTx = TrafficStats.getUidTxBytes(uid);//获取某个网络UID的发送字节数 总接收量
            double traffic = receiveRx + sendTx;
            double sumTraffic = getDouble(traffic / 1024 / 1024);
            return sumTraffic + "MB";
        } else if (FBaseTools.instance().getDeviceType() == DeviceType.DEVICE_RK3288) {
            long total = getEthAppUsage(uid, getTimesMonthMorning(), getNow());
            String totalPhrase = Formatter.formatFileSize(FBaseTools.getContext(), total);
            return totalPhrase;
        } else {
            return "0B";
        }
    }

    public static double getDouble(double d) {
        return (double) Math.round(d * 100) / 100;
    }


    /**
     * 获取系统总计消耗的流量
     *
     * @param startTime 开始时间戳
     * @param endTime   结束时间戳
     * @return 系统总计消耗的流量
     */
    public static long getEthTotalUsage(long startTime, final long endTime) {
        Log.d(TAG, "geEthSysTotalUsageData: start =" + startTime + ",endtime=" + endTime);
        long value = 0;
        try {
            // wait a few seconds before kicking off
            INetworkStatsService mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            //Thread.sleep(2 * DateUtils.SECOND_IN_MILLIS);
            //强制更新
            mStatsService.forceUpdate();
            INetworkStatsSession mStatsSession = mStatsService.openSession();
            NetworkTemplate mTemplate = NetworkTemplate.buildTemplateEthernet();
            ///*FIELD_RX_BYTES | FIELD_TX_BYTES*/
            NetworkStatsHistory networkStatsHistory = mStatsSession.getHistoryForNetwork(mTemplate, NetworkStatsHistory.FIELD_ALL);
            NetworkStatsHistory.Entry entry = null;
            long bucketDuration = networkStatsHistory.getBucketDuration();
            entry = networkStatsHistory.getValues(startTime, endTime, System.currentTimeMillis(), entry);
            value = entry != null ? entry.rxBytes + entry.txBytes : 0;
            Log.d(TAG, "geEthSysTotalUsageData: " + entry.rxBytes + "," + entry.txBytes);
            final String totalPhrase = Formatter.formatFileSize(FBaseTools.getContext(), value);
            long totalBytes = networkStatsHistory.getTotalBytes();
            int afterBucketCount2 = networkStatsHistory.getIndexAfter(startTime);
            int beforeBucketCount2 = networkStatsHistory.getIndexBefore(startTime);
            Log.i(TAG, "geEthSysTotalUsageData afterBucketCount2:" + afterBucketCount2 + ",beforeBucketCount2:" + beforeBucketCount2);
            Log.d(TAG, "geEthSysTotalUsageData bucketDuration =" + bucketDuration + "totalPhrase:" + totalPhrase + ",totalBytes:" + totalBytes);
            mStatsSession.close();
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.d(TAG, "geEthSysTotalUsageData:>e1=" + e.getMessage());
        } finally {
            Log.d(TAG, "geEthSysTotalUsageData:finally");
        }
        Log.d(TAG, "geEthSysTotalUsageData total_value1:" + value);
        return value;
    }


    /**
     * 获取移动数据流量系统总计消耗的流量
     *
     * @return 系统总计消耗的流量
     */
    public static long getMobileTotalUsage(long startTime,long endTime) {
        //得到当前系统时间
        long value = 0;
        try {
            // wait a few seconds before kicking off
            INetworkStatsService mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            //Thread.sleep(2 * DateUtils.SECOND_IN_MILLIS);
            //强制更新
            mStatsService.forceUpdate();
            INetworkStatsSession mStatsSession = mStatsService.openSession();
            NetworkTemplate mTemplate = NetworkTemplate.buildTemplateMobileWildcard();
            ///*FIELD_RX_BYTES | FIELD_TX_BYTES*/
            NetworkStatsHistory networkStatsHistory = mStatsSession.getHistoryForNetwork(mTemplate, FIELD_RX_BYTES | FIELD_TX_BYTES);
            NetworkStatsHistory.Entry entry = null;
            long bucketDuration = networkStatsHistory.getBucketDuration();
            entry = networkStatsHistory.getValues(startTime, endTime, System.currentTimeMillis(), entry);
            value = entry != null ? entry.rxBytes + entry.txBytes : 0;
            //上行流量
            Log.d(TAG, "geMobileTotalUsageData: tx=" + entry.txBytes);
            //下行流量
            Log.d(TAG, "geMobileTotalUsageData: rx=" + entry.rxBytes);
            final String totalPhrase = Formatter.formatFileSize(FBaseTools.getContext(), value);
            //这里是总流量
            long totalBytes = networkStatsHistory.getTotalBytes();
            int afterBucketCount2 = networkStatsHistory.getIndexAfter(startTime);
            int beforeBucketCount2 = networkStatsHistory.getIndexBefore(startTime);
            Log.i(TAG, "geMobileTotalUsageData afterBucketCount2:" + afterBucketCount2 + ",beforeBucketCount2:" + beforeBucketCount2);
            Log.d(TAG, "geMobileTotalUsageData bucketDuration =" + bucketDuration + ",totalPhrase:" + totalPhrase + ",totalBytes:" + totalBytes);
            mStatsSession.close();
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.d(TAG, "geMobileTotalUsageData:>e1=" + e.getMessage());
        } finally {
            Log.d(TAG, "geMobileTotalUsageData:finally");
        }
        Log.d(TAG, "geMobileTotalUsageData total_value1:" + value);
        return value;
    }

    /**
     * 根据UID获取该应用的上下行流量 总计
     *
     * @param uid       apk的uid
     * @param startTime 开始时间戳
     * @param endTime   结束时间戳
     * @return 该uid的apk总消耗流量
     */
    public static long getEthAppUsage(int uid, long startTime, long endTime) {
        long value = 0;
        try {
            INetworkStatsService mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            // wait a few seconds before kicking off
            //强制更新
            mStatsService.forceUpdate();
            INetworkStatsSession mStatsSession = mStatsService.openSession();
            NetworkTemplate mTemplate = NetworkTemplate.buildTemplateEthernet();
            NetworkStatsHistory networkStatsHistory = mStatsSession.getHistoryForUid(mTemplate, uid, SET_ALL, TAG_NONE, FIELD_RX_BYTES | FIELD_TX_BYTES);

            NetworkStatsHistory.Entry entry = null;
            long bucketDuration = networkStatsHistory.getBucketDuration();
            entry = networkStatsHistory.getValues(startTime, endTime, System.currentTimeMillis(), entry);
            //KLog.d(TAG, ">>>getDataByUid: rx=" + entry.rxBytes + ",tx=" + entry.txBytes);
            value = entry != null ? entry.rxBytes + entry.txBytes : 0;
            //final String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), value);
            long totalBytes = networkStatsHistory.getTotalBytes();
            int afterBucketCount2 = networkStatsHistory.getIndexAfter(startTime);
            int beforeBucketCount2 = networkStatsHistory.getIndexBefore(startTime);
            mStatsSession.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            //KLog.d(TAG, ">>>getMsimTotalData:finally");
        }
        //KLog.d(TAG, ">>>total_value1:" + value);
        return value;
    }

    /**
     * 获取据当前时间的一个月之前
     * <p>
     * //下面这里是获取这个月的一号
     * Calendar cal = Calendar.getInstance();
     * cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
     * cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
     *
     * @return
     */
    public static long getTimesMonthMorning() {
        //SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        //得到当前系统时间
        Date currentTime = new Date();
        //返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
        long now = currentTime.getTime();
        currentTime = new Date(now - 86400000 * 24);
        long now1 = currentTime.getTime();
        currentTime = new Date(now1 - 86400000 * 6);

        //String current = formatter.format(currentTime);
        return currentTime.getTime();
    }

    /**
     * 获取昨天的时间戳
     * @return
     */
    public static long getYesterDayTime() {
        long nowTime = System.currentTimeMillis();
        long yesterdayTime = nowTime - 86400000;
        return yesterdayTime;
    }




    /**
     * 得到现在的时间戳
     *
     * @return
     */
    public static long getNow() {
        long lTime = Calendar.getInstance().getTimeInMillis();
        return lTime;
    }

}
