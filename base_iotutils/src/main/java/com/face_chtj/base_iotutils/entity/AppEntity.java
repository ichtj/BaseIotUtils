package com.face_chtj.base_iotutils.entity;

import android.graphics.drawable.Drawable;

import java.util.List;

public class AppEntity {
    /**
     * 编号
     */
    private String id;
    /**
     * app名称
     */
    private String appName;
    /**
     * 包名
     */
    private String packageName;
    /**
     * 版本号
     */
    private String versionCode;
    /**
     * 版本名称
     */
    private String versionName;
    /**
     * app图标
     */
    private Drawable icon;
    /**
     * 是否选中
     */
    private boolean isCheck;
    /**
     * 当前下标
     */
    private int position;

    /**
     * uid
     */
    private int uid;
    /**
     * 是否是系统应用
     */
    private boolean isSys;
    /**
     * 包名下的进程信息
     */
    private List<ProcessEntity> mProcessEntity;

    private List<String> mRunServiceList;

    public AppEntity() {
    }

    public AppEntity(String id, String appName, String packageName, String versionCode, String versionName, Drawable icon, boolean isCheck, int position, int uid, boolean isSys, List<ProcessEntity> mProcessEntity, List<String> mRunServiceList) {
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
        this.mProcessEntity = mProcessEntity;
        this.mRunServiceList = mRunServiceList;
    }


    public List<String> getmRunServiceList() {
        return mRunServiceList;
    }

    public void setmRunServiceList(List<String> mRunServiceList) {
        this.mRunServiceList = mRunServiceList;
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

    public List<ProcessEntity> getmProcessEntity() {
        return mProcessEntity;
    }

    public void setmProcessEntity(List<ProcessEntity> mProcessEntity) {
        this.mProcessEntity = mProcessEntity;
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
