package com.face_chtj.base_iotutils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.Set;

public class GsonUtils {
    protected GsonUtils() {
    }

    private static class CustomExclusionStrategy implements ExclusionStrategy {
        private final Set<String> excludeFields;

        public CustomExclusionStrategy(Set<String> excludeFields) {
            this.excludeFields = excludeFields;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return excludeFields.contains(f.getName());
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

    /**
     * 根据对象返回json   不过滤空值字段
     */
    public static String toJsonWtihNullField(Object obj){
        Gson nullableGson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping ()
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                .create();
        return nullableGson.toJson(obj);
    }

    /**
     * 根据对象返回json  过滤空值字段
     */
    public static String toJsonFilterNullField(Object obj){
        Gson filterNullGson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping ()
                .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                .create();
        return filterNullGson.toJson(obj);
    }

    /**
     * 将json转化为对应的实体对象
     * new TypeToken<HashMap<String, Object>>(){}.getType()
     */
    public static <T>  T fromJson(String json, Type type) throws JSONException {
        Gson nullableGson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping ()
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                .create();
        return nullableGson.fromJson(json, type);
    }

    public static String exclude(Object object, Set<String> excludeFields) {
        Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new CustomExclusionStrategy(excludeFields))
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping ()
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                .create();
        return gson.toJson(object);
    }

}


