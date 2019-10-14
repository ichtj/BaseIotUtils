package com.chtj.socket;

/**
 * 数据处理
 */
public class SocketData {
    public byte[] data;
    public int offset;
    public int size;

    public SocketData(byte[] data, int offset, int size) {
        this.data = data;
        this.offset = offset;
        this.size = size;
    }
}
