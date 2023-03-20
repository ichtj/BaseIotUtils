package com.wave_chtj.example.callback;

public interface INetTimerCallback {
    void refreshNet(String time,String dbm,String localIp,String netType,boolean isNet4G,boolean pingResult);
}
