package com.face_chtj.base_iotutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;

/**
 * Create on 2020/3/17
 * author chtj
 * desc 压缩相关工具类
 */
import java.util.zip.ZipInputStream;

public class ZipUtils {

    public static void unzipToDirectory(String zipFilePath, String destinationFolderPath) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                String filePath = destinationFolderPath + File.separator + entryName;

                // Ensure the entry is not a directory
                if (!entry.isDirectory()) {
                    // Extract the file
                    extractFile(zipInputStream, filePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
        }
    }
}

