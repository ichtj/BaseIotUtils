package com.wave_chtj.example.application;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.keeplive.BaseIotUtils;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.SCREEN_TYPE;
import com.wave_chtj.example.crash.CrashHandler;
import com.wave_chtj.example.greendao.DaoMaster;
import com.wave_chtj.example.greendao.DaoSession;


/**
 * Create on 2019/10/16
 * author chtj
 * desc 启动app时的优化
 */
public class InitializeService extends IntentService {
    private static final String TAG="InitializeService";
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
        KLog.d(TAG,"performInit");
        //CrashHandler.getInstance().init(getApplication());
        //KLog.d("performInit begin:" + System.currentTimeMillis());
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        //1080,1920是为了适配而去设置相关的值
        //设置宽度|高度布局尺寸 layout 布局文件以pt为单位 setBaseScreenParam(1080,1920,true)
        BaseIotUtils.instance().
                setBaseScreenParam(1080,1920,true).
                setCreenType(SCREEN_TYPE.HEIGHT).//按照宽度适配
                create(getApplication());

    }

    private static DaoSession mDaoSession;

    private void initGreenDao() {
        //创建数据库mydb.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(BaseIotUtils.getContext(),"mydb.db");
        //获取可写数据库
        SQLiteDatabase database = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(database);
        //获取Dao对象管理者
        mDaoSession = daoMaster.newSession();
    }
}