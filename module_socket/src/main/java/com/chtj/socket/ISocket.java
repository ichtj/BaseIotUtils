package com.chtj.socket;

import android.content.Context;

/**
 * TCP/UDP 回调接口
 */
public interface ISocket {
    /**
     * 连接网络
     *
     * @param ctx ctx只有ssl连接的时候才需要，
     *            其他的情况可为null
     */
    void connect(Context ctx);

    void send(byte[] data);

    /**
     * 发送数据
     *
     * @param data   发送的数据
     * @param offset 偏移量
     * @param size   长度
     */
    void send(byte[] data, int offset, int size);

    /**
     * 结束连接
     */
    void close();

    /**
     * 是否是连接的
     */
    boolean isClosed();

    /**
     * 添加tcp状态的回调
     *
     * @param listener
     */
    void setSocketListener(ISocketListener listener);
}
