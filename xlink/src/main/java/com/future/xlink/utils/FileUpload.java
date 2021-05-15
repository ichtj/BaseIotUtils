package com.future.xlink.utils;

import android.util.Log;

import com.future.xlink.api.response.BaseResponse;
import com.future.xlink.api.retrofit.RetrofitClient;
import com.future.xlink.bean.InitParams;
import com.future.xlink.logs.Log4J;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * 大文件拆分上传处理
 * */

public class FileUpload {
    public static void readAndUpload(String filePath, String fileName,InitParams params) throws Exception {
        File file = new File(filePath);
        long total = file.length();
        long startPos = 0;
        long readSize =2* 524288;  //每次读取的大小，1M

        long readLen = 0;
        while (readLen < total) {   //直到所有读取完成为止
            long rs = readLen + readSize <= total ? readSize : (int) total - readLen;
            byte[] datas = ZFileUtils.readFile(filePath, startPos, rs); //分段读取文件
            readLen += datas.length;
           boolean result= uploadBreakpoint(fileName, startPos, datas, total,params); //上传数据
            if (!result){
                Log4J.info(FileUpload.class,"readAndUpload","failed==>"+startPos);
                return;
            }
            Log4J.info(FileUpload.class,"readAndUpload","success==>"+startPos);
            startPos += datas.length;
        }
    }
    private static boolean uploadBreakpoint(String fileName, long startPos, byte[] datas, long total,InitParams params) throws Exception {
        int retryCount = 5; //重试的次数
        boolean suc = false;
        do {
            suc = doUploadBreakpointEx(fileName, startPos, datas, total,params); //上传数据
        } while (!suc && --retryCount > 0); //失败重试
        return suc;
    }
    /**
     * 上传文件
     */
    private static boolean doUploadBreakpointEx(String fileName, long startPos, byte[] datas, long total, InitParams params) throws Exception {
        String url = "api/iot/reg/device/file/upload";
        try {
            Map<String, RequestBody> data = new HashMap<>();
            MediaType textType = MediaType.parse("text/plain");
            data.put("fileName", RequestBody.create(textType, fileName));

            RequestBody file = RequestBody.create(MediaType.parse("application/octet-stream"), datas);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, file);

            long timestamp = System.currentTimeMillis();
            String token =Utils.getToken(params,String.valueOf(timestamp));
            System.out.println("token: " + token);
            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1111");
            Response<BaseResponse>  response=
            RetrofitClient.getInstance()
                    .doUploadFile(GlobalConfig.HTTP_SERVER+url,token,String.valueOf(timestamp),params.sn,data,
                            filePart,getContentRange(startPos,datas,total)).execute();
            BaseResponse baseResponse=response.body();
            if (baseResponse!=null&&baseResponse.status==0){
                return  true;
            }
//            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1112==>"+JckJsonHelper.toJson(response));
//                    .enqueue(new Callback<BaseResponse>() {
//                        @Override
//                        public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
//
//                            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1112==>"+JckJsonHelper.toJson(response));
//                            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1112==>"+response.code());
//                            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1112==>"+startPos+response.isSuccessful());
//                        }
//
//                        @Override
//                        public void onFailure(Call<BaseResponse> call, Throwable t) {
//                            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1113");
//                        }
//                    });
//            Response<BaseResponse> response=call.execute();
//            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1112");
//            if (response !=null&&response.isSuccessful()){
//                    //执行成功
//                System.out.println("文件上传成功");
//                Log4J.info(FileUpload.class,"doUploadBreakpointEx","1113");
//                return true;
//            }else {
//                //执行失败
//                return false;
//            }

//            Call<RestResult<Object>> call = apis.create(FileClient.class).uploadDeviceFile(url, data, filePart,
//                    getContentRange(startPos, datas, total),
//                    token, timestamp + "", DEVICE_SN);
//            RestResult<Object> rsl = RetrofitUtils.checkAndGetData(call);
//            if (rsl != null) {
//                if (rsl.getStatus() == RestResultStatusEnum.SUCCESS.value()) {
//                    return true;
//                } else {
//                    System.out.println(rsl);
//                }
//            }
        } catch (Exception e) {
            Log4J.info(FileUpload.class,"doUploadBreakpointEx","1114");
            e.printStackTrace();
        }
        return false;
    }
    private static String getContentRange(long startPos, byte[] datas, long total) {
        //设置Content-Range来支持断点续传，格式：
        //Content-Range: bytes (unit first byte pos) – [last byte pos]/[entity legth]
        //Content-Range: bytes 0-800/801 //801:文件总大小
        return "bytes " + startPos + "-" + (startPos + datas.length - 1) + "/" + total;
    }
    private String mUpUrl;
    private File mPath;
    private Call mCall;
    private Map<String,String> mParams;
    private long mAlreadyUpLength = 0;//已经上传长度
    private long mTotalLength = 0;//整体文件大小
    private int mSign = 0;
    /**
     * post断点上传
     * @param upUrl
     * @param upFilePathAndName
     * @param params
     * @param listener
     */
//    public void postRenewalUpRequest(final String upUrl, final File upFilePathAndName, final Map<String,String> params){
//        synchronized (this){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    mSign = 2;
//                    mUpUrl = upUrl;
//                    mPath = upFilePathAndName;
//                    mParams = params;
////                    mHttpUpListener = listener;
//                    RequestBody requestBody = new RequestBody() {
//                        @Override
//                        public MediaType contentType() {
//                            return null;
//                        }
//
//
//                        @Override
//                        public void writeTo(BufferedSink sink) throws IOException {
//                            RandomAccessFile randomAccessFile = new RandomAccessFile(mPath, "rw");
//                            if (mTotalLength == 0) {
//                                mTotalLength = randomAccessFile.length();
//                            }
//                            if (mAlreadyUpLength != 0){
//                                randomAccessFile.seek(mAlreadyUpLength);
//                            }
//                            byte[] bytes = new byte[2048];
//                            int len = 0;
//                            try {
//                                while ((len = randomAccessFile.read(bytes)) != -1) {
//                                    sink.write(bytes, 0, len);
//                                    mAlreadyUpLength = mAlreadyUpLength + len;
////                                    if (mHttpUpListener != null) {
////                                        mHttpUpListener.onUpFile(mTotalLength, mAlreadyUpLength);
////                                    }
//                                }
//                            }catch (Exception e){
//                                Log.e("postRenewalUpRequest", "上传中断");
//                            }finally {
//                                mAlreadyUpLength = randomAccessFile.getFilePointer();
//                                randomAccessFile.close();//关闭流
//                                Log.e("postRenewalUpRequest", "流关闭");
//                            }
//
//                        }
//                    };
//
//                    MultipartBody.Builder builder = new MultipartBody.Builder();
//                    if (mParams!=null) {
//                        Set<String> keys = mParams.keySet();
//                        for (String key : keys) {
//                            builder.addFormDataPart(key, mParams.get(key));
//                        }
//                    }
//                    builder.addFormDataPart("file", mPath.getName(), requestBody);
//                    MultipartBody multipartBody = builder.build();
//
//                    Request request = new Request.Builder()
//                            .url(mUpUrl)
//                            .header("RANGE","bytes="+mAlreadyUpLength+"-"+mTotalLength)
//                            .post(multipartBody)
//                            .build();
//                    mCall = OkHttpClientCreate.CreateClient().newCall(request);
//                    mCall.enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//                            if (mHttpUpListener != null) {
//                                mHttpUpListener.onFailure(call, e);
//                            }
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            if (mHttpUpListener != null) {
//                                mHttpUpListener.onResponse(call, response);
//                            }
//                            mAlreadyUpLength = 0;
//                            mTotalLength = 0;
//
//                        }
//                    });
//
//                }
//            }).start();
//        }
//
//
//    }
}
