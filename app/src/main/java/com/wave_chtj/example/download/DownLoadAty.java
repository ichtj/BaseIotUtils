package com.wave_chtj.example.download;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.download.DownLoadManager;
import com.face_chtj.base_iotutils.download.progress.ProgressCallBack;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;

/**
 * Create on 2019/10/10
 * author chtj
 */
public class DownLoadAty extends BaseActivity {
    private static final String TAGS="DownLoadAty";
    //文件下载地址
    private String downloadUrl = "https://ad-1257276602.cos.ap-guangzhou.myqcloud.com/20190121/5de956aa6ce04c088f73ad95301915b6.xls";
    //存放地址
    private String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    //替换的文件名称
    private String destFileName = "5de956aa6ce04c088f73ad95301915b6.xls";
    @BindView(R.id.pb_progressbar)
    ProgressBar pbProgressbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
    }

    //文件下载
    public void downloadFile(View view) {
        DownLoadManager.getInstance().load(downloadUrl, new ProgressCallBack<ResponseBody>(destFileDir, destFileName) {
            @Override
            public void onStart() {
                super.onStart();
                Log.d(TAGS,"开始下载...");
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                ToastUtils.showShort("文件下载完成！");
            }

            @Override
            public void progress(final long progress, final long total) {
                Log.e(TAGS, "progress=" + progress + ",total=" + total);
                double progressNumber = ((progress * 1.0) / total) * 100;
                Log.e(TAGS, "progressNumber=" + progressNumber);
                pbProgressbar.setProgress((int) progressNumber);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                KLog.e(TAGS,e.getMessage());
                ToastUtils.showShort("文件下载失败！");
            }
        });
    }
}
