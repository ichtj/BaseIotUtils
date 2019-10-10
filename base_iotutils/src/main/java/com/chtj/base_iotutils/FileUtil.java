package com.chtj.base_iotutils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * 写入数据
     *
     * @param filename 路径+文件名称
     * @param content  写入的内容
     * @param isCover  是否覆盖文件的内容
     * @return 是否成功 true|false
     */
    public static boolean writeFileData(String filename, String content, boolean isCover) {
        try {
            File file = new File(filename);
            //如果文件不存在
            if (!file.exists()) {
                //重新创建文件
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, !isCover);
            byte[] bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.close();//关闭文件输出流
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "writeFileData: ", e);
        }
        return false;
    }

    /**
     * 读取文件内容
     *
     * @param fileName 路径+文件名称
     * @return 读取到的内容
     */
    public static String readFileData(String fileName) {
        String result = "";
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return "";
            }
            FileInputStream fis = new FileInputStream(file);
            //获取文件长度
            int lenght = fis.available();
            byte[] buffer = new byte[lenght];
            fis.read(buffer);
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "readFileData: ", e);
        }
        return result;
    }

    /**
     * 修改文件名称
     *
     * @param sourcePath 源文件路径+文件名称
     * @param goalPath   目标路径+文件名称
     * @return 是否成功 true|false
     */
    public static boolean reFileName(String sourcePath, String goalPath) {
        boolean isCompleted = false;
        if (sourcePath != null && !sourcePath.equals("") && goalPath != null && !goalPath.equals("")) {
            File firstFile = new File(sourcePath);
            if (firstFile.exists()) {
                File secondFile = new File(goalPath);
                isCompleted = firstFile.renameTo(secondFile);
            }
        }
        return isCompleted;
    }

    /**
     * 删除文件
     *
     * @param fileName 路径+文件名称
     * @return 是否删除成功
     */
    public static boolean delFile(String fileName) {
        boolean isCompleted = false;
        if (fileName != null && !fileName.equals("")) {
            File file = new File(fileName);
            if (file.exists()) {
                isCompleted = file.delete();
            }
        }
        return isCompleted;
    }
}
