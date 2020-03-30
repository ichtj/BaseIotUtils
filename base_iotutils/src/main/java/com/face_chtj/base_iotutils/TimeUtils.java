package com.face_chtj.base_iotutils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Create on 2020/3/9
 * author chtj
 * desc 时间工具类
 */
public class TimeUtils {
    /**
     * 时间戳 转 String类型的精确到时分秒
     * @param time
     * @return
     */
    public static String tsToMs(int time) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        long time1000 = Long.parseLong(String.valueOf(time)) * 1000;
        return fm.format(time1000);
    }

    /**
     * 时间戳 转 String类型的年月日
     * @param time
     * @return
     */
    public static String tsToYMD(int time) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        long time1000 = Long.parseLong(String.valueOf(time)) * 1000;
        return fm.format(time1000);
    }
    /**
     * 获取今天年月日
     * @return 2017-08-14
     */
    public static String getTodayDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return sdf.format(date);
    }

    /**
     *  当前毫秒级时间
     * @return 2017/04/07-11:01:06:109
     */
    public static String getMillisecondTime() {
        return new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS", Locale.CHINA).format(new Date());
    }
    /**
     * 获取当前系统的时间戳
     * @return 1502697135
     */
    public static int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static String tsToYMDcn(int time) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy年MM月dd日");
        long time1000 = Long.parseLong(String.valueOf(time)) * 1000;
        return fm.format(time1000);
    }

    public static String getTodayAddMonthDate(int month) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = null;
        try {
            now = sdf.parse(getTodayDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, month);
        return sdf.format(calendar.getTime());


    }
    /**
     * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm"<br />
     * 如果获取失败，返回null
     * @return
     */
    public static String getUTCTimeStr() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
        StringBuffer UTCTimeBuffer = new StringBuffer();
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance() ;
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        UTCTimeBuffer.append(year).append("-").append(month).append("-").append(day);
        UTCTimeBuffer.append("T").append(hour).append(":").append(minute).append(":").append(second) ;
        return UTCTimeBuffer.toString();

    }

}
