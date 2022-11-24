package com.wave_chtj.example.network;

public interface INetMonitor {
    void getPingList(String []pingList);
    void getNowTime(String time);
    void getNetType(String netType,boolean isPing);
    void getResetErrCount(int errCount);
    void getTotalCount(int totalCount);
    void getDbm(String dBm);
}
