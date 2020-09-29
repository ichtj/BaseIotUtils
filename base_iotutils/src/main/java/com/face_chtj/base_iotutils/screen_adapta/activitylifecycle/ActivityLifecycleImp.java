package com.face_chtj.base_iotutils.screen_adapta.activitylifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created to :Activity启动生命周期的监听.
 */

public class ActivityLifecycleImp implements Application.ActivityLifecycleCallbacks {

    /**
     * 让 {@link Fragment} 支持自定义适配参数
     */
    private FragmentLifecycleCallbacksImpl mFragmentLifecycleCallbacks;
    /**
     * 屏幕适配逻辑策略类
     */
    private AutoAdaptStrategy mAutoAdaptStrategy;

    public ActivityLifecycleImp(AutoAdaptStrategy autoAdaptStrategy) {
        mFragmentLifecycleCallbacks = new FragmentLifecycleCallbacksImpl(autoAdaptStrategy);
        this.mAutoAdaptStrategy = autoAdaptStrategy;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof FragmentActivity) {
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, true);
        }
        //Activity 中的 setContentView(View) 一定要在 super.onCreate(Bundle); 之后执行
        if (mAutoAdaptStrategy != null) {
            mAutoAdaptStrategy.applyAdapt(activity, activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (mAutoAdaptStrategy != null) {
            mAutoAdaptStrategy.applyAdapt(activity, activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    /**
     * 设置屏幕适配逻辑策略类
     *
     * @param autoAdaptStrategy {@link AutoAdaptStrategy}
     */
    public void setAutoAdaptStrategy(AutoAdaptStrategy autoAdaptStrategy) {
        mAutoAdaptStrategy = autoAdaptStrategy;
        mFragmentLifecycleCallbacks.setAutoAdaptStrategy(autoAdaptStrategy);
    }
}
