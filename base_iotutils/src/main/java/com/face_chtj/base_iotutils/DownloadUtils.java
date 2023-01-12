package com.face_chtj.base_iotutils;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.entity.FileCacheData;
import com.face_chtj.base_iotutils.entity.DownloadStatus;
import com.face_chtj.base_iotutils.callback.IDownloadCallback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private OkHttpClient client;
    private int MAX_BUFF_SIZE = 2048;
    private Call call;
    //当前正在里的任务
    private static Map<String, Integer> currentTaskList = new HashMap<>();
    private List<FileCacheData> fileCacheDataList = new ArrayList<>();

    /**
     * 是否正在执行任务下载
     *
     * @return true| false
     */
    public boolean isRunDownloadTask() {
        if (currentTaskList.size() > 0) {
            //判断是否有暂停的任务 暂停的任务也相当于没有在执行任务下载
            int count = 0;
            for (Map.Entry<String, Integer> entry : currentTaskList.entrySet()) {
                if (currentTaskList.get(entry.getKey()) == DownloadStatus.STATUS_PAUSE) {
                    count++;
                    if (count == currentTaskList.size()) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 初始化一次即可
     */
    public DownloadUtils() {
        initData(MAX_BUFF_SIZE);
    }

    /**
     * 初始化一次即可
     */
    public DownloadUtils(int maxBuffSize) {
        initData(maxBuffSize);
    }

    private void initData(int maxBuffSize) {
        this.MAX_BUFF_SIZE = maxBuffSize;
        //在下载、暂停后的继续下载中可复用同一个client对象
        client = getProgressClient();
    }

    /**
     * 每次下载需要新建新的Call对象
     * new File(fileCacheData.getFilePath()).length() 获取文件断点位置 并以此为起点去下载，请留意是否支持断点下载
     */
    private Call newCall(FileCacheData fileCacheData) {
        long fileLength = new File(fileCacheData.getFilePath()).length();
        KLog.d("newCall: fileLength=" + fileLength);
        Request request = new Request.Builder()
                .url(fileCacheData.getUrl())
                .tag(fileCacheData.getRequestTag())
                .header("RANGE", "bytes=" + fileLength + "-")//断点续传要用到的，指示下载的区间
                .build();
        return client.newCall(request);
    }

    public OkHttpClient getProgressClient() {
        // 拦截器，用上ProgressResponseBody
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(originalResponse.body())
                        .build();
            }
        };

        return new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build();
    }

    /**
     * 相同的地址的requestTag的任务不会重复下载，会提示任务存在
     * 使用download会自动判断文件是否有下载过，如果已经下载完成，再次重新下载，会直接提示完成，如果需要重新下载，请调用{@link #cancel()}关闭任务
     */
    public void addStartTask(final FileCacheData fileCacheData, final IDownloadCallback downloadCallBack) {
        if (fileCacheData != null) {
            String requestTag = fileCacheData.getRequestTag();
            //防止任务重复下载
            if (currentTaskList != null && currentTaskList.size() > 0) {
                Integer status = currentTaskList.get(requestTag);
                if (status != null && status == DownloadStatus.STATUS_RUNNING) {
                    downloadCallBack.taskExist(fileCacheData);
                    return;
                }
            }
            //该集合中没有任务正在处理
            currentTaskList.put(requestTag, DownloadStatus.STATUS_RUNNING);
            downloadCallBack.downloadStatus(fileCacheData, currentTaskList.get(requestTag));
            //获取url对应的key
            call = newCall(fileCacheData);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    KLog.d("onFailure:>=" + e.getMessage());
                    downloadCallBack.error(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    save(response, fileCacheData, downloadCallBack);
                }
            });
        }
    }

    /**
     * 暂停所有任务
     */
    public void pause() {
        for (Map.Entry<String, Integer> entry : currentTaskList.entrySet()) {
            currentTaskList.put(entry.getKey(), DownloadStatus.STATUS_PAUSE);
        }
    }

    /**
     * 按tag暂停任务
     *
     * @param requestTag
     */
    public void pause(String requestTag) {
        if (currentTaskList != null && currentTaskList.size() > 0 && currentTaskList.containsKey(requestTag)) {
            currentTaskList.put(requestTag, DownloadStatus.STATUS_PAUSE);
        }
    }


    /**
     * 将文件写入到本地
     *
     * @param response
     * @param fileCacheData
     * @param downloadCallBack
     */
    private void save(Response response, FileCacheData fileCacheData, IDownloadCallback downloadCallBack) {
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(new File(fileCacheData.getFilePath()), "rwd");
            long currentFileLenght = randomAccessFile.length();
            long bodyContentLength = body.contentLength();
            fileCacheData.setTotal(bodyContentLength + currentFileLenght);
            KLog.d("save: currentFileLength=" + currentFileLenght + ",bodyContentLength=" + bodyContentLength + ",totalLength=" + fileCacheData.getTotal());
            //body.contentLength()存放了这次下载的文件的总长度 current得到之前下载过的文件长度
            if (currentFileLenght >= fileCacheData.getTotal()) {
                downloadCallBack.downloadProgress(fileCacheData, 100);
                currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.STATUS_COMPLETE);
                downloadCallBack.downloadStatus(fileCacheData, currentTaskList.get(fileCacheData.getRequestTag()));
                return;
            }
            //从文件的断点开始下载
            randomAccessFile.seek(currentFileLenght);
            byte[] buffer = new byte[MAX_BUFF_SIZE];
            int len;
            //每次读取最多不超过2*1024个字节
            while ((len = bis.read(buffer)) != -1) {
                //先写入到文件中
                randomAccessFile.write(buffer, 0, len);
                if (currentTaskList.get(fileCacheData.getRequestTag()) == DownloadStatus.STATUS_PAUSE) {
                    downloadCallBack.downloadStatus(fileCacheData, currentTaskList.get(fileCacheData.getRequestTag()));
                    return;
                }
                //记录当前进度
                currentFileLenght += len;
                fileCacheData.setCurrent(currentFileLenght);
                //计算已经下载的百分比
                int percent = (int) (fileCacheData.getCurrent() * 100 / fileCacheData.getTotal());
                boolean isComplete = currentFileLenght >= fileCacheData.getTotal();
                downloadCallBack.downloadProgress(fileCacheData, percent);
                if (isComplete) {
                    //防止(len = bis.read(buffer) ResponseBody读到其他任务的流
                    currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.STATUS_COMPLETE);
                    downloadCallBack.downloadStatus(fileCacheData, currentTaskList.get(fileCacheData.getRequestTag()));
                    break;
                }
            }
            //把已完成的任务添加到集合中去
            fileCacheDataList.add(fileCacheData);
            //删除当前的这个执行任务
            currentTaskList.remove(fileCacheData.getRequestTag());
            if (currentTaskList.size() <= 0) {
                //将完成的所有任务回调回去
                downloadCallBack.allDownloadComplete(fileCacheDataList);
                //回调之后进行清除操作
                fileCacheDataList.clear();
            }
        } catch (Throwable e) {
            downloadCallBack.error(e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Throwable e) {
                    KLog.e("errMeg:" + e.getMessage());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable e) {
                    KLog.e("errMeg:" + e.getMessage());
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Throwable e) {
                    KLog.e("errMeg:" + e.getMessage());
                }
            }
        }
    }

    /**
     * 关闭任务
     */
    public void cancel() {
        if (client != null) {
            client.dispatcher().cancelAll();
        }
        if (call != null) {
            call.cancel();
        }
        if (currentTaskList != null) {
            currentTaskList.clear();
        }
        if (fileCacheDataList != null) {
            currentTaskList.clear();
        }
    }
}