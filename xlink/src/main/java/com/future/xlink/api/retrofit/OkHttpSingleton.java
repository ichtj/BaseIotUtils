package com.future.xlink.api.retrofit;

import android.text.TextUtils;


import com.future.xlink.logs.Log4J;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.Buffer;

final class OkHttpSingleton {
    private static final long CONNECT_TIMEOUT = 60;
    private static final long WRITE_TIMEOUT = 30;
    private static final long READ_TIMEOUT = 30;

    private OkHttpSingleton() {
    }

    private static class OkHttpBuilder {
        private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .retryOnConnectionFailure(true)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public static OkHttpClient getInstance() {
        return OkHttpBuilder.CLIENT;
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
            Request original = chain.request();
            HttpUrl.Builder builder = original.url().newBuilder();

//            builder.addQueryParameter("controlCode", DataManager.getInstance().getSystemProvaderData(GlobalConfig.CONTROL_CODE_KEY));
//            builder.addQueryParameter("token", DataManager.getInstance().getSystemProvaderData(ShareParams.TOKEN_KEY));
//            builder.addQueryParameter("app_name", BuildConfig.AppName);
//            builder.addQueryParameter("type", "kt_tool");
            HttpUrl modifiedUrl = builder.build();
            Request request = original.newBuilder().url(modifiedUrl).build();
            try {
//                if (BuildConfig.IsDebug) {
                    String params = getParameters(request.body());
                    String urlStr = request.url().toString();
                    if (TextUtils.isEmpty(params)) {
                        Log4J.http(urlStr);
                    } else {
                        Log4J.http(urlStr + "&" + params);
                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return chain.proceed(request);
        }
    }

    private static String getParameters(RequestBody requestBody) {
        try {
            if (requestBody != null) {
                final Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(buffer.inputStream());
                final StringBuilder resultBuffer = new StringBuilder();
                byte[] inputBytes = new byte[1024];
                while (true) {
                    int count = bufferedInputStream.read(inputBytes);
                    if (count <= 0) {
                        break;
                    }
                    resultBuffer.append(new String(Arrays.copyOf(inputBytes, count), "UTF-8"));
                }
                String data=resultBuffer.toString();
                data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                data = data.replaceAll("\\+", "%2B");
                final String parameter = URLDecoder.decode(data, "UTF-8");
                bufferedInputStream.close();
                return parameter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}