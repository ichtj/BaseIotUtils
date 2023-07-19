package com.face_chtj.base_iotutils.entity;

public class DnsBean {
    public String dns;
    public boolean isPass;
    public String from;
    public int ttl=-1;
    public int delay=-1;

    public DnsBean(String dns, boolean isPass) {
        this.dns = dns;
        this.isPass = isPass;
    }

    public DnsBean(String dns, boolean isPass, String from, int ttl, int delay) {
        this.dns = dns;
        this.isPass = isPass;
        this.from = from;
        this.ttl = ttl;
        this.delay = delay;
    }

    public DnsBean() {
    }

    @Override
    public String toString() {
        //return "DnsBean{" + "dns='" + dns + '\'' + ", isPass=" + isPass + ", from='" + from + '\'' + ", ttl=" + ttl + ", delay=" + delay + '}';
        return "{" + "dns='" + dns +'}';
    }
}
