package com.face_chtj.base_iotutils.screen_adapta;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     time  : 2018/11/15
 *     desc  : utils about adapt screen 布局以pt为单位
 * </pre>
 */
public final class AdaptScreenUtils {

    //反射
    private static List<Field> sMetricsFields;

    private AdaptScreenUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Adapt for the horizontal screen, and call it in {@link android.app.Activity#getResources()}.
     * 例如：480*800 3.7尺寸屏幕 xhdpi=160.0 yhdpi=160.0
     * 下面的1080是我们在Application 中oncreate BaseIotUtils.instance()...create();中初始化填写的
     * newXdpi=(480*72.0)/1080=32.0
     */
    public static Resources adaptWidth( Resources resources, final int designWidth) {
        float newXdpi = (resources.getDisplayMetrics().widthPixels * 72f) / designWidth;
        applyDisplayMetrics(resources, newXdpi);
        return resources;
    }

    /**
     * Adapt for the vertical screen, and call it in {@link android.app.Activity#getResources()}.
     */
    public static Resources adaptHeight(final Resources resources, final int designHeight) {
        return adaptHeight(resources, designHeight, false);
    }

    /**
     * Adapt for the vertical screen, and call it in {@link android.app.Activity#getResources()}.
     */
    public static Resources adaptHeight(final Resources resources, final int designHeight, final boolean includeNavBar) {
        float screenHeight = (resources.getDisplayMetrics().heightPixels
                + (includeNavBar ? getNavBarHeight(resources) : 0)) * 72f;
        float newXdpi = screenHeight / designHeight;
        applyDisplayMetrics(resources, newXdpi);
        return resources;
    }

    private static int getNavBarHeight(final Resources resources) {
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return resources.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    /**
     * @param resources The resources.
     * @return the resource
     */
    public static Resources closeAdapt(final Resources resources) {
        float newXdpi = Resources.getSystem().getDisplayMetrics().density * 72f;
        applyDisplayMetrics(resources, newXdpi);
        return resources;
    }

    /**
     * Value of pt to value of px.
     *
     * @param ptValue The value of pt.
     * @return value of px
     */
    public static int pt2Px(final float ptValue) {
        DisplayMetrics metrics = BaseIotUtils.getContext().getResources().getDisplayMetrics();
        return (int) (ptValue * metrics.xdpi / 72f + 0.5);
    }

    /**
     * Value of px to value of pt.
     *
     * @param pxValue The value of px.
     * @return value of pt
     */
    public static int px2Pt(final float pxValue) {
        DisplayMetrics metrics = BaseIotUtils.getContext().getResources().getDisplayMetrics();
        return (int) (pxValue * 72 / metrics.xdpi + 0.5);
    }

    private static void applyDisplayMetrics(final Resources resources, final float newXdpi) {
        resources.getDisplayMetrics().xdpi = newXdpi;
        BaseIotUtils.getContext().getResources().getDisplayMetrics().xdpi = newXdpi;
        applyOtherDisplayMetrics(resources, newXdpi);
    }

    private static void applyOtherDisplayMetrics(final Resources resources, final float newXdpi) {
        if (sMetricsFields == null) {
            sMetricsFields = new ArrayList<>();
            Class resCls = resources.getClass();
            //获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段
            Field[] declaredFields = resCls.getDeclaredFields();
            while (declaredFields != null && declaredFields.length > 0) {
                for (Field field : declaredFields) {
                    //用来判断一个类Class1和另一个类Class2是否相同或是另一个类的超类或接口
                    if (field.getType().isAssignableFrom(DisplayMetrics.class)) {
                        //当isAccessible()的结果是false时不允许通过反射访问该字段
                        //isAccessible()的结果是true时作用就是让我们在用反射时访问私有变量
                        field.setAccessible(true);
                        DisplayMetrics tmpDm = getMetricsFromField(resources, field);
                        if (tmpDm != null) {
                            sMetricsFields.add(field);
                            tmpDm.xdpi = newXdpi;
                        }
                    }
                }
                //通过反射使用超类的私有属性
                resCls = resCls.getSuperclass();
                if (resCls != null) {
                    declaredFields = resCls.getDeclaredFields();
                } else {
                    break;
                }
            }
        } else {
            applyMetricsFields(resources, newXdpi);
        }
    }

    private static void applyMetricsFields(final Resources resources, final float newXdpi) {
        for (Field metricsField : sMetricsFields) {
            try {
                DisplayMetrics dm = (DisplayMetrics) metricsField.get(resources);
                if (dm != null) {
                    dm.xdpi = newXdpi;
                }
            } catch (Exception e) {
                //KLog.d("AdaptScreenUtils", "applyMetricsFields: " + e.getMessage());
            }
        }
    }

    private static DisplayMetrics getMetricsFromField(final Resources resources, final Field field) {
        try {
            //根据resoure,填充数据到field中
            return (DisplayMetrics) field.get(resources);
        } catch (Exception e) {
            //KLog.d("AdaptScreenUtils", "getMetricsFromField: " + e);
            return null;
        }
    }
}
