package com.wave_chtj.example.network;

public interface NetMonitorCallBack {
    void getPingResult(boolean isPing);
    void getPingList(String []pingList);
    void getNowTime(String time);
    void getNetType(String netType);
    void getResetErrCount(int errCount);
    void getTotalCount(int totalCount);
    void getDbm(String dBm);
}
