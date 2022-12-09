package com.face_chtj.base_iotutils.entity;

public class DnsBean {
    public String dns;
    public boolean isPass;

    public DnsBean(String dns, boolean isPass) {
        this.dns = dns;
        this.isPass = isPass;
    }

    public DnsBean() {
    }

    @Override
    public String toString() {
        return "DnsBean{" +
                "dns='" + dns + '\'' +
                ", isPass=" + isPass +
                '}';
    }
}
