package com.face_chtj.base_iotutils;

import com.face_chtj.base_iotutils.download.DownloadErrorCode;
import com.face_chtj.base_iotutils.entity.FileData;
import com.face_chtj.base_iotutils.entity.DownloadStatus;
import com.face_chtj.base_iotutils.callback.IDownloadCallback;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 多任务下载管理工具类
 * 任务整个过程依据requestTag来标识，请用不同的标识区分
 * 注：该下载工具类没有使用Sqlite来进行保存进度，而是通过获取文件的长度来判断断点下载的位置
 * 该工具类会打印一些日志，若后期相对稳定后，将会去掉日志
 * 具体使用请参考README.md的描述进行
 * 多个任务只需要一个DownloadCallBack作为进度监听，并且依据requestTag来做区分即可
 * <p>
 * BufferedInputStream 8192解释
 * 我们调用缓冲流来读取数据，系统会先看一下缓冲区中有没有可用数据，有的话直接从缓冲区中复制数据给用户
 * 如果缓冲区中没有可用数据，则从真正的InputStream中读一次性读取8K的数据保存在缓冲区中，然后再从缓冲区中复制数据给用户
 * 如果用户接收数据的数组长度大于或等于缓冲区的长度，则系统就不会使用缓存区来保存数据了，而是直接从InputStream中读取数据保存到用户的数组中
 * <p>
 * 此工具类可满足多个场景需求
 * 若您在使用过程发现问题时可及时提出 随后将会在恰当的时间做更新
 */
public class DownloadUtils {

    private final Map<String, Call> callMap = new HashMap<String, Call>();
    private final OkHttpClient client;
    private int MAX_BUFF_SIZE = 2048;
    private final Map<String, Integer> statusMap = new HashMap<String, Integer>();
    private final Map<String, Integer> progressMap = new HashMap<String, Integer>();
    private final List<FileData> fileDatas = new ArrayList<FileData>();
    private final List<IDownloadCallback> iCallBack = new ArrayList<IDownloadCallback>();
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
        if (instance().statusMap.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : instance().statusMap.entrySet()) {
            if (entry.getValue() == DownloadStatus.STATUS_RUNNING) {
                return true;
            }
        }

        return false;
    }

    private DownloadUtils() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(originalResponse.body()).build();
            }
        };
        client = new OkHttpClient.Builder().addNetworkInterceptor(interceptor).build();
    }

    public static void setBuffSize(int maxBuffSize) {
        instance().MAX_BUFF_SIZE = maxBuffSize;
    }

    public static void addStartTask(final FileData fileData) {
        if (fileData == null) {
            return;
        }

        final String tag = fileData.getRequestTag();

        if (instance().progressMap.containsKey(tag) ||
                (instance().statusMap.containsKey(tag) && instance().statusMap.get(tag) == DownloadStatus.STATUS_RUNNING)) {
            for (int i = 0; i < instance().iCallBack.size(); i++) {
                instance().iCallBack.get(i).taskExist(fileData);
            }
            return;
        }

        instance().progressMap.put(tag, 0);
        instance().statusMap.put(tag, DownloadStatus.STATUS_RUNNING);

        for (int i = 0; i < instance().iCallBack.size(); i++) {
            instance().iCallBack.get(i).downloadStatus(fileData, DownloadStatus.STATUS_RUNNING);
        }

        long downloadedLength = new File(fileData.getFilePath()).length();

        Request request = new Request.Builder()
                .url(fileData.getUrl())
                .tag(tag)
                .header("RANGE", "bytes=" + downloadedLength + "-")
                .build();

        Call call = instance().client.newCall(request);
        instance().callMap.put(tag, call);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException throwable) {
                instance().callMap.remove(tag);
                instance().statusMap.remove(tag);
                instance().progressMap.remove(tag);
                if (instance().statusMap!=null&&instance().statusMap.get(fileData.getRequestTag())!=null){
                    int errorCode = DownloadErrorCode.UNKNOWN_ERROR;
                    if (throwable instanceof UnknownHostException) {
                        errorCode = DownloadErrorCode.UNKNOWN_HOST;
                    } else if (throwable instanceof SocketTimeoutException) {
                        errorCode = DownloadErrorCode.SOCKET_TIMEOUT;
                    } else if (throwable instanceof ConnectException) {
                        errorCode = DownloadErrorCode.CONNECT_EXCEPTION;
                    } else if (throwable instanceof SSLHandshakeException) {
                        errorCode = DownloadErrorCode.SSL_HANDSHAKE;
                    } else if (throwable instanceof FileNotFoundException) {
                        errorCode = DownloadErrorCode.FILE_NOT_FOUND;
                    } else if (throwable instanceof IOException) {
                        if (!NetUtils.reloadDnsPing()){
                            errorCode = DownloadErrorCode.UNKNOWN_HOST;
                        }else{
                            errorCode = DownloadErrorCode.IO_ERROR;
                        }
                    }
                    // 回调错误信息
                    for (int i = 0; i < instance().iCallBack.size(); i++) {
                        instance().iCallBack.get(i).error(fileData, throwable, errorCode);
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                save(response, fileData);
            }
        });
    }

    public static void pause() {
        for (Map.Entry<String, Integer> entry : instance().statusMap.entrySet()) {
            instance().statusMap.put(entry.getKey(), DownloadStatus.STATUS_PAUSE);
        }
    }

    public static void pause(String requestTag) {
        if (instance().statusMap.containsKey(requestTag)) {
            instance().statusMap.put(requestTag, DownloadStatus.STATUS_PAUSE);
        }
    }

    public static void removeTask(String requestTag) {
        Call call = instance().callMap.remove(requestTag);
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }

        instance().statusMap.remove(requestTag);
        instance().progressMap.remove(requestTag);

        Iterator<FileData> iterator = instance().fileDatas.iterator();
        while (iterator.hasNext()) {
            FileData data = iterator.next();
            if (requestTag.equals(data.getRequestTag())) {
                iterator.remove();
                break;
            }
        }

        FileData dummy = new FileData();
        dummy.setRequestTag(requestTag);
        for (int i = 0; i < instance().iCallBack.size(); i++) {
            instance().iCallBack.get(i).downloadStatus(dummy, DownloadStatus.STATUS_CANCELLED);
        }
    }

    private static void save(Response response, FileData fileData) {
        String tag = fileData.getRequestTag();
        ResponseBody body = response.body();
        InputStream in = body != null ? body.byteStream() : null;
        BufferedInputStream bis = new BufferedInputStream(in);
        RandomAccessFile raf = null;

        try {
            File file = new File(fileData.getFilePath());
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            raf = new RandomAccessFile(file, "rwd");
            long currentLength = raf.length();
            long totalLength = body != null ? body.contentLength() : 0;
            fileData.setTotal(currentLength + totalLength);

            if (currentLength >= fileData.getTotal()) {
                completeDownload(tag, fileData, 100);
                return;
            }

            raf.seek(currentLength);
            byte[] buffer = new byte[instance().MAX_BUFF_SIZE];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                raf.write(buffer, 0, len);

                if (instance().statusMap.get(tag) == DownloadStatus.STATUS_PAUSE) {
                    for (int i = 0; i < instance().iCallBack.size(); i++) {
                        instance().iCallBack.get(i).downloadStatus(fileData, DownloadStatus.STATUS_PAUSE);
                    }
                    return;
                }

                currentLength += len;
                fileData.setCurrent(currentLength);
                int percent = (int) (currentLength * 100 / fileData.getTotal());

                if (percent > instance().progressMap.get(tag)) {
                    instance().progressMap.put(tag, percent);
                    for (int i = 0; i < instance().iCallBack.size(); i++) {
                        instance().iCallBack.get(i).downloadProgress(fileData, percent);
                    }
                }

                if (currentLength >= fileData.getTotal()) {
                    completeDownload(tag, fileData, 100);
                    break;
                }
            }

            instance().fileDatas.add(fileData);
            instance().statusMap.remove(tag);
            instance().progressMap.remove(tag);
            instance().callMap.remove(tag);

            if (instance().statusMap.isEmpty()) {
                for (int i = 0; i < instance().iCallBack.size(); i++) {
                    instance().iCallBack.get(i).allDownloadComplete(new ArrayList<FileData>(instance().fileDatas));
                }
                instance().fileDatas.clear();
            }

        } catch (Throwable throwable) {
            if (instance().statusMap!=null&&instance().statusMap.get(fileData.getRequestTag())!=null){
                int errorCode = DownloadErrorCode.UNKNOWN_ERROR;
                if (throwable instanceof UnknownHostException) {
                    errorCode = DownloadErrorCode.UNKNOWN_HOST;
                } else if (throwable instanceof SocketTimeoutException) {
                    errorCode = DownloadErrorCode.SOCKET_TIMEOUT;
                } else if (throwable instanceof ConnectException) {
                    errorCode = DownloadErrorCode.CONNECT_EXCEPTION;
                } else if (throwable instanceof SSLHandshakeException) {
                    errorCode = DownloadErrorCode.SSL_HANDSHAKE;
                } else if (throwable instanceof FileNotFoundException) {
                    errorCode = DownloadErrorCode.FILE_NOT_FOUND;
                } else if (throwable instanceof IOException) {
                    if (!NetUtils.reloadDnsPing()){
                        errorCode = DownloadErrorCode.UNKNOWN_HOST;
                    }else{
                        errorCode = DownloadErrorCode.IO_ERROR;
                    }
                }
                // 回调错误信息
                for (int i = 0; i < instance().iCallBack.size(); i++) {
                    instance().iCallBack.get(i).error(fileData, throwable, errorCode);
                }
            }
        } finally {
            try {
                bis.close();
            } catch (Throwable ignored) {}
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable ignored) {}
            if (raf != null) {
                try {
                    raf.close();
                } catch (Throwable ignored) {}
            }
        }
    }

    private static void completeDownload(String tag, FileData fileData, int percent) {
        instance().statusMap.put(tag, DownloadStatus.STATUS_COMPLETE);
        instance().progressMap.put(tag, percent);
        for (int i = 0; i < instance().iCallBack.size(); i++) {
            instance().iCallBack.get(i).downloadProgress(fileData, percent);
            instance().iCallBack.get(i).downloadStatus(fileData, DownloadStatus.STATUS_COMPLETE);
        }
    }

    public static void cancelAll() {
        instance().client.dispatcher().cancelAll();
        for (Map.Entry<String, Call> entry : instance().callMap.entrySet()) {
            if (!entry.getValue().isCanceled()) {
                entry.getValue().cancel();
            }
        }
        instance().callMap.clear();
        instance().statusMap.clear();
        instance().progressMap.clear();
        instance().fileDatas.clear();
    }
}
