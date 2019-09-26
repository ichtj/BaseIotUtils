package com.chtj.base_iotutils.serialport.entity;

/**
 * Create on 2019/9/25
 * author chtj
 */
public class HeartBeatEntity {
    private byte[] heartBeatComm;//执行的心跳包命令
    private int heartBeatFlag;//执行心跳包的标志
    private int delayMillis;//多少周期执行一次心跳包检测毫秒

    public HeartBeatEntity(byte[] heartBeatComm, int heartBeatFlag, int delayMillis) {
        this.heartBeatComm = heartBeatComm;
        this.heartBeatFlag = heartBeatFlag;
        this.delayMillis = delayMillis;
    }

    public byte[] getHeartBeatComm() {
        return heartBeatComm;
    }

    public void setHeartBeatComm(byte[] heartBeatComm) {
        this.heartBeatComm = heartBeatComm;
    }

    public int getHeartBeatFlag() {
        return heartBeatFlag;
    }

    public void setHeartBeatFlag(int heartBeatFlag) {
        this.heartBeatFlag = heartBeatFlag;
    }

    public int getDelayMillis() {
        if(delayMillis==0){
            delayMillis=1*60*1000;//如果忘记设置心跳包的执行周期，将设置为1分钟
        }
        return delayMillis;
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }
}
