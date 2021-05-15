package com.future.xlink.listener;


import com.future.xlink.bean.Protocal;
import com.future.xlink.bean.common.ConnectLostType;
import com.future.xlink.bean.common.ConnectType;
import com.future.xlink.bean.common.InitState;

public interface MessageListener {
    /**
     * 初始化回调状态
     * @param initState 初始化结果
     * */
    void initState(InitState initState);
    /**
     * mqtt连接成功后，断网/服务器连接中断后，状态回调
     * @param cause 异常中断原因
     * */
    void connectionLost(ConnectLostType type,Throwable cause);
//    /**
//     * mqtt连接成功状态返回
//     * @param  reconnect 是否重连成功
//     * @param  msg 重连的描述
//     * */
//    void connectComplete(boolean reconnect, String msg);
    /**
     * 连接创建后结果返回
     * @param type 创建连接后结果定义返回
     * */
    void connectState(ConnectType type);
    /**
     * mqtt后台服务器下发给客户端的消息
     * @param  protocal  消息协议体
     * */
    void messageArrived(Protocal protocal);
}
