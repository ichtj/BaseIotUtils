package com.face_chtj.base_iotutils;

import android.os.Handler;
import android.os.Looper;

import com.face_chtj.base_iotutils.callback.IDownloadCallback;
import com.face_chtj.base_iotutils.download.DownloadErrorCode;
import com.face_chtj.base_iotutils.entity.DownloadStatus;
import com.face_chtj.base_iotutils.entity.FileData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 多任务下载管理工具类。
 * 通过 requestTag 区分任务，使用目标文件长度实现断点续传。
 */
public class DownloadUtils {

    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    private final Map<String, Call> callMap = new ConcurrentHashMap<String, Call>();
    private final Map<String, Integer> statusMap = new ConcurrentHashMap<String, Integer>();
    private final Map<String, Integer> progressMap = new ConcurrentHashMap<String, Integer>();
    private final List<FileData> fileDatas = new CopyOnWriteArrayList<FileData>();
    private final List<IDownloadCallback> iCallBack = new CopyOnWriteArrayList<IDownloadCallback>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient client;

    private volatile int maxBuffSize = DEFAULT_BUFFER_SIZE;
    private static volatile DownloadUtils sInstance;

    private static DownloadUtils instance() {
        if (sInstance == null) {
            synchronized (DownloadUtils.class) {
                if (sInstance == null) {
                    sInstance = new DownloadUtils();
                }
            }
        }
        return sInstance;
    }

    private DownloadUtils() {
        client = new OkHttpClient.Builder().build();
    }

    public static void registerCallback(IDownloadCallback callback) {
        if (callback != null && !instance().iCallBack.contains(callback)) {
            instance().iCallBack.add(callback);
        }
    }

    public static void unRegisterCallback(IDownloadCallback callback) {
        if (callback != null) {
            instance().iCallBack.remove(callback);
        }
    }

    public static void unAllRegisterCallback() {
        instance().iCallBack.clear();
    }

    public static boolean isRunningTask() {
        for (Map.Entry<String, Integer> entry : instance().statusMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue() == DownloadStatus.STATUS_RUNNING) {
                return true;
            }
        }
        return false;
    }

    public static void setBuffSize(int maxBuffSize) {
        if (maxBuffSize > 0) {
            instance().maxBuffSize = maxBuffSize;
        }
    }

    public static void addStartTask(final FileData fileData) {
        if (!prepareFileData(fileData)) {
            dispatchError(fileData, new IllegalArgumentException("fileData url/filePath/requestTag is invalid"),
                    DownloadErrorCode.UNKNOWN_ERROR);
            return;
        }

        final String tag = fileData.getRequestTag();
        Integer status = instance().statusMap.get(tag);
        if (status != null && status == DownloadStatus.STATUS_RUNNING) {
            dispatchTaskExist(fileData);
            return;
        }

        instance().progressMap.put(tag, 0);
        instance().statusMap.put(tag, DownloadStatus.STATUS_RUNNING);
        dispatchStatus(fileData, DownloadStatus.STATUS_RUNNING);

        File file = new File(fileData.getFilePath());
        long downloadedLength = file.exists() ? file.length() : 0;

        Request.Builder requestBuilder = new Request.Builder()
                .url(fileData.getUrl())
                .tag(tag);
        if (downloadedLength > 0) {
            requestBuilder.header("Range", "bytes=" + downloadedLength + "-");
        }

        Call call = instance().client.newCall(requestBuilder.build());
        instance().callMap.put(tag, call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException throwable) {
                if (isPausedOrCancelled(tag, call)) {
                    return;
                }
                cleanupTask(tag, true);
                dispatchError(fileData, throwable, getErrorCode(throwable));
                dispatchAllCompleteIfIdle();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    save(response, fileData);
                } catch (Throwable throwable) {
                    if (!isPausedOrCancelled(tag, call)) {
                        cleanupTask(tag, true);
                        dispatchError(fileData, throwable, getErrorCode(throwable));
                        dispatchAllCompleteIfIdle();
                    }
                }
            }
        });
    }

    public static void pause() {
        for (String tag : new ArrayList<String>(instance().statusMap.keySet())) {
            pause(tag);
        }
    }

    public static void pause(String requestTag) {
        if (isEmpty(requestTag)) {
            return;
        }
        Integer status = instance().statusMap.get(requestTag);
        if (status == null || status != DownloadStatus.STATUS_RUNNING) {
            return;
        }
        instance().statusMap.put(requestTag, DownloadStatus.STATUS_PAUSE);
        instance().progressMap.remove(requestTag);
        Call call = instance().callMap.remove(requestTag);
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        FileData fileData = new FileData();
        fileData.setRequestTag(requestTag);
        dispatchStatus(fileData, DownloadStatus.STATUS_PAUSE);
    }

    public static void resumeAll(List<FileData> fileDataList) {
        if (fileDataList == null || fileDataList.isEmpty()) {
            return;
        }
        for (FileData fileData : fileDataList) {
            resume(fileData);
        }
    }

    public static void resume(FileData fileData) {
        if (!prepareFileData(fileData)) {
            dispatchError(fileData, new IllegalArgumentException("fileData url/filePath/requestTag is invalid"),
                    DownloadErrorCode.UNKNOWN_ERROR);
            return;
        }
        addStartTask(fileData);
    }

    public static void removeTask(String requestTag) {
        if (isEmpty(requestTag)) {
            return;
        }

        instance().statusMap.put(requestTag, DownloadStatus.STATUS_CANCELLED);
        Call call = instance().callMap.remove(requestTag);
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }

        cleanupTask(requestTag, true);
        FileData dummy = new FileData();
        dummy.setRequestTag(requestTag);
        dispatchStatus(dummy, DownloadStatus.STATUS_CANCELLED);
        dispatchAllCompleteIfIdle();
    }

    public static void cancelAll() {
        instance().client.dispatcher().cancelAll();
        for (Map.Entry<String, Call> entry : instance().callMap.entrySet()) {
            Call call = entry.getValue();
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
        }
        instance().callMap.clear();
        instance().statusMap.clear();
        instance().progressMap.clear();
        instance().fileDatas.clear();
    }

    private static void save(Response response, FileData fileData) throws IOException {
        String tag = fileData.getRequestTag();
        ResponseBody body = response.body();
        try {
            if (response.code() == 416) {
                File file = new File(fileData.getFilePath());
                if (file.exists() && file.length() > 0) {
                    fileData.setCurrent(file.length());
                    fileData.setTotal(file.length());
                    completeDownload(tag, fileData);
                    dispatchAllCompleteIfIdle();
                    return;
                }
                throw new DownloadHttpException(response.code());
            }
            if (body == null) {
                throw new IOException("response body is null");
            }
            if (!response.isSuccessful() && response.code() != 206) {
                throw new DownloadHttpException(response.code());
            }

            File file = new File(fileData.getFilePath());
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("create parent dir failed: " + parent.getAbsolutePath());
            }

            long currentLength = file.exists() ? file.length() : 0;
            if (currentLength > 0 && response.code() == 200) {
                // Server ignored Range. Restart to avoid appending duplicated bytes.
                currentLength = 0;
            }

            long contentLength = body.contentLength();
            long totalLength = contentLength > 0 ? currentLength + contentLength : -1;
            if (totalLength > 0) {
                fileData.setTotal(totalLength);
            }

            InputStream inputStream = null;
            BufferedInputStream bis = null;
            RandomAccessFile raf = null;
            boolean completed = false;
            inputStream = body.byteStream();
            try {
                bis = new BufferedInputStream(inputStream);
                raf = new RandomAccessFile(file, "rwd");
                if (currentLength == 0) {
                    raf.setLength(0);
                }
                raf.seek(currentLength);

                byte[] buffer = new byte[instance().maxBuffSize];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    Integer status = instance().statusMap.get(tag);
                    if (status == null || status == DownloadStatus.STATUS_CANCELLED) {
                        return;
                    }
                    if (status == DownloadStatus.STATUS_PAUSE) {
                        return;
                    }

                    raf.write(buffer, 0, len);
                    currentLength += len;
                    fileData.setCurrent(currentLength);

                    if (totalLength > 0) {
                        int percent = (int) (currentLength * 100 / totalLength);
                        Integer lastPercent = instance().progressMap.get(tag);
                        if (lastPercent == null || percent > lastPercent) {
                            instance().progressMap.put(tag, percent);
                            dispatchProgress(fileData, percent);
                        }
                        if (currentLength >= totalLength) {
                            completed = true;
                            break;
                        }
                    }
                }
                if (totalLength <= 0) {
                    completed = true;
                }
            } finally {
                closeQuietly(raf);
                closeQuietly(bis);
                closeQuietly(inputStream);
            }

            if (completed) {
                completeDownload(tag, fileData);
                dispatchAllCompleteIfIdle();
            }
        } finally {
            response.close();
        }
    }

    private static void completeDownload(String tag, FileData fileData) {
        instance().fileDatas.add(fileData);
        cleanupTask(tag, true);
        dispatchProgress(fileData, 100);
        dispatchStatus(fileData, DownloadStatus.STATUS_COMPLETE);
    }

    private static boolean prepareFileData(FileData fileData) {
        if (fileData == null || isEmpty(fileData.getUrl()) || isEmpty(fileData.getFilePath())) {
            return false;
        }
        if (isEmpty(fileData.getRequestTag())) {
            fileData.setRequestTag(fileData.getUrl());
        }
        return true;
    }

    private static void cleanupTask(String tag, boolean removeStatus) {
        instance().callMap.remove(tag);
        instance().progressMap.remove(tag);
        if (removeStatus) {
            instance().statusMap.remove(tag);
        }
    }

    private static boolean isPausedOrCancelled(String tag, Call call) {
        Integer status = instance().statusMap.get(tag);
        return (status != null && (status == DownloadStatus.STATUS_PAUSE || status == DownloadStatus.STATUS_CANCELLED))
                || (call != null && call.isCanceled());
    }

    private static void dispatchAllCompleteIfIdle() {
        if (!instance().statusMap.isEmpty()) {
            return;
        }
        final List<FileData> completedFiles = new ArrayList<FileData>(instance().fileDatas);
        if (completedFiles.isEmpty()) {
            return;
        }
        instance().fileDatas.clear();
        postToMain(new Runnable() {
            @Override
            public void run() {
                for (IDownloadCallback callback : instance().iCallBack) {
                    try {
                        callback.allDownloadComplete(completedFiles);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private static void dispatchProgress(final FileData fileData, final int percent) {
        postToMain(new Runnable() {
            @Override
            public void run() {
                for (IDownloadCallback callback : instance().iCallBack) {
                    try {
                        callback.downloadProgress(fileData, percent);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private static void dispatchStatus(final FileData fileData, final int status) {
        postToMain(new Runnable() {
            @Override
            public void run() {
                for (IDownloadCallback callback : instance().iCallBack) {
                    try {
                        callback.downloadStatus(fileData, status);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private static void dispatchTaskExist(final FileData fileData) {
        postToMain(new Runnable() {
            @Override
            public void run() {
                for (IDownloadCallback callback : instance().iCallBack) {
                    try {
                        callback.taskExist(fileData);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private static void dispatchError(final FileData fileData, final Throwable throwable, final int errorCode) {
        postToMain(new Runnable() {
            @Override
            public void run() {
                for (IDownloadCallback callback : instance().iCallBack) {
                    try {
                        callback.error(fileData, throwable, errorCode);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private static void postToMain(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            instance().mainHandler.post(runnable);
        }
    }

    private static int getErrorCode(Throwable throwable) {
        if (throwable instanceof UnknownHostException) {
            return DownloadErrorCode.UNKNOWN_HOST;
        } else if (throwable instanceof SocketTimeoutException) {
            return DownloadErrorCode.SOCKET_TIMEOUT;
        } else if (throwable instanceof ConnectException) {
            return DownloadErrorCode.CONNECT_EXCEPTION;
        } else if (throwable instanceof SSLHandshakeException) {
            return DownloadErrorCode.SSL_HANDSHAKE;
        } else if (throwable instanceof FileNotFoundException) {
            return DownloadErrorCode.FILE_NOT_FOUND;
        } else if (throwable instanceof DownloadHttpException) {
            return DownloadErrorCode.HTTP_ERROR;
        } else if (throwable instanceof IOException) {
            return DownloadErrorCode.IO_ERROR;
        }
        return DownloadErrorCode.UNKNOWN_ERROR;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private static class DownloadHttpException extends IOException {
        DownloadHttpException(int code) {
            super("http error: " + code);
        }
    }
}
