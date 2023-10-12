package com.face_chtj.base_iotutils.entity;

import android.graphics.drawable.Drawable;

import java.util.List;

public class AppEntity {
    public String appName;
    public String packageName;
    public int versionCode;
    public String versionName;
    public Drawable icon;
    public boolean isTopApp;
    public boolean isRunning;
    public boolean isSystemApp;
    public boolean isCheck;//是否选中
    public int uid;
    public int pid;
    public String sourceDir;
    public List<ProcessEntity> pkgProcess;//包名下的进程信息
    public List<String> pkgService;//该应用运行的服务
    public AppEntity(String appName, String packageName, int versionCode, String versionName, Drawable icon, boolean isTopApp, boolean isRunning, boolean isSystemApp, boolean isCheck, int uid, int pid, String sourceDir, List<ProcessEntity> pkgProcess, List<String> pkgService) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.icon = icon;
        this.isTopApp = isTopApp;
        this.isRunning = isRunning;
        this.isSystemApp = isSystemApp;
        this.isCheck = isCheck;
        this.uid = uid;
        this.pid = pid;
        this.sourceDir = sourceDir;
        this.pkgProcess = pkgProcess;
        this.pkgService = pkgService;
    }

    public AppEntity() {
    }
}
