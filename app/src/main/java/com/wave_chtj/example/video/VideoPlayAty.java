package com.wave_chtj.example.video;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.ZipUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

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
    VideoPlayerView jz_video;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        initFile();
        jz_video = findViewById(R.id.jz_video);
        //设置地址
        jz_video.setUp("/sdcard/aging.mp4"
                , "VPU");
        //设置缩略图
        //jz_video.posterImageView.setImageDrawable(ContextCompat.getDrawable(VideoPlayAty.this,R.drawable.jz_loading_bg));
        //全屏按钮
        jz_video.fullscreenButton.setVisibility(View.VISIBLE);
        jz_video.startButton.performClick();
    }


    //zip保存的路径
    private static final String savePath = "/sdcard/aging.zip";
    // 文件名
    private static final String fileName = "aging.zip";
    //解压缩后的路径
    private static final String unZipPath = "/sdcard/";

    public void initFile() {
        try {
            //视频文件不存在时将文件保存到本地
            if (!new File(savePath).exists()) {
                InputStream input = getAssets().open(fileName);
                writeToLocal(savePath, input);
                boolean isUnzip = ZipUtils.unzipFile(savePath, unZipPath);
                if (isUnzip && new File(savePath).exists()) {
                    KLog.d("Video ready！");
                    ToastUtils.success("加载成功");
                } else {
                    KLog.d("Video not ready！");
                    ToastUtils.error("加载失败");
                }
            } else {
                KLog.d("Aging_Test_Video.mp4 exist");
                ToastUtils.success("加载成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e("errMeg:" + e.getMessage());
        }
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
