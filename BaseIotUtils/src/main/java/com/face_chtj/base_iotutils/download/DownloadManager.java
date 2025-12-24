package com.face_chtj.base_iotutils.download;

import android.content.Context;
import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    private static final String TAG=DownloadManager.class.getSimpleName();
    private final OkHttpClient client;
    private final DownloadDatabaseHelper dbHelper;
    private final Map<String, Call> currentDownloads;
    private final DownloadCallback callback;
    private final ScheduledExecutorService progressExecutor;

    public DownloadManager(Context context, DownloadCallback callback) {
        this.client = new OkHttpClient();
        this.dbHelper = new DownloadDatabaseHelper(context);
        this.currentDownloads = new HashMap<>();
        this.callback = callback;
        this.progressExecutor = Executors.newScheduledThreadPool(1);
        restoreDownloads();
    }

    public void downloadFile(String url, String filePath) {
        File file = new File(filePath);
        File parent=new File(file.getParent());
        Log.d(TAG, "downloadFile: parent>>"+parent.getAbsolutePath());
        if (!parent.exists()){
            parent.mkdirs();
        }
        DownloadTask task = new DownloadTask(url, filePath, 0, false);
        dbHelper.addDownloadTask(task);
        startDownload(task);
    }

    private void startDownload(final DownloadTask task) {
        if (currentDownloads.containsKey(task.getUrl())) {
            Log.d(TAG,"Already in download queue: " + task.getUrl());
            return;
        }

        Request request = new Request.Builder().url(task.getUrl()).build();
        Call call = client.newCall(request);
        currentDownloads.put(task.getUrl(), call);
        callback.onDownloadStatusChanged(new DownloadStatus(task.getUrl(), DownloadStatus.Status.STARTED));

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"Download failed: " + task.getUrl());
                currentDownloads.remove(task.getUrl());
                callback.onDownloadStatusChanged(new DownloadStatus(task.getUrl(), DownloadStatus.Status.ERROR, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onFailure(call, new IOException("Unexpected code " + response));
                    return;
                }

                File file = new File(task.getFilePath());
                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(file)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    int totalBytesRead = 0;
                    final int contentLength = (int) response.body().contentLength();

                    // Schedule progress updates
                    final int finalTotalBytesRead = totalBytesRead;
                    progressExecutor.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                        int progress = (int) (((double) finalTotalBytesRead / contentLength) * 100);
                        callback.onDownloadProgress(task.getUrl(), progress);
                        }
                    }, 0, 1, TimeUnit.SECONDS);

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                    }

                    progressExecutor.shutdown();
                    callback.onDownloadStatusChanged(new DownloadStatus(task.getUrl(), DownloadStatus.Status.COMPLETED));
                } finally {
                    currentDownloads.remove(task.getUrl());
                    dbHelper.deleteDownloadTask(task.getUrl());
                }
            }
        });
    }

    private DownloadTask getDownloadTaskByUrl(String url) {
        List<DownloadTask> tasks = dbHelper.getAllDownloadTasks();
        for (DownloadTask task : tasks) {
            if (task.getUrl().equals(url)) {
                return task;
            }
        }
        return null;
    }

    public void pauseDownload(String url) {
        Call call = currentDownloads.get(url);
        if (call != null) {
            call.cancel();
            DownloadTask task = getDownloadTaskByUrl(url);
            if (task != null) {
                task.setPaused(true);
                dbHelper.addDownloadTask(task);
                callback.onDownloadStatusChanged(new DownloadStatus(url, DownloadStatus.Status.PAUSED));
            }
        }
    }

    public void resumeDownload(String url) {
        DownloadTask task = getDownloadTaskByUrl(url);
        if (task != null && task.isPaused()) {
            task.setPaused(false);
            dbHelper.addDownloadTask(task);
            startDownload(task);
            callback.onDownloadStatusChanged(new DownloadStatus(url, DownloadStatus.Status.RESUMED));
        }
    }

    public void deleteDownload(String url) {
        pauseDownload(url);
        dbHelper.deleteDownloadTask(url);
        currentDownloads.remove(url);
        DownloadTask task = getDownloadTaskByUrl(url);
        if (task != null) {
            new File(task.getFilePath()).delete();
        }
    }

    private void restoreDownloads() {
        List<DownloadTask> tasks = dbHelper.getAllDownloadTasks();
        for (DownloadTask task : tasks) {
            if (!task.isPaused()) {
                startDownload(task);
            }
        }
    }
}
