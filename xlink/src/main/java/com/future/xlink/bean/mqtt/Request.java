package com.future.xlink.bean.mqtt;

/**
 * 请求参数主体
 * **/
public class Request<T> {
    /**
     * 请求端生成的消息事物ID，用于分辨响应结果消息
     * */
    public  String iid;
    /**
     * 说明该消息的动作是命令事物消息
     * */
    public String act;
    /**
     * 响应主题，如App为主题，则为APP订阅的主题
     * */
    public String ack;
    /**
     * 为请求端传入的功能调用
     * */
    public T inputs;
}
