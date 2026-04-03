package com.ichtj.basetools.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.NetworkUtils;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.PACKAGES;

import org.apache.poi.ss.formula.functions.T;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Route(path = PACKAGES.BASE + "cameraJni")
public class CameraJNIAty extends BaseActivity {

    private static final String TAG = "CameraJNIAty";

    TextureView texture_left;
    TextureView texture_right;
    EditText etWidth;
    EditText etHeight;

    Spinner spLeft;
    Spinner spRight;

    CameraJNI cameraLeft;
    CameraJNI cameraRight;

    List<Integer> cameraIds = new ArrayList<>();

    private Bitmap leftBitmap;
    private Bitmap rightBitmap;

    private int previewWidth = 640;
    private int previewHeight = 480;

    private final Object lock = new Object();
    SimpleTimer timer = new SimpleTimer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_camera);

        texture_left = findViewById(R.id.texture_left);
        texture_right = findViewById(R.id.texture_right);

        etWidth = findViewById(R.id.etWidth);
        etHeight = findViewById(R.id.etHeight);

        spLeft = findViewById(R.id.spLeft);
        spRight = findViewById(R.id.spRight);

        cameraIds = getAvailableCameraIds();
        initSpinner();

        cameraLeft = new CameraJNI() {
            @Override
            public void onPreviewFrame(int cameraId, byte[] data) {
                Bitmap bmp = decodeFrame(data);
                synchronized (lock) {
                    leftBitmap = bmp;
                }
                drawLeft();
            }
        };

        cameraRight = new CameraJNI() {
            @Override
            public void onPreviewFrame(int cameraId, byte[] data) {
                Bitmap bmp = decodeFrame(data);
                synchronized (lock) {
                    rightBitmap = bmp;
                }
                drawRight();
            }
        };

        findViewById(R.id.btn_open_left).setOnClickListener(v -> openLeft());
        findViewById(R.id.btn_close_left).setOnClickListener(v -> closeLeft());

        findViewById(R.id.btn_open_right).setOnClickListener(v -> openRight());
        findViewById(R.id.btn_close_right).setOnClickListener(v -> closeRight());

        findViewById(R.id.btn_capture_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret=cameraLeft.capture();
                ToastUtils.info(ret==0?"成功":"失败");
            }
        });
        findViewById(R.id.btn_capture_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret=cameraRight.capture();
                ToastUtils.info(ret==0?"成功":"失败");
            }
        });
        findViewById(R.id.btnLeftRunning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRunning=cameraLeft.isRunning();
                int ret=cameraLeft.getState();
                ToastUtils.info("状态："+ret+"  运行："+isRunning);
            }
        });
        findViewById(R.id.btnRightRunning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRunning=cameraRight.isRunning();
                int ret=cameraRight.getState();
                ToastUtils.info("状态："+ret+" , 运行："+isRunning);
            }
        });

        timer.start(1500, new SimpleTimer.Callback() {
            @Override
            public void onTick() {
                ShellUtils.execCommand("chmod 777 /sys/class/leds/work/brightness",true);
                ShellUtils.execCommand("echo 0 > /sys/class/leds/work/brightness",true);
                try {
                    Thread.sleep(500);
                }catch (Throwable throwable){
                }
                ShellUtils.execCommand("echo 100 > /sys/class/leds/work/brightness",true);
            }
        });
    }

    // ================= 核心解码（关键优化） =================

    private Bitmap decodeFrame(byte[] data) {

        boolean isJpeg = data.length > 2 &&
                (data[0] & 0xFF) == 0xFF &&
                (data[1] & 0xFF) == 0xD8;

        if (isJpeg) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }

        // ⭐ RGB 数据
        int[] pixels = new int[previewWidth * previewHeight];

        int idx = 0;
        for (int i = 0; i < data.length; i += 3) {

            int r = data[i] & 0xFF;
            int g = data[i + 1] & 0xFF;
            int b = data[i + 2] & 0xFF;

            pixels[idx++] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        Bitmap bmp = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        bmp.setPixels(pixels, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        return bmp;
    }

    // ================= Spinner =================

    private void initSpinner() {

        List<String> list = new ArrayList<>();
        list.add("请选择摄像头");

        for (Integer id : cameraIds) {
            list.add("video" + id);
        }

//        ShellUtils.CommandResult commandResult=ShellUtils.execCommand("chmod 777 /dev/video*", true);
//        Log.d(TAG, "initSpinner: commandResult>>" + commandResult   );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                list
        );

        spLeft.setAdapter(adapter);
        spRight.setAdapter(adapter);
    }

    private Integer getSelectedId(Spinner sp) {
        int pos = sp.getSelectedItemPosition();
        if (pos == 0) return null;
        return cameraIds.get(pos - 1);
    }

    // ================= 左摄 =================

    private void openLeft() {
        Integer id = getSelectedId(spLeft);
        if (id == null) {
            Toast.makeText(this, "请选择左摄像头", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            previewWidth = Integer.parseInt(etWidth.getText().toString());
            previewHeight = Integer.parseInt(etHeight.getText().toString());

            cameraLeft.open(id, previewWidth, previewHeight);
            cameraLeft.start();

        } catch (Exception e) {
            Toast.makeText(this, "分辨率错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeLeft() {
        cameraLeft.release();
    }

    private void drawLeft() {
        if (!texture_left.isAvailable()) return;

        Canvas canvas = texture_left.lockCanvas();
        if (canvas == null) return;

        Bitmap bmp;
        synchronized (lock) {
            bmp = leftBitmap;
        }

        if (bmp != null) {
            canvas.drawBitmap(bmp, null,
                    new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
        }

        texture_left.unlockCanvasAndPost(canvas);
    }

    // ================= 右摄 =================

    private void openRight() {
        Integer id = getSelectedId(spRight);
        if (id == null) {
            Toast.makeText(this, "请选择右摄像头", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            previewWidth = Integer.parseInt(etWidth.getText().toString());
            previewHeight = Integer.parseInt(etHeight.getText().toString());

            cameraRight.open(id, previewWidth, previewHeight);
            cameraRight.start();

        } catch (Exception e) {
            Toast.makeText(this, "分辨率错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeRight() {
        cameraRight.release();
    }

    private void drawRight() {
        if (!texture_right.isAvailable()) return;

        Canvas canvas = texture_right.lockCanvas();
        if (canvas == null) return;

        Bitmap bmp;
        synchronized (lock) {
            bmp = rightBitmap;
        }

        if (bmp != null) {
            canvas.drawBitmap(bmp, null,
                    new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
        }

        texture_right.unlockCanvasAndPost(canvas);
    }

    public static List<Integer> getAvailableCameraIds() {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            if (new File("/dev/video" + i).exists()) {
                list.add(i);
            }
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraLeft.release();
        cameraRight.release();
        timer.stop();
    }
}