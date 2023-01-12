package com.face_chtj.base_iotutils;

import android.text.TextUtils;
import android.util.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    /**
     * 图片转base64字符串
     * @param path 图片地址
     */
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            byte[] data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            return Base64.encodeToString(data, Base64.NO_CLOSE);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return "";
    }
}
