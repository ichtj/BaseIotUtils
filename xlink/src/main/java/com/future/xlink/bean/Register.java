package com.future.xlink.bean;

import android.text.TextUtils;

public class Register {
    public  String ssid; //后端随机生成的上报SSID
    public  String mqttBroker; //mqtt的broker服务地址
    public  String mqttUsername; // 连接到mqtt账号
    public  String mqttPassword; //连接到mqtt的密码
    public  boolean isNull(){
        return TextUtils.isEmpty(ssid)||TextUtils.isEmpty(mqttBroker)||
                TextUtils.isEmpty(mqttUsername)||TextUtils.isEmpty(mqttPassword);
    }

    @Override
    public String toString() {
        return "Register{" +
                "ssid='" + ssid + '\'' +
                ", mqttBroker='" + mqttBroker + '\'' +
                ", mqttUsername='" + mqttUsername + '\'' +
                ", mqttPassword='" + mqttPassword + '\'' +
                '}';
    }
}
