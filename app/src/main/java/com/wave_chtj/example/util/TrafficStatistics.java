package com.wave_chtj.example.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Handler;
import android.util.Log;

import com.face_chtj.base_iotutils.KLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 统计最近一小时、一天的流量消耗
 */
public class TrafficStatistics extends TrafficStats {

    private static final String TAG = TrafficStatistics.class.getSimpleName();
    /**
     * 最近一次保存的时间
     */
    private static final String KEY_LAST_TIME = "KEY_LAST_TIME";
    /**
     * 最近保存的分钟流量消耗
     */
    private static final String KEY_MINUTE_DATA = "KEY_MINUTE_DATA";
    /**
     * 最近保存的小时流量消耗
     */
    private static final String KEY_HOURS_DATA = "KEY_HOURS_DATA";
    Handler handler = new Handler();
    //存储每分钟消耗的流量
    List<Long> minute_data = new ArrayList<>();
    //存储每小时消耗的流量
    List<Long> hours_data = new ArrayList<>();
    /**
     * 清零时间
     */
    private static String RESET_TIME1 = "23:58";
    /**
     * 清零时间
     */
    private static String RESET_TIME2 = "23:59";

    private long last_minute_data;

    private Context mContext;

    public TrafficStatistics(Context context) {
        mContext = context;
    }

    /**
     * 开始统计流量
     */
    public void statistics() {

        long last_time = get(KEY_LAST_TIME);
        long this_time = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

        String lastday = dateformat.format(last_time);
        String today = dateformat.format(this_time);
        if (!lastday.equals(today)) {//不是同一天，当天流量清零
            hours_data.clear();
            clearList(KEY_HOURS_DATA);
        } else {
            hours_data = getList(KEY_HOURS_DATA);
        }

        if ((this_time - last_time) / 1000 < 3600) {//间隔一小时内
            minute_data = getList(KEY_MINUTE_DATA);
        } else {
            minute_data.clear();
            clearList(KEY_MINUTE_DATA);
        }
        last_minute_data = getTotalRxBytes() + getTotalTxBytes();
        handler.postDelayed(caclDataRunnable, 60 * 1000);
    }

    int count;

    final Runnable caclDataRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                //每天结束前当天流量清零
                if (getTimeShort().equals(RESET_TIME1) || getTimeShort().equals(RESET_TIME2)) {
                    hours_data.clear();
                }

                count++;
                //存储每分钟消耗的流量
                if (minute_data.size() == 60) {
                    minute_data.remove(0);
                }
                //获取当前手机启动后 发送+接收的字节流量总数
                long this_minute_time_data = getTotalRxBytes() + getTotalTxBytes();
                //添加当前这一分钟消耗的流量
                long used_data = this_minute_time_data - last_minute_data;
                minute_data.add(used_data);
                //如果当前的时间达到一个小时=60分钟
                if (count == 60) {
                    //如果小时流量已存储为24小时的 删除第一个小时
                    if (hours_data.size() == 24) {
                        hours_data.remove(0);
                    }
                    //将当前这个小时的流量保存到一个小时消耗的流量中
                    hours_data.add(getRecentHoursData());
                    count = 0;
                }
                //最后一次总字节消耗
                last_minute_data = this_minute_time_data;
                long time = System.currentTimeMillis();
                sava(KEY_LAST_TIME, time);
                savaList(KEY_MINUTE_DATA, minute_data);
                savaList(KEY_HOURS_DATA, hours_data);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
            handler.postDelayed(this, 1 * 60 * 1000);
        }
    };


    /**
     * 获取最近一小时流量
     *
     * @return
     */
    public long getRecentHoursData() {
        long total = 0;
        for (int i = 0; i < minute_data.size(); i++) {
            total += minute_data.get(i);
        }
        return total;

    }

    /**
     * 获取当前app消耗的流量
     *
     * @return
     */
    public static long getUidFlow(int uid) {
        int systemuid =  android.os.Process.SYSTEM_UID;
        KLog.d(TAG,"systemuid="+systemuid);
        long receiveRx = getUidRxBytes(uid);//获取某个网络UID的接受字节数 总接收量
        long sendTx = getUidTxBytes(uid);//获取某个网络UID的发送字节数 总接收量
        KLog.d(TAG, "receiveRx: "+receiveRx+",sendTx："+sendTx+",uid="+uid);
        return receiveRx + sendTx;
    }


    public static double getDouble(double d){
        return  (double) Math.round(d * 100) / 100;
    }
    /**
     * 获取当天流量
     *
     * @return
     */
    public long getTodayData() {
        long total = 0;
        if (hours_data.size() == 0) {
            return getRecentHoursData();
        }

        for (int i = 0; i < hours_data.size(); i++) {
            total += hours_data.get(i);
            if (hours_data.size() != 24) {
                total += getRecentHoursData();
            }
        }
        return total;

    }


    /**
     * 获取时间 小时:分; HH:mm
     *
     * @return
     */
    public static String getTimeShort() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String dateString = formatter.format(new Date());
        //Log.d(TAG, "getTimeShort:dateString=: "+dateString);
        return dateString;
    }

    /**
     * 保存
     */
    private void savaList(String key, List<Long> value) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("save", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value.size());

        for (int i = 0; i < value.size(); i++) {
            editor.putLong(key + i, value.get(i));
        }
        editor.commit();
        editor.apply();
    }

    /**
     * 获取
     *
     * @return
     */
    private List<Long> getList(String key) {
        List<Long> list = new ArrayList<>();
        SharedPreferences sp = mContext.getSharedPreferences("save", Context.MODE_PRIVATE);
        int size = sp.getInt(key, 0);

        for (int i = 0; i < size; i++) {
            Long data = sp.getLong(key + i, 0);
            list.add(data);
        }
        return list;
    }


    /**
     * 清楚本地数据
     *
     * @return
     */
    private void clearList(String key) {
        SharedPreferences sp = mContext.getSharedPreferences("save", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int size = sp.getInt(key, 0);
        for (int i = 0; i < size; i++) {
            editor.putLong(key + i, 0);
        }
        editor.commit();
        editor.apply();
    }

    /**
     * 保存
     */
    private void sava(String key, Long value) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("save", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
        editor.apply();
    }

    /**
     * 获取
     *
     * @return
     */
    private Long get(String key) {
        List<Long> list = new ArrayList<>();
        SharedPreferences sp = mContext.getSharedPreferences("save", Context.MODE_PRIVATE);
        return sp.getLong(key, 0);
    }

}
