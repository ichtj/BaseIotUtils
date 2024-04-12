package com.face_chtj.base_iotutils.network;

import androidx.annotation.NonNull;

/**
 * 描述：检查BaseUrl是否以"/"结尾
 */
public class BaseUrlUtils {

    public static String checkBaseUrl(@NonNull String url) {
        if (url.endsWith("/")) {
            return url;
        } else {
            return url + "/";
        }
    }
}
