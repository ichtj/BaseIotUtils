package com.wave_chtj.example.keepservice;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.chtj.base_iotutils.keepservice.BaseIotUtils;
import com.chtj.base_iotutils.keepservice.IntentWrapper;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

public class KeepServiceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_keep_service);
        //初始化后台保活Service
        BaseIotUtils.initSerice(TraceServiceImpl.class, BaseIotUtils.DEFAULT_WAKE_UP_INTERVAL);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            //开启服务
            case R.id.btn_start:
                TraceServiceImpl.sShouldStopService = false;
                BaseIotUtils.startServiceMayBind(TraceServiceImpl.class);
                break;
            //处理白名单
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                break;
            //关闭服务
            case R.id.btn_stop:
                TraceServiceImpl.stopService();
                break;
        }
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }
}
