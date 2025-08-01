package com.ichtj.basetools.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Create on 2019/10/10
 * author chtj
 * 相机录像
 * 1.添加权限
 *<uses-permission android:name="android.permission.CAMERA" />
 *<uses-permission android:name="android.permission.RECORD_AUDIO" />
 *<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 *<uses-feature android:name="android.hardware.camera2" />
 * 2.开始录像,存放位置/storage/emulated/0/Android/data/com.ichtj.basetools/files/Movies/
 */
public class CameraRecordingAty extends BaseActivity {
    private static final String TAG = CameraRecordingAty.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private MediaRecorder mediaRecorder;
    private String cameraId;
    private boolean isRecording = false;
    private Button recordButton;

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return true; }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textureView = findViewById(R.id.textureView);
        recordButton = findViewById(R.id.recordButton);
        textureView.setSurfaceTextureListener(textureListener);

        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(cameraId, stateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(1920, 1080);
            Surface surface = new Surface(texture);

            CaptureRequest.Builder previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        captureSession.setRepeatingRequest(previewBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            String videoPath=getVideoFilePath();
            Log.d(TAG, "startRecording: videoPath>>"+videoPath);
            mediaRecorder.setOutputFile(videoPath);
            mediaRecorder.setVideoEncodingBitRate(10000000);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoSize(1920, 1080);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();

            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(1920, 1080);
            Surface previewSurface = new Surface(texture);
            Surface recorderSurface = mediaRecorder.getSurface();

            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureBuilder.addTarget(previewSurface);
            captureBuilder.addTarget(recorderSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recorderSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        captureSession.setRepeatingRequest(captureBuilder.build(), null, null);
                        mediaRecorder.start();
                        isRecording = true;
                        runOnUiThread(() -> recordButton.setText("Stop Recording"));
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, null);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            captureSession.stopRepeating();
            captureSession.close();
            isRecording = false;
            recordButton.setText("Start Recording");
            startPreview();
            Toast.makeText(this, "Video saved", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private String getVideoFilePath() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return storageDir.getPath() + File.separator + "VIDEO_" + timeStamp + ".mp4";
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            stopRecording();
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}