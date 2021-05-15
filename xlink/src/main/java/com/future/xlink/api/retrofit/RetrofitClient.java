package com.future.xlink.api.retrofit;

import android.util.Log;

import com.future.xlink.api.ApiService;
import com.future.xlink.logs.Log4J;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.json.JSONObject;

import java.lang.reflect.Type;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private RetrofitClient() {
    }

    private static class SingletonInstance {
        private static final String HOST_IP = formatHost("https://www.baidu.com");
        private static final ApiService INSTANCE = new Retrofit.Builder()
                .client(OkHttpSingleton.getInstance())
                .baseUrl(getServerHostIp())
                .addConverterFactory(GsonConverterFactory.create(gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ApiService.class);
    }

    public static ApiService getInstance() {
        return SingletonInstance.INSTANCE;
    }

    private static class StringConverter implements JsonSerializer<String>, JsonDeserializer<String> {
        @Override
        public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return new JsonPrimitive("");
            } else {
                return new JsonPrimitive(src);
            }
        }

        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
//            Log4J.info(StringConverter.class,"deserialize",json.toString());
            return json.getAsJsonPrimitive().getAsString();
        }
    }

    private static Gson gson() {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(String.class, new StringConverter());
        return gb.create();
    }

    public static String getServerHostIp() {
        return SingletonInstance.HOST_IP;
    }

    public static String formatHost(String host) {
        return String.format("http://%s/", host);
    }
}

