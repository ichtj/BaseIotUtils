package com.face_chtj.base_iotutils.convert;

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
 *
 * {@link #tsToMs(int)} yyyy-MM-dd HH:mm:ss
 * {@link #tsToYMD(int)} yyyy-MM-dd
 * {@link #getTodayDate()} 获取今天年月日
 * {@link #getTodayDateHms(String)} 获取今天年月日 可添加格式
 * {@link #getTime()} 获取当前系统的时间戳
 * {@link #getTodayAddMonthDate(int)} 获取今天添加月份的日期
 * {@link #getUTCTimeStr()} 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm
 * {@link #differentDaysByMillisecond(Date, Date)} 两个Date间隔天数计算
 * {@link #getWeekOfDate()} 获取当前日期是星期几
 *
 */
public class TimeUtils {
    /**
     * 时间戳 转 String类型的精确到时分秒
     * @param time
     * @return
     */
    public static String tsToMs(int time) {
        long time1000 = Long.parseLong(String.valueOf(time)) * 1000;
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(time1000);
    }

    /**
     * 时间戳 转 String类型的年月日
     * @param time
     * @return
     */
    public static String tsToYMD(int time) {
        long time1000 = Long.parseLong(String.valueOf(time)) * 1000;
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(time1000);
    }
    /**
     * 获取今天年月日
     * @return 2017-08-14
     */
    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
    }

    /**
     * 获取今天年月日时分秒
     * yyyy-MM-dd HH:mm:ss
     * @return 2017-08-14 11:53:52
     */
    public static String getTodayDateHms(String pattern) {
        return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date());
    }

    /**
     * 获取当前系统秒级别的时间戳
     * @return 1502697135
     */
    public static int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * 获取当前系统毫秒的时间戳
     * @return 1502697135
     */
    public static long getTimeMs() {
        return System.currentTimeMillis();
    }

    /**
     * 获取今天添加月份的日期
     * @param month 添加的月份
     * @return 日期
     */
    public static String getTodayAddMonthDate(int month) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date now =sdf.parse(getTodayDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MONTH, month);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm
     * 如果获取失败，返回null
     * @return
     */
    public static String getUTCTimeStr() {
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


    /**
     * 两个Date间隔天数计算
     * @param after 临近的时间
     * @param before 之前的时间
     * @return 间隔天数
     */
    public static int differentDaysByMillisecond(Date after, Date before) {
        return Math.abs((int) ((after.getTime() - before.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate() {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

}
