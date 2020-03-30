package com.face_chtj.base_iotutils;

/**
 * Create on 2020/3/17
 * author chtj
 * desc 判断字符串是否为空
 */
public class StringUtils {
    /**
     * 是否为空
     * @param str 字符串
     * @return true 空 false 非空
     */
    public static Boolean isEmpty(String str) {
        if(str == null || str.length() == 0 || "null".equals(str)) {
            return true;
        }

        return false;
    }
}
