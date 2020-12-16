package com.chtj.framework.keeplive;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.chtj.framework.FBaseService;
import com.chtj.framework.FBaseTools;

import java.util.ArrayList;

/**
 * 应用保活
 */
public class KeepLiveUtils {

    /**
     * 开启保活服务
     *
     * @param pullTime 拉起时间
     * @param pkgs     包名
     */
    public static void startKeepLive(int pullTime, String... pkgs) {
        if (!isWorked(FBaseService.class.getName())) {
            FBaseTools.getContext().startService(new Intent(FBaseTools.getContext(), FBaseService.class));
        }
    }

    /**
     * 关闭保活服务
     */
    public static void stopKeepLive() {
        FBaseTools.getContext().stopService(new Intent(FBaseTools.getContext(), FBaseService.class));
    }

    /**
     * 获取Service是否正在运行
     *
     * @param servicePath 完整包名的服务类名 com.xxx.xxx.XXService
     * @return true正在运行|false没有运行
     */
    private static boolean isWorked(String servicePath) {
        ActivityManager myManager = (ActivityManager) FBaseTools.getContext().getApplicationContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(servicePath)) {
                return true;
            }
        }
        return false;
    }
}
