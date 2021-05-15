package com.future.xlink.bean.common;

public enum RespType {
    RESP_SUCCESS(0,"成功"),
    RESP_OUTTIME(1,"消息处理超时"),
    RESP_IID_REPEAT(1000,"消息iid重复存在"),
    RESP_IID_LOST(1001,"缺少消息参数iid"),
    RESP_CONNECT_LOST(1002,"服务连接中断，停止发送");


    int type;
    String value;


    RespType(int type, String value) {
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
