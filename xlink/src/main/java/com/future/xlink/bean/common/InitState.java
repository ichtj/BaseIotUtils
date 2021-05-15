package com.future.xlink.bean.common;

/**
 * 初始化结果状态值枚举类型
 **/
public enum InitState {
    INIT_PARAMS_LOST(1007, "注册参数传递时丢失"),
    INIT_GETAGENT_FAIL(1002, "获取代理服务器列表失败"),
    INIT_GETAGENT_ERR(500, "获取代理服务器列表异常"),
    INIT_REGISTER_AGENT_FAIL(1004, "注册代理服务器失败"),
    INIT_REGISTER_AGENT_ERR(501, "注册代理服务器异常"),
    INIT_CACHE_NOEXIST(1005, "注册代理服务器成功但缓存已丢失"),
    INIT_CONN_SERVICE_ERR(1008, "初始化连接服务异常"),
    INIT_DEVICE_NOT_EIXST_ERR(1009, "设备未在平台注册"),
    INIT_SUCCESS(0, "初始化成功");

    int type;
    String value;


    InitState(int type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getTye() {
        return type;
    }


    @Override
    public String toString() {
        return value;
    }
}
