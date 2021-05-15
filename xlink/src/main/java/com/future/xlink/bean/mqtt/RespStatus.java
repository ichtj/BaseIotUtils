package com.future.xlink.bean.mqtt;

/**
 * 消息应答定义
 * */
public class RespStatus {
    /**
     * 事件消息应答响应状态
     * */
    public int _status;
    /**
     * 响应描述
     * */
    public String _description;
    public RespStatus(int _status,String _description){
        this._status=_status;
        this._description=_description;
    }
}
