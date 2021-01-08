package com.chtj.framework.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Create on 2019/11/18
 * author chtj
 * desc
 */
public class KeepAliveData implements Parcelable {
    private String packageName;
    private boolean isEnable;
    private String type;
    private String serviceName;

    public KeepAliveData(String packageName, String type, boolean isEnable) {
        this.packageName = packageName;
        this.type = type;
        this.isEnable = isEnable;
    }

    public KeepAliveData(String packageName, String type, String serviceName, boolean isEnable) {
        this.packageName = packageName;
        this.type = type;
        this.serviceName = serviceName;
        this.isEnable = isEnable;
    }

    protected KeepAliveData(Parcel in) {
        packageName = in.readString();
        isEnable = in.readByte() != 0;
        type = in.readString();
        serviceName = in.readString();
    }

    public static final Creator<KeepAliveData> CREATOR = new Creator<KeepAliveData>() {
        @Override
        public KeepAliveData createFromParcel(Parcel in) {
            return new KeepAliveData(in);
        }

        @Override
        public KeepAliveData[] newArray(int size) {
            return new KeepAliveData[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeByte((byte) (isEnable ? 1 : 0));
        dest.writeString(type);
        dest.writeString(serviceName);
    }

    public void readFromParcel(Parcel in) {
        this.packageName = in.readString();
        this.isEnable = in.readBoolean();
        this.type = in.readString();
        this.serviceName = in.readString();
    }


}
