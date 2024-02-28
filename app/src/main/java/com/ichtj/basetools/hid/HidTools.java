package com.ichtj.basetools.hid;

public class HidTools {
    static {
        System.loadLibrary("hidtest");
    }

    // JNI
    public native static int sendCmds(byte[] data);

    public native static void startMonitoring(IHidCallback iHidCallback);

    public native static void stopMonitoring();
}
