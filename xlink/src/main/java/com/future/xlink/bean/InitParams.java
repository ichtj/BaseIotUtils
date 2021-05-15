package com.future.xlink.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 初始化参数
 */
public class InitParams implements Serializable {
    /**
     * 根据后台申请参数匹配生成
     */
    public String token = "";
    /**
     * 是否开启缓存
     */
    public boolean bufferEnable = false;
    /**
     * 是否开启自动重连
     */
    public boolean automaticReconnect = false;
    /**
     * 自动重连模式下超时响应时间，单位：分钟
     * */
    public  int reconnectTime=3;
    /**
     * 是否开启清除缓存
     */
    public boolean cleanSession = true;
    /**
     * 缓存大小
     */
    public int bufferSize = 100;
    /**
     * 超时时间,sdk默认发送超时时间,单位s
     */
    public int outTime = 30;
    /**
     * 心跳发送间隔时长,单位s
     */
    public int keepAliveTime = 10;
    /**
     * 设备唯一编码,可以为工控的IMEI编号，不可为空
     */
    public String sn = "";
    /**
     * 动态分配加密参数，设备后台申请分配，不可为空
     */
    @NonNull
    public String secret = "";
    /**
     * 预留加密算法key，设备后台申请分配，不可为空
     */
    @NonNull
    public String key = "";
    /**
     * 预留加密算法iv_key，设备后台申请分配，不可为空
     */
    public String iv_key = "";
    /**
     * 产品id，设备后台申请分配，不可为空
     */
    @NonNull
    public String pdid = "";

    @Override
    public String toString() {
        return "InitParams{" +
                "token='" + token + '\'' +
                ", bufferEnable=" + bufferEnable +
                ", automaticReconnect=" + automaticReconnect +
                ", reconnectTime=" + reconnectTime +
                ", cleanSession=" + cleanSession +
                ", bufferSize=" + bufferSize +
                ", outTime=" + outTime +
                ", keepAliveTime=" + keepAliveTime +
                ", sn='" + sn + '\'' +
                ", secret='" + secret + '\'' +
                ", key='" + key + '\'' +
                ", iv_key='" + iv_key + '\'' +
                ", pdid='" + pdid + '\'' +
                '}';
    }
}
