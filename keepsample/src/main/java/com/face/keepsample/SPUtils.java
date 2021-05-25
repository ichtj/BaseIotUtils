package com.face.keepsample;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * 存储工具类
 *
 * 调用方式：SPUtils.putString("you_Key", udid);
 *
 * {@link #putBoolean(String, boolean)} 存储Boolean类型的值
 * {@link #putInt(String, int)} 存储Int类型的值
 *
 * {@link #getString(String, String)} 获取String类型的值
 * {@link #getBoolean(String, Boolean)} 获取Boolean类型的值
 * {@link #getInt(String, int)} 获取Int类型的值
 *
 * {@link #deleAll()} 删除全部 key
 * {@link #deleShare(String)} 删除 单个 key
 */
public class SPUtils {
    private static final String NAME = "config";

    /**
     * 存储String类型的值
     * 
     * @param key      key值
     * @param value    要存储的String值
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).commit();
    }

    /**
     * 获取String类型的值
     * 
     * @param key      key
     * @param defValue 默认值
     * @return
     */
    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defValue);
    }


    /**
     * 存储Int类型的值
     * 
     * @param key      key
     * @param value    要存储的Int值
     */
    public static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key, value).commit();
    }

    /**
     * 获取Int类型的值
     *
     * @param key      key
     * @param defValue 默认值
     * @return
     */
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defValue);
    }

    /**
     * 存储Long类型的值
     *
     * @param key      key
     * @param value    要存储的long值
     */
    public static void putLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(key, value).commit();
    }


    /**
     * 获取Long类型的值
     *
     * @param key      key
     * @param defValue 默认值
     * @return
     */
    public static long getLong(Context context,  String key, long defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, defValue);
    }


    /**
     * 存储Boolean类型的值
     * 
     * @param key      key
     * @param value    要存储Boolean值
     */
    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    /**
     * 获取Boolean类型的值
     * 
     * @param key      key
     * @param defValue 默认值
     * @return
     */
    public static boolean getBoolean(Context context, String key, Boolean defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defValue);
    }

    /**
     * 删除 单个 key
     * @param key key值
     */
    public static void deleShare(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(key).commit();
    }

    /**
     * 删除全部 key
     */
    public static void deleAll(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }
}