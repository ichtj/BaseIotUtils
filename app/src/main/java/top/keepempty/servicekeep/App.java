package top.keepempty.servicekeep;

import android.app.Application;

import com.chtj.base_iotutils.back_service.BaseIotTools;
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        BaseIotTools.initialize(this, TraceServiceImpl.class, /*DaemonEnv.DEFAULT_WAKE_UP_INTERVAL*/5000);
        TraceServiceImpl.sShouldStopService = false;
        BaseIotTools.startServiceMayBind(TraceServiceImpl.class);
    }
}
