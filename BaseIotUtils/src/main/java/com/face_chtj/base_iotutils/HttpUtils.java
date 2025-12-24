package com.face_chtj.base_iotutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    private static final byte[] LOCKER = new byte[0];
    private static HttpUtils mInstance;
    private OkHttpClient mOkHttpClient;

    private HttpUtils() {
        OkHttpClient.Builder ClientBuilder=new OkHttpClient.Builder();
        ClientBuilder.readTimeout(20, TimeUnit.SECONDS);//读取超时
        ClientBuilder.connectTimeout(6, TimeUnit.SECONDS);//连接超时
        ClientBuilder.writeTimeout(60, TimeUnit.SECONDS);//写入超时
        //支持HTTPS请求，跳过证书验证
        ClientBuilder.sslSocketFactory(createSSLSocketFactory());
        ClientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        mOkHttpClient=ClientBuilder.build();
    }

    /**
     * 单例模式获取NetUtils
     * @return
     */
    public static HttpUtils getInstance() {
        if (mInstance == null) {
            synchronized (LOCKER) {
                if (mInstance == null) {
                    mInstance = new HttpUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * get请求，同步方式，获取网络数据，是在主线程中执行的，需要新起线程，将其放到子线程中执行
     * @param url
     * @return
     */
    public  Response getDataSynFromNet(String url) {
        //1 构造Request
        Request.Builder builder = new Request.Builder();
        Request request=builder.get().url(url).build();
        //2 将Request封装为Call
        Call call = mOkHttpClient.newCall(request);
        //3 执行Call，得到response
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
    /**
     * post请求，同步方式，提交数据，是在主线程中执行的，需要新起线程，将其放到子线程中执行
     * @param url
     * @param bodyParams
     * @return
     */
    public  Response postDataSynToNet(String url,Map<String,String> bodyParams) {
        //1构造RequestBody
        RequestBody body=setRequestBody(bodyParams);
        //2 构造Request
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.post(body).url(url).build();
        //3 将Request封装为Call
        Call call = mOkHttpClient.newCall(request);
        //4 执行Call，得到response
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
    /**
     * 自定义网络回调接口
     */
    public interface MyNetCall{
        void success(Call call, Response response) throws IOException;
        void failed(Call call, IOException e);
    }

    /**
     * get请求，异步方式，获取网络数据，是在子线程中执行的，需要切换到主线程才能更新UI
     * @param url
     * @param myNetCall
     * @return
     */
    public  void getDataAsynFromNet(String url, final MyNetCall myNetCall) {
        //1 构造Request
        Request.Builder builder = new Request.Builder();
        Request request=builder.get().url(url).build();
        //2 将Request封装为Call
        Call call = mOkHttpClient.newCall(request);
        //3 执行Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myNetCall.failed(call,e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                myNetCall.success(call,response);

            }
        });
    }

    /**
     * post请求，异步方式，提交数据，是在子线程中执行的，需要切换到主线程才能更新UI
     * @param url
     * @param bodyParams
     * @param myNetCall
     */
    public  void postDataAsynToNet(String url, Map<String,String> bodyParams, final MyNetCall myNetCall) {
        //1构造RequestBody
        RequestBody body=setRequestBody(bodyParams);
        //2 构造Request
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.post(body).url(url).build();
        //3 将Request封装为Call
        Call call = mOkHttpClient.newCall(request);
        //4 执行Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myNetCall.failed(call,e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                myNetCall.success(call,response);
            }
        });
    }
    /**
     * post的请求参数，构造RequestBody
     * @param BodyParams
     * @return
     */
    private RequestBody setRequestBody(Map<String, String> BodyParams){
        RequestBody body=null;
        okhttp3.FormBody.Builder formEncodingBuilder=new okhttp3.FormBody.Builder();
        if(BodyParams != null){
            Iterator<String> iterator = BodyParams.keySet().iterator();
            String key = "";
            while (iterator.hasNext()) {
                key = iterator.next().toString();
                formEncodingBuilder.add(key, BodyParams.get(key));
                Log.d("post http", "post_Params==="+key+"===="+BodyParams.get(key));
            }
        }
        body=formEncodingBuilder.build();
        return body;

    }

    /**
     * 判断网络是否可用
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     * @return
     */
    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }
    /**
     * 用于信任所有证书
     */
    class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
