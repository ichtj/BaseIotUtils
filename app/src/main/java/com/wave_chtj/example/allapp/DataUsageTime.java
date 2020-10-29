package com.wave_chtj.example.allapp;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataUsageTime {


    public static long getTimesMonthMorning() {
        //获取这个月的一号
        /*Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));*/

        //获取据当前时间的一个月之前
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        Date currentTime = new java.util.Date();//得到当前系统时间
        long now = currentTime.getTime();//返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
        currentTime = new Date(now - 86400000 * 24);
        long now1 = currentTime.getTime();
        currentTime = new Date(now1 - 86400000 * 6);

        String current = formatter.format(currentTime);
        return currentTime.getTime();
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

