package com.face_chtj.base_iotutils;

import android.util.Log;

import com.face_chtj.base_iotutils.entity.FileEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:文件工具类
 * --写入数据 {@link #writeFileData(String filename, String content, boolean isCover)}
 * --读取文件内容 {@link #readFileData(String fileName)}
 * --修改文件名称 {@link #reFileName(String sourcePath, String goalPath)}
 * --复制文件到指定目录下 {@link #copyFile(String, String)}
 * --将InputStream写入本地文件 {@link #writeToLocal(String, InputStream)}
 * --删除文件 {@link #delFile(String fileName)}
 * --得到文件夹大小 {@link #getDirectoryFormatSize(String)}----得到byte,kb,mb,gb
 * --得到文件大小 {@link #getFileFormatSize(String)}}----------得到byte,kb,mb,gb
 * --获取指定文件夹的大小 {@link #getFileSizes(String)}
 * --获取指定文件的可读大小 {@link #getFileAvailable(String)}
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 写入数据
     *
     * @param filename 路径+文件名称
     * @param content  写入的内容
     * @param isCover  是否覆盖文件的内容 true 覆盖原文件内容  | flase 追加内容在最后
     * @return 是否成功 true|false
     */
    public static boolean writeFileData(String filename, String content, boolean isCover) {
        FileOutputStream fos = null;
        try {
            File file = new File(filename);
            //如果文件不存在
            if (!file.exists()) {
                //重新创建文件
                file.createNewFile();
            }
            fos = new FileOutputStream(file, !isCover);
            byte[] bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(TAG, "writeFileData: " + e.getMessage());
        } finally {
            try {
                fos.close();//关闭文件输出流
            } catch (Exception e) {
                e.printStackTrace();
                KLog.e(TAG, "errMeg:" + e.getMessage());
            }
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
            if (fis != null) {
                fis.close();
            }
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(TAG, "readFileData: " + e.getMessage());
        }
        return result;
    }

    /**
     * 复制文件到指定目录下
     *
     * @param oldPath 源地址
     * @param newPath 目标地址
     */
    public static void copyFile(String oldPath, String newPath) {
        int byteread = 0;
        File oldfile = new File(oldPath);
        if (oldfile.exists()) { //文件存在时
            InputStream inStream=null;
            FileOutputStream fos_stm=null;
            try {
                inStream= new FileInputStream(oldPath); //读入原文件
                fos_stm= new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fos_stm.write(buffer, 0, byteread);
                }
            }catch (Throwable throwable){
                Log.e(TAG, "copyFile1: ", throwable);
            }finally {
                try {
                    if(inStream!=null){
                        inStream.close();
                    }
                }catch (Throwable throwable){
                    Log.e(TAG, "copyFile2: ", throwable);
                }
                try {
                    if(fos_stm!=null){
                        fos_stm.close();
                    }
                }catch (Throwable throwable){
                    Log.e(TAG, "copyFile3: ", throwable);
                }
            }
        }
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

    /**
     * 获取指定文件夹的大小
     *
     * @param directoryPath 文件夹路径
     * @return 按照单位BYTE, KB, MB, GB返回
     */
    public static long getFileSizes(String directoryPath) {
        KLog.d(TAG, "directoryPath=" + directoryPath);
        File file = new File(directoryPath);
        long size = 0;
        if (file.exists() && file.isDirectory()) {
            File flist[] = file.listFiles();//文件夹目录下的所有文件
            if (flist == null) {//4.2的模拟器空指针。
                return 0;
            }
            if (flist != null) {
                for (int i = 0; i < flist.length; i++) {
                    if (flist[i].isDirectory()) {//判断是否父目录下还有子目录
                        size = size + getFileSizes(flist[i].getAbsolutePath());///
                    } else {
                        //*getFileSize(flist[i].getAbsolutePath())*/
                        size = size + flist[i].length();///
                    }
                }
            }
        }
        return size;
    }

    /**
     * 获取某个目录下的文件和文件夹
     *
     * @param directoryPath 文件夹路径
     */
    public static List<FileEntity> getFileDirectory(String directoryPath) {
        List<FileEntity> fileListEntityList = new ArrayList<>();
        File file = new File(directoryPath);
        if (file.exists() && file.isDirectory()) {
            File flist[] = file.listFiles();//文件夹目录下的所有文件
            if (flist != null) {
                for (int i = 0; i < flist.length; i++) {
                    long size;
                    if (flist[i].isDirectory()) {//判断是否父目录下还有子目录
                        size = getFileSizes(flist[i].getAbsolutePath());///
                    } else {
                        //*getFileSize(flist[i].getAbsolutePath())*/
                        size = flist[i].length();
                    }
                    //获取上次修改的时间
                    String lastModified = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.CHINA).format(new Date(flist[i].lastModified()));
                    FileEntity fileEntity = new FileEntity(flist[i].getName(), size, file.getAbsolutePath() + "/", lastModified, flist[i].isDirectory());
                    fileListEntityList.add(fileEntity);
                }
            }
        }
        return fileListEntityList;
    }


    /**
     * 得到文件夹大小
     *
     * @param directoryPath 文件夹路径
     * @return 按照单位BYTE, KB, MB, GB返回
     */
    public static String getDirectoryFormatSize(String directoryPath) {
        // 转换文件大小
        String fileSizeString = "";
        long lengthSum = getFileSizes(directoryPath);
        DecimalFormat df = new DecimalFormat("#.00");
        if (lengthSum < 1024) {
            if (lengthSum == 0) {
                fileSizeString = "0B";
            } else {
                fileSizeString = df.format((double) lengthSum) + "B";
            }
        } else if (lengthSum < 1048576) {
            fileSizeString = df.format((double) lengthSum / 1024) + "K";
        } else if (lengthSum < 1073741824) {
            fileSizeString = df.format((double) lengthSum / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) lengthSum / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return 按照单位BYTE, KB, MB, GB返回
     */
    public static String getFileFormatSize(String filePath) {
        File file = new File(filePath);
        // 转换文件大小
        String fileSizeString = "";
        if (file.exists() && file.isFile()) {
            long lengthSum = file.length();
            DecimalFormat df = new DecimalFormat("#.00");
            if (lengthSum < 1024) {
                fileSizeString = df.format((double) lengthSum) + "B";
            } else if (lengthSum < 1048576) {
                fileSizeString = df.format((double) lengthSum / 1024) + "K";
            } else if (lengthSum < 1073741824) {
                fileSizeString = df.format((double) lengthSum / 1048576) + "M";
            } else {
                fileSizeString = df.format((double) lengthSum / 1073741824) + "G";
            }
        }
        return fileSizeString;
    }

    /**
     * 获取指定文件的大小
     *
     * @param filePath 文件路径
     * @return 文件总大小
     */
    public static long getFileAvailable(String filePath) {
        KLog.d(TAG, "filePath=" + filePath);
        File file = new File(filePath);
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);//使用FileInputStream读入file的数据流
                size = fis.available();//文件的大小
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }

    /**
     * 将InputStream写入本地文件
     *
     * @param destDirPath 写入本地目录
     * @param input       输入流
     */
    public static void writeToLocal(String destDirPath, InputStream input)
            throws IOException {

        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destDirPath);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }
}
