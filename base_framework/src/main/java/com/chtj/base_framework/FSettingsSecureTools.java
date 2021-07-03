package com.chtj.base_framework;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

public class FSettingsSecureTools {
    public static void setIntValue(Context context, String key, int value) {
        Settings.Secure.putInt(context.getContentResolver(), key, value);
    }

    public static void setLongValue(Context context, String key, long value) {
        Settings.Secure.putLong(context.getContentResolver(), key, value);
    }

    public static void setFloatValue(Context context, String key, float value) {
        Settings.Secure.putFloat(context.getContentResolver(), key, value);
    }

    public static void setStringValue(Context context, String key, String value) {
        Settings.Secure.putString(context.getContentResolver(), key, value);
    }

    public static int getIntValue(Context context, String key, int defaultValue) {
        return Settings.Secure.getInt(context.getContentResolver(), key, defaultValue);
    }

    public static long getLongValue(Context context, String key, long defaultValue) {
        return Settings.Secure.getLong(context.getContentResolver(), key, defaultValue);
    }

    public static float getFloatValue(Context context, String key, float defaultValue) {
        return Settings.Secure.getFloat(context.getContentResolver(), key, defaultValue);
    }

    public static String getStringValue(Context context, String defaultValue) {
        return Settings.Secure.getString(context.getContentResolver(), defaultValue);
    }

    public static Uri getUriValue(Uri uri, String name) {
        return Settings.Secure.getUriFor(uri, name);
    }

    public static Uri getUriValue(String name) {
        return Settings.Secure.getUriFor(name);
    }
}
