package com.chtj.base_iotutils.entity;

import java.util.List;

/**
 * Create on 2019/9/18
 * author chtj
 */
public class ComEntity {
    private String com;//串口号
    private int baudrate;//波特率
    private int timeOut;//读取超时时间
    private int retriesCount;//异常时默认执行的次数 默认为1次
    private List<Byte> headDataList;//数据头 AA 55
    private int dataArrayStartIndex;//data标识的长度 开始的位置 00 01
    private int dataArrayLeng;//data标识的长度 00 01 长度为2
    private int fixedLength;//除data长度以外的固定长度 6
    private int instructionStartIndex;//指令位置
    private List<Byte> instructionList;//指令集合
    private HeartBeatEntity heartBeatEntity;//心跳包相关
    //如果写入的命令存在一条以上时 执行完成一条后
    //如果添加进来的flag不管上一条是否执行失败 继续向下执行
    //否则只执行第一条就退出了
    private List<Integer> flagFilterArray;

    //不包含心跳包
    public ComEntity(String com, int baudrate, int timeOut, int retriesCount, List<Byte> headDataList, int dataArrayStartIndex, int dataArrayLeng, int fixedLength, int instructionStartIndex, List<Byte> instructionList, List<Integer> flagFilterArray) {
        this.com = com;
        this.baudrate = baudrate;
        this.timeOut = timeOut;
        this.retriesCount = retriesCount;
        this.headDataList = headDataList;
        this.dataArrayStartIndex = dataArrayStartIndex;
        this.dataArrayLeng = dataArrayLeng;
        this.fixedLength = fixedLength;
        this.instructionStartIndex = instructionStartIndex;
        this.instructionList = instructionList;
        this.flagFilterArray = flagFilterArray;
    }
    //包含心跳包
    public ComEntity(String com, int baudrate, int timeOut, int retriesCount, List<Byte> headDataList, int dataArrayStartIndex, int dataArrayLeng, int fixedLength, int instructionStartIndex, List<Byte> instructionList, HeartBeatEntity heartBeatEntity, List<Integer> flagFilterArray) {
        this.com = com;
        this.baudrate = baudrate;
        this.timeOut = timeOut;
        this.retriesCount = retriesCount;
        this.headDataList = headDataList;
        this.dataArrayStartIndex = dataArrayStartIndex;
        this.dataArrayLeng = dataArrayLeng;
        this.fixedLength = fixedLength;
        this.instructionStartIndex = instructionStartIndex;
        this.instructionList = instructionList;
        this.heartBeatEntity = heartBeatEntity;
        this.flagFilterArray = flagFilterArray;
    }

    public int getDataArrayStartIndex() {
        return dataArrayStartIndex;
    }

    public void setDataArrayStartIndex(int dataArrayStartIndex) {
        this.dataArrayStartIndex = dataArrayStartIndex;
    }

    public int getInstructionStartIndex() {
        return instructionStartIndex;
    }

    public void setInstructionStartIndex(int instructionStartIndex) {
        this.instructionStartIndex = instructionStartIndex;
    }

    public List<Byte> getHeadDataList() {
        return headDataList;
    }

    public void setHeadDataList(List<Byte> headDataList) {
        this.headDataList = headDataList;
    }

    public List<Byte> getInstructionList() {
        return instructionList;
    }

    public void setInstructionList(List<Byte> instructionList) {
        this.instructionList = instructionList;
    }


    public int getDataArrayLeng() {
        if(dataArrayLeng==0){
            dataArrayLeng=1;
        }
        return dataArrayLeng;
    }

    public void setDataArrayLeng(int dataArrayLeng) {
        this.dataArrayLeng = dataArrayLeng;
    }

    public int getFixedLength() {
        return fixedLength;
    }

    public void setFixedLength(int fixedLength) {
        this.fixedLength = fixedLength;
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

    public int getRetriesCount() {
        if(retriesCount==0){
            retriesCount=1;
        }
        return retriesCount;
    }

    public void setRetriesCount(int retriesCount) {
        this.retriesCount = retriesCount;
    }

    public HeartBeatEntity getHeartBeatEntity() {
        return heartBeatEntity;
    }

    public void setHeartBeatEntity(HeartBeatEntity heartBeatEntity) {
        this.heartBeatEntity = heartBeatEntity;
    }

    public List<Integer> getFlagFilterArray() {
        return flagFilterArray;
    }

    public void setFlagFilterArray(List<Integer> flagFilterArray) {
        this.flagFilterArray = flagFilterArray;
    }
}
