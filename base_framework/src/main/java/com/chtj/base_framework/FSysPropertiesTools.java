package com.chtj.base_framework;

import android.os.SystemProperties;

public class FSysPropertiesTools {

    public static void setSysValue(String key, String value) {
        SystemProperties.set(key, value);
    }

    public static String getSysStringValue(String key, String defaultValue) {
        return SystemProperties.get(key, defaultValue);
    }

    public static int getSysIntValue(String key, int defaultValue) {
        return SystemProperties.getInt(key, defaultValue);
    }

    public static long getSysLongValue(String key, long defaultValue) {
        return SystemProperties.getLong(key, defaultValue);
    }

    public static boolean getSysBoolValue(String key, boolean defaultValue) {
        return SystemProperties.getBoolean(key, defaultValue);
    }
}
