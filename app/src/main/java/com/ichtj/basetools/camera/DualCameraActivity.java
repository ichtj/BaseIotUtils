package com.ichtj.basetools.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.view.TopTitleBar;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DualCameraActivity extends BaseActivity implements TopTitleBar.OnTextViewClickListener {

    private static final String TAG = "DualCameraActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 2001;
    private static final long RECONNECT_DELAY_MS = 1500L;
    private static final long WATCHDOG_INTERVAL_MS = 2000L;
    private static final long PREVIEW_TIMEOUT_MS = 4000L;

    private TopTitleBar ctTopView;
    private TextureView textureLeft;
    private TextureView textureRight;
    private TextView tvCaptureViewSize;
    private EditText etWidth;
    private EditText etHeight;
    private Spinner spLeft;
    private Spinner spRight;

    private final List<String> cameraIds = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CameraPane leftPane;
    private CameraPane rightPane;
    private boolean permissionGranted;
    private boolean activityActive;

    private final Runnable watchdogTask = new Runnable() {
        @Override
        public void run() {
            if (!activityActive) {
                return;
            }
            if (leftPane != null) {
                leftPane.checkHealth();
            }
            if (rightPane != null) {
                rightPane.checkHealth();
            }
            mainHandler.postDelayed(this, WATCHDOG_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_camera);
        initView();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ToastUtils.info("Camera2 is not supported on this system.");
            finish();
            return;
        }
        initSpinner();
        initPanes();
        initListener();
        checkPermissions();
    }

    private void initView() {
        ctTopView = findViewById(R.id.ctTopView);
        ctTopView.setTextCenter("多摄像头Camera2");
        textureLeft = findViewById(R.id.texture_left);
        textureRight = findViewById(R.id.texture_right);
        tvCaptureViewSize = findViewById(R.id.tvCaptureViewSize);
        etWidth = findViewById(R.id.etWidth);
        etHeight = findViewById(R.id.etHeight);
        spLeft = findViewById(R.id.spLeft);
        spRight = findViewById(R.id.spRight);
        ctTopView.setOnTextViewClickListener(this);
    }

    private void initPanes() {
        leftPane = new CameraPane("LEFT", textureLeft);
        rightPane = new CameraPane("RIGHT", textureRight);
    }

    private void initListener() {
        findViewById(R.id.btn_open_left).setOnClickListener(v -> openLeft());
        findViewById(R.id.btn_close_left).setOnClickListener(v -> closeLeft());
        findViewById(R.id.btn_capture_left).setOnClickListener(v -> leftPane.capture());
        findViewById(R.id.btn_start_record_left).setOnClickListener(v -> leftPane.startRecord());
        findViewById(R.id.btn_stop_record_left).setOnClickListener(v -> leftPane.stopRecord(true));
        findViewById(R.id.btnLeftRunning).setOnClickListener(v -> leftPane.showStatus());

        findViewById(R.id.btn_open_right).setOnClickListener(v -> openRight());
        findViewById(R.id.btn_close_right).setOnClickListener(v -> closeRight());
        findViewById(R.id.btn_capture_right).setOnClickListener(v -> rightPane.capture());
        findViewById(R.id.btn_start_record_right).setOnClickListener(v -> rightPane.startRecord());
        findViewById(R.id.btn_stop_record_right).setOnClickListener(v -> rightPane.stopRecord(true));
        findViewById(R.id.btnRightRunning).setOnClickListener(v -> rightPane.showStatus());

        spLeft.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                updateSizeTips(getSelectedId(spLeft));
            }
        });
        spRight.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                if (getSelectedId(spLeft) == null) {
                    updateSizeTips(getSelectedId(spRight));
                }
            }
        });
    }

    private void checkPermissions() {
        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted) {
            return;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                REQUEST_CAMERA_PERMISSION);
    }

    private void initSpinner() {
        cameraIds.clear();
        cameraIds.addAll(getAvailableCameraIds());
        List<String> list = new ArrayList<>();
        list.add("Select camera");
        for (String id : cameraIds) {
            list.add("camera " + id);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                list
        );
        spLeft.setAdapter(adapter);
        spRight.setAdapter(adapter);
        updateSizeTips(getSelectedId(spLeft));
    }

    private List<String> getAvailableCameraIds() {
        List<String> list = new ArrayList<>();
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (manager == null) {
            return list;
        }
        try {
            list.addAll(Arrays.asList(manager.getCameraIdList()));
        } catch (CameraAccessException e) {
            Log.e(TAG, "getAvailableCameraIds", e);
        }
        return list;
    }

    private String getSelectedId(Spinner spinner) {
        int pos = spinner.getSelectedItemPosition();
        if (pos <= 0 || pos - 1 >= cameraIds.size()) {
            return null;
        }
        return cameraIds.get(pos - 1);
    }

    private void openLeft() {
        openPane(leftPane, getSelectedId(spLeft), "Please select the left camera.");
    }

    private void closeLeft() {
        leftPane.userClose();
    }

    private void openRight() {
        openPane(rightPane, getSelectedId(spRight), "Please select the right camera.");
    }

    private void closeRight() {
        rightPane.userClose();
    }

    private void openPane(CameraPane pane, String cameraId, String emptyMsg) {
        if (!permissionGranted) {
            checkPermissions();
            ToastUtils.info("Camera and audio permissions are required.");
            return;
        }
        if (TextUtils.isEmpty(cameraId)) {
            Toast.makeText(this, emptyMsg, Toast.LENGTH_SHORT).show();
            return;
        }
        Size requestSize = parseRequestSize();
        if (requestSize == null) {
            Toast.makeText(this, "Invalid preview size.", Toast.LENGTH_SHORT).show();
            return;
        }
        pane.open(cameraId, requestSize);
        updateSizeTips(cameraId);
    }

    private Size getSupportedPreviewSize(String cameraId, Size requestSize) {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (manager == null) {
            return requestSize;
        }
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                return requestSize;
            }
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            if (sizes == null || sizes.length == 0) {
                return requestSize;
            }
            return chooseBestSize(sizes, requestSize);
        } catch (Exception e) {
            Log.e(TAG, "getSupportedPreviewSize cameraId=" + cameraId, e);
            return requestSize;
        }
    }

    private Size chooseBestSize(Size[] sizes, Size requestSize) {
        if (sizes == null || sizes.length == 0) {
            return requestSize;
        }
        Size exact = null;
        Size sameRatio = null;
        long sameRatioDiff = Long.MAX_VALUE;
        Size closest = sizes[0];
        long closestDiff = Long.MAX_VALUE;
        long requestArea = (long) requestSize.getWidth() * requestSize.getHeight();
        for (Size size : sizes) {
            long area = (long) size.getWidth() * size.getHeight();
            long diff = Math.abs(area - requestArea);
            if (size.getWidth() == requestSize.getWidth() && size.getHeight() == requestSize.getHeight()) {
                exact = size;
                break;
            }
            if ((long) size.getWidth() * requestSize.getHeight() == (long) size.getHeight() * requestSize.getWidth()) {
                if (diff < sameRatioDiff) {
                    sameRatioDiff = diff;
                    sameRatio = size;
                }
            }
            if (diff < closestDiff) {
                closestDiff = diff;
                closest = size;
            }
        }
        if (exact != null) {
            return exact;
        }
        if (sameRatio != null) {
            return sameRatio;
        }
        return closest;
    }

    private Size parseRequestSize() {
        try {
            int width = Integer.parseInt(etWidth.getText().toString().trim());
            int height = Integer.parseInt(etHeight.getText().toString().trim());
            if (width <= 0 || height <= 0) {
                return null;
            }
            return new Size(width, height);
        } catch (Exception e) {
            Log.e(TAG, "parseRequestSize", e);
            return null;
        }
    }

    private void updateSizeTips(String cameraId) {
        if (TextUtils.isEmpty(cameraId)) {
            tvCaptureViewSize.setText("");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (manager == null) {
            return;
        }
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                tvCaptureViewSize.setText("camera " + cameraId + " has no size info");
                return;
            }
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            if (sizes == null || sizes.length == 0) {
                sizes = map.getOutputSizes(ImageFormat.JPEG);
            }
            if (sizes == null || sizes.length == 0) {
                tvCaptureViewSize.setText("camera " + cameraId + " has no size info");
                return;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("camera ").append(cameraId).append(" sizes: ");
            int count = Math.min(sizes.length, 8);
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    builder.append(" , ");
                }
                builder.append(sizes[i].getWidth()).append("x").append(sizes[i].getHeight());
            }
            tvCaptureViewSize.setText(builder.toString());
        } catch (Exception e) {
            Log.e(TAG, "updateSizeTips cameraId=" + cameraId, e);
            tvCaptureViewSize.setText("camera " + cameraId + " size query failed");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityActive = true;
        mainHandler.removeCallbacks(watchdogTask);
        mainHandler.postDelayed(watchdogTask, WATCHDOG_INTERVAL_MS);
        if (leftPane != null) {
            leftPane.onHostResume();
        }
        if (rightPane != null) {
            rightPane.onHostResume();
        }
    }

    @Override
    protected void onPause() {
        activityActive = false;
        mainHandler.removeCallbacks(watchdogTask);
        if (leftPane != null) {
            leftPane.onHostPause();
        }
        if (rightPane != null) {
            rightPane.onHostPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (leftPane != null) {
            leftPane.destroy();
        }
        if (rightPane != null) {
            rightPane.destroy();
        }
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            return;
        }
        permissionGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                break;
            }
        }
        if (!permissionGranted) {
            ToastUtils.info("Camera permissions were denied.");
        }
    }

    @Override
    public void onTextLeftClick() {
        finish();
    }

    @Override
    public void onTextCenterClick() {
    }

    @Override
    public void onTextRightClick() {
    }

    private void toast(String msg) {
        mainHandler.post(() -> ToastUtils.info(msg));
    }

    private String buildFilePath(String prefix, String suffix) {
        File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir == null) {
            dir = getFilesDir();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(dir, prefix + "_" + time + suffix).getAbsolutePath();
    }

    private abstract static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
            onSelected(position);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }

        public abstract void onSelected(int position);
    }

    private final class CameraPane {
        private static final int STATE_IDLE = 0;
        private static final int STATE_OPENING = 1;
        private static final int STATE_PREVIEW = 2;
        private static final int STATE_RECORDING = 3;
        private static final int STATE_RECONNECTING = 4;
        private static final int STATE_ERROR = 5;

        private final String label;
        private final TextureView textureView;
        private final HandlerThread cameraThread;
        private final Handler cameraHandler;
        private final Runnable reconnectRunnable = new Runnable() {
            @Override
            public void run() {
                if (!shouldBeOpen || userClosed || !activityActive || !surfaceReady) {
                    return;
                }
                state = STATE_RECONNECTING;
                closeInternal(false, false);
                openInternal();
            }
        };

        private CameraDevice cameraDevice;
        private CameraCaptureSession captureSession;
        private CaptureRequest.Builder previewBuilder;
        private MediaRecorder mediaRecorder;
        private Surface previewSurface;
        private Surface recorderSurface;
        private String cameraId;
        private Size requestSize;
        private Size activePreviewSize;
        private boolean shouldBeOpen;
        private boolean userClosed = true;
        private boolean surfaceReady;
        private boolean recording;
        private int state = STATE_IDLE;
        private long lastPreviewFrameAt;
        private String recordingPath;

        private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                surfaceReady = true;
                if (shouldBeOpen && !userClosed) {
                    scheduleReconnect("surface available", 0L);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                surfaceReady = false;
                cameraHandler.post(() -> closeInternal(false, false));
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                lastPreviewFrameAt = SystemClock.elapsedRealtime();
            }
        };

        private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                state = STATE_OPENING;
                startPreview(false);
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.w(TAG, label + " disconnected: " + cameraId);
                closeCameraDevice(camera);
                if (!userClosed && shouldBeOpen) {
                    scheduleReconnect("camera disconnected", RECONNECT_DELAY_MS);
                }
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.e(TAG, label + " error=" + error + " cameraId=" + cameraId);
                state = STATE_ERROR;
                closeCameraDevice(camera);
                if (!userClosed && shouldBeOpen) {
                    scheduleReconnect("camera error=" + error, RECONNECT_DELAY_MS);
                }
            }
        };

        CameraPane(String label, TextureView textureView) {
            this.label = label;
            this.textureView = textureView;
            this.textureView.setSurfaceTextureListener(surfaceTextureListener);
            this.surfaceReady = textureView.isAvailable();
            this.cameraThread = new HandlerThread("dual-camera-" + label);
            this.cameraThread.start();
            this.cameraHandler = new Handler(cameraThread.getLooper());
        }

        void open(String cameraId, Size requestSize) {
            this.cameraId = cameraId;
            this.requestSize = requestSize;
            this.activePreviewSize = getSupportedPreviewSize(cameraId, requestSize);
            this.shouldBeOpen = true;
            this.userClosed = false;
            if (activePreviewSize != null
                    && (activePreviewSize.getWidth() != requestSize.getWidth()
                    || activePreviewSize.getHeight() != requestSize.getHeight())) {
                toast(label + " use size " + activePreviewSize.getWidth() + "x" + activePreviewSize.getHeight());
            }
            scheduleReconnect("manual open", 0L);
        }

        void userClose() {
            shouldBeOpen = false;
            userClosed = true;
            state = STATE_IDLE;
            cameraHandler.removeCallbacks(reconnectRunnable);
            cameraHandler.post(() -> closeInternal(true, false));
        }

        void onHostResume() {
            if (shouldBeOpen && !userClosed && surfaceReady) {
                scheduleReconnect("host resume", 0L);
            }
        }

        void onHostPause() {
            cameraHandler.removeCallbacks(reconnectRunnable);
            cameraHandler.post(() -> closeInternal(false, false));
        }

        void destroy() {
            shouldBeOpen = false;
            userClosed = true;
            cameraHandler.removeCallbacksAndMessages(null);
            closeBlocking();
            cameraThread.quitSafely();
        }

        void capture() {
            Bitmap bitmap = textureView.getBitmap();
            if (!surfaceReady || bitmap == null) {
                toast(label + " preview is not ready.");
                return;
            }
            String path = buildFilePath("capture_" + label, ".jpg");
            cameraHandler.post(() -> saveBitmap(bitmap, path));
        }

        void startRecord() {
            cameraHandler.post(() -> {
                if (!isCameraReady()) {
                    toast(label + " camera is not open.");
                    return;
                }
                if (recording) {
                    toast(label + " is already recording.");
                    return;
                }
                recordingPath = buildFilePath("record_" + label, ".mp4");
                try {
                    prepareRecorder();
                    startPreview(true);
                } catch (Exception e) {
                    Log.e(TAG, label + " startRecord", e);
                    releaseRecorder();
                    scheduleReconnect("startRecord exception", RECONNECT_DELAY_MS);
                }
            });
        }

        void stopRecord(boolean notify) {
            cameraHandler.post(() -> stopRecordInternal(notify, true));
        }

        void showStatus() {
            String msg = label
                    + " state=" + stateToText(state)
                    + " cameraId=" + cameraId
                    + " requestSize=" + sizeToText(requestSize)
                    + " activeSize=" + sizeToText(activePreviewSize)
                    + " shouldOpen=" + shouldBeOpen
                    + " userClosed=" + userClosed
                    + " recording=" + recording
                    + " previewAlive=" + isPreviewAlive();
            toast(msg);
        }

        void checkHealth() {
            if (!shouldBeOpen || userClosed || !activityActive || !surfaceReady) {
                return;
            }
            if (state == STATE_PREVIEW || state == STATE_RECORDING || state == STATE_RECONNECTING || state == STATE_OPENING) {
                if (!isPreviewAlive()) {
                    scheduleReconnect("preview timeout", 0L);
                }
                return;
            }
            scheduleReconnect("state=" + stateToText(state), 0L);
        }

        private void openInternal() {
            if (TextUtils.isEmpty(cameraId) || requestSize == null || !surfaceReady || userClosed || !shouldBeOpen || !activityActive) {
                return;
            }
            if (activePreviewSize == null) {
                activePreviewSize = getSupportedPreviewSize(cameraId, requestSize);
            }
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            if (manager == null) {
                state = STATE_ERROR;
                return;
            }
            if (ActivityCompat.checkSelfPermission(DualCameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                state = STATE_ERROR;
                permissionGranted = false;
                return;
            }
            try {
                state = STATE_OPENING;
                manager.openCamera(cameraId, stateCallback, cameraHandler);
            } catch (Exception e) {
                Log.e(TAG, label + " openInternal cameraId=" + cameraId, e);
                state = STATE_ERROR;
                scheduleReconnect("open exception", RECONNECT_DELAY_MS);
            }
        }

        private void startPreview(boolean withRecorder) {
            if (!isCameraReady()) {
                return;
            }
            try {
                closeSession();
                closeSurfaces();
                SurfaceTexture texture = textureView.getSurfaceTexture();
                if (texture == null) {
                    scheduleReconnect("texture is null", RECONNECT_DELAY_MS);
                    return;
                }
                Size previewSize = activePreviewSize != null ? activePreviewSize : requestSize;
                texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                previewSurface = new Surface(texture);
                previewBuilder = cameraDevice.createCaptureRequest(withRecorder ? CameraDevice.TEMPLATE_RECORD : CameraDevice.TEMPLATE_PREVIEW);
                previewBuilder.addTarget(previewSurface);

                List<Surface> surfaces = new ArrayList<>();
                surfaces.add(previewSurface);
                if (withRecorder) {
                    if (mediaRecorder == null) {
                        prepareRecorder();
                    }
                    recorderSurface = mediaRecorder.getSurface();
                    previewBuilder.addTarget(recorderSurface);
                    surfaces.add(recorderSurface);
                }

                cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        captureSession = session;
                        try {
                            previewBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                            captureSession.setRepeatingRequest(previewBuilder.build(), null, cameraHandler);
                            lastPreviewFrameAt = SystemClock.elapsedRealtime();
                            if (withRecorder && mediaRecorder != null) {
                                mediaRecorder.start();
                                recording = true;
                                state = STATE_RECORDING;
                                toast(label + " record started.");
                            } else {
                                state = STATE_PREVIEW;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, label + " onConfigured", e);
                            scheduleReconnect("setRepeatingRequest exception", RECONNECT_DELAY_MS);
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.e(TAG, label + " configure failed");
                        state = STATE_ERROR;
                        scheduleReconnect("configure failed", RECONNECT_DELAY_MS);
                    }
                }, cameraHandler);
            } catch (Exception e) {
                Log.e(TAG, label + " startPreview", e);
                state = STATE_ERROR;
                scheduleReconnect("startPreview exception", RECONNECT_DELAY_MS);
            }
        }

        private void prepareRecorder() throws IOException {
            releaseRecorder();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(recordingPath);
            mediaRecorder.setVideoEncodingBitRate(10_000_000);
            mediaRecorder.setVideoFrameRate(30);
            Size previewSize = activePreviewSize != null ? activePreviewSize : requestSize;
            mediaRecorder.setVideoSize(previewSize.getWidth(), previewSize.getHeight());
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
        }

        private void stopRecordInternal(boolean notify, boolean restartPreview) {
            if (!recording && mediaRecorder == null) {
                return;
            }
            try {
                if (recording && mediaRecorder != null) {
                    mediaRecorder.stop();
                }
            } catch (Exception e) {
                Log.e(TAG, label + " stopRecordInternal", e);
            } finally {
                recording = false;
                releaseRecorder();
            }
            if (restartPreview && isCameraReady() && shouldBeOpen && !userClosed) {
                startPreview(false);
            } else if (!isCameraReady()) {
                state = STATE_IDLE;
            }
            if (notify) {
                toast(label + " record stopped.");
            }
        }

        private void scheduleReconnect(String reason, long delayMs) {
            if (!shouldBeOpen || userClosed) {
                return;
            }
            Log.w(TAG, label + " scheduleReconnect: " + reason);
            state = STATE_RECONNECTING;
            cameraHandler.removeCallbacks(reconnectRunnable);
            if (recording) {
                stopRecordInternal(false, false);
            }
            cameraHandler.postDelayed(reconnectRunnable, delayMs);
        }

        private void closeInternal(boolean clearIntent, boolean restartPreview) {
            stopRecordInternal(false, restartPreview);
            closeSession();
            closeDevice();
            closeSurfaces();
            if (clearIntent) {
                cameraId = null;
                requestSize = null;
                activePreviewSize = null;
            }
        }

        private void closeSession() {
            if (captureSession != null) {
                try {
                    captureSession.stopRepeating();
                } catch (Exception e) {
                    Log.w(TAG, label + " closeSession stopRepeating", e);
                }
                try {
                    captureSession.abortCaptures();
                } catch (Exception e) {
                    Log.w(TAG, label + " closeSession abortCaptures", e);
                }
                try {
                    captureSession.close();
                } catch (Exception e) {
                    Log.w(TAG, label + " closeSession close", e);
                }
                captureSession = null;
            }
        }

        private void closeDevice() {
            if (cameraDevice != null) {
                try {
                    cameraDevice.close();
                } catch (Exception e) {
                    Log.w(TAG, label + " closeDevice", e);
                }
                cameraDevice = null;
            }
        }

        private void closeCameraDevice(@NonNull CameraDevice camera) {
            if (camera == cameraDevice) {
                closeInternal(false, false);
            } else {
                try {
                    camera.close();
                } catch (Exception e) {
                    Log.w(TAG, label + " closeCameraDevice other", e);
                }
            }
        }

        private void closeSurfaces() {
            if (previewSurface != null) {
                previewSurface.release();
                previewSurface = null;
            }
            recorderSurface = null;
        }

        private void releaseRecorder() {
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.reset();
                } catch (Exception e) {
                    Log.w(TAG, label + " releaseRecorder reset", e);
                }
                try {
                    mediaRecorder.release();
                } catch (Exception e) {
                    Log.w(TAG, label + " releaseRecorder release", e);
                }
                mediaRecorder = null;
            }
            recorderSurface = null;
        }

        private void closeBlocking() {
            final Object lock = new Object();
            synchronized (lock) {
                cameraHandler.post(() -> {
                    closeInternal(true, false);
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                });
                try {
                    lock.wait(2000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private boolean isCameraReady() {
            return cameraDevice != null && surfaceReady && requestSize != null;
        }

        private String sizeToText(Size size) {
            if (size == null) {
                return "null";
            }
            return size.getWidth() + "x" + size.getHeight();
        }

        private boolean isPreviewAlive() {
            if (!shouldBeOpen || userClosed) {
                return false;
            }
            if (lastPreviewFrameAt <= 0L) {
                return false;
            }
            return SystemClock.elapsedRealtime() - lastPreviewFrameAt <= PREVIEW_TIMEOUT_MS;
        }

        private void saveBitmap(Bitmap bitmap, String path) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                fos.flush();
                toast(label + " capture saved: " + path);
            } catch (Exception e) {
                Log.e(TAG, label + " saveBitmap", e);
                toast(label + " capture failed.");
            } finally {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.w(TAG, label + " saveBitmap close", e);
                    }
                }
            }
        }

        private String stateToText(int state) {
            switch (state) {
                case STATE_IDLE:
                    return "IDLE";
                case STATE_OPENING:
                    return "OPENING";
                case STATE_PREVIEW:
                    return "PREVIEW";
                case STATE_RECORDING:
                    return "RECORDING";
                case STATE_RECONNECTING:
                    return "RECONNECTING";
                case STATE_ERROR:
                    return "ERROR";
                default:
                    return "UNKNOWN";
            }
        }
    }
}
