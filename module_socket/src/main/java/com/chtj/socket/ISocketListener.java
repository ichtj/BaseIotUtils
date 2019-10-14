package com.chtj.socket;

/**
 * TCP/UDP 回调接口
 */
public interface ISocketListener {
    /**
     * 接收的数据
     * @param data
     * @param offset
     * @param size
     */
    void recv(byte[] data, int offset, int size);

    /**
     * 接收的数据
     * @param data
     */
    void writeSuccess(byte[] data);

    /**
     * 连接成功
     */
    void connSuccess();

    /**
     * 连接异常
     * @param t
     */
    void connFaild(Throwable t);

    /**
     * 关闭连接
     */
    void connClose();
}
