package com.future.xlink.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JckJsonHelper {

    private  static  ObjectMapper mapper=null;
    public static ObjectMapper get() {
        if (mapper==null){
            mapper=new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper;
    }

    public static String toJson(Object obj) {
        try {
            ObjectMapper objectMapper=get();
            return objectMapper.writeValueAsString(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}