package com.chtj.base_iotutils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.chtj.base_iotutils.back_service.BaseIotTools;
import com.chtj.base_iotutils.entity.AppEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * 获取当前android系统内的所有包名
 * 根据包名获取app信息
 */
public class AppMegUtils {
    /**
     * 获取所有应用
     * @return 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getApkInfoList() {
        List<AppEntity> appEntityList =new ArrayList<AppEntity>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps =BaseIotTools.getContext().getPackageManager().queryIntentActivities(intent, 0);
        //for循环遍历ResolveInfo对象获取包名和类名
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo info = apps.get(i);
            String packageName = info.activityInfo.packageName;
            CharSequence cls = info.activityInfo.name;
            Drawable icon=info.loadIcon(BaseIotTools.getContext().getPackageManager());
            CharSequence name = info.activityInfo.loadLabel(BaseIotTools.getContext().getPackageManager());
            AppEntity entity = new AppEntity(i + "", name.toString(), packageName,icon, false, i);
            appEntityList.add(entity);
            Log.e("！！！！！", name + "----" + packageName + "----" + cls);
        }
        return appEntityList;
    }

    /**
     * 查询手机内非系统应用
     * @return 包含包名下app名称，图标的明细信息list
     */
    public static List<AppEntity> getAllApkInfoListByNonSystem() {
        List<AppEntity> appEntityList = new ArrayList<AppEntity>();
        PackageManager pManager = BaseIotTools.getContext().getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断是否为非系统预装的应用程序
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                // customs applications
                AppEntity entity = new AppEntity(i + "",
                        pManager.getApplicationLabel(pak.applicationInfo).toString(),
                        pak.applicationInfo.packageName,
                        pManager.getApplicationIcon(pak.applicationInfo),
                        false, i);
                appEntityList.add(entity);
            }
        }
        return appEntityList;
    }

    /**
     *
     * 获取应用名称
     * @param packageName
     */
    public static String getAppName(String packageName) {
        PackageManager pm = BaseIotTools.getContext().getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            // 应用名称
            String appName=pm.getApplicationLabel(appInfo).toString();
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
     * @param packagename 包名
     * @return 进程PID -1为错误 其他值 为进程PID
     */
    public  static int getPidByPackageName( String packagename){
        ActivityManager am = (ActivityManager)BaseIotTools.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mRunningProcess = am.getRunningAppProcesses();
        int pid=-1;
        for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess){
            if(amProcess.processName.equals(packagename)){
                pid=amProcess.pid;
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
        String pName = BaseIotTools.getContext().getPackageName();
        int versionCode = 0;

        try {
            PackageInfo pinfo = BaseIotTools.getContext().getPackageManager().getPackageInfo(
                    pName, PackageManager.GET_CONFIGURATIONS);
            versionCode = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取APP-VersionName
     *
     * @return
     */
    public static String getAppVersionName() {
        String pName = BaseIotTools.getContext().getPackageName();
        String versionName = "";

        try {
            PackageInfo pinfo =  BaseIotTools.getContext().getPackageManager().getPackageInfo(
                    pName, PackageManager.GET_CONFIGURATIONS);
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }



}
