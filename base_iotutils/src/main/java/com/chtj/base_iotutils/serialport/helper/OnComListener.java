package com.chtj.base_iotutils.serialport.helper;

/**
 * Create on 2019/9/5
 * author chtj
 */
public interface OnComListener {
    //命令写入成功
    void writeCommand(byte[] comm, int flag);
    //获取到了数据流中的数据
    void readCommand(byte[] comm, int flag);
    //命令已全部写完
    void writeComplet(int flag);
    //执行超时
    void isReadTimeOut(int flag);
    //是否已经打开
    void isOpen(boolean isOpen);
    //得到串口状态 正常TRUE 异常FALSE
    void comStatus(boolean isNormal);
}
