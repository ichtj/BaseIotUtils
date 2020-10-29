package com.wave_chtj.example.allapp;

import android.content.Context;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;

import static android.net.NetworkStats.SET_ALL;
import static android.net.NetworkStats.TAG_NONE;
import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;

/**
 * 该工具类目前只针对RK 3288 7.1.2系统对以太网流量消耗的统计
 */
public class EthDataUsageUtils {
    private static final String TAG = EthDataUsageUtils.class.getName();

    private static EthDataUsageUtils ethDataUsageUtils;
    INetworkStatsService mStatsService;

    //单例模式
    public static EthDataUsageUtils getInstance() {
        if (ethDataUsageUtils == null) {
            synchronized (EthDataUsageUtils.class) {
                if (ethDataUsageUtils == null) {
                    ethDataUsageUtils = new EthDataUsageUtils();


                }
            }
        }
        return ethDataUsageUtils;
    }

    /**
     * 获取系统总计消耗的流量
     *
     * @param startTime 开始时间戳
     * @param endTime   结束时间戳
     * @return 系统总计消耗的流量
     */
    public long getSystemTotalUsageData(long startTime, final long endTime) {
        KLog.d(TAG, "getMsimTotalData: start =" + startTime + ",endtime=" + endTime);
        long value = 0;
        try {
            // wait a few seconds before kicking off
            mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            Thread.sleep(2 * DateUtils.SECOND_IN_MILLIS);
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
            final String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), value);
            long totalBytes = networkStatsHistory.getTotalBytes();
            int afterBucketCount2 = networkStatsHistory.getIndexAfter(startTime);
            int beforeBucketCount2 = networkStatsHistory.getIndexBefore(startTime);
            KLog.i(TAG, "afterBucketCount2:" + afterBucketCount2 + ",beforeBucketCount2:" + beforeBucketCount2);
            KLog.d(TAG, "bucketDuration =" + bucketDuration + "totalPhrase:" + totalPhrase + ",totalBytes:" + totalBytes);
            mStatsSession.close();
        } catch (RemoteException e) {
            e.printStackTrace();
            KLog.d(TAG, "getMsimTotalData:>e1=" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            KLog.d(TAG, "getMsimTotalData:>e2=" + e.getMessage());
        } finally {
            KLog.d(TAG, "getMsimTotalData:finally");
        }
        KLog.d(TAG, "total_value1:" + value);
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
    public long getAppDataUsageByUid(int uid, long startTime, long endTime) {
        KLog.d(TAG, ">>>getMsimTotalData: start =" + startTime + ",endtime=" + endTime + ",uid=" + uid);
        long value = 0;
        try {
            mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            // wait a few seconds before kicking off
            //强制更新
            mStatsService.forceUpdate();
            INetworkStatsSession mStatsSession = mStatsService.openSession();
            NetworkTemplate mTemplate = NetworkTemplate.buildTemplateEthernet();
            NetworkStatsHistory networkStatsHistory = mStatsSession.getHistoryForUid(mTemplate, uid, SET_ALL, TAG_NONE, FIELD_RX_BYTES | FIELD_TX_BYTES);

            NetworkStatsHistory.Entry entry = null;
            long bucketDuration = networkStatsHistory.getBucketDuration();
            entry = networkStatsHistory.getValues(startTime, endTime, System.currentTimeMillis(), entry);
            KLog.d(TAG, ">>>getDataByUid: rx=" + entry.rxBytes + ",tx=" + entry.txBytes);
            value = entry != null ? entry.rxBytes + entry.txBytes : 0;
            //final String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), value);
            long totalBytes = networkStatsHistory.getTotalBytes();
            int afterBucketCount2 = networkStatsHistory.getIndexAfter(startTime);
            int beforeBucketCount2 = networkStatsHistory.getIndexBefore(startTime);
            KLog.i(TAG, ">>>afterBucketCount2:" + afterBucketCount2 + ",beforeBucketCount2:" + beforeBucketCount2);
            KLog.d(TAG, ">>>bucketDuration =" + bucketDuration +/* ",totalPhrase:" + totalPhrase +*/ ",totalBytes:" + totalBytes);
            mStatsSession.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            KLog.d(TAG, ">>>getMsimTotalData:finally");
        }
        KLog.d(TAG, ">>>total_value1:" + value);
        return value;
    }


}
