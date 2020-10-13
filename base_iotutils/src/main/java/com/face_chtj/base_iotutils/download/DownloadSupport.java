package com.face_chtj.base_iotutils.download;

import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.entity.FileCacheData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

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
    private String cacheFilePath="/sdcard/baseiotcloud/";
    private String cacheFileName="history.txt";

    public interface DownloadCallBack {
        void download(FileCacheData fileCacheData, int percent, boolean isComplete);

        void error(Exception e);
    }

    public DownloadSupport() {
        //在下载、暂停后的继续下载中可复用同一个client对象
        client = getProgressClient();
        try {
            File file=new File(cacheFilePath);
            if(!file.exists()){
                file.mkdirs();
            }
            file=new File(cacheFilePath+cacheFileName);
            if(!file.exists()){
                file.createNewFile();
            }
        }catch (Exception e){
            e.printStackTrace();
            KLog.e(TAG,"errMeg:"+e.getMessage());
        }
    }

    //每次下载需要新建新的Call对象
    private Call newCall(FileCacheData fileCacheData) {
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
     * 使用download会自动判断文件是否有下载过
     * 会保留该url文件的进度 注意是是根据url来进行判断
     * 不是根据本地文件来进行判断是否有下载过 ,所以url请设置固定地址,不然该文件的缓存会失效，并重新开始下载
     */
    public void download(final FileCacheData fileCacheData, final DownloadCallBack downloadCallBack) {
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
     * 暂停任务
     *
     * @param requestTag
     */
    public void pause(String requestTag) {
        for (Call call : client.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(requestTag)) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (call.request().tag().equals(requestTag)) {
                call.cancel();
            }
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
        long current = 0;
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        fileCacheData.setTotal(body.contentLength());
        try {
            randomAccessFile = new RandomAccessFile(new File(fileCacheData.getFilePath()), "rwd");
            current = randomAccessFile.length();
            //从文件的断点开始下载
            randomAccessFile.seek(current);
            byte[] buffer = new byte[2 * 1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                //每次读取最多不超过2*1024个字节
                current += len;
                fileCacheData.setCurrent(current);
                //计算已经下载的百分比
                int percent = (int) (fileCacheData.getCurrent() * 100 / fileCacheData.getTotal());
                downloadCallBack.download(fileCacheData, percent, current >= fileCacheData.getTotal());
                randomAccessFile.write(buffer, 0, len);
            }
            //存储下载信息
            KLog.d(TAG,"save:> history result="+ FileUtils.writeFileData(cacheFilePath+cacheFileName,fileCacheData.getFileName()+"_",false));
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

    /**
     * 删除以往下载的文件信息
     */
    public void deleteFile(String saveRootPath){
        String result= FileUtils.readFileData(cacheFilePath+cacheFileName);
        if(result.equals("")){
            KLog.d(TAG,"deleteFile:> no file");
        }else{
            String[] fileArray=result.split("_");
            for (int i = 0; i < fileArray.length; i++) {
                if(!fileArray[i].equals("null")&&!fileArray.equals("")){
                    FileUtils.delFile(saveRootPath+fileArray[i]);
                }
            }
            //最后删除该文件
            FileUtils.delFile(cacheFilePath+cacheFileName);
        }

    }
}