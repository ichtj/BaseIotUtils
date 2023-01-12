package com.face_chtj.base_iotutils;

import android.text.TextUtils;

import com.face_chtj.base_iotutils.entity.BaseUrlBean;
import com.face_chtj.base_iotutils.network.BaseUrlUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 描述：构建Retrofit实例，采用单例模式，全局共享同一个Retrofit
 */
public class RequestClientTools {
    private static final int CONNECTTIMEOUT = 25;
    private static final int READTIMEOUT    = 25;
    private static final int WRITETIMEOUT   = 25;
    private static RequestClientTools INSTANCE = null;
    private final Retrofit mRetrofit;
    private String BASEURL = "http://www.baidu.com";

    private RequestClientTools() {
        mRetrofit = create();
    }

    /**
     * 采用单例模式
     *
     * @return RequestClientManager
     */
    @NonNull
    private static RequestClientTools getInstance() {
        if (INSTANCE == null) {
            synchronized (RequestClientTools.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RequestClientTools();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 创建Api接口实例
     *
     * @param clazz Api接口类
     * @param <T>   Api接口
     * @return Api接口实例
     */
    @NonNull
    public static <T> T getService(@NonNull Class<T> clazz) {
        return getInstance().getRetrofit(clazz).create(clazz);
    }

    @NonNull
    private Retrofit getRetrofit(@Nullable Class<?> clazz) {
        if (clazz == null) {
            return mRetrofit;
        }
        return create(BASEURL);
    }

    /**
     * 创建Retrofit实例
     */
    Retrofit create() {
        return create(BASEURL);
    }

    /**
     * 创建Retrofit实例
     */
    private Retrofit create(@NonNull String baseUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .client(createOkHttpClient())
                .baseUrl(BaseUrlUtils.checkBaseUrl(baseUrl));
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        //这里可以接收字符串内容
        builder.addConverterFactory(ScalarsConverterFactory.create());
        //这里是接收json数据内容
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient();
        Gson gson = gsonBuilder.create();
        builder.addConverterFactory(GsonConverterFactory.create(gson));
        return builder.build();
    }

    /**
     * 创建OkHttpClient实例
     *
     * @return OkHttpClient
     */
    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //设置超时时长
        builder.connectTimeout(CONNECTTIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(READTIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITETIMEOUT, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);//设置出现错误进行重新连接。
        //这里拦截并修改所访问的地址 携带的参数
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //获取request
                Request request = chain.request();
                //从request中获取原有的HttpUrl实例oldHttpUrl
                HttpUrl oldHttpUrl = request.url();
                KLog.d("intercept() oldHttpUrl port >> "+oldHttpUrl.port()+",host >> "+oldHttpUrl.host());
                //获取request的创建者builder
                Request.Builder builder = request.newBuilder();
                //从request中获取headers，通过给定的键url_name
                List<String> headerValues = request.headers("urlname");
                if (headerValues != null && headerValues.size() > 0) {
                    //如果有这个header，先将配置的header删除，因此header仅用作app和okhttp之间使用
                    builder.removeHeader("urlname");
                    builder.removeHeader("forcePort");
                    //匹配获得新的BaseUrl
                    String headerValue = headerValues.get(0);
                    String customizePort= request.header("forcePort");
                    KLog.d("intercept() customize port >> "+customizePort);
                    HttpUrl newHttpUrl=null;
                    if (BaseIotUtils.getiBaseUrlCallback() != null) {
                        for (Map.Entry<String, BaseUrlBean> maps : BaseIotUtils.getiBaseUrlCallback().baseUrlValues().entrySet()) {
                            KLog.d("createOkHttpClient() maps key >> " + maps.getKey() + ",value >> " + maps.getValue().toString());
                            if (maps.getKey().equals(headerValue)) {
                                KLog.d("intercept() find the key >> " + maps.getKey());
                                HttpUrl.Builder newBaseUrl = HttpUrl.parse(maps.getValue().host).newBuilder();
                                if(maps.getValue().port!=-1){
                                    newBaseUrl.port(maps.getValue().port);
                                }
                                if(!TextUtils.isEmpty(customizePort)){
                                    newBaseUrl.port(Integer.parseInt(customizePort));
                                }
                                newHttpUrl=newBaseUrl.build();
                                break;
                            }
                        }
                    } else {
                        newHttpUrl = oldHttpUrl;
                    }
                    KLog.d("intercept() set before >> "+newHttpUrl.host()+", >> "+newHttpUrl.port());
                    HttpUrl newFullUrl = oldHttpUrl.newBuilder()
                            .port(newHttpUrl.port())
                            .host(newHttpUrl.host()).build();
                    //重建这个request，通过builder.url(newFullUrl).build()；
                    // 然后返回一个response至此结束修改
                    KLog.d("Url", "intercept: " + newFullUrl.toString());
                    return chain.proceed(builder.url(newFullUrl).build());
                }
                return chain.proceed(request);
            }
        });
        return builder.build();
    }
}
