package com.chtj.framework.keeplive;

/**
 * Create on 2019/11/18
 * author chtj
 * desc
 */
public class KeepLiveData {
    private long id;
    private String packageName;
    private String remarks;
    private String isEnable;
    private String type;
    private String serviceName;

    public KeepLiveData(long id, String packageName, String remarks, String isEnable, String type, String serviceName) {
        this.id = id;
        this.packageName = packageName;
        this.remarks = remarks;
        this.isEnable = isEnable;
        this.type = type;
        this.serviceName=serviceName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(String isEnable) {
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
}
