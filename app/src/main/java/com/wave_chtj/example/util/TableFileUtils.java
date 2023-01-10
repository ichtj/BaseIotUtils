package com.wave_chtj.example.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Create on 2020/7/17
 * author chtj
 * desc
 */
public class TableFileUtils {
    /**
     * zip保存的路径
     */
    private static final String xlsPath = "/sdcard/table.xls";
    /**
     * 检查xls文件是否存在
     * @return
     */
    public static boolean checkXlsExist(){
        if(new File("/sdcard/table.xls").exists()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 检查xlsx文件是否存在
     * @return
     */
    public static boolean checkXlsxExist(){
        if(new File("/sdcard/table.xlsx").exists()){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 将InputStream写入本地文件
     *
     * @param destDirPath 写入本地目录
     * @param input       输入流
     * @throws IOException
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
