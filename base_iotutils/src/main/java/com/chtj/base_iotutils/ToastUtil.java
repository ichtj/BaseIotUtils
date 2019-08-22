package com.chtj.base_iotutils;
import android.widget.Toast;

import com.chtj.base_iotutils.back_service.BaseIotTools;

/**
 * Toast统一管理类
 */
public class ToastUtil {

    public static boolean isShow = true;

    /*cannot be instantiated*/
    private ToastUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort(CharSequence message) {
        if (isShow)
            Toast.makeText(BaseIotTools.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort(int message) {
        if (isShow)
            Toast.makeText(BaseIotTools.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(CharSequence message) {
        if (isShow)
            Toast.makeText(BaseIotTools.getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(int message) {
        if (isShow)
            Toast.makeText(BaseIotTools.getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show(CharSequence message, int duration) {
        if (isShow)
            Toast.makeText(BaseIotTools.getContext(), message, duration).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show(int message, int duration) {
        if (isShow)
            Toast.makeText(BaseIotTools.getContext(), message, duration).show();
    }

}
