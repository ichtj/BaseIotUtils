package com.face_chtj.base_iotutils.entity;

import android.graphics.drawable.Drawable;

import java.util.List;

public class AppEntity {
    public String appName;
    public String packageName;
    public int versionCode;
    public String versionName;
    public long firstInstallTime;
    public long lastUpdateTime;
    public boolean isTopApp;
    public boolean isRunning;
    public boolean isSystemApp;
    public boolean isCheck;//是否选中
    public boolean isLauncherApp;//是否为桌面应用
    public int uid;
    public int pid;
    public String sourceDir;
    public List<ProcessEntity> pkgProcess;//包名下的进程信息
    public List<String> pkgService;//该应用运行的服务
    public long apkSize;
    public int memoryUsage;//MB
    public float cpuUsageRatio;//百分比
    public int dataUsage;//月流量
    public boolean isEnableKeepAlive;

    public AppEntity(String appName, String packageName, int versionCode, String versionName, long firstInstallTime, long lastUpdateTime, boolean isTopApp, boolean isRunning, boolean isSystemApp, boolean isCheck, boolean isLauncherApp, int uid, int pid, String sourceDir, List<ProcessEntity> pkgProcess, List<String> pkgService, long apkSize, int memoryUsage, float cpuUsageRatio, int dataUsage, boolean isEnableKeepAlive) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
        this.isTopApp = isTopApp;
        this.isRunning = isRunning;
        this.isSystemApp = isSystemApp;
        this.isCheck = isCheck;
        this.isLauncherApp = isLauncherApp;
        this.uid = uid;
        this.pid = pid;
        this.sourceDir = sourceDir;
        this.pkgProcess = pkgProcess;
        this.pkgService = pkgService;
        this.apkSize = apkSize;
        this.memoryUsage = memoryUsage;
        this.cpuUsageRatio = cpuUsageRatio;
        this.dataUsage = dataUsage;
        this.isEnableKeepAlive = isEnableKeepAlive;
    }

    public AppEntity() {
    }
}