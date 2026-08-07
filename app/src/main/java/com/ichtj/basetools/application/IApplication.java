package com.ichtj.basetools.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.chtj.base_framework.FBaseTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ScreenAdaptUtils;

/**
 * Create on 2019/11/5
 * author chtj
 * desc
 */
public class IApplication extends Application {
    private static final String TAG = IApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: IApplication");
        if (false) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(this); // 尽可能早，推荐在Application中初始化
        KLog.init(true);
//        CrashHandler.getInstance().init(getApplication());
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        BaseIotUtils.instance().create(this);
        ScreenAdaptUtils.init(this, ScreenAdaptUtils.AdaptBase.WIDTH, 1080f);
        FBaseTools.instance().create(this);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
