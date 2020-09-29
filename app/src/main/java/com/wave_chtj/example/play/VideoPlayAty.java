package com.wave_chtj.example.play;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ZipUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.internal.Utils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

/**
 * Create on 2020/7/13
 * author chtj
 * desc
 */
public class VideoPlayAty extends BaseActivity {
    private static final String TAG = "VideoPlayAty";
    VideoPlayerView jz_video;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        jz_video = (VideoPlayerView) findViewById(R.id.jz_video);
        playVideo();
    }



    /**
     * 播放相关
     */
    private void playVideo() {
        //设置地址
        jz_video.setUp("/sdcard/aging.mp4"
                , "VPU");
        //设置缩略图
        //jz_video.posterImageView.setImageDrawable(ContextCompat.getDrawable(VideoPlayAty.this,R.drawable.jz_loading_bg));
        //全屏按钮
        jz_video.fullscreenButton.setVisibility(View.VISIBLE);
        //自动播放
        jz_video.startButton.performClick();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JzvdStd.releaseAllVideos();
    }

}
