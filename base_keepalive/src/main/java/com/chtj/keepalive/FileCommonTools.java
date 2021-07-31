package com.chtj.keepalive;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class FileCommonTools {
    private static final String TAG = "FileCommonTools";

    /**
     * 应用保活
     */
    public static final String SAVE_KEEPLIVE_PATH = "/sdcard/keepalive/";
    public static final String SAVE_KEEPLIVE_FILE_NAME = "keepalive.txt";

    /**
     * 读取文件内容
     *
     * @param fileName 路径+文件名称
     * @return 读取到的内容
     */
    public static String readFileData(String fileName) {
        String result = "";
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return "";
            }
            FileInputStream fis = new FileInputStream(file);
            //获取文件长度
            int lenght = fis.available();
            byte[] buffer = new byte[lenght];
            fis.read(buffer);
            if (fis != null) {
                fis.close();
            }
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "readFileData: " + e.getMessage());
        }
        return result;
    }

    /**
     * 写入数据
     *
     * @param filename 路径+文件名称
     * @param content  写入的内容
     * @param isCover  是否覆盖文件的内容 true 覆盖原文件内容  | flase 追加内容在最后
     * @return 是否成功 true|false
     */
    public static boolean writeFileData(String filename, String content, boolean isCover) {
        FileOutputStream fos = null;
        try {
            File file = new File(filename);
            //如果文件不存在
            if (!file.exists()) {
                //重新创建文件
                file.createNewFile();
            }
            fos = new FileOutputStream(file, !isCover);
            byte[] bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "writeFileData: " + e.getMessage());
        } finally {
            try {
                fos.close();//关闭文件输出流
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 获取该包名中的主界面
     *
     * @param packageName
     * @return
     */
    private static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    /**
     * 启动第三方apk
     * <p>
     * 如果已经启动apk，则直接将apk从后台调到前台运行（类似home键之后再点击apk图标启动），如果未启动apk，则重新启动
     */
    public static void openApk(Context context, String packName) {
        if (needStartAty(context, packName)) {
            Intent intent = getAppOpenIntentByPackageName(context, packName);
            context.startActivity(intent);
            Log.d(TAG, "launch this apk...  packagename=" + packName);
        } else {
            boolean isFindPkg=false;
            PackageManager packageManager =context.getPackageManager();
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
            for (int i = 0; i < pinfo.size(); i++) {
                if (pinfo.get(i).packageName.equalsIgnoreCase(packName)) {
                    isFindPkg=true;
                    break;
                }
            }
            Log.d(TAG, isFindPkg?"find this packageName "+packName:" not find this packageName"+packName);
        }
    }


    /**
     * 启用其他应用中的Service
     *
     * @param packName           包名
     * @param servicePackageName service包名路径
     */
    public static void openService(Context context, String packName, String servicePackageName) {
        try {
            Log.d(TAG, "launch this service...  servicePackageName=" + servicePackageName);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packName, servicePackageName));
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    /**
     * 判断该包名的应用是否存在
     *
     * @param packageName
     * @return
     */
    private static boolean needStartAty(Context context, String packageName) {
        boolean isNeedStartAty = false;
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            String topPkg = ((ActivityManager.RunningTaskInfo) list.get(0)).topActivity.getPackageName();
            //return list != null ? ((RunningTaskInfo)list.get(0)).topActivity.getPackageName() : null;
            if (topPkg != null && packageName.equals(topPkg)) {
                //证明应用已存在 并且已运行在前台
                isNeedStartAty = false;
            } else {
                //表示apk未运行在前台
                isNeedStartAty = true;
            }
        }
        return isNeedStartAty;
    }
}
