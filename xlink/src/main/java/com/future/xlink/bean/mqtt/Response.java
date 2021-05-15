package com.future.xlink.bean.mqtt;

/**
 * 响应数据结构体
 * */
public class Response<T> {
    /**
     * 请求端生成的消息事物ID，用于分辨响应结果消息
     * */
    public  String iid;
    /**
     * 说明该消息的动作是命令事物消息
     * */
    public String act;
    /**
     * 响应数据的结果回复
     * */
    public T payload;
}
