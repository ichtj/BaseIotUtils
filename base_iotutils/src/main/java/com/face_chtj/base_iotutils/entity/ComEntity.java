package com.face_chtj.base_iotutils.entity;
/**
 * Create on 2019/9/18
 * author chtj
 */
public class ComEntity {
    private String com;//串口号
    private int baudrate;//波特率
    private int timeOut;//读取超时等待时间
    private int retriesCount;//异常时默认执行的次数 默认为1次
    private HeartBeatEntity heartBeatEntity;//心跳包相关

    public ComEntity(String com, int baudrate, int timeOut, int retriesCount, HeartBeatEntity heartBeatEntity) {
        this.com = com;
        this.baudrate = baudrate;
        this.timeOut = timeOut;
        this.retriesCount = retriesCount;
        this.heartBeatEntity = heartBeatEntity;
    }

    public String getCom() {
        return com;
    }

    public void setCom(String com) {
        this.com = com;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public HeartBeatEntity getHeartBeatEntity() {
        return heartBeatEntity;
    }

    public void setHeartBeatEntity(HeartBeatEntity heartBeatEntity) {
        this.heartBeatEntity = heartBeatEntity;
    }
    public int getRetriesCount() {
        if(retriesCount==0){
            retriesCount=1;
        }
        return retriesCount;
    }

    public void setRetriesCount(int retriesCount) {
        this.retriesCount = retriesCount;
    }
}
