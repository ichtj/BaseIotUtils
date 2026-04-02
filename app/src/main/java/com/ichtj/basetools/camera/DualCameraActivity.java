package com.ichtj.basetools.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DualCameraActivity extends BaseActivity {
    private static final String TAG = "DualCameraActivity";
    private TextureView leftView, rightView;
    private TextView tvCaptureViewSize;

    private CameraDevice leftCamera, rightCamera;
    private CameraCaptureSession leftSession, rightSession;

    private HandlerThread cameraThread;
    private Handler cameraHandler;

    private CameraManager cameraManager;

    private String leftId = "0";
    private String rightId = "1";

    // ⭐新增：ImageReader
    private ImageReader leftReader, rightReader;

    // ⭐新增：保存路径
    private final String SAVE_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/DCIM/DualCamera/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_camera);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        leftView = findViewById(R.id.texture_left);
        rightView = findViewById(R.id.texture_right);
        tvCaptureViewSize = findViewById(R.id.tvCaptureViewSize);

        startThread();

        findViewById(R.id.btn_open_left).setOnClickListener(v -> openLeft());
        findViewById(R.id.btn_close_left).setOnClickListener(v -> closeLeft());
        findViewById(R.id.btn_capture_left).setOnClickListener(v -> captureLeft());

        findViewById(R.id.btn_open_right).setOnClickListener(v -> openRight());
        findViewById(R.id.btn_close_right).setOnClickListener(v -> closeRight());
        findViewById(R.id.btn_capture_right).setOnClickListener(v -> captureRight());

        getCameraIds();
        getCaptureViewSize();
        // ⭐创建目录
        new File(SAVE_PATH).mkdirs();
    }

    public void getCameraIds(){
        try {
            int size=cameraManager.getCameraIdList().length;
            if (size==1){
                leftId=cameraManager.getCameraIdList()[0];
            }else if (size==2){
                leftId=cameraManager.getCameraIdList()[0];
                rightId=cameraManager.getCameraIdList()[1];
            }else{
                ToastUtils.info( "检测到超过2个摄像头，只加载前两个！", Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getCaptureViewSize() {
        try {
            StringBuilder stringBuilder=new StringBuilder();
            for (int i = 0; i < cameraManager.getCameraIdList().length; i++) {
                CameraCharacteristics characteristics =
                        cameraManager.getCameraCharacteristics(cameraManager.getCameraIdList()[i]);

                StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map != null) {
                    Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
                    stringBuilder.append("摄像头ID : "+cameraManager.getCameraIdList()[i]+",支持分辨率: ");
                    for (Size size : sizes) {
                        Log.d(TAG,
                                "支持分辨率: " + size.getWidth() + "x" + size.getHeight());
                        stringBuilder.append(size.getWidth()+"x"+size.getHeight()+" ");
                    }
                    stringBuilder.append("\n");
                }
            }
            tvCaptureViewSize.setText(stringBuilder.toString());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startThread() {
        cameraThread = new HandlerThread("cameraThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    // ================= 左 =================

    private void openLeft() {
        try {
            // ⭐创建 ImageReader
            leftReader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 2);
            leftReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireLatestImage();
                if (image == null) return;

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                saveImage(bytes, "LEFT");

                image.close();
            }, cameraHandler);

            cameraManager.openCamera(leftId, leftCallback, cameraHandler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private final CameraDevice.StateCallback leftCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            leftCamera = camera;
            createPreview(camera, leftView, true);
        }

        @Override public void onDisconnected(CameraDevice camera) { camera.close(); }
        @Override public void onError(CameraDevice camera, int error) { camera.close(); }
    };

    private void closeLeft() {
        if (leftSession != null) leftSession.close();
        if (leftCamera != null) leftCamera.close();
        if (leftReader != null) leftReader.close();
    }

    private void captureLeft() {
        capture(leftCamera, leftSession, leftReader);
    }

    // ================= 右 =================

    private void openRight() {
        try {
            rightReader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 2);
            rightReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireLatestImage();
                if (image == null) return;

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                saveImage(bytes, "RIGHT");

                image.close();
            }, cameraHandler);

            cameraManager.openCamera(rightId, rightCallback, cameraHandler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private final CameraDevice.StateCallback rightCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            rightCamera = camera;
            createPreview(camera, rightView, false);
        }

        @Override public void onDisconnected(CameraDevice camera) { camera.close(); }
        @Override public void onError(CameraDevice camera, int error) { camera.close(); }
    };

    private void closeRight() {
        if (rightSession != null) rightSession.close();
        if (rightCamera != null) rightCamera.close();
        if (rightReader != null) rightReader.close();
    }

    private void captureRight() {
        capture(rightCamera, rightSession, rightReader);
    }

    // ================= 预览 =================

    private void createPreview(CameraDevice camera, TextureView view, boolean isLeft) {
        try {
            SurfaceTexture texture = view.getSurfaceTexture();
            texture.setDefaultBufferSize(1280, 720);
            Surface previewSurface = new Surface(texture);

            Surface imageSurface = isLeft ? leftReader.getSurface() : rightReader.getSurface();

            CaptureRequest.Builder builder =
                    camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(previewSurface);

            camera.createCaptureSession(Arrays.asList(previewSurface, imageSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            try {
                                session.setRepeatingRequest(builder.build(),
                                        null, cameraHandler);

                                if (isLeft) leftSession = session;
                                else rightSession = session;

                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {}
                    }, cameraHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 拍照 =================

    private void capture(CameraDevice camera,
                         CameraCaptureSession session,
                         ImageReader reader) {
        try {
            if (camera == null || session == null) return;

            CaptureRequest.Builder builder =
                    camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            builder.addTarget(reader.getSurface());

            session.capture(builder.build(),
                    new CameraCaptureSession.CaptureCallback() {},
                    cameraHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 保存 =================

    private void saveImage(byte[] data, String tag) {
        try {
            File file = new File(SAVE_PATH,
                    tag + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            ToastUtils.success("保存成功: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.error("保存失败");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeLeft();
        closeRight();
        cameraThread.quitSafely();
    }
}