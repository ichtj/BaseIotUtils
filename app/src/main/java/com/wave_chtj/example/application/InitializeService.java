package com.wave_chtj.example.application;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.chtj.framework.FBaseTools;
import com.chtj.framework.entity.DeviceType;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.SCREEN_TYPE;


/**
 * Create on 2019/10/16
 * author chtj
 * desc 启动app时的优化
 */
public class InitializeService extends IntentService {
    private static final String TAG = "InitializeService";
    private static final String ACTION_INIT_WHEN_APP_CREATE = "com.anly.githubapp.service.action.INIT";

    public InitializeService() {
        super("InitializeService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT_WHEN_APP_CREATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_WHEN_APP_CREATE.equals(action)) {
                performInit();
            }
        }
    }

    private void performInit() {
        KLog.init(true);
        // CrashHandler.getInstance().init(getApplication());
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        //1080,1920是为了适配而去设置相关的值
        //设置宽度|高度布局尺寸 layout 布局文件以pt为单位 setBaseScreenParam(1080,1920,true)
        BaseIotUtils.instance().
                setBaseScreenParam(1080, 1920, true).
                setCreenType(SCREEN_TYPE.WIDTH).//按照宽度适配
                create(getApplication());
        //frameworkjarUtils
        FBaseTools.instance(DeviceType.DEVICE_RK3288).create(getApplication());
    }





}