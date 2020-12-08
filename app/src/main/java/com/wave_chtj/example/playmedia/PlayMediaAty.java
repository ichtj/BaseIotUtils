package com.wave_chtj.example.playmedia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.enums.PLAY_STATUS;
import com.face_chtj.base_iotutils.audio.PlayStateChangeListener;
import com.face_chtj.base_iotutils.audio.PlayUtils;
import com.face_chtj.base_iotutils.enums.VOLUME_TYPE;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

public class PlayMediaAty extends BaseActivity {
    private static final String TAG = "PlayMediaAty";
    Button btn_pause_resume;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_media);
        btn_pause_resume = findViewById(R.id.btn_pause_resume);
        btn_pause_resume = findViewById(R.id.btn_pause_resume);
    }

    /**
     * 播放
     *
     * @param view
     */
    public void playMediaClick(View view) {
        PlayUtils.getInstance().
                setPlayStateChangeListener(new PlayStateChangeListener() {
                    @Override
                    public void onPlayStateChange(PLAY_STATUS play_status) {
                        KLog.d(TAG," play_status= "+play_status.name());
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
            PlayUtils.getInstance().pausePlay();
            isPause = false;
            btn_pause_resume.setText("继续");
        } else {
            KLog.d(TAG,"继续");
            PlayUtils.getInstance().resumePlay();
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
        PlayUtils.getInstance().stopPlaying();
        isPause = true;
        btn_pause_resume.setText("暂停");
    }

    /**
     * 左声道
     *
     * @param view
     */
    public void switchChannelLeftClick(View view) {
        PlayUtils.getInstance().setVolumeType(VOLUME_TYPE.LEFT);
    }

    /**
     * 右声道
     *
     * @param view
     */
    public void switchChannelRightClick(View view) {
        PlayUtils.getInstance().setVolumeType(VOLUME_TYPE.RIGHT);
    }

    /**
     * 左右声道
     *
     * @param view
     */
    public void switchChannelAllClick(View view) {
        PlayUtils.getInstance().setVolumeType(VOLUME_TYPE.ALL);
    }
}
