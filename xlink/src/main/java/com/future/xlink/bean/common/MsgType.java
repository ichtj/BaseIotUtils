package com.future.xlink.bean.common;

public enum MsgType {
    MSG_PRO("up","上报主题"),  //服务属性

    //事件event
    MSG_EVENT("evt","上报事件");

    String type;
    String value;


    MsgType(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getTye() {
        return type;
    }


    @Override
    public String toString() {
        return value;
    }
}
