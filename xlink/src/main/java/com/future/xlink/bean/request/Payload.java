package com.future.xlink.bean.request;

public class Payload {
    public  String did; //设备SN码
    public  String pdid; //产品ID
    public boolean isNew; //为true时为新设备激活
    public String oldMqttBroker; //旧的代理服务连接
}
