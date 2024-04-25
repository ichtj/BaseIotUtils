package com.face_chtj.base_iotutils;

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
    private Call call;
    private final OkHttpClient client;
    private int MAX_BUFF_SIZE = 2048;
    private Map<String, Integer> currentTaskList=new HashMap<>();
    private List<FileCacheData> fileCacheDataList=new ArrayList<>();
    private List<IDownloadCallback> iDownloadCallback=new ArrayList<>();
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

    public static void registerCallback(IDownloadCallback downloadCallBack) {
        if (downloadCallBack!=null){
            if (!instance().iDownloadCallback.contains(downloadCallBack)) {
                instance().iDownloadCallback.add(downloadCallBack);
            }
        }
    }

    public static void unRegisterCallback(IDownloadCallback downloadCallBack) {
        if (downloadCallBack!=null){
            instance().iDownloadCallback.remove(downloadCallBack);
        }
    }

    /**
     * 是否正在执行任务下载
     *
     * @return true| false
     */
    public static boolean isRunDownloadTask() {
        if (instance().currentTaskList.size() > 0) {
            //判断是否有暂停的任务 暂停的任务也相当于没有在执行任务下载
            int count = 0;
            for (Map.Entry<String, Integer> entry : instance().currentTaskList.entrySet()) {
                if (instance().currentTaskList.get(entry.getKey()) == DownloadStatus.STATUS_PAUSE) {
                    count++;
                    if (count == instance().currentTaskList.size()) {
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
    private DownloadUtils() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(originalResponse.body())
                        .build();
            }
        };
        client= new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build();
    }

    public static void setBuffSize(int maxBuffSize) {
        instance().MAX_BUFF_SIZE = maxBuffSize;
    }

    /**
     * 相同的地址的requestTag的任务不会重复下载，会提示任务存在
     * 使用download会自动判断文件是否有下载过，如果已经下载完成，再次重新下载，会直接提示完成，如果需要重新下载，请调用{@link #cancelAll()}关闭任务
     */
    public static void addStartTask(final FileCacheData fileCacheData) {
        if (fileCacheData != null) {
            String requestTag = fileCacheData.getRequestTag();
            //防止任务重复下载
            if (instance().currentTaskList.size() > 0) {
                Integer status = instance().currentTaskList.get(requestTag);
                if (status != null && status == DownloadStatus.STATUS_RUNNING) {
                    for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                        instance().iDownloadCallback.get(i).taskExist(fileCacheData);
                    }
                    return;
                }
            }
            //该集合中没有任务正在处理
            instance().currentTaskList.put(requestTag, DownloadStatus.STATUS_RUNNING);
            for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                instance().iDownloadCallback.get(i).downloadStatus(fileCacheData, instance().currentTaskList.get(requestTag));
            }

            long fileLength = new File(fileCacheData.getFilePath()).length();
            Request request = new Request.Builder()
                    .url(fileCacheData.getUrl())
                    .tag(fileCacheData.getRequestTag())
                    .header("RANGE", "bytes=" + fileLength + "-")//断点续传要用到的，指示下载的区间
                    .build();
            instance().call= instance().client.newCall(request);

            instance().call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                        instance().iDownloadCallback.get(i).error(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    save(response, fileCacheData);
                }
            });
        }
    }

    /**
     * 暂停所有任务
     */
    public static void pause() {
        for (Map.Entry<String, Integer> entry : instance().currentTaskList.entrySet()) {
            instance().currentTaskList.put(entry.getKey(), DownloadStatus.STATUS_PAUSE);
        }
    }

    /**
     * 按tag暂停任务
     */
    public static void pause(String requestTag) {
        if (instance().currentTaskList.size() > 0 && instance().currentTaskList.containsKey(requestTag)) {
            instance().currentTaskList.put(requestTag, DownloadStatus.STATUS_PAUSE);
        }
    }


    /**
     * 将文件写入到本地
     */
    private static void save(Response response, FileCacheData fileCacheData) {
        ResponseBody body = response.body();
        InputStream in = body != null ? body.byteStream() : null;
        BufferedInputStream bis = new BufferedInputStream(in);
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            File file=new File(fileCacheData.getFilePath());
            //获取父目录
            File parent=new File(file.getParent());
            if (!parent.exists()){
                //如果目录不存在则创建相应的目录
                parent.mkdirs();
            }
            randomAccessFile = new RandomAccessFile(file, "rwd");
            long currentFileLenght = randomAccessFile.length();
            long bodyContentLength = body != null ? body.contentLength() : 0;
            fileCacheData.setTotal(bodyContentLength + currentFileLenght);
            if (currentFileLenght >= fileCacheData.getTotal()) {
                instance().currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.STATUS_COMPLETE);
                for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                    instance().iDownloadCallback.get(i).downloadProgress(fileCacheData, 100);
                    instance().iDownloadCallback.get(i).downloadStatus(fileCacheData, instance().currentTaskList.get(fileCacheData.getRequestTag()));
                }
                return;
            }
            //从文件的断点开始下载
            randomAccessFile.seek(currentFileLenght);
            byte[] buffer = new byte[instance().MAX_BUFF_SIZE];
            int len;
            //每次读取最多不超过2*1024个字节
            while ((len = bis.read(buffer)) != -1) {
                //先写入到文件中
                randomAccessFile.write(buffer, 0, len);
                if (instance().currentTaskList.get(fileCacheData.getRequestTag()) == DownloadStatus.STATUS_PAUSE) {
                    for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                        instance().iDownloadCallback.get(i).downloadStatus(fileCacheData, instance().currentTaskList.get(fileCacheData.getRequestTag()));
                    }
                    return;
                }
                //记录当前进度
                currentFileLenght += len;
                fileCacheData.setCurrent(currentFileLenght);
                //计算已经下载的百分比
                int percent = (int) (fileCacheData.getCurrent() * 100 / fileCacheData.getTotal());
                boolean isComplete = currentFileLenght >= fileCacheData.getTotal();
                for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                    instance().iDownloadCallback.get(i).downloadProgress(fileCacheData, percent);
                }
                if (isComplete) {
                    //防止(len = bis.read(buffer) ResponseBody读到其他任务的流
                    instance().currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.STATUS_COMPLETE);
                    for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                        instance().iDownloadCallback.get(i).downloadStatus(fileCacheData, instance().currentTaskList.get(fileCacheData.getRequestTag()));
                    }
                    break;
                }
            }
            //把已完成的任务添加到集合中去
            instance().fileCacheDataList.add(fileCacheData);
            //删除当前的这个执行任务
            instance().currentTaskList.remove(fileCacheData.getRequestTag());
            if (instance().currentTaskList.size() == 0) {
                //将完成的所有任务回调回去
                for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                    instance().iDownloadCallback.get(i).allDownloadComplete(instance().fileCacheDataList);
                }
                //回调之后进行清除操作
                instance().fileCacheDataList.clear();
            }
        } catch (Throwable throwable) {
            for (int i = 0; i < instance().iDownloadCallback.size(); i++) {
                instance().iDownloadCallback.get(i).error(throwable);
            }
        } finally {
            try {
                bis.close();
            } catch (Throwable e) {
            }
            try {
                in.close();
            } catch (Throwable e) {
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    /**
     * 关闭任务
     */
    public static void cancelAll() {
        if (instance().client != null) {
            instance().client.dispatcher().cancelAll();
        }
        if (instance().call != null) {
            instance().call.cancel();
        }
        instance().currentTaskList.clear();
        instance().currentTaskList.clear();
    }
}