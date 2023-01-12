package com.wave_chtj.example.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.callback.IDownloadCallback;
import com.face_chtj.base_iotutils.DownloadUtils;
import com.face_chtj.base_iotutils.entity.FileCacheData;
import com.face_chtj.base_iotutils.NetUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Create on 2019/10/10
 * author chtj
 * desc 文件下载
 */
public class FileDownLoadAty extends BaseActivity {
    private static final String TAG = "DownLoadAty";
    ProgressBar pbProgressbar1, pbProgressbar2, pbProgressbar3, pbProgressbar4;
    TextView tvResult1, tvResult2, tvResult3, tvResult4;
    TextView tvTime1, tvTime2, tvTime3, tvTime4;
    private String saveRootPath = "/sdcard/";
    private String saveCachePath = "/sdcard/fileDownload.txt";
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

    //文件下载地址
    public static final String downloadUrl4 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/test_file/lock.BIN";
    //替换的文件名称
    public String fileName4 = "lock.BIN";


    DownloadUtils downloadUtils;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        tvResult1 = findViewById(R.id.tvResult1);
        tvResult2 = findViewById(R.id.tvResult2);
        tvResult3 = findViewById(R.id.tvResult3);
        tvResult4 = findViewById(R.id.tvResult4);
        pbProgressbar1 = findViewById(R.id.pbProgressbar1);
        pbProgressbar2 = findViewById(R.id.pbProgressbar2);
        pbProgressbar3 = findViewById(R.id.pbProgressbar3);
        pbProgressbar4 = findViewById(R.id.pbProgressbar4);
        tvTime1 = findViewById(R.id.tvTime1);
        tvTime2 = findViewById(R.id.tvTime2);
        tvTime3 = findViewById(R.id.tvTime3);
        tvTime4 = findViewById(R.id.tvTime4);
        //设置最大进度位100
        pbProgressbar1.setMax(100);
        pbProgressbar2.setMax(100);
        pbProgressbar3.setMax(100);
        pbProgressbar4.setMax(100);

        File file = new File(saveCachePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        downloadUtils = new DownloadUtils();
    }

    /**
     * 清理下载的文件
     *
     * @param view
     */
    public void clearFile(View view) {
        String[] readArry = FileUtils.readFileData(saveCachePath).split("_");
        if (readArry != null && readArry.length > 0) {
            for (int i = 0; i < readArry.length; i++) {
                FileUtils.delFile(readArry[i]);
            }
        }
        FileUtils.writeFileData(saveCachePath, "", true);
        pbProgressbar1.setProgress(0);
        pbProgressbar2.setProgress(0);
        pbProgressbar3.setProgress(0);
        pbProgressbar4.setProgress(0);
        tvResult1.setText("update1.zip >>> 0%");
        tvResult2.setText("update2.zip >>> 0%");
        tvResult3.setText("Settings.apk >>> 0%");
        tvResult4.setText("lock.BIN >>> 0%");
        tvTime1.setText("");
        tvTime2.setText("");
        tvTime3.setText("");
        tvTime4.setText("");
    }


    /**
     * 是否正在下载
     *
     * @param view
     */
    public void getdown_status(View view) {
        ToastUtils.info("是否正在执行下载:" + downloadUtils.isRunDownloadTask());
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause1(View view) {
        if (fileCacheData != null) {
            downloadUtils.pause(fileCacheData.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause2(View view) {
        if (fileCacheData2 != null) {
            downloadUtils.pause(fileCacheData2.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause3(View view) {
        if (fileCacheData3 != null) {
            downloadUtils.pause(fileCacheData3.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause4(View view) {
        if (fileCacheData4 != null) {
            downloadUtils.pause(fileCacheData4.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause(View view) {
        downloadUtils.pause();
    }

    /**
     * 关闭所有任务
     *
     * @param view
     */
    public void downloadStop(View view) {
        downloadUtils.cancel();
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
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName1, false);
        tvTime1.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
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
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName2, false);
        tvTime2.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        //-----------------------------------------------------------
    }

    FileCacheData fileCacheData3 = null;

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
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName3, false);
        tvTime3.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        //-----------------------------------------------------------
    }

    FileCacheData fileCacheData4 = null;

    //文件下载
    public void downloadFile4(View view) {
        if (NetUtils.getNetWorkType() == NetUtils.NETWORK_NO) {
            ToastUtils.error("当前无网络连接！");
            return;
        }
        fileCacheData4 = new FileCacheData();
        fileCacheData4.setUrl(downloadUrl4);
        fileCacheData4.setFileName(fileName4);
        fileCacheData4.setRequestTag(downloadUrl4);
        fileCacheData4.setFilePath(saveRootPath + fileName4);
        addDownloadTask(fileCacheData4);
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName4, false);
        tvTime4.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        //-----------------------------------------------------------
    }

    /**
     * 添加下载任务
     *
     * @param fileCacheData
     */
    public void addDownloadTask(FileCacheData fileCacheData) {
        downloadUtils.addStartTask(fileCacheData, downloadCallBack);
    }

    //下载进度  可根据设置的requestTag来区分属于哪个下载进度 fileCacheData.getRequestTag()
    IDownloadCallback downloadCallBack = new IDownloadCallback() {
        @Override
        public void downloadProgress(FileCacheData fileCacheData, int percent) {
            KLog.d(TAG, "download:>filename=" + fileCacheData.getFileName() + ",percent=" + percent + ",current=" + fileCacheData.getCurrent());
            Message message1 = handler.obtainMessage();
            message1.obj = fileCacheData;
            message1.arg1 = percent;
            handler.sendMessage(message1);
        }

        @Override
        public void error(Throwable e) {
            KLog.d(TAG, "error:>errMeg=" + e.getMessage());
            downloadUtils.cancel();
        }

        @Override
        public void taskExist(FileCacheData fileCacheData) {
            ToastUtils.warning("任务存在");
        }

        @Override
        public void allDownloadComplete(List<FileCacheData> fileCacheDataList) {
            for (int i = 0; i < fileCacheDataList.size(); i++) {
                KLog.d(TAG, "allDownloadComplete:>requestTag=" + fileCacheDataList.get(i).getRequestTag() + "" + fileCacheDataList.get(i).getFileName() + "," + fileCacheDataList.get(i).getCurrent() + "," + fileCacheDataList.get(i).getTotal());
            }
        }

        @Override
        public void downloadStatus(FileCacheData fileCacheData, int downloadStatus) {
            KLog.d(TAG, "downloadStatus:>requestTag =" + fileCacheData.getRequestTag() + ",status=" + downloadStatus);
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
                case downloadUrl4:
                    pbProgressbar4.setProgress(msg.arg1);
                    tvResult4.setText("lock.BIN >>> " + msg.arg1 + "%");
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadUtils.cancel();
    }
}
