package com.face_chtj.base_iotutils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Create on 2020/3/5
 * author chtj
 * desc Service管理工具
 * {@link #isWorked(String)} Service是否正在运行
 * {@link #stopService(String)} 停止Service
 */
public class ServiceUtils {
    /**
     * 获取Service是否正在运行
     * @param servicePath 完整包名的服务类名 com.xxx.xxx.XXService
     * @return true正在运行|false没有运行
     */
    public static boolean isWorked(String servicePath) {
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

    /**
     * 停止服务
     *
     * @param className 完整包名的服务类名 com.xxx.xxx.XXService
     * @return {@code true}: 停止成功<br>{@code false}: 停止失败
     */
    public static boolean stopService(String className) {
        try {
            Intent intent = new Intent(BaseIotUtils.getContext(), Class.forName(className));
            return BaseIotUtils.getContext().stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据包名及Service路径启动这个service
     *
     * @param packName    包名
     * @param servicePath Service路劲
     */
    public static void startService(String packName, String servicePath) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packName, servicePath));
        BaseIotUtils.getContext().startService(intent);
    }
}
