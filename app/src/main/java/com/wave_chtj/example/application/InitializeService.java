package com.wave_chtj.example.application;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ZipUtils;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.screen_adapta.activitylifecycle.SCREEN_TYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


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
        KLog.d(TAG, "performInit");
        // CrashHandler.getInstance().init(getApplication());
        //KLog.d("performInit begin:" + System.currentTimeMillis());
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        //1080,1920是为了适配而去设置相关的值
        //设置宽度|高度布局尺寸 layout 布局文件以pt为单位 setBaseScreenParam(1080,1920,true)
        BaseIotUtils.instance().
                setBaseScreenParam(1080, 1920, true).
                setCreenType(SCREEN_TYPE.WIDTH).//按照宽度适配
                create(getApplication());
        initFile();
    }

    //zip保存的路径
    private static final String savePath = "/sdcard/aging.zip";
    // 文件名
    private static final String fileName = "aging.zip";
    //解压缩后的路径
    private static final String unZipPath = "/sdcard/";

    public void initFile() {
        try {
            //视频文件不存在时将文件保存到本地
            if (!new File(savePath).exists()) {
                InputStream input = getAssets().open(fileName);
                writeToLocal(savePath, input);
                boolean isUnzip = ZipUtils.unzipFile(savePath, unZipPath);
                if (isUnzip && new File(savePath).exists()) {
                    KLog.d(TAG, "Video ready！");
                } else {
                    KLog.d(TAG, "Video not ready！");
                }
            } else {
                KLog.d(TAG, "Aging_Test_Video.mp4 exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    /**
     * 将InputStream写入本地文件
     *
     * @param destDirPath 写入本地目录
     * @param input       输入流
     * @throws IOException
     */
    public static void writeToLocal(String destDirPath, InputStream input)
            throws IOException {

        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destDirPath);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }

}