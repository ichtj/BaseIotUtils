package com.face_chtj.base_iotutils.entity;

import android.graphics.drawable.Drawable;

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

    public AppEntity() {
    }

    public AppEntity(String id, String appName, String packageName, Drawable icon, boolean isCheck, int position) {
        this.id = id;
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.isCheck = isCheck;
        this.position = position;
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
}
