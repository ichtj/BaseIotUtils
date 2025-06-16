package com.ichtj.basetools.accessibility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class ScreenRecorderUtil {
    private final Activity mActivity;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    private static final int VIDEO_WIDTH = 720;
    private static final int VIDEO_HEIGHT = 1280;
    private final int mScreenDensity;

    public ScreenRecorderUtil(Activity activity) {
        this.mActivity = activity;
        mProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
    }

    public void requestCapturePermission(int requestCode) {
        Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
        mActivity.startActivityForResult(captureIntent, requestCode);
    }

    public void startRecordingFromService(Intent data, int resultCode) {
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        initRecorder();

        // 启动前台服务
        ScreenRecordService.setRecorderUtil(this);
        Intent intent = new Intent(mActivity, ScreenRecordService.class);
        intent.setAction(ScreenRecordService.ACTION_START);
        ContextCompat.startForegroundService(mActivity, intent);
    }

    public void startRecordingInternal() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenRecorder",
                VIDEO_WIDTH, VIDEO_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);
        mMediaRecorder.start();
    }

    public void stopRecording() {
        try {
            if (mVirtualDisplay != null) mVirtualDisplay.release();
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            if (mMediaProjection != null) mMediaProjection.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRecorder() {
        String path = new File(mActivity.getExternalFilesDir(null), "record_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(path);
        mMediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
