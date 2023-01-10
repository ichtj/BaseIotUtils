package com.face_chtj.base_iotutils.entity;

import android.graphics.drawable.Drawable;

import java.util.List;

public class AppEntity {
    private String id;//编号
    private String appName;//app名称
    private String packageName;//包名
    private String versionCode;//版本号
    private String versionName;//版本名称
    private Drawable icon;//app图标
    private boolean isCheck;//是否选中
    private int position;//当前下标
    private int uid;//uid
    private boolean isSys;//是否是系统应用
    private List<ProcessEntity> pkgProcess;//包名下的进程信息
    private List<String> pkgService;//该应用运行的服务

    public AppEntity() {
    }

    public AppEntity(String id, String appName, String packageName, String versionCode, String versionName, Drawable icon, boolean isCheck, int position, int uid, boolean isSys, List<ProcessEntity> pkgProcess, List<String> pkgService) {
        this.id = id;
        this.appName = appName;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.icon = icon;
        this.isCheck = isCheck;
        this.position = position;
        this.uid = uid;
        this.isSys = isSys;
        this.pkgProcess = pkgProcess;
        this.pkgService = pkgService;
    }


    public List<ProcessEntity> getPkgProcess() {
        return pkgProcess;
    }

    public void setPkgProcess(List<ProcessEntity> pkgProcess) {
        this.pkgProcess = pkgProcess;
    }

    public List<String> getPkgService() {
        return pkgService;
    }

    public void setPkgService(List<String> pkgService) {
        this.pkgService = pkgService;
    }
    public boolean isSys() {
        return isSys;
    }

    public void setSys(boolean sys) {
        isSys = sys;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setIsSys(boolean isSys) {
        this.isSys = isSys;
    }

    public boolean getIsSys() {
        return isSys;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
