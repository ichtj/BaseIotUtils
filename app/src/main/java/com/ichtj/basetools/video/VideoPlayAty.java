package com.ichtj.basetools.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.face_chtj.base_iotutils.ZipUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

/**
 * Create on 2020/7/13
 * author chtj
 * desc
 */
public class VideoPlayAty extends BaseActivity {
    private static final int FILE_PICKER_REQUEST_CODE = 1;
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
        // 启动文件选择器Intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*"); // 只显示视频文件
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedFileUri = data.getData();
                String selectedFilePath= UriPathUtils.getPath(selectedFileUri);
                playVideo(selectedFilePath);
                Toast.makeText(this, "选定的文件路径：" + selectedFilePath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "未选择任何文件", Toast.LENGTH_SHORT).show();
            }
        }
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
