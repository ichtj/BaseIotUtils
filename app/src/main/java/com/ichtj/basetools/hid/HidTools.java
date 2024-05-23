package com.ichtj.basetools.hid;

public class HidTools {
    static {
        System.loadLibrary("hidtest");
    }

    // JNI
    public native static int sendCmds(String dev,byte[] data);

    public native static void init(String dev,IHidCallback iHidCallback);

    public native static void stopMonitoring();
}
