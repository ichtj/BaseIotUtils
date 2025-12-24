package com.face_chtj.base_iotutils.entity;

public class DnsBean {
    public String cmd;
    public String dns;
    public boolean isPass;
    public int srcIndex;
    public String from;
    public int ttl=-1;
    public int delay=-1;

    public DnsBean(String cmd, String dns, boolean isPass, int srcIndex) {
        this.cmd = cmd;
        this.dns = dns;
        this.isPass = isPass;
        this.srcIndex = srcIndex;
    }

    public DnsBean(String cmd, String dns, boolean isPass, int srcIndex, String from, int ttl, int delay) {
        this.cmd = cmd;
        this.dns = dns;
        this.isPass = isPass;
        this.srcIndex = srcIndex;
        this.from = from;
        this.ttl = ttl;
        this.delay = delay;
    }

    public DnsBean() {
    }

    @Override
    public String toString() {
        return "DnsBean{" + "cmd='" + cmd + '\'' + ", dns='" + dns + '\'' + ", isPass=" + isPass + ", from='" + from + '\'' + ", ttl=" + ttl + ", delay=" + delay + '}';
    }

    public String toColorString() {
        String pingResult=isPass?"<font color=\"#00FF37\">true</font>":"<font color=\"#FF0000\">false</font>";
        return "DnsBean{" + "cmd='" + cmd + '\'' + ", dns='" + dns + '\'' + ", isPass=" + pingResult + ", from='" + from + '\'' + ", ttl=" + ttl + ", delay=" + delay + '}';
    }
}
