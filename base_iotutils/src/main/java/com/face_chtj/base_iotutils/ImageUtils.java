package com.face_chtj.base_iotutils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    /**
     * base64字符串转bitmap
     * @param base64String 字符串
     * @return bitmap
     */
    public static Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
