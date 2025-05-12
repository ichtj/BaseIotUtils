package com.face_chtj.base_iotutils;

import com.face_chtj.base_iotutils.entity.FileData;
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
    private Map<String, Integer> statusMap = new HashMap<>();
    private Map<String, Integer> progressMap = new HashMap<>();
    private List<FileData> fileDatas = new ArrayList<>();
    private List<IDownloadCallback> iCallBack = new ArrayList<>();
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
        if (downloadCallBack != null) {
            if (!instance().iCallBack.contains(downloadCallBack)) {
                instance().iCallBack.add(downloadCallBack);
            }
        }
    }

    public static void unRegisterCallback(IDownloadCallback downloadCallBack) {
        if (downloadCallBack != null) {
            instance().iCallBack.remove(downloadCallBack);
        }
    }

    public static void unAllRegisterCallback(IDownloadCallback downloadCallBack) {
        if (instance().iCallBack.size()>0) {
            for (int i = 0; i < instance().iCallBack.size(); i++) {
                instance().iCallBack.remove(instance().iCallBack.get(i));
            }
        }
    }

    /**
     * 是否正在执行任务下载
     *
     * @return true| false
     */
    public static boolean isRunningTask() {
        if (instance().statusMap.size() > 0) {
            //判断是否有暂停的任务 暂停的任务也相当于没有在执行任务下载
            int count = 0;
            for (Map.Entry<String, Integer> entry : instance().statusMap.entrySet()) {
                if (instance().statusMap.get(entry.getKey()) == DownloadStatus.STATUS_PAUSE) {
                    count++;
                    if (count == instance().statusMap.size()) {
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
        client = new OkHttpClient.Builder()
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
    public static void addStartTask(final FileData fileData) {
        if (fileData != null) {
            String requestTag = fileData.getRequestTag();
            //防止任务重复下载
            if (instance().progressMap.containsKey(requestTag) ||
                    instance().statusMap.containsKey(requestTag) &&
                            instance().statusMap.get(requestTag) == DownloadStatus.STATUS_RUNNING) {
                for (int i = 0; i < instance().iCallBack.size(); i++) {
                    instance().iCallBack.get(i).taskExist(fileData);
                }
            } else {
                //该集合中没有任务正在处理
                instance().progressMap.put(requestTag, 0);
                instance().statusMap.put(requestTag, DownloadStatus.STATUS_RUNNING);
                for (int i = 0; i < instance().iCallBack.size(); i++) {
                    instance().iCallBack.get(i).downloadStatus(fileData, instance().statusMap.get(requestTag));
                }

                long fileLength = new File(fileData.getFilePath()).length();
                Request request = new Request.Builder()
                        .url(fileData.getUrl())
                        .tag(fileData.getRequestTag())
                        .header("RANGE", "bytes=" + fileLength + "-")//断点续传要用到的，指示下载的区间
                        .build();
                instance().call = instance().client.newCall(request);

                instance().call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        for (int i = 0; i < instance().iCallBack.size(); i++) {
                            instance().iCallBack.get(i).error(fileData,e);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        save(response, fileData);
                    }
                });
            }
        }
    }

    /**
     * 暂停所有任务
     */
    public static void pause() {
        for (Map.Entry<String, Integer> entry : instance().statusMap.entrySet()) {
            instance().statusMap.put(entry.getKey(), DownloadStatus.STATUS_PAUSE);
        }
    }

    /**
     * 按tag暂停任务
     */
    public static void pause(String requestTag) {
        if (instance().statusMap.size() > 0 && instance().statusMap.containsKey(requestTag)) {
            instance().statusMap.put(requestTag, DownloadStatus.STATUS_PAUSE);
        }
    }


    /**
     * 将文件写入到本地
     */
    private static void save(Response response, FileData fileData) {
        String reqTag=fileData.getRequestTag();
        ResponseBody body = response.body();
        InputStream in = body != null ? body.byteStream() : null;
        BufferedInputStream bis = new BufferedInputStream(in);
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            File file = new File(fileData.getFilePath());
            //获取父目录
            File parent = new File(file.getParent());
            if (!parent.exists()) {
                //如果目录不存在则创建相应的目录
                parent.mkdirs();
            }
            randomAccessFile = new RandomAccessFile(file, "rwd");
            long currentFileLenght = randomAccessFile.length();
            long bodyContentLength = body != null ? body.contentLength() : 0;
            fileData.setTotal(bodyContentLength + currentFileLenght);
            if (currentFileLenght >= fileData.getTotal()) {
                instance().statusMap.put(reqTag, DownloadStatus.STATUS_COMPLETE);
                instance().progressMap.put(reqTag,100);
                for (int i = 0; i < instance().iCallBack.size(); i++) {
                    instance().iCallBack.get(i).downloadProgress(fileData, 100);
                    instance().iCallBack.get(i).downloadStatus(fileData, instance().statusMap.get(reqTag));
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
                if (instance().statusMap.get(reqTag) == DownloadStatus.STATUS_PAUSE) {
                    for (int i = 0; i < instance().iCallBack.size(); i++) {
                        instance().iCallBack.get(i).downloadStatus(fileData, instance().statusMap.get(reqTag));
                    }
                    return;
                }
                //记录当前进度
                currentFileLenght += len;
                fileData.setCurrent(currentFileLenght);
                //计算已经下载的百分比
                int percent = (int) (fileData.getCurrent() * 100 / fileData.getTotal());
                boolean isComplete = currentFileLenght >= fileData.getTotal();
                for (int i = 0; i < instance().iCallBack.size(); i++) {
                    if (percent>instance().progressMap.get(reqTag)){
                        instance().progressMap.put(reqTag,percent);
                        instance().iCallBack.get(i).downloadProgress(fileData, percent);
                    }
                }
                if (isComplete) {
                    //防止(len = bis.read(buffer) ResponseBody读到其他任务的流
                    instance().statusMap.put(reqTag, DownloadStatus.STATUS_COMPLETE);
                    for (int i = 0; i < instance().iCallBack.size(); i++) {
                        instance().iCallBack.get(i).downloadStatus(fileData, instance().statusMap.get(reqTag));
                    }
                    break;
                }
            }
            //把已完成的任务添加到集合中去
            instance().fileDatas.add(fileData);
            //删除当前的这个执行任务
            instance().statusMap.remove(reqTag);
            instance().progressMap.remove(reqTag);
            if (instance().statusMap.size() == 0) {
                //将完成的所有任务回调回去
                for (int i = 0; i < instance().iCallBack.size(); i++) {
                    instance().iCallBack.get(i).allDownloadComplete(instance().fileDatas);
                }
                //回调之后进行清除操作
                instance().fileDatas.clear();
            }
        } catch (Throwable throwable) {
            for (int i = 0; i < instance().iCallBack.size(); i++) {
                instance().iCallBack.get(i).error(fileData,throwable);
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
     * 关闭所有任务
     */
    public static void cancelAll() {
        if (instance().client != null) {
            instance().client.dispatcher().cancelAll();
        }
        if (instance().call != null) {
            instance().call.cancel();
        }
        instance().statusMap.clear();
        instance().progressMap.clear();
        instance().fileDatas.clear();
    }
}