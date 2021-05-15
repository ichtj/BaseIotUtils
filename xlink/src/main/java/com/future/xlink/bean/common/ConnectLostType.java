package com.future.xlink.bean.common;

/**
 * 定义服务中断类型
 * */
public enum ConnectLostType {
    LOST_TYPE_0(0,"连接服务中断"),
    LOST_TYPE_1(1,"网络和代理服务连接正常,通讯异常"),
    LOST_TYPE_2(2,"代理服务器连接异常"),
    LOST_TYPE_3(3,"多次重连失败,设备网络异常");
    int type;
    String value;


    ConnectLostType(int type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getTye() {
        return type;
    }


}
