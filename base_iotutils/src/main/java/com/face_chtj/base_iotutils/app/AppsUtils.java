package com.face_chtj.base_iotutils.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.StringUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;
import com.face_chtj.base_iotutils.entity.ProcessEntity;
import com.face_chtj.base_iotutils.BaseIotUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:AppsUtils相关工具类
 * --查询桌面所有应用 {@link #getDeskTopAppList()}
 * --查询设备内非系统应用 {@link #getNormalAppList()}
 * --查询手机内系统应用 {@link #getSystemAppList()}
 * --获取当前应用名称 {@link #getAppName(String packageName)}
 * --根据包名获取进程PID {@link #getPidByPackageName(String packagename)}
 * --获取APP-VersionCode {@link #getAppVersionCode()}
 * --获取APP-VersionName {@link #getAppVersionName()}
 * --获取getSdkVersion {@link #getSdkVersion()}
 * --获取getAndroidVersion {@link #getAndroidVersion()}
 * --判断 App 是否处于前台 {@link #isAppForeground()}
 * --根据包名启动app {@link #startApp(String)}
 * --获得该包名的应用中的主界面 {@link #getMainIntent(String)}}
 * --根据包名获取APP是否正在运行 {@link #isAppRunning(String)}
 */
public class AppsUtils {
    private static final String TAG = "AppsUtils";

    /**
     *获取当前系统使用的android api版本号
     */
    public static int getSdkVersion(){
        return android.os.Build.VERSION.SDK_INT;
    }


    /**
     *获取当前系统的android版本
     * 例如android4.4 android7.1.2 android11等
     */
    public static String getAndroidVersion(){
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 查询桌面所有应用
     *
     * @return 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getDeskTopAppList() {
        List<AppEntity> appEntityList = new ArrayList<AppEntity>();
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager pm = BaseIotUtils.getContext().getPackageManager();
            List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
            //for循环遍历ResolveInfo对象获取包名和类名
            for (int i = 0; i < apps.size(); i++) {
                ResolveInfo info = apps.get(i);
                String packageName = info.activityInfo.packageName;
                Drawable icon = info.loadIcon(BaseIotUtils.getContext().getPackageManager());
                ApplicationInfo ai = pm.getApplicationInfo(info.activityInfo.packageName, PackageManager.GET_ACTIVITIES);
                CharSequence name = info.activityInfo.loadLabel(BaseIotUtils.getContext().getPackageManager());
                boolean isSys = false;
                if ((ai.flags & ai.FLAG_SYSTEM) != 0) {
                    isSys = true;
                }
                String versionCode = pm.getPackageInfo(packageName, 0).versionCode + "";
                String versionName = pm.getPackageInfo(packageName, 0).versionName;
                AppEntity entity = new AppEntity(i + "", name.toString(), packageName, versionCode + "", versionName, icon, false, i, ai.uid, isSys, getAllProcess(packageName),getRunService(packageName));
                appEntityList.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.d(TAG, "errMeg:" + e.getMessage());
        }
        return appEntityList;
    }

    /**
     * 查询手机内非系统应用
     *
     * @return 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getNormalAppList() {
        List<AppEntity> appEntityList = new ArrayList<AppEntity>();
        PackageManager pManager = BaseIotUtils.getContext().getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断是否为非系统预装的应用程序
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                // customs applications
                String pkgName=pak.applicationInfo.packageName;
                AppEntity entity = new AppEntity(i + "",
                        pManager.getApplicationLabel(pak.applicationInfo).toString(),
                        pkgName,
                        pak.versionCode + "",
                        pak.versionName,
                        pManager.getApplicationIcon(pak.applicationInfo),
                        false,
                        i,
                        pak.applicationInfo.uid,
                        false,
                        getAllProcess(pkgName),getRunService(pkgName));
                appEntityList.add(entity);
            }
        }
        return appEntityList;
    }

    /**
     * 根据输入的包名 查找应用是否在本地
     *
     * @param packageName 包名
     * @return 是否存在
     */
    public static boolean existLocal(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            BaseIotUtils.getContext().getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 查询手机内系统应用
     *
     * @return 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getSystemAppList() {
        List<AppEntity> appEntityList = new ArrayList<AppEntity>();
        PackageManager pManager = BaseIotUtils.getContext().getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断是否为非系统预装的应用程序
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) != 0) {
                // customs applications
                String pkgName=pak.applicationInfo.packageName;
                AppEntity entity = new AppEntity(i + "",
                        pManager.getApplicationLabel(pak.applicationInfo).toString(),
                        pkgName,
                        pak.versionCode + "",
                        pak.versionName,
                        pManager.getApplicationIcon(pak.applicationInfo),
                        false, i, pak.applicationInfo.uid, true, getAllProcess(pkgName),getRunService(pkgName));
                appEntityList.add(entity);
            }
        }
        return appEntityList;
    }


    /**
     * 根据包名获取进程PID
     *
     * @param packagename 包名
     * @return 进程PID -1为错误 其他值 为进程PID
     * 该获取进程pid 类似与 adb shell top命令
     * 根据包名未查询到进程 可能是这个包名的程序未启动 启动后即可查看到该包名的进程
     */
    public static List<ProcessEntity> getAllProcess(String packagename) {
        List<ProcessEntity> processEntityList = new ArrayList<>();
        ActivityManager am = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mRunningProcess = am.getRunningAppProcesses();
        int pid = -1;
        for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
            //KLog.d(TAG, "processName: " + amProcess.processName + ",pid=" + amProcess.pid);
            if (amProcess.processName.indexOf(packagename) != -1) {
                pid = amProcess.pid;
                ProcessEntity processEntity = new ProcessEntity();
                processEntity.setPid(pid);
                processEntity.setProcessName(amProcess.processName);
                processEntityList.add(processEntity);
            }
        }
        return processEntityList;
    }

    /**
     * 根据包名获取正在运行的服务
     * @param packagename 包名
     * @return 正在运行的Service
     */
    public static List<String> getRunService(String packagename) {
        List<String> serviceList=new ArrayList<>();
        ActivityManager activityManager = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            ComponentName service=serviceInfo.service;
            if(service.getPackageName().equals(packagename)){
                serviceList.add(service.getClassName());
            }
        }
        return serviceList;
    }

    /**
     * 获取当前应用名称
     *
     * @param packageName
     */
    public static String getAppName(String packageName) {
        PackageManager pm = BaseIotUtils.getContext().getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            // 应用名称
            String appName = pm.getApplicationLabel(appInfo).toString();
            //应用图标
            //Drawable appIcon = pm.getApplicationIcon(appInfo);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据包名获取进程PID
     *
     * @param packagename 包名
     * @return 进程PID -1为错误 其他值 为进程PID
     */
    public static int getPidByPackageName(String packagename) {
        ActivityManager am = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mRunningProcess = am.getRunningAppProcesses();
        int pid = -1;
        for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
            if (amProcess.processName.equals(packagename)) {
                pid = amProcess.pid;
                break;
            }
        }
        return pid;
    }

    /**
     * 获取APP-VersionCode
     *
     * @return
     */
    public static int getAppVersionCode() {
        try {
            Context context=BaseIotUtils.getContext();
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return  pinfo.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取APP-VersionName
     *
     * @return
     */
    public static String getAppVersionName() {
        try {
            Context context=BaseIotUtils.getContext();
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return pinfo.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 判断 App 是否处于前台
     *
     * @return true |false
     */
    public static boolean isAppForeground() {
        Context context=BaseIotUtils.getContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (aInfo.processName.equals(context.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取APP是否正在运行
     *
     * @param packageName 包名
     * @return 运行状态
     */
    public static boolean isAppRunning(String packageName) {
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packageName) && info.baseActivity.getPackageName().equals(packageName)) {
                isAppRunning = true;
                //find it, break
                break;
            }
        }
        return isAppRunning;
    }



    /**
     * 带提示窗口卸载
     *
     * @param packageName 包名
     */
    public static void uninstall(String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        BaseIotUtils.getContext().startActivity(intent);
    }

    /**
     * 卸载应用成功&失败
     *
     * @param packageName
     * @param isKeepData
     * @return
     */
    public static boolean uninstallSilent(String packageName, boolean isKeepData) {
        boolean isRoot = isRoot();
        String command = "LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm uninstall " + (isKeepData ? "-k" : "") + packageName;
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(new String[]{command}, isRoot);
        if (commandResult.successMsg != null
                && commandResult.successMsg.toLowerCase().contains("success")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断设备是否有root权限
     *
     * @return
     */
    public static boolean isRoot() {
        String su = "su";
        //手机本来已经有root权限（/system/bin/su已经存在，adb shell里面执行su就可以切换root权限下）
        String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/"};
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取最顶层的应用
     *
     * @return
     */
    public static String getTopApp() {
        ActivityManager am = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null) {
            return (list.get(0).topActivity.getPackageName());
        } else {
            return null;
        }
    }

    /**
     * 获得该包名的应用中的MainActivity
     *
     * @param packageName 包名
     * @return
     */
    public static Intent getMainIntent(String packageName) {
        String mainAct = null;
        PackageManager pkgMag = BaseIotUtils.getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        @SuppressLint("WrongConstant")
        List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
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
     * 但是这个应用需要有一个最先启动的activity，即需要有个activity加上
     * 打开设置里的应用详情
     *
     * @param packageName
     * @throws Exception
     */
    public static void openPackage(String packageName) throws Exception {
        PackageManager packageManager = BaseIotUtils.getContext().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        BaseIotUtils.getContext().startActivity(intent);
    }

    /**
     * 根据包名获取启动该app的主界面
     *
     * @param packageName 包名
     */
    public static void startApp(String packageName) {
        //根据包名获取该应用的主页面
        Intent intent = getMainIntent(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        BaseIotUtils.getContext().startActivity(intent);
    }


}
