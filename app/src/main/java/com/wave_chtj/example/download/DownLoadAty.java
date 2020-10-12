package com.wave_chtj.example.download;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.download.DownLoadManager;
import com.face_chtj.base_iotutils.download.progress.ProgressCallBack;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;

/**
 * Create on 2019/10/10
 * author chtj
 * desc 文件下载
 */
public class DownLoadAty extends BaseActivity {
    private static final String TAG = "DownLoadAty";
    @BindView(R.id.btn_start_down)
    Button btnStartDown;
    @BindView(R.id.pb_progressbar)
    ProgressBar pbProgressbar;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_pause)
    Button btnPause;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.pb_progressbar2)
    ProgressBar pbProgressbar2;
    //文件下载地址
    private String downloadUrl = "https://ad-1257276602.cos.ap-guangzhou.myqcloud.com/20190121/5de956aa6ce04c088f73ad95301915b6.xls";
    //替换的文件名称
    private String destFileName = "5de956aa6ce04c088f73ad95301915b6.xls";


    //文件下载地址
    private String fwDownloadUrl = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/test_AIO145/update.zip";
    //替换的文件名称
    private String fwDestFileName = "update.zip";



    //存放地址
    private String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    DownloadTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
    }

    //文件下载
    public void downloadFile(View view) {
        if (NetUtils.getNetWorkType() == NetUtils.NETWORK_NO) {
            ToastUtils.error("当前无网络连接！");
            return;
        }
        DownLoadManager.getInstance().load(downloadUrl, new ProgressCallBack<ResponseBody>(destFileDir, destFileName) {
            @Override
            public void onStart() {
                super.onStart();
                Log.d(TAG, "开始下载...");
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                ToastUtils.showShort("文件下载完成！");
            }

            @Override
            public void progress(final long progress, final long total) {
                Log.e(TAG, "progress=" + progress + ",total=" + total);
                double progressNumber = ((progress * 1.0) / total) * 100;
                Log.e(TAG, "progressNumber=" + progressNumber);
                pbProgressbar.setProgress((int) progressNumber);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                KLog.e(TAG, e.getMessage());
                ToastUtils.showShort("文件下载失败！");
            }
        });
    }

    DownloadListener downloadListener=new DownloadListener() {
        @Override
        public void taskStart(@NonNull DownloadTask task) {
            KLog.d(TAG,"taskStart:116>=");
        }

        @Override
        public void connectTrialStart(@NonNull DownloadTask task, @NonNull Map<String, List<String>> requestHeaderFields) {
            KLog.d(TAG,"connectTrialStart:121>=");
        }

        @Override
        public void connectTrialEnd(@NonNull DownloadTask task, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {
            KLog.d(TAG,"connectTrialEnd:126>=");
        }

        @Override
        public void downloadFromBeginning(@NonNull DownloadTask task, @NonNull BreakpointInfo info, @NonNull ResumeFailedCause cause) {
            KLog.d(TAG,"downloadFromBeginning:131>=");
        }

        @Override
        public void downloadFromBreakpoint(@NonNull DownloadTask task, @NonNull BreakpointInfo info) {
            KLog.d(TAG,"downloadFromBreakpoint:136>=");
        }

        @Override
        public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {
            KLog.d(TAG,"connectStart:141>=");
        }

        @Override
        public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {
            KLog.d(TAG,"connectEnd:146>=");
        }

        @Override
        public void fetchStart(@NonNull DownloadTask task, int blockIndex, long contentLength) {
            KLog.d(TAG,"fetchStart:151> contentLength="+contentLength);
        }

        @Override
        public void fetchProgress(@NonNull DownloadTask task, int blockIndex, long increaseBytes) {
            KLog.d(TAG,"fetchProgress:156> increaseBytes="+increaseBytes);
        }

        @Override
        public void fetchEnd(@NonNull DownloadTask task, int blockIndex, long contentLength) {
            KLog.d(TAG,"fetchEnd:161> contentLength="+contentLength);
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause) {
            KLog.d(TAG,"taskEnd:166>=");
        }
    };

    @OnClick({R.id.btn_start, R.id.btn_pause, R.id.btn_stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                task.enqueue(downloadListener);
                break;
            case R.id.btn_pause:

                break;
            case R.id.btn_stop:
                // cancel
                task.cancel();
                break;
        }
    }
}
