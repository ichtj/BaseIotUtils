package com.wave_chtj.example.crash;

public class CrashTools {
    static {
        System.loadLibrary("crashtest");
    }

    // JNI
    public native static void crashtest();
}
