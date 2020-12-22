package com.chtj.base_framework.entity;

/**
 * Create on 2019/11/18
 * author chtj
 * desc
 */
public class KeepLiveData {
    private String packageName;
    private boolean isEnable;
    private String type;
    private String serviceName;

    public KeepLiveData(String packageName, String type, boolean isEnable) {
        this.packageName = packageName;
        this.type = type;
        this.isEnable = isEnable;
    }

    public KeepLiveData(String packageName, String type, String serviceName, boolean isEnable) {
        this.packageName = packageName;
        this.type = type;
        this.serviceName = serviceName;
        this.isEnable = isEnable;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "KeepLiveData{" +
                "packageName='" + packageName + '\'' +
                ", isEnable=" + isEnable +
                ", type='" + type + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
