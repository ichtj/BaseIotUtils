package com.wave_chtj.example.video;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.face_chtj.base_iotutils.KLog;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PlayCacheVideoAty extends BaseActivity implements View.OnClickListener {
    private static final String TAG = PlayCacheVideoAty.class.getSimpleName();
    private SurfaceView mCameraPreview;
    private SurfaceHolder mSurfaceHolder;
    private ImageButton mShutter;
    private TextView mMinutePrefix;
    private TextView mMinuteText;
    private TextView mSecondPrefix;
    private TextView mSecondText;

    ImageButton record_play;
    VideoPlayerView videoPlayerView;
    private String lastFileName;
    private Camera mCamera;
    private MediaRecorder mRecorder;
    private final static int CAMERA_ID = 0;

    private boolean mIsRecording = false;
    private boolean mIsSufaceCreated = false;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocache);
        localRecord();
    }

    public void localRecord() {
        videoPlayerView = findViewById(R.id.videoPlayerView);
        record_play = findViewById(R.id.record_play);
        record_play.setOnClickListener(this);

        mCameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        mMinutePrefix = (TextView) findViewById(R.id.timestamp_minute_prefix);
        mMinuteText = (TextView) findViewById(R.id.timestamp_minute_text);
        mSecondPrefix = (TextView) findViewById(R.id.timestamp_second_prefix);
        mSecondText = (TextView) findViewById(R.id.timestamp_second_text);

        mSurfaceHolder = mCameraPreview.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mShutter = (ImageButton) findViewById(R.id.record_shutter);
        mShutter.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsRecording) {
            stopRecording();
        }
        stopPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsSufaceCreated = false;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mIsSufaceCreated = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            startPreview();
        }
    };

    //启动预览
    private void startPreview() {
        if (mCamera != null || !mIsSufaceCreated) {
            Log.d(TAG, "startPreview will return");
            return;
        }
        try {
            mCamera = Camera.open(CAMERA_ID);

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = getBestPreviewSize(1080, 1920, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
            }

            //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            //parameters.setPreviewFrameRate(20);

            //设置相机预览方向
            mCamera.setDisplayOrientation(90);

            mCamera.setParameters(parameters);

            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
//          mCamera.setPreviewCallback(mPreviewCallback);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }

            mCamera.startPreview();
        }catch (Throwable e){
            e.printStackTrace();
            KLog.e("errMeg:"+e.getMessage());
        }
    }

    private void stopPreview() {
        //释放Camera对象
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.record_shutter) {
            if (mIsRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        } else if (v.getId() == R.id.record_play) {
            KLog.d("onClick() lastFileName >> " + lastFileName);
            //设置地址
            videoPlayerView.setUp(lastFileName
                    , "VPU");
            //设置缩略图
            //jz_video.posterImageView.setImageDrawable(ContextCompat.getDrawable(VideoPlayAty.this,R.drawable.jz_loading_bg));
            //全屏按钮
            videoPlayerView.fullscreenButton.setVisibility(View.VISIBLE);
            videoPlayerView.startButton.performClick();
        }
    }


    private void startRecording() {
        try {
            mRecorder=new MediaRecorder();
            //给Recorder设置Camera对象，保证录像跟预览的方向保持一致
            mRecorder.setCamera(mCamera);
            mRecorder.setOrientationHint(90);  //改变保存后的视频文件播放时是否横屏(不加这句，视频文件播放的时候角度是反的)
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置从麦克风采集声音
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 设置从摄像头采集图像
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);  // 设置视频的输出格式 为MP4
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); // 设置音频的编码格式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // 设置视频的编码格式
            mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);// 设置视频编码的比特率
            mRecorder.setVideoSize(1280, 720);  // 设置视频大小
            mRecorder.setVideoFrameRate(20); // 设置帧率
            //mRecorder.setMaxDuration(10000); //设置最大录像时间为10s
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            //设置视频存储路径
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator + "VideoRecorder");
            if (!file.exists()) {
                //多级文件夹的创建
                file.mkdirs();
            }
            lastFileName = file.getPath() + File.separator + "VID_" + System.currentTimeMillis() + ".mp4";
            mRecorder.setOutputFile(lastFileName);

            mRecorder.prepare();
            mRecorder.start();   // Recording is now started
            mShutter.setImageDrawable(getResources().getDrawable(R.mipmap.recording_shutter_hl));
            mIsRecording = true;
            //开始录像后，每隔1s去更新录像的时间戳
            mHandler.postDelayed(mTimestampRunnable, 1000);
        } catch (Exception e) {
            mIsRecording = false;
            KLog.e("startRecording e >> " + e.getMessage());
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        mShutter.setImageDrawable(getResources().getDrawable(R.mipmap.recording_shutter));
        mIsRecording = false;

        mHandler.removeCallbacks(mTimestampRunnable);
        if (null != lastFileName && !"".equals(lastFileName)) {
            File f = new File(lastFileName);
            String name = f.getName().substring(0,
                    f.getName().lastIndexOf(".mp4"));
            name += "_" + getRecordTime() + ".mp4";
            String newPath = f.getParentFile().getAbsolutePath() + "/"
                    + name;
            if (f.renameTo(new File(newPath))) {
                int i = 0;
                i++;
            }
        }

        //将录像时间还原
        mMinutePrefix.setVisibility(View.VISIBLE);
        mMinuteText.setText("0");
        mSecondPrefix.setVisibility(View.VISIBLE);
        mSecondText.setText("0");

        //重启预览
        startPreview();
    }

    private Runnable mTimestampRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimestamp();
            mHandler.postDelayed(this, 1000);
        }
    };

    private String getRecordTime() {
        int second = Integer.parseInt(mSecondText.getText().toString());
        int minute = Integer.parseInt(mMinuteText.getText().toString());
        if (minute > 0) {
            return minute + "m" + second + "s";
        }
        return second + "s";
    }

    private void updateTimestamp() {
        int second = Integer.parseInt(mSecondText.getText().toString());
        int minute = Integer.parseInt(mMinuteText.getText().toString());
        second++;
        Log.d(TAG, "second: " + second);

        if (second < 10) {
            mSecondText.setText(String.valueOf(second));
        } else if (second >= 10 && second < 60) {
            mSecondPrefix.setVisibility(View.GONE);
            mSecondText.setText(String.valueOf(second));
        } else if (second >= 60) {
            mSecondPrefix.setVisibility(View.VISIBLE);
            mSecondText.setText("0");

            minute++;
            mMinuteText.setText(String.valueOf(minute));
        } else if (minute >= 10) {
            mMinutePrefix.setVisibility(View.GONE);
        }
    }
}
