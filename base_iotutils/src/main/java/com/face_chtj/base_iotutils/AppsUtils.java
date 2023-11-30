package com.face_chtj.base_iotutils;

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
import android.os.Build;
import android.text.TextUtils;

import com.face_chtj.base_iotutils.entity.AppEntity;
import com.face_chtj.base_iotutils.entity.ProcessEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:AppsUtils相关工具类
 * --查询桌面所有应用 {@link #getAllApp(boolean)} ()}
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
    /**
     * 获取当前系统使用的android api版本号
     */
    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取当前系统的android版本
     * 例如android4.4 android7.1.2 android11等
     */
    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static String getAppPath(String pkgName) {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("pm path " + pkgName, true);
        KLog.d("getAppPath() path >> " + commandResult.successMsg);
        return commandResult.result == 0 ? commandResult.successMsg.replace("package:", "") : "null";
    }

    /**
     * 查询所有应用 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getAllApp(boolean needSysApp) {
        PackageManager packageManager = BaseIotUtils.getContext().getPackageManager();
        ActivityManager activityManager = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<AppEntity> appList = new ArrayList<>();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : installedApps) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (!needSysApp&&isSystemApp){
                    continue;
                }
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                String packageName = appInfo.packageName;
                int versionCode = packageInfo.versionCode;
                String versionName = packageInfo.versionName;
                Drawable icon = packageManager.getApplicationIcon(appInfo);
                int uid = appInfo.uid;
                int pid = getPid(appInfo.packageName,activityManager);
                String sourceDir = appInfo.sourceDir;
                String topApp=getTopApp();
                boolean isRunning = isAppRunning(appInfo.packageName);
                boolean isTopApp = appInfo.packageName.contains(topApp);
                AppEntity app = new AppEntity(appName, packageName, versionCode, versionName, icon, isTopApp, isRunning, isSystemApp,false, uid, pid, sourceDir,getAllProcess(appInfo.packageName), getRunService(appInfo.packageName));
                appList.add(app);
            } catch (Throwable e) {
            }
        }
        return appList;
    }

    public static int getPid(String packageName,ActivityManager activityManager){
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (processInfo.processName.equals(packageName)) {
                return processInfo.pid;
            }
        }
        return -1;
    }

    /**
     * 根据输入的包名 查找应用是否在本地
     *
     * @param packageName 包名
     * @return 是否存在
     */
    public static boolean existLocal(String packageName) {
        if (ObjectUtils.isEmpty(packageName)) {
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
        for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
            if (amProcess.processName.indexOf(packagename) != -1) {
                int pid = amProcess.pid;
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
     *
     * @param packagename 包名
     * @return 正在运行的Service
     */
    public static List<String> getRunService(String packagename) {
        List<String> serviceList = new ArrayList<>();
        ActivityManager activityManager = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            ComponentName service = serviceInfo.service;
            if (service.getPackageName().equals(packagename)) {
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
            Context context = BaseIotUtils.getContext();
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return pinfo.versionCode;
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
            Context context = BaseIotUtils.getContext();
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return pinfo.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取应用uid
     */
    public static int getUidByPackageName(String packageName) {
        PackageManager packageManager = BaseIotUtils.getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 判断 App 是否处于前台
     *
     * @return true |false
     */
    public static boolean isAppForeground() {
        Context context = BaseIotUtils.getContext();
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
     * @return
     */
    public static boolean uninstallSilent(boolean isSys, boolean isReboot, String appName, String packageName) {
        String[] cmd = new String[]{
                "mount -o rw,remount -t ext4 /system",
                "rm -rf /system/priv-app/" + appName,
                "rm -rf /system/app/" + appName,
                "pm uninstall " + packageName,
                isReboot ? "reboot" : ""
        };
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(cmd, isRoot());
        return isSys ? commandResult.result == 0 : commandResult.successMsg != null && commandResult.successMsg.toLowerCase().contains("success");
    }

    /**
     * 静默安装
     *
     * @param isSys    是否是系统应用
     * @param isReboot 安装后是否需要重启
     * @param appName  app名称 英文
     * @param appPath  app路径
     */
    public static boolean installSilent(boolean isSys, boolean isReboot, String appName, String appPath) {
        try {
            if (isSys) {
                String cpu_abi = "lib/" + Build.CPU_ABI;
                ZipFile zip = new ZipFile(appPath);
                Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry ze = entries.nextElement();
                    if (ze.getName().contains(cpu_abi)) {
                        String soName = ze.getName().replace(cpu_abi + "/", "");
                        KLog.d("installSilent:>soName=" + soName);
                        FileUtils.writeToLocal("/data/" + soName, zip.getInputStream(ze));
                    }
                }
                zip.close();
                //复制lib库到system/lib目录下 并授予权限
                String[] command = new String[]{
                        "mount -o rw,remount -t ext4 /system",
                        "cp -rf /data/*.so /system/lib/",//拷贝lib库
                        "rm -rf /system/priv-app/" + appName.replace(".apk", "") + "*",//删除原有的APK
                        "cp -rf " + appPath + " /system/priv-app/",//拷贝已下载好的APK
                        "chmod 777 /system/lib/*.so",//授权so库
                        "chmod 777 /system/priv-app/" + appName.replace(".apk", "") + "*",//授权apk
                        "rm -rf " + appPath,//删除已下载好的APK
                        "rm -rf /data/*.so",//删除临时目录下的so库
                        isReboot ? "reboot" : ""//确认重启
                };
                ShellUtils.CommandResult cmdResult = ShellUtils.execCommand(command, true);
                return cmdResult.result == 0 ? new File("/system/priv-app/" + appName).exists() : false;
            } else {
                String[] command = new String[]{"pm install -r " + appPath + "\n", "rm -rf " + appPath + "\n", isReboot ? "reboot" : ""};
                ShellUtils.CommandResult cmdResult = ShellUtils.execCommand(command, true);
                return cmdResult.result == 0;
            }
        } catch (Throwable e) {
            KLog.e("installSilent() err >> " + e.getMessage());
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
