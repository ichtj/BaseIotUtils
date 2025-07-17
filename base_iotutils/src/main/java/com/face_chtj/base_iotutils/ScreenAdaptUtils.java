package com.face_chtj.base_iotutils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;

/**
 * 屏幕适配工具类
 */
public class ScreenAdaptUtils {
    public enum AdaptBase {
        WIDTH, HEIGHT
    }
    private static float originalDensity;
    private static float originalScaledDensity;
    private static AdaptBase adaptBase = AdaptBase.WIDTH;
    private static float designSize = 1080f; // 默认设计图宽度1080dp

    public static void init(final Application application, final AdaptBase base, final float designDp) {
        adaptBase = base;
        designSize = designDp;

        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        if (originalDensity == 0f) {
            originalDensity = appDisplayMetrics.density;
            originalScaledDensity = appDisplayMetrics.scaledDensity;

            // 监听系统字体变化
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        originalScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {}
            });
        }

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                setCustomDensity(activity, application);
            }

            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityResumed(Activity activity) {}
            @Override public void onActivityPaused(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }

    private static void setCustomDensity(Activity activity, Application application) {
        final DisplayMetrics appMetrics = application.getResources().getDisplayMetrics();

        float targetDensity;
        if (adaptBase == AdaptBase.WIDTH) {
            targetDensity = appMetrics.widthPixels / designSize;
        } else {
            targetDensity = appMetrics.heightPixels / designSize;
        }

        float targetScaledDensity = targetDensity * (originalScaledDensity / originalDensity);
        int targetDensityDpi = (int) (160 * targetDensity);

        // 应用于 Activity
        DisplayMetrics activityMetrics = activity.getResources().getDisplayMetrics();
        activityMetrics.density = targetDensity;
        activityMetrics.scaledDensity = targetScaledDensity;
        activityMetrics.densityDpi = targetDensityDpi;

        // 应用于 Application（部分第三方库会使用 app context）
        appMetrics.density = targetDensity;
        appMetrics.scaledDensity = targetScaledDensity;
        appMetrics.densityDpi = targetDensityDpi;
    }
}
