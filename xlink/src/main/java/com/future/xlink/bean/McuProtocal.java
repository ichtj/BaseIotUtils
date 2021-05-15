package com.future.xlink.bean;

import com.future.xlink.utils.GlobalConfig;

import java.lang.reflect.Type;

public class McuProtocal extends Protocal {
    public  String act;
    /**
     * 响应主题，如APP为请求端，则为APP订阅的主题,用户无需定义
     * */
    public String ack;

    public long time; //消息到达时间
    public  int type; //当前消息类型
    public  int status; //是否处理标志位 0，没处理，1处理

    public boolean isOverTime() {
        long t=System.currentTimeMillis()-time;
        if (overtime*1000<=GlobalConfig.OVER_TIME){
            overtime=GlobalConfig.OVER_TIME;
        }
        return time != 0 && (System.currentTimeMillis() - time >= overtime);
    }

}
