package com.ichtj.basetools.video;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.FileDialogSelectUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

/**
 * Create on 2020/7/13
 * author chtj
 * desc
 */
public class VideoPlayAty extends BaseActivity {
    private static final String TAG = VideoPlayAty.class.getSimpleName();
    VideoPlayerView jz_video;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        jz_video = findViewById(R.id.jz_video);
    }

    private void playVideo(String url) {
        //设置地址
        jz_video.setUp(url, "VPU");
        jz_video.startVideo();
    }

    public void selectFileClick(View view){
        FileDialogSelectUtils fileDialogSelectUtils =new FileDialogSelectUtils(this, new File("/sdcard/"), new FileDialogSelectUtils.FileSelectCallback() {
            @Override
            public void onFileSelected(List<File> selected) {
                Log.d(TAG, "onFileSelected: "+selected);
                playVideo(selected.get(0).getAbsolutePath());
            }
        }).setSizeRatio(0.3f,0.5f,30).setSingleSelect(true);
        fileDialogSelectUtils.show();
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
