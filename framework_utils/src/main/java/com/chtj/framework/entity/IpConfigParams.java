package com.chtj.framework.entity;

public class IpConfigParams {
    private String ip;
    private String dns1;
    private String dns2;
    private String gateWay;//默认网关
    private String mask;//子网掩码

    public IpConfigParams(String ip, String dns1, String dns2, String gateWay, String mask) {
        this.ip = ip;
        this.dns1 = dns1;
        this.dns2 = dns2;
        this.gateWay = gateWay;
        this.mask = mask;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(String dns1) {
        this.dns1 = dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public void setDns2(String dns2) {
        this.dns2 = dns2;
    }

    public String getGateWay() {
        return gateWay;
    }

    public void setGateWay(String gateWay) {
        this.gateWay = gateWay;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }
}
