package com.ichtj.basetools.camera;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class CameraJNINew {

    static {
        System.loadLibrary("dual_camera");
    }

    private long handle;
    private boolean released = false;
    private boolean started = false;
    private boolean recording = false;

    public native long nativeOpen(int id, int w, int h);

    public native int nativeStartPreview(long handle);

    public native int nativeStopPreview(long handle);

    public native void nativeRelease(long handle);

    public native int nativeGetState(long handle);

    public native boolean nativeIsRunning(long handle);

    public native int nativeCapture(long handle);
    public native int nativeStartRecord(long handle, String filePath);
    public native int nativeStopRecord(long handle);

    public boolean isRunning() {
        return nativeIsRunning(handle);
    }

    public int getState() {
        return nativeGetState(handle);
    }

    public void open(int id, int w, int h) {
        if (handle != 0) return;

        handle = nativeOpen(id, w, h);
        released = false;
    }

    public void start() {
        if (handle == 0 || started) return;

        nativeStartPreview(handle);
        started = true;
    }

    public void stop() {
        if (handle == 0 || !started) return;

        stopRecord();
        nativeStopPreview(handle);
        started = false;
    }

    public int startRecord(String filePath) {
        if (handle == 0 || released || !started) return -1;
        if (recording) return 0;
        int ret = nativeStartRecord(handle, filePath);
        if (ret == 0) recording = true;
        return ret;
    }

    public int stopRecord() {
        if (handle == 0 || released) return -1;
        if (!recording) return 0;
        int ret = nativeStopRecord(handle);
        if (ret == 0) recording = false;
        return ret;
    }

    public void release() {
        if (handle == 0 || released) return;

        stop();
        nativeRelease(handle);

        handle = 0;
        released = true;
    }

    public int capture() {
        if (handle == 0 || released) return -1;
        int ret = nativeCapture(handle);
        return ret;
    }

    // 预览回调
    public void onPreviewFrame(int cameraId, byte[] data) {
        Log.d("CameraJNINew", "preview " + cameraId + " size=" + data.length);
    }

    // ⭐ 拍照回调
    public void onCaptureFrame(int cameraId, byte[] data) {

        Log.d("CameraJNINew", "capture " + cameraId);

        // 直接保存 JPEG（MJPG就是JPEG）
        try {
            File file = new File("/sdcard/capture_" + cameraId + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();

            Log.d("CameraJNINew", "saved: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
