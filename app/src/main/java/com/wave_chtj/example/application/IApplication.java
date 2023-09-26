package com.wave_chtj.example.application;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.face_chtj.base_iotutils.NetMonitorUtils;

/**
 * Create on 2019/11/5
 * author chtj
 * desc
 */
public class IApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (false) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(this); // 尽可能早，推荐在Application中初始化
        InitializeService.start(this);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
