package com.ichtj.basetools.entity;

public class NetBean {
    public String[] pingDns;
    public String dbm;
    public String localIp;
    public String netType;
    public boolean isNet4G;
    public boolean[]pingResult;
    public boolean netConnect;

    public NetBean(String[] pingDns, String dbm, String localIp, String netType, boolean isNet4G, boolean[] pingResult, boolean netConnect) {
        this.pingDns = pingDns;
        this.dbm = dbm;
        this.localIp = localIp;
        this.netType = netType;
        this.isNet4G = isNet4G;
        this.pingResult = pingResult;
        this.netConnect = netConnect;
    }
}
