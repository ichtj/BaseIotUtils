package com.face_chtj.base_iotutils.download;

import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.entity.FileCacheData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * current 当前得进度 MD5
 * total   总进度    MD2
 */
public class DownloadSupport {

    private static final String TAG = DownloadSupport.class.getName();
    private OkHttpClient client;
    private Call call;
    private String cacheFilePath = "/sdcard/baseiotcloud/";
    private String cacheFileName = "history.txt";
    private static Map<String, DownloadStatus> currentTaskList = new HashMap<>();

    public interface DownloadCallBack {
        //下载过程
        void download(FileCacheData fileCacheData, int percent, boolean isComplete);

        //下载状态
        void downloadStatus(String requestTag, DownloadStatus downloadStatus);

        //异常状态
        void error(Exception e);
    }

    public DownloadSupport() {
        //在下载、暂停后的继续下载中可复用同一个client对象
        client = getProgressClient();
        try {
            File file = new File(cacheFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(cacheFilePath + cacheFileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    //每次下载需要新建新的Call对象
    private Call newCall(FileCacheData fileCacheData) {
        KLog.d(TAG,"newCall:>Breakpoint="+new File(fileCacheData.getFilePath()).length());
        //断点位置
        Request request = new Request.Builder()
                .url(fileCacheData.getUrl())
                .tag(fileCacheData.getRequestTag())
                .header("RANGE", "bytes=" + new File(fileCacheData.getFilePath()).length() + "-")//断点续传要用到的，指示下载的区间
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
     * 相同的地址的url的任务不能重复下载，会提示任务存在
     * 使用download会自动判断文件是否有下载过
     * 会保留该url文件的进度 注意是是根据url来进行判断
     * 不是根据本地文件来进行判断是否有下载过 ,所以url请设置固定地址,不然该文件的缓存会失效，并重新开始下载
     */
    public void addStartTask(final FileCacheData fileCacheData, final DownloadCallBack downloadCallBack) {
        //防止任务重复下载，扰乱进度
        if (currentTaskList != null && currentTaskList.size() > 0) {
            DownloadStatus downloadStatus = currentTaskList.get(fileCacheData.getRequestTag());
            if (downloadStatus == DownloadStatus.RUNNING) {
                KLog.d(TAG, "download:>the task already exist");
                return;
            }
        }
        //该集合中没有任务正在处理
        currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.RUNNING);
        downloadCallBack.downloadStatus(fileCacheData.getRequestTag(), currentTaskList.get(fileCacheData.getRequestTag()));
        //获取url对应的key
        call = newCall(fileCacheData);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                save(response, fileCacheData, downloadCallBack);
            }
        });
    }

    /**
     * 暂停所有任务
     */
    public void pause() {
        for (Map.Entry<String, DownloadStatus> entry : currentTaskList.entrySet()) {
            currentTaskList.put(entry.getKey(), DownloadStatus.PAUSE);
        }
    }

    /**
     * 按tag暂停任务
     *
     * @param requestTag
     */
    public void pause(String requestTag) {
        if (currentTaskList != null && currentTaskList.size() > 0 && currentTaskList.containsKey(requestTag)) {
            currentTaskList.put(requestTag, DownloadStatus.PAUSE);
        }
    }


    /**
     * 将文件写入到本地
     *
     * @param response
     * @param fileCacheData
     * @param downloadCallBack
     */
    private void save(Response response, FileCacheData fileCacheData, DownloadCallBack downloadCallBack) {
        //存储下载信息 用于清理缓存文件
        FileUtils.writeFileData(cacheFilePath + cacheFileName, fileCacheData.getFileName() + "_", false);
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(new File(fileCacheData.getFilePath()), "rwd");
            long current = randomAccessFile.length();
            //body.contentLength()存放了这次下载的文件的总长度 current得到之前下载过的文件长度
            fileCacheData.setTotal(body.contentLength()+current);
            KLog.d(TAG,"save:>total length="+fileCacheData.getTotal()+",curent="+current);
            if (current >= fileCacheData.getTotal()) {
                downloadCallBack.download(fileCacheData, 100, true);
                currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.COMPLETE);
                downloadCallBack.downloadStatus(fileCacheData.getRequestTag(), currentTaskList.get(fileCacheData.getRequestTag()));
                return;
            }
            //从文件的断点开始下载
            randomAccessFile.seek(current);
            byte[] buffer = new byte[2 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                if (currentTaskList.get(fileCacheData.getRequestTag()) == DownloadStatus.PAUSE) {
                    currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.PAUSE);
                    downloadCallBack.downloadStatus(fileCacheData.getRequestTag(), currentTaskList.get(fileCacheData.getRequestTag()));
                    return;
                }
                //每次读取最多不超过2*1024个字节
                current += len;
                fileCacheData.setCurrent(current);
                //计算已经下载的百分比
                int percent = (int) (fileCacheData.getCurrent() * 100 / fileCacheData.getTotal());
                boolean isComplete = current >= fileCacheData.getTotal();
                downloadCallBack.download(fileCacheData, percent, isComplete);
                randomAccessFile.write(buffer, 0, len);
                if (isComplete) {
                    //防止(len = bis.read(buffer) ResponseBody读到其他任务的流
                    currentTaskList.put(fileCacheData.getRequestTag(), DownloadStatus.COMPLETE);
                    downloadCallBack.downloadStatus(fileCacheData.getRequestTag(), currentTaskList.get(fileCacheData.getRequestTag()));
                    break;
                }
            }
            //删除当前的这个执行任务
            currentTaskList.remove(fileCacheData.getRequestTag());
        } catch (IOException e) {
            e.printStackTrace();
            downloadCallBack.error(e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
            }

        }
    }

    //按照requestTag关闭任务
    public void cancel() {
        if(client!=null){
            client.dispatcher().cancelAll();
        }
        if(call!=null){
            call.cancel();
        }
        if(currentTaskList!=null){
            currentTaskList.clear();
        }
    }

    /**
     * 删除以往下载的文件信息
     */
    public void deleteFile(String saveRootPath) {
        //查询以往记录的下载信息
        String result = FileUtils.readFileData(cacheFilePath + cacheFileName);
        KLog.d(TAG, "deleteFile:>read File=" + result);
        if (result.equals("")) {
            KLog.d(TAG, "deleteFile:> no file");
        } else {
            String[] fileArray = result.split("_");
            for (int i = 0; i < fileArray.length; i++) {
                if (!fileArray[i].equals("null") && !fileArray.equals("")) {
                    FileUtils.delFile(saveRootPath + fileArray[i]);
                }
            }
            //最后删除该文件
            FileUtils.delFile(cacheFilePath + cacheFileName);
        }
        if (currentTaskList != null) {
            currentTaskList.clear();
        }
    }
}