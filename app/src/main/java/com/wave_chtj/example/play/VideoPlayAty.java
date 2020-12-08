package com.wave_chtj.example.play;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
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
    Button btn_load_video;
    Button btn_play;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        jz_video = findViewById(R.id.jz_video);
        btn_load_video = findViewById(R.id.btn_load_video);
        btn_play = findViewById(R.id.btn_play);
    }


    //zip保存的路径
    private static final String savePath = "/sdcard/aging.zip";
    // 文件名
    private static final String fileName = "aging.zip";
    //解压缩后的路径
    private static final String unZipPath = "/sdcard/";

    public void initFile(View view) {
        try {
            //视频文件不存在时将文件保存到本地
            if (!new File(savePath).exists()) {
                InputStream input = getAssets().open(fileName);
                writeToLocal(savePath, input);
                boolean isUnzip = ZipUtils.unzipFile(savePath, unZipPath);
                if (isUnzip && new File(savePath).exists()) {
                    KLog.d(TAG, "Video ready！");
                    ToastUtils.success("加载成功");
                } else {
                    KLog.d(TAG, "Video not ready！");
                    ToastUtils.error("加载失败");
                }
            } else {
                KLog.d(TAG, "Aging_Test_Video.mp4 exist");
                ToastUtils.success("加载成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    public void playVideoClick(View view) {
        playVideo();
    }

    /**
     * 将InputStream写入本地文件
     *
     * @param destDirPath 写入本地目录
     * @param input       输入流
     * @throws IOException
     */
    public static void writeToLocal(String destDirPath, InputStream input)
            throws IOException {

        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destDirPath);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
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
