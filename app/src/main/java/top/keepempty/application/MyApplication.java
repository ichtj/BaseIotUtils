package top.keepempty.application;

import android.app.Application;

import com.chtj.base_iotutils.back_service.BaseIotTools;
import com.chtj.base_iotutils.screen.activitylifecycle.SCREEN_TYPE;

import top.keepempty.servicekeep.TraceServiceImpl;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        //setBaseWidth setBaseDpi 是为了适配而去设置相关的值

        BaseIotTools.instance().
                setBaseWidth(1080).//设置宽度布局尺寸
                setBaseHeight(1920).//设置高度布局尺寸
                setCreenType(SCREEN_TYPE.WIDTH).//按照宽度适配
                setAutoScreenAdaptation(false).//开启自动适配 true 开启  false关闭
                initSerice(TraceServiceImpl.class, /*DaemonEnv.DEFAULT_WAKE_UP_INTERVAL*/5000).//是否初始化后台保活Service
                        create(this);

        TraceServiceImpl.sShouldStopService = false;
        //开启后台保活服务
        BaseIotTools.startServiceMayBind(TraceServiceImpl.class);
    }
}
