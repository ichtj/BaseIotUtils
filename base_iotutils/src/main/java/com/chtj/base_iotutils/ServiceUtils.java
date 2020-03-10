package com.chtj.base_iotutils;

import android.app.ActivityManager;
import android.content.Context;

import com.chtj.base_iotutils.keeplive.BaseIotUtils;

import java.util.ArrayList;

/**
 * Create on 2020/3/5
 * author chtj
 * desc Service管理工具
 */
public class ServiceUtils {
    /**
     * 获取Service是否正在运行
     * @param servicePath
     * @return true正在运行|false没有运行
     */
    private boolean isWorked(String servicePath) {
        ActivityManager myManager = (ActivityManager) BaseIotUtils.getContext().getApplicationContext().getSystemService(
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
