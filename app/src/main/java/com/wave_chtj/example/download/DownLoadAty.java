package com.wave_chtj.example.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.download.DownloadStatus;
import com.face_chtj.base_iotutils.download.DownloadSupport;
import com.face_chtj.base_iotutils.entity.FileCacheData;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * Create on 2019/10/10
 * author chtj
 * desc 文件下载
 */
public class DownLoadAty extends BaseActivity {
    private static final String TAG = "DownLoadAty";
    ProgressBar pbProgressbar1, pbProgressbar2,pbProgressbar3;
    TextView tvResult1, tvResult2,tvResult3;
    private String saveRootPath = "/sdcard/";
    //文件下载地址
    public static final String downloadUrl1 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/BM54/v0.99/update.zip";
    //替换的文件名称
    public String fileName1 = "update1.zip";


    //文件下载地址
    public static final String downloadUrl2 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/test_AIO145/update.zip";
    //替换的文件名称
    public String fileName2 = "update2.zip";


    //文件下载地址
    public static final String downloadUrl3 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/20200108-APK/Settings.apk";
    //替换的文件名称
    public String fileName3 = "Settings.apk";



    DownloadSupport downloadSupport;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        tvResult1 = findViewById(R.id.tvResult1);
        tvResult2 = findViewById(R.id.tvResult2);
        tvResult3 = findViewById(R.id.tvResult3);
        pbProgressbar1 = findViewById(R.id.pbProgressbar1);
        pbProgressbar2 = findViewById(R.id.pbProgressbar2);
        pbProgressbar3 = findViewById(R.id.pbProgressbar3);
        //设置最大进度位100
        pbProgressbar1.setMax(100);
        pbProgressbar2.setMax(100);
        pbProgressbar3.setMax(100);
        downloadSupport=new DownloadSupport();
    }

    /**
     * 清理下载的文件
     *
     * @param view
     */
    public void clearFile(View view) {
        downloadSupport.deleteFile(saveRootPath);
        pbProgressbar1.setProgress(0);
        pbProgressbar2.setProgress(0);
        pbProgressbar3.setProgress(0);
        tvResult1.setText("update1.zip >>> 0%");
        tvResult2.setText("update2.zip >>> 0%");
        tvResult3.setText("Settings.apk >>> 0%");
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause1(View view) {
        if(fileCacheData!=null){
            downloadSupport.pause(fileCacheData.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause2(View view) {
        if(fileCacheData2!=null){
            downloadSupport.pause(fileCacheData2.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause3(View view) {
        if(fileCacheData3!=null){
            downloadSupport.pause(fileCacheData3.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause(View view) {
        downloadSupport.pause();
    }

    /**
     * 关闭所有任务
     *
     * @param view
     */
    public void downloadStop(View view) {
        downloadSupport.cancel();
    }

    FileCacheData fileCacheData = null;

    //文件下载
    public void downloadFile1(View view) {
        if (NetUtils.getNetWorkType() == NetUtils.NETWORK_NO) {
            ToastUtils.error("当前无网络连接！");
            return;
        }
        //开启任务下载----------------------这里可执行多个任务 重复执行即可---------
        fileCacheData = new FileCacheData();
        fileCacheData.setUrl(downloadUrl1);
        fileCacheData.setFileName(fileName1);
        fileCacheData.setRequestTag(downloadUrl1);
        fileCacheData.setFilePath(saveRootPath + fileName1);
        addDownloadTask(fileCacheData);
        //-----------------------------------------------------------
    }

    FileCacheData fileCacheData2 = null;

    //文件下载
    public void downloadFile2(View view) {
        if (NetUtils.getNetWorkType() == NetUtils.NETWORK_NO) {
            ToastUtils.error("当前无网络连接！");
            return;
        }
        fileCacheData2 = new FileCacheData();
        fileCacheData2.setUrl(downloadUrl2);
        fileCacheData2.setFileName(fileName2);
        fileCacheData2.setRequestTag(downloadUrl2);
        fileCacheData2.setFilePath(saveRootPath + fileName2);
        addDownloadTask(fileCacheData2);
        //-----------------------------------------------------------
    }

    FileCacheData fileCacheData3= null;

    //文件下载
    public void downloadFile3(View view) {
        if (NetUtils.getNetWorkType() == NetUtils.NETWORK_NO) {
            ToastUtils.error("当前无网络连接！");
            return;
        }
        fileCacheData3 = new FileCacheData();
        fileCacheData3.setUrl(downloadUrl3);
        fileCacheData3.setFileName(fileName3);
        fileCacheData3.setRequestTag(downloadUrl3);
        fileCacheData3.setFilePath(saveRootPath + fileName3);
        addDownloadTask(fileCacheData3);
        //-----------------------------------------------------------
    }

    /**
     * 添加下载任务
     *
     * @param fileCacheData
     */
    public void addDownloadTask(FileCacheData fileCacheData) {
        downloadSupport.addStartTask(fileCacheData, downloadCallBack);
    }

    //下载进度  可根据设置的requestTag来区分属于哪个下载进度 fileCacheData.getRequestTag()
    DownloadSupport.DownloadCallBack downloadCallBack = new DownloadSupport.DownloadCallBack() {
        @Override
        public void download(FileCacheData fileCacheData, int percent, boolean isComplete) {
            KLog.d(TAG,"download:>filename="+fileCacheData.getFileName()+",percent="+percent+",current="+fileCacheData.getCurrent());
            Message message1 = handler.obtainMessage();
            message1.obj = fileCacheData;
            message1.arg1 = percent;
            handler.sendMessage(message1);
        }

        @Override
        public void error(Exception e) {
            KLog.d(TAG, "error:>errMeg=" + e.getMessage());
        }

        @Override
        public void downloadStatus(String requestTag, DownloadStatus downloadStatus) {
            KLog.d(TAG, "downloadStatus:>requestTag =" + requestTag + ",status=" + downloadStatus.name());
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FileCacheData fileCacheData = (FileCacheData) msg.obj;
            switch (fileCacheData.getRequestTag()) {
                case downloadUrl1:
                    pbProgressbar1.setProgress(msg.arg1);
                    tvResult1.setText("update1.zip >>> " + msg.arg1 + "%");
                    break;
                case downloadUrl2:
                    pbProgressbar2.setProgress(msg.arg1);
                    tvResult2.setText("update2.zip >>> " + msg.arg1 + "%");
                    break;
                case downloadUrl3:
                    pbProgressbar3.setProgress(msg.arg1);
                    tvResult3.setText("Settings.apk >>> " + msg.arg1 + "%");
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadSupport.cancel();
    }
}
