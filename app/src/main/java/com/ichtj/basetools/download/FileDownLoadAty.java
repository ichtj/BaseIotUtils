package com.ichtj.basetools.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.DownloadUtils;
import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.callback.IDownloadCallback;
import com.face_chtj.base_iotutils.entity.DownloadStatus;
import com.face_chtj.base_iotutils.entity.FileData;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * File download demo page.
 */
public class FileDownLoadAty extends BaseActivity {
    private static final String TAG = FileDownLoadAty.class.getSimpleName();

    public static final String downloadUrl1 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/OC07_PRJ/V1.04/rk3288-ota-20230816191256.zip";
    public static final String downloadUrl2 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/OC22_PRJ/V1.01/rk3288-ota-20241223113919.zip";
    public static final String downloadUrl3 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/OC22_PRJ/V1.02/rk3288-ota-20241226093105.zip";
    public static final String downloadUrl4 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/OC22_PRJ/V1.03/rk3288-ota-20241227094039.zip";

    public String fileName1 = "update1.zip";
    public String fileName2 = "update2.zip";
    public String fileName3 = "update3.zip";
    public String fileName4 = "update4.zip";

    private final String saveRootPath = "/sdcard/test/download/";
    private final String saveCachePath = "/sdcard/fileDownload.txt";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextView tvSummary;
    private TextView tvSavePath;
    private TextView[] tvResults;
    private TextView[] tvTimes;
    private ProgressBar[] progressBars;
    private final FileData[] fileDataList = new FileData[4];
    private final String[] startTimes = new String[4];
    private final int[] percents = new int[]{0, 0, 0, 0};
    private final int[] statuses = new int[]{0, 0, 0, 0};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        tvSummary = findViewById(R.id.tvSummary);
        tvSavePath = findViewById(R.id.tvSavePath);
        tvSavePath.setText("保存目录：" + saveRootPath);

        tvResults = new TextView[]{
                findViewById(R.id.tvResult1),
                findViewById(R.id.tvResult2),
                findViewById(R.id.tvResult3),
                findViewById(R.id.tvResult4)
        };
        tvTimes = new TextView[]{
                findViewById(R.id.tvTime1),
                findViewById(R.id.tvTime2),
                findViewById(R.id.tvTime3),
                findViewById(R.id.tvTime4)
        };
        progressBars = new ProgressBar[]{
                findViewById(R.id.pbProgressbar1),
                findViewById(R.id.pbProgressbar2),
                findViewById(R.id.pbProgressbar3),
                findViewById(R.id.pbProgressbar4)
        };

        for (ProgressBar progressBar : progressBars) {
            progressBar.setMax(100);
        }
        resetAllViews();
        ensureCacheFile();
        DownloadUtils.registerCallback(downloadCallBack);
    }

    public void clearFile(View view) {
        DownloadUtils.cancelAll();
        String cache = FileUtils.readFileData(saveCachePath);
        String[] paths = cache == null ? new String[0] : cache.split("_");
        for (String path : paths) {
            if (path != null && path.length() > 0) {
                FileUtils.delFile(path);
            }
        }
        FileUtils.writeFileData(saveCachePath, "", true);
        for (int i = 0; i < fileDataList.length; i++) {
            fileDataList[i] = null;
            startTimes[i] = null;
            percents[i] = 0;
            statuses[i] = 0;
            updateTaskView(i, getFileName(i), 0, 0, 0, "未开始");
        }
        updateSummary();
        ToastUtils.info("已清理下载任务和文件");
    }

    public void getdown_status(View view) {
        updateSummary();
        ToastUtils.info("是否正在执行下载：" + DownloadUtils.isRunningTask());
    }

    public void resumeAllTask(View view) {
        List<FileData> tasks = new ArrayList<>();
        for (int i = 0; i < fileDataList.length; i++) {
            if (fileDataList[i] == null) {
                fileDataList[i] = createFileData(i);
            }
            tasks.add(fileDataList[i]);
            if (startTimes[i] == null) {
                startTimes[i] = TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss");
            }
            statuses[i] = DownloadStatus.STATUS_RUNNING;
            long localLength = new File(fileDataList[i].getFilePath()).length();
            updateTaskView(i, fileDataList[i].getFileName(), percents[i], localLength, fileDataList[i].getTotal(), "下载中");
        }
        DownloadUtils.resumeAll(tasks);
        updateSummary();
    }

    public void downTaskPause1(View view) {
        pauseTask(0);
    }

    public void downTaskPause2(View view) {
        pauseTask(1);
    }

    public void downTaskPause3(View view) {
        pauseTask(2);
    }

    public void downTaskPause4(View view) {
        pauseTask(3);
    }

    public void cancelTask1(View view) {
        cancelTask(0);
    }

    public void cancelTask2(View view) {
        cancelTask(1);
    }

    public void cancelTask3(View view) {
        cancelTask(2);
    }

    public void cancelTask4(View view) {
        cancelTask(3);
    }

    public void downTaskPause(View view) {
        DownloadUtils.pause();
        for (int i = 0; i < fileDataList.length; i++) {
            if (fileDataList[i] != null && statuses[i] != DownloadStatus.STATUS_COMPLETE
                    && statuses[i] != DownloadStatus.STATUS_CANCELLED) {
                statuses[i] = DownloadStatus.STATUS_PAUSE;
                updateTaskView(i, getFileName(i), percents[i], fileDataList[i].getCurrent(), fileDataList[i].getTotal(), "已暂停");
            }
        }
        updateSummary();
    }

    public void downloadStop(View view) {
        DownloadUtils.cancelAll();
        for (int i = 0; i < fileDataList.length; i++) {
            statuses[i] = DownloadStatus.STATUS_CANCELLED;
            updateTaskView(i, getFileName(i), percents[i], 0, 0, "已取消");
        }
        updateSummary();
    }

    public void downloadFile1(View view) {
        startTask(0);
    }

    public void downloadFile2(View view) {
        startTask(1);
    }

    public void downloadFile3(View view) {
        startTask(2);
    }

    public void downloadFile4(View view) {
        startTask(3);
    }

    public void addDownloadTask(FileData fileData) {
        DownloadUtils.addStartTask(fileData);
    }

    private void startTask(int index) {
        FileData fileData = createFileData(index);
        fileDataList[index] = fileData;
        startTimes[index] = TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss");
        statuses[index] = DownloadStatus.STATUS_RUNNING;
        addDownloadTask(fileData);
        FileUtils.writeFileData(saveCachePath, "_" + fileData.getFilePath(), false);
        long localLength = new File(fileData.getFilePath()).length();
        updateTaskView(index, fileData.getFileName(), percents[index], localLength, 0, "下载中");
        updateSummary();
    }

    private void pauseTask(int index) {
        FileData fileData = fileDataList[index];
        if (fileData == null) {
            ToastUtils.warning(getFileName(index) + " 尚未创建任务");
            return;
        }
        DownloadUtils.pause(fileData.getRequestTag());
        statuses[index] = DownloadStatus.STATUS_PAUSE;
        updateTaskView(index, fileData.getFileName(), percents[index], fileData.getCurrent(), fileData.getTotal(), "已暂停");
        updateSummary();
    }

    private void cancelTask(int index) {
        FileData fileData = fileDataList[index];
        if (fileData == null) {
            ToastUtils.warning(getFileName(index) + " 尚未创建任务");
            return;
        }
        DownloadUtils.removeTask(fileData.getRequestTag());
        statuses[index] = DownloadStatus.STATUS_CANCELLED;
        updateTaskView(index, fileData.getFileName(), percents[index], fileData.getCurrent(), fileData.getTotal(), "已取消");
        updateSummary();
    }

    private FileData createFileData(int index) {
        FileData fileData = new FileData();
        fileData.setUrl(getUrl(index));
        fileData.setFileName(getFileName(index));
        fileData.setRequestTag(getUrl(index));
        fileData.setFilePath(saveRootPath + getFileName(index));
        return fileData;
    }

    private final IDownloadCallback downloadCallBack = new IDownloadCallback() {
        @Override
        public void downloadProgress(final FileData fileData, final int percent) {
            postUi(new Runnable() {
                @Override
                public void run() {
                    int index = findIndex(fileData);
                    if (index < 0) {
                        return;
                    }
                    if (statuses[index] == DownloadStatus.STATUS_PAUSE
                            || statuses[index] == DownloadStatus.STATUS_CANCELLED
                            || statuses[index] == DownloadStatus.STATUS_COMPLETE) {
                        return;
                    }
                    percents[index] = percent;
                    statuses[index] = DownloadStatus.STATUS_RUNNING;
                    updateTaskView(index, fileData.getFileName(), percent, fileData.getCurrent(), fileData.getTotal(), "下载中");
                    updateSummary();
                }
            });
        }

        @Override
        public void downloadStatus(final FileData fileData, final int downloadStatus) {
            postUi(new Runnable() {
                @Override
                public void run() {
                    int index = findIndex(fileData);
                    if (index < 0) {
                        return;
                    }
                    if (downloadStatus == DownloadStatus.STATUS_RUNNING
                            && (statuses[index] == DownloadStatus.STATUS_PAUSE
                            || statuses[index] == DownloadStatus.STATUS_CANCELLED
                            || statuses[index] == DownloadStatus.STATUS_COMPLETE)) {
                        return;
                    }
                    statuses[index] = downloadStatus;
                    String statusText = getStatusText(downloadStatus);
                    if (downloadStatus == DownloadStatus.STATUS_COMPLETE) {
                        percents[index] = 100;
                    }
                    FileData localData = fileDataList[index] != null ? fileDataList[index] : fileData;
                    updateTaskView(index, getFileName(index), percents[index], localData.getCurrent(), localData.getTotal(), statusText);
                    updateSummary();
                }
            });
        }

        @Override
        public void allDownloadComplete(final List<FileData> fileDataList) {
            postUi(new Runnable() {
                @Override
                public void run() {
                    KLog.d(TAG, "allDownloadComplete: " + fileDataList.size());
                    updateSummary();
                    ToastUtils.success("全部下载完成：" + fileDataList.size() + " 个文件");
                }
            });
        }

        @Override
        public void error(final FileData fileData, final Throwable e, final int errCode) {
            postUi(new Runnable() {
                @Override
                public void run() {
                    int index = findIndex(fileData);
                    if (index >= 0) {
                        statuses[index] = -2;
                        updateTaskView(index, getFileName(index), percents[index],
                                fileData == null ? 0 : fileData.getCurrent(),
                                fileData == null ? 0 : fileData.getTotal(),
                                "错误：" + errCode);
                    }
                    KLog.d(TAG, "download error, errCode=" + errCode + ", msg=" + (e == null ? "" : e.getMessage()));
                    updateSummary();
                    ToastUtils.error("下载失败：" + errCode);
                }
            });
        }

        @Override
        public void taskExist(final FileData fileData) {
            postUi(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.warning("任务已存在：" + (fileData == null ? "" : fileData.getFileName()));
                }
            });
        }
    };

    private void updateTaskView(int index, String fileName, int percent, long current, long total, String statusText) {
        progressBars[index].setProgress(percent);
        tvResults[index].setText(fileName + "    " + percent + "%");
        StringBuilder builder = new StringBuilder();
        builder.append("状态：").append(statusText);
        builder.append(" | 已下载：").append(formatBytes(current));
        if (total > 0) {
            builder.append(" / ").append(formatBytes(total));
        }
        if (startTimes[index] != null) {
            builder.append(" | 开始：").append(startTimes[index]);
        }
        tvTimes[index].setText(builder.toString());
    }

    private void updateSummary() {
        int running = 0;
        int paused = 0;
        int completed = 0;
        int cancelled = 0;
        for (int status : statuses) {
            if (status == DownloadStatus.STATUS_RUNNING) {
                running++;
            } else if (status == DownloadStatus.STATUS_PAUSE) {
                paused++;
            } else if (status == DownloadStatus.STATUS_COMPLETE) {
                completed++;
            } else if (status == DownloadStatus.STATUS_CANCELLED) {
                cancelled++;
            }
        }
        tvSummary.setText("下载概览：运行 " + running + " | 暂停 " + paused + " | 完成 " + completed + " | 取消 " + cancelled);
    }

    private void resetAllViews() {
        for (int i = 0; i < progressBars.length; i++) {
            updateTaskView(i, getFileName(i), 0, 0, 0, "未开始");
        }
        updateSummary();
    }

    private void ensureCacheFile() {
        File file = new File(saveCachePath);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                KLog.d(TAG, "create cache file failed: " + e.getMessage());
            }
        }
    }

    private int findIndex(FileData fileData) {
        if (fileData == null || fileData.getRequestTag() == null) {
            return -1;
        }
        String tag = fileData.getRequestTag();
        for (int i = 0; i < 4; i++) {
            if (tag.equals(getUrl(i))) {
                return i;
            }
        }
        return -1;
    }

    private String getUrl(int index) {
        switch (index) {
            case 0:
                return downloadUrl1;
            case 1:
                return downloadUrl2;
            case 2:
                return downloadUrl3;
            case 3:
                return downloadUrl4;
            default:
                return "";
        }
    }

    private String getFileName(int index) {
        switch (index) {
            case 0:
                return fileName1;
            case 1:
                return fileName2;
            case 2:
                return fileName3;
            case 3:
                return fileName4;
            default:
                return "";
        }
    }

    private String getStatusText(int status) {
        if (status == DownloadStatus.STATUS_RUNNING) {
            return "下载中";
        } else if (status == DownloadStatus.STATUS_PAUSE) {
            return "已暂停";
        } else if (status == DownloadStatus.STATUS_COMPLETE) {
            return "已完成";
        } else if (status == DownloadStatus.STATUS_CANCELLED) {
            return "已取消";
        }
        return "未开始";
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        float kb = bytes / 1024f;
        if (kb < 1024f) {
            return String.format(Locale.US, "%.1f KB", kb);
        }
        float mb = kb / 1024f;
        if (mb < 1024f) {
            return String.format(Locale.US, "%.1f MB", mb);
        }
        return String.format(Locale.US, "%.2f GB", mb / 1024f);
    }

    private void postUi(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadUtils.cancelAll();
        DownloadUtils.unRegisterCallback(downloadCallBack);
        mainHandler.removeCallbacksAndMessages(null);
    }
}
