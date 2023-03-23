package com.wave_chtj.example.callback;

public interface INetTimerCallback {
    void refreshNet(String time,String [] dns,String dbm,String localIp,String netType,boolean isNet4G,boolean pingResult);
}
