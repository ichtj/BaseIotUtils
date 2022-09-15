package com.face_chtj.base_iotutils.entity;

public class BaseUrlBean {
    public String host;
    public int port;

    public BaseUrlBean(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return "BaseUrlBean{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
