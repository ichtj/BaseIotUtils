package com.future.xlink.bean;


import io.reactivex.annotations.NonNull;

public  class Protocal <T>{

    /**
     * 消息id
     * */
    @NonNull
    public String iid; //消息id
    /**
     * 客户端发送给代理服务器的消息json消息串
     * */
    public T tx;
    /**
     * 代理服务器发送给客户端json消息串
     * */
    public String rx;
    /**
     * 超时响应时间,xlink默认最小超时时间为10s,单位 s
     * */
    public  int overtime=0;

}
