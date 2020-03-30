package com.face_chtj.base_iotutils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;


/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:屏幕相关工具类
 * --获取屏幕宽度 {@link #getScreenWidth (Context context)}
 * --获取屏幕高度 {@link #getScreenHeight(Context context)}
 * --获取屏幕像素，尺寸，dpi相关信息 {@link #getScreenInfo(Activity activity)}
 */
public class ScreenInfoUtils {
    /**
     * 获取屏幕宽度
     *
     * @param context Context
     * @return 屏幕宽度（px）
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * 获取屏幕高度
     *
     * @param   context  Context
     * @return 屏幕高度  (px)
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * 获取屏幕像素，尺寸，dpi相关信息
     *
     * @param activity 上下文
     * @return 屏幕信息
     */
    public static String getScreenInfo(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //4.2开始有虚拟导航栏，增加了该方法才能准确获取屏幕高度
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        } else {
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            //displayMetrics = activity.getResources().getDisplayMetrics();//或者该方法也行
        }
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        double x = Math.pow(point.x / displayMetrics.xdpi, 2);//dm.xdpi是屏幕x方向的真实密度值，比上面的densityDpi真实。
        double y = Math.pow(point.y / displayMetrics.ydpi, 2);//dm.xdpi是屏幕y方向的真实密度值，比上面的densityDpi真实。
        double screenInches = Math.sqrt(x + y);
        return "screenSize=" + screenInches
                + ",densityDpi=" + displayMetrics.densityDpi
                + ",width=" + displayMetrics.widthPixels
                + ",height=" + displayMetrics.heightPixels;
    }

}
