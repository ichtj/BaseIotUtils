package com.face.keepsample;

import android.graphics.drawable.Drawable;

import com.chtj.keepalive.entity.KeepAliveData;

public class SimpleKeepAlive extends KeepAliveData {

    private Drawable icon;
    private String appName;

    public SimpleKeepAlive(String packageName, int type, String serviceName, boolean isEnable, Drawable icon, String appName) {
        super(packageName, type, serviceName, isEnable);
        this.icon = icon;
        this.appName = appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
