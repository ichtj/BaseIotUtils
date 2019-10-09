package com.wave_chtj.example.application;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.chtj.base_iotutils.keepservice.BaseIotUtils;
import com.chtj.base_iotutils.screen_adapta.activitylifecycle.SCREEN_TYPE;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.tinker.entry.DefaultApplicationLike;

/**
 * Create on 2019/9/29
 * author chtj
 */
public class SampleApplicationLike  extends DefaultApplicationLike {

    public static final String TAG = "Tinker.SampleApplicationLike";

    public SampleApplicationLike(Application application, int tinkerFlags,
                                 boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime,
                                 long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        Bugly.init(getApplication(), "0e875eba19", true);
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        //setBaseWidth setBaseDpi 是为了适配而去设置相关的值

        BaseIotUtils.instance().
                setBaseWidth(1080).//设置宽度布局尺寸
                setBaseHeight(1920).//设置高度布局尺寸
                setCreenType(SCREEN_TYPE.WIDTH).//按照宽度适配
                setAutoScreenAdaptation(true).//开启自动适配 true 开启  false关闭
                        create(getApplication());

    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }

}