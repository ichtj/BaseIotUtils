package com.face_chtj.base_iotutils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import com.face_chtj.base_iotutils.entity.AppEntity;
import com.face_chtj.base_iotutils.entity.ProcessEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.List;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:AppsUtils相关工具类
 * --查询桌面所有应用 {@link #getDeskTopAppList()}
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
    private static final String TAG = AppsUtils.class.getSimpleName();

    /**
     * 获取当前系统使用的android api版本号
     */
    public static int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取当前系统的android版本
     * 例如android4.4 android7.1.2 android11等
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getAppPath(String pkgName) {
        try {
            PackageManager pm = BaseIotUtils.getContext().getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            // appInfo.sourceDir 就是APK文件的路径
            return appInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询桌面所有应用 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getDeskTopAppList() {
        try {
            Context context = BaseIotUtils.getContext();
            List<AppEntity> appEntityList = new ArrayList<AppEntity>();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
            for (int i = 0; i < apps.size(); i++) {
                ResolveInfo info = apps.get(i);
                String pkg = info.activityInfo.packageName;
                PackageInfo packageInfo = pm.getPackageInfo(pkg, 0);
                long firstInstallTime = packageInfo.firstInstallTime;
                long lastUpdateTime = packageInfo.lastUpdateTime;
                ApplicationInfo ai = pm.getApplicationInfo(info.activityInfo.packageName, PackageManager.GET_ACTIVITIES);
                CharSequence name = info.activityInfo.loadLabel(context.getPackageManager());
                boolean isSys = (ai.flags & ai.FLAG_SYSTEM) != 0;
                String topApp = getTopApp();
                boolean isTopApp = ai.packageName.contains(topApp);
                int vCode = pm.getPackageInfo(pkg, 0).versionCode;
                String vName = pm.getPackageInfo(pkg, 0).versionName;
                String sourceDir = ai.sourceDir;
                boolean isRunning = isAppRunning(ai.packageName);
                AppEntity entity = new AppEntity(name.toString(), pkg, vCode, vName, firstInstallTime, lastUpdateTime,/* icon,*/ isTopApp,
                        isRunning, isSys, false, true, getUidByPackageName(pkg), getPidByPackageName(pkg), sourceDir, getAllProcess(pkg), getRunService(pkg),
                getApkSize(context,pkg),getAppMemoryInMB(context,pkg),getAppCpuUsage(context,pkg),0,false);
                appEntityList.add(entity);
            }
            return appEntityList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationIcon(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static float getAppCpuUsage(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return 0f;

        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes == null) return 0f;

        for (ActivityManager.RunningAppProcessInfo proc : processes) {
            if (proc.processName.equals(packageName)) {
                int pid = proc.pid;
                try {
                    long[] cpu1 = readProcStat(pid);
                    long totalCpu1 = readTotalCpu();
                    Thread.sleep(360); // 采样间隔
                    long[] cpu2 = readProcStat(pid);
                    long totalCpu2 = readTotalCpu();

                    long appCpu = cpu2[0] - cpu1[0];
                    long totalCpu = totalCpu2 - totalCpu1;

                    return totalCpu > 0 ? (appCpu * 100f / totalCpu) : 0f;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return 0f;
    }

    // 返回进程 CPU 总时间 user + system
    private static long[] readProcStat(int pid) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/" + pid + "/stat"));
        String line = reader.readLine();
        reader.close();
        String[] parts = line.split("\\s+");
        long utime = Long.parseLong(parts[13]);
        long stime = Long.parseLong(parts[14]);
        return new long[]{utime + stime};
    }

    // 返回系统 CPU 总时间
    private static long readTotalCpu() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
        String line = reader.readLine();
        reader.close();
        String[] parts = line.split("\\s+");
        long total = 0;
        for (int i = 1; i < parts.length; i++) {
            total += Long.parseLong(parts[i]);
        }
        return total;
    }



    public static int getAppMemoryInMB(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return 0;

        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        if (runningProcesses == null) return 0;

        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.processName.equals(packageName)) {
                int[] pids = new int[]{processInfo.pid};
                Debug.MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(pids);
                if (memoryInfos.length > 0) {
                    // PSS 单位是 KB，转换为 MB
                    int pssKB = memoryInfos[0].getTotalPss();
                    int pssMB = (pssKB + 1023) / 1024; // 四舍五入取整
                    return pssMB;
                }
            }
        }
        return 0; // 应用未运行或未找到
    }


    public static long getApkSize(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            File apkFile = new File(appInfo.sourceDir);
            if (apkFile.exists()) {
                return apkFile.length(); // 返回字节数
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }


    // 判断应用是否为桌面应用
    public static boolean isLauncherApp(String packageName) {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launcherIntent.setPackage(packageName);

        PackageManager packageManager = BaseIotUtils.getContext().getPackageManager();
        ResolveInfo resolveInfo = packageManager.resolveActivity(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null;
    }

    /**
     * 查询所有应用 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getAllApp() {
        Context context = BaseIotUtils.getContext();
        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<AppEntity> appList = new ArrayList<>();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : installedApps) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                boolean isSys = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                String name = packageManager.getApplicationLabel(appInfo).toString();
                String pkg = appInfo.packageName;
                int vCode = packageInfo.versionCode;
                String versionName = packageInfo.versionName;
                long firstInstallTime = packageInfo.firstInstallTime;
                long lastUpdateTime = packageInfo.lastUpdateTime;
//                Drawable icon = packageManager.getApplicationIcon(appInfo);
                int uid = appInfo.uid;
                int pid = getPid(appInfo.packageName, activityManager);
                String sourceDir = appInfo.sourceDir;
                String topApp = getTopApp();
                boolean isTopApp = appInfo.packageName.contains(topApp);
                boolean isRunning = isAppRunning(appInfo.packageName);
                boolean isLauncherApp = isLauncherApp(pkg);
                AppEntity entity = new AppEntity(name, pkg, vCode, versionName, firstInstallTime, lastUpdateTime,/* icon,*/ isTopApp,
                        isRunning, isSys, false, isLauncherApp, getUidByPackageName(pkg), getPidByPackageName(pkg), sourceDir, getAllProcess(pkg), getRunService(pkg),
                        getApkSize(context,pkg),getAppMemoryInMB(context,pkg),getAppCpuUsage(context,pkg),0,false);
                appList.add(entity);
            } catch (Throwable e) {
            }
        }
        return appList;
    }

    public static int getPid(String pkg, ActivityManager aManager) {
        for (ActivityManager.RunningAppProcessInfo processInfo : aManager.getRunningAppProcesses()) {
            if (processInfo.processName.equals(pkg)) {
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
        if (ObjectUtils.isEmpty(packageName)) {
            return false;
        }

        ShellUtils.CommandResult commandResult=ShellUtils.execCommand("ps | grep " + packageName, true);
        boolean isComplete= commandResult.result == 0&&commandResult.successMsg.contains (packageName);
        if (!isComplete){
            ActivityManager am = (ActivityManager) BaseIotUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
                if (processInfos != null) {
                    for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                        if (packageName.equals(info.processName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return isComplete;
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
        String apkPath = "";
        if (isSys) {
            try {
                PackageManager packageManager = BaseIotUtils.getContext().getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                apkPath = new File(applicationInfo.sourceDir).getParent();
            } catch (Throwable throwable) {
            }
        }
        if (Build.VERSION.SDK_INT > 25) {
            String[] mountCmd=new String[]{
                    "mount -o rw,remount /dev/block/dm-0",
                    "mount -o rw,remount /dev/block/dm-1",
                    "mount -o rw,remount /dev/block/dm-2",
                    "mount -o rw,remount /dev/block/dm-3",
                    "mount -o rw,remount /dev/block/dm-4",
                    "mount -o rw,remount /",
            };
             ShellUtils.execCommand(mountCmd, true);
        } else {
             ShellUtils.execCommand("mount -o rw,remount -t ext4 /system", true);
        }
        String[] cmd = new String[]{
                isSys ? (!apkPath.equals("") ? "rm -rf " + apkPath + "*" : "") : "pm uninstall " + packageName,
                "sync",
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
        return list != null ? list.get(0).topActivity.getPackageName() : null;
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
     */
    public static void openPackage(String packageName) {
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

    /**
     * 获取sha256签名
     *
     * @param apkPath apk路径
     * @return 签名
     */
    public static String getSHA256FromAPK(String apkPath) {
        try {
            PackageManager pm = BaseIotUtils.getContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES);
            if (packageInfo != null && packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                for (Signature signature : packageInfo.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(signature.toByteArray());
                    return bytesToHex(md.digest());
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "SHA-256 get fail", e);
        }
        return "";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

}
