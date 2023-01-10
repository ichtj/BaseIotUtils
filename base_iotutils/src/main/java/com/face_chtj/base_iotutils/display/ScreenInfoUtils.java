package com.face_chtj.base_iotutils.display;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;


/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:屏幕相关工具类
 * --获取屏幕宽度 {@link #getScreenWidth (Context context)}
 * --获取屏幕高度 {@link #getScreenHeight(Context context)}
 * --获取屏幕像素，尺寸，dpi相关信息 {@link #getScreenInfo(Activity activity)}
 * --设置app内屏幕亮度 {@link #setAppScreenBrightness(Activity, int)} )}
 * --设置系统屏幕亮度 {@link #setAppScreenBrightness(Activity, int)} )}
 */
public class ScreenInfoUtils {
    private static final String TAG = "ScreenInfoUtils";
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
        return "screenSize=" + screenInches + ",densityDpi=" + displayMetrics.densityDpi + ",width=" + displayMetrics.widthPixels + ",height=" + displayMetrics.heightPixels;
    }

    /**
     * 设置 APP界面屏幕亮度值方法
     **/
    public static void setAppScreenBrightness(Activity aty,int birghtessValue) {
        Window window = aty.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = birghtessValue / 255.0f;
        window.setAttributes(lp);
    }

    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0  为手动调节屏幕亮度
     */
    public static void  setScreenMode(int mode){
        try{
            Settings.System.putInt(BaseIotUtils.getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
        }catch (Exception localException){
            KLog.d(TAG," errMeg= "+localException.getMessage());
            localException.printStackTrace();
        }
    }

    /**
     * 获取系统默认屏幕亮度值 屏幕亮度值范围（0-255）
     * **/
    public static int getScreenBrightness() {
        ContentResolver contentResolver = BaseIotUtils.getContext().getContentResolver();
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1);
    }


    /**
     * 保存当前的屏幕亮度值，并使之生效
     */
    public static void setSysScreenBrightness(int birghtessValue) {
        // 首先需要设置为手动调节屏幕亮度模式
        setScreenManualMode();
        ContentResolver contentResolver = BaseIotUtils.getContext().getContentResolver();
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, birghtessValue);
    }


    /**
     * 3.关闭光感，设置手动调节背光模式
     *
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC 自动调节屏幕亮度模式值为1
     *
     * SCREEN_BRIGHTNESS_MODE_MANUAL 手动调节屏幕亮度模式值为0
     * **/
    public static void setScreenManualMode() {
        ContentResolver contentResolver =  BaseIotUtils.getContext().getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

}
