package com.ichtj.basetools.application;

import android.app.Application;
import android.app.IntentService;
import android.content.Intent;

import com.chtj.base_framework.FBaseTools;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.ScreenAdaptUtils;


/**
 * Create on 2019/10/16
 * author chtj
 * desc 启动app时的优化
 */
public class InitializeService extends IntentService {
    private static final String TAG=InitializeService.class.getSimpleName();
    private static final String ACTION_INIT_WHEN_APP_CREATE = "com.anly.githubapp.service.action.INIT";
    private static Application mContext;
    public InitializeService() {
        super("InitializeService");
    }

    public static void start(Application context) {
        mContext=context;
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
//        CrashHandler.getInstance().init(getApplication());
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        BaseIotUtils.instance().create(mContext);
        ScreenAdaptUtils.init(mContext, ScreenAdaptUtils.AdaptBase.HEIGHT, 1920f);
        FBaseTools.instance()
                .create(getApplication());
    }

}