package com.ichtj.basetools.download;

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
import com.face_chtj.base_iotutils.entity.FileData;
import com.face_chtj.base_iotutils.NetUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Create on 2019/10/10
 * author chtj
 * desc 文件下载
 */
public class FileDownLoadAty extends BaseActivity{
    private static final String TAG = FileDownLoadAty.class.getSimpleName();
    ProgressBar pbProgressbar1, pbProgressbar2, pbProgressbar3, pbProgressbar4;
    TextView tvResult1, tvResult2, tvResult3, tvResult4;
    TextView tvTime1, tvTime2, tvTime3, tvTime4;
    private String saveRootPath = "/sdcard/test/download/";
    private String saveCachePath = "/sdcard/fileDownload.txt";
    //文件下载地址
    public static final String downloadUrl1 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/I78_PRJ/V1.01/rk3568_r-ota-eng.ach.zip";
    //替换的文件名称
    public String fileName1 = "update1.zip";

    //文件下载地址
    public static final String downloadUrl2 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/JY01_PRJ/V1.02/rk3288-ota-20231127111716.zip";
    //替换的文件名称
    public String fileName2 = "update2.zip";

    //文件下载地址
    public static final String downloadUrl3 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/KT01_PRJ/V1.03/rk3288-ota-20230901102957.zip";
    //替换的文件名称
    public String fileName3 = "update3.zip";

    //文件下载地址
    public static final String downloadUrl4 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/L11_V1.18/OTA-L11_FIPC5330_V1.18_20220509134410.zip";
    //替换的文件名称
    public String fileName4 = "update4.zip";

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
        DownloadUtils.registerCallback(downloadCallBack);
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
        tvResult3.setText("update3.zip >>> 0%");
        tvResult4.setText("update4.zip >>> 0%");
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
        ToastUtils.info("是否正在执行下载:" + DownloadUtils.isRunningTask());
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause1(View view) {
        if (fileData != null) {
            DownloadUtils.pause(fileData.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause2(View view) {
        if (fileData2 != null) {
            DownloadUtils.pause(fileData2.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause3(View view) {
        if (fileData3 != null) {
            DownloadUtils.pause(fileData3.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause4(View view) {
        if (fileData != null) {
            DownloadUtils.removeTask(fileData.getRequestTag());
        }
    }

    /**
     * 暂停下载的任务
     *
     * @param view
     */
    public void downTaskPause(View view) {
        DownloadUtils.pause();
    }

    /**
     * 关闭所有任务
     *
     * @param view
     */
    public void downloadStop(View view) {
        DownloadUtils.cancelAll();
    }

    FileData fileData = null;

    //文件下载
    public void downloadFile1(View view) {
        //开启任务下载----------------------这里可执行多个任务 重复执行即可---------
        fileData = new FileData();
        fileData.setUrl(downloadUrl1);
        fileData.setFileName(fileName1);
        fileData.setRequestTag(downloadUrl1);
        fileData.setFilePath(saveRootPath + fileName1);
        addDownloadTask(fileData);
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName1, false);
        tvTime1.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        //-----------------------------------------------------------
    }

    FileData fileData2 = null;

    //文件下载
    public void downloadFile2(View view) {
        fileData2 = new FileData();
        fileData2.setUrl(downloadUrl2);
        fileData2.setFileName(fileName2);
        fileData2.setRequestTag(downloadUrl2);
        fileData2.setFilePath(saveRootPath + fileName2);
        addDownloadTask(fileData2);
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName2, false);
        tvTime2.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        //-----------------------------------------------------------
    }

    FileData fileData3 = null;

    public void downloadFile3(View view) {
        fileData3 = new FileData();
        fileData3.setUrl(downloadUrl3);
        fileData3.setFileName(fileName3);
        fileData3.setRequestTag(downloadUrl3);
        fileData3.setFilePath(saveRootPath + fileName3);
        addDownloadTask(fileData3);
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName3, false);
        tvTime3.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        //-----------------------------------------------------------
    }

    FileData fileData4 = null;

    public void downloadFile4(View view) {
        fileData4 = new FileData();
        fileData4.setUrl(downloadUrl4);
        fileData4.setFileName(fileName4);
        fileData4.setRequestTag(downloadUrl4);
        fileData4.setFilePath(saveRootPath + fileName4);
        addDownloadTask(fileData4);
        FileUtils.writeFileData(saveCachePath, "_" + saveRootPath + fileName4, false);
        tvTime4.setText(TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss"));
        //-----------------------------------------------------------
    }

    public void addDownloadTask(FileData fileData) {
        DownloadUtils.addStartTask(fileData);
    }

    //下载进度  可根据设置的requestTag来区分属于哪个下载进度 fileCacheData.getRequestTag()
    IDownloadCallback downloadCallBack = new IDownloadCallback() {
        @Override
        public void downloadProgress(FileData fileData, int percent) {
            KLog.d(TAG, "download:>filename=" + fileData.getFileName() + ",percent=" + percent + ",current=" + fileData.getCurrent());
            Message message1 = handler.obtainMessage();
            message1.obj = fileData;
            message1.arg1 = percent;
            handler.sendMessage(message1);
        }

        @Override
        public void error(FileData fileData, Throwable e,int errCode) {
            KLog.d(TAG, "error:>fileName="+fileData.getFileName()+",err>>" + e.getMessage()+",errCode>>"+errCode);
//            DownloadUtils.cancelAll();
        }

        @Override
        public void taskExist(FileData fileData) {
            KLog.d(TAG, "taskExist:>fileName=" +fileData.getFileName());
            ToastUtils.warning("任务存在");
        }

        @Override
        public void allDownloadComplete(List<FileData> fileDataList) {
            for (int i = 0; i < fileDataList.size(); i++) {
                KLog.d(TAG, "allDownloadComplete:>requestTag=" + fileDataList.get(i).getRequestTag() + "" + fileDataList.get(i).getFileName() + "," + fileDataList.get(i).getCurrent() + "," + fileDataList.get(i).getTotal());
            }
        }

        @Override
        public void downloadStatus(FileData fileData, int downloadStatus) {
            KLog.d(TAG, "downloadStatus:>requestTag =" + fileData.getRequestTag() + ",status=" + downloadStatus);
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FileData fileData = (FileData) msg.obj;
            switch (fileData.getRequestTag()) {
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
                    tvResult3.setText("update3.zip >>> " + msg.arg1 + "%");
                    break;
                case downloadUrl4:
                    pbProgressbar4.setProgress(msg.arg1);
                    tvResult4.setText("update4.zip >>> " + msg.arg1 + "%");
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadUtils.cancelAll();
        DownloadUtils.unRegisterCallback(downloadCallBack);
    }
}
