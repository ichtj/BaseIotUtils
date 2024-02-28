package com.ichtj.basetools.crash;

public class CrashTools {
    static {
        System.loadLibrary("crashtest");
    }

    // JNI
    public native static void crashtest();
}
