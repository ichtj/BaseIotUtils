package com.wave_chtj.example.audio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.callback.IAudioState;
import com.face_chtj.base_iotutils.callback.IPlayStateCallback;
import com.face_chtj.base_iotutils.AudioUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

public class AudioAty extends BaseActivity {
    private static final String TAG = "PlayMediaAty";
    Button btn_pause_resume;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        btn_pause_resume = findViewById(R.id.btn_pause_resume);
        btn_pause_resume = findViewById(R.id.btn_pause_resume);
    }

    /**
     * 播放
     *
     * @param view
     */
    public void playMediaClick(View view) {
        AudioUtils.getInstance().
                setPlayStateChangeListener(new IPlayStateCallback() {
                    @Override
                    public void onPlayStateChange(@IAudioState int play_status) {
                        KLog.d(TAG," play_status= "+play_status);
                    }

                    @Override
                    public void getProgress(int sumProgress, int nowProgress) {
                        KLog.d(TAG, " sumProgress= " + sumProgress + ",nowProgress= " + nowProgress);
                    }

                }).
                startPlaying("/sdcard/4576.wav");
    }

    boolean isPause = true;

    /**
     * 暂停
     *
     * @param view
     */
    public void pauseMediaClick(View view) {
        if (isPause) {
            KLog.d(TAG,"暂停");
            AudioUtils.getInstance().pausePlay();
            isPause = false;
            btn_pause_resume.setText("继续");
        } else {
            KLog.d(TAG,"继续");
            AudioUtils.getInstance().resumePlay();
            isPause = true;
            btn_pause_resume.setText("暂停");
        }
    }

    /**
     * 关闭
     *
     * @param view
     */
    public void stopMediaClick(View view) {
        AudioUtils.getInstance().stopPlaying();
        isPause = true;
        btn_pause_resume.setText("暂停");
    }

    /**
     * 左声道
     *
     * @param view
     */
    public void switchChannelLeftClick(View view) {
        AudioUtils.getInstance().setVolumeType(AudioUtils.LEFT);
    }

    /**
     * 右声道
     *
     * @param view
     */
    public void switchChannelRightClick(View view) {
        AudioUtils.getInstance().setVolumeType(AudioUtils.RIGHT);
    }

    /**
     * 左右声道
     *
     * @param view
     */
    public void switchChannelAllClick(View view) {
        AudioUtils.getInstance().setVolumeType(AudioUtils.ALL);
    }
}
