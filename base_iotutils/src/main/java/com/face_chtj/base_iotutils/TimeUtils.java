package com.face_chtj.base_iotutils;

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
 * {@link #timestampToDate(String,long)} 获取今天年月日
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
//    public static final String TIME_YMD="yyyy-MM-dd";
//    public static final String TIME_YMDHM="yyyy-MM-dd HH:mm";
//    public static final String TIME_YMDHMS="yyyy-MM-dd HH:mm:ss";
//    public static final String TIME_YMDHMS_CN="yyyy年MM月dd日 HH时mm分ss秒";
//    public static final String TIME_YMDHMS_UNSIGNED="yyyyMMddHHmmss";
    public static final String DATE_FORMAT_MERGE = "yyyyMMddHHmmss"; // 默认格式，包括日期和时间
    public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss"; // 默认格式，包括日期和时间
    public static final String DATE_FORMAT_DATE_ONLY = "yyyy-MM-dd"; // 仅包含日期，不包括时间
    public static final String DATE_FORMAT_TIME_ONLY = "HH:mm:ss"; // 仅包含时间，不包括日期
    public static final String DATE_FORMAT_CUSTOM = "yyyy/MM/dd HH-mm-ss"; // 自定义格式
    public static final String DATE_FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; // ISO 8601格式
    public static final String DATE_FORMAT_SHORT_DATE = "MM/dd/yyyy"; // 短日期格式，例如：03/28/2024
    public static final String DATE_FORMAT_SHORT_DATE_TIME = "MM/dd/yyyy HH:mm"; // 短日期时间格式，例如：03/28/2024 15:30
    public static final String DATE_FORMAT_SHORT_TIME = "HH:mm"; // 短时间格式，例如：15:30
    public static final String DATE_FORMAT_WEEKDAY = "EEEE"; // 星期几，例如：星期一、星期二
    public static final String DATE_FORMAT_MONTH = "MMMM"; // 月份，例如：一月、二月
    public static final String DATE_FORMAT_YEAR = "yyyy"; // 年份，例如：2024
    /**
     * 时间戳 转 String类型的年月日
     * @param time
     * @return
     */
    public static String timestampToDate(String pattern,long time) {
        return new SimpleDateFormat(pattern, Locale.CHINA).format(time * 1000);
    }
    /**
     * 获取今天年月日
     */
    public static String getTodayDate() {
        return new SimpleDateFormat(DATE_FORMAT_DATE_ONLY, Locale.CHINA).format(new Date());
    }

    /**
     * 获取今天年月日时分秒
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
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DATE_ONLY);
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
