/*************************************************************************
 > File Name: RKRecoverySystem.java
 > Author: jkand.huang
 > Mail: jkand.huang@rock-chips.com
 > Created Time: Wed 02 Nov 2016 03:10:47 PM CST
 ************************************************************************/
package com.chtj.base_framework.upgrade;

import android.content.Context;
import android.os.Build;
import android.os.RecoverySystem;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FRecoverySystemTools {
    private static final String TAG = "FRecoverySystemTools";
    private static final String ROOT_PATH = "/cache/recovery";
    private static File RECOVERY_DIR = new File(ROOT_PATH);
    private static File UPDATE_LAST_INSTALL_FILE = new File(RECOVERY_DIR, "last_install");
    private static File UPDATE_LAST_UPDATE_FILE = new File(RECOVERY_DIR, "last_update");
    //可以查看固件版本的文件位置
    private static String FWVERSION_CONTENT_FILE_PATH = "META-INF/com/android/metadata";

    public static void installPackage(Context context, File packageFile) throws IOException {
        String filename = packageFile.getCanonicalPath();
        String readFileInfo = readZipFileContent(packageFile.getAbsolutePath(), FWVERSION_CONTENT_FILE_PATH);
        String upFwVersion = "";
        Pattern pattern = Pattern.compile("([0-9a-zA-Z]+_)*V[0-9]{1,2}(\\.\\d{1,3})");//获取版本号的正则表达式
        Matcher matcher = pattern.matcher(readFileInfo);
        if (matcher.find()) {
            String info = matcher.group(0);
            upFwVersion = info.split("_")[2].replace("V", "");
            String currentFwVersion = getCurrentFwVersion();
            Log.d(TAG, "installPackage: upFwVersion=" + upFwVersion + ",currentFwVersion=" + currentFwVersion+",readFileInfo="+readFileInfo);
            writeFlagCommand(filename, upFwVersion, currentFwVersion);
            RecoverySystem.installPackage(context, packageFile);
        } else {
            throw new IOException("ota package not exist fwVersion!");
        }
    }

    private static String getFwVersionByOta(String zipFilepath, String readFileName) {
        try {
            ZipFile zip = new ZipFile(zipFilepath);
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
            StringBuilder content = new StringBuilder();
            ZipEntry ze;
            // 枚举zip文件内的文件/
            while (entries.hasMoreElements()) {
                ze = entries.nextElement();
                // 读取目标对象
                Log.d(TAG, "getFwVersionByOta: getName=" + ze.getName());
                if (ze.getName().equals(readFileName)) {
                    Scanner scanner = new Scanner(zip.getInputStream(ze));
                    while (scanner.hasNextLine()) {
                        content.append(scanner.nextLine());
                    }
                    scanner.close();
                }
            }
            zip.close();
            Log.d(TAG, "getFwVersionByOta: content=" + content.toString());
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
        return "";
    }

    /**
     * 获取指定路径下压缩包中的文件 并且取到文件中的内容
     * @param file 压缩包的路径
     * @param fileName 压缩包内的文件所在路径
     * @return 文件内容
     */
    public static String readZipFileContent(String file, String fileName) {
        try {
            ZipFile zf = new ZipFile(file);
            ZipEntry ze = zf.getEntry(fileName);
            InputStream in = zf.getInputStream(ze);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = br.readLine()) != null) {
                result.append(line + "\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
        return "";
    }

    public static String readLastUpdateCommand() {
        if (UPDATE_LAST_UPDATE_FILE.exists()) {
            Log.d(TAG, "UPDATE_LAST_UPDATE_FILE is exists");
            char[] buf = new char[128];
            int readCount = 0;
            ;
            try {
                FileReader reader = new FileReader(UPDATE_LAST_UPDATE_FILE);
                readCount = reader.read(buf, 0, buf.length);
                //Log.d(TAG, "readCount = " + readCount + " buf.length = " + buf.length);
            } catch (IOException e) {
                Log.e(TAG, "can not read " + ROOT_PATH + "/last_update! errMeg=" + e.getMessage());
            } finally {
                UPDATE_LAST_UPDATE_FILE.delete();
            }

            StringBuilder sBuilder = new StringBuilder();
            for (int i = 0; i < readCount; i++) {
                if (buf[i] == 0) {
                    break;
                }
                sBuilder.append(buf[i]);
            }
            return sBuilder.toString();
        } else {
            Log.d(TAG, "UPDATE_LAST_UPDATE_FILE is not exists");
            return null;
        }
    }


    public static String readLastInstallCommand() {
        if (UPDATE_LAST_INSTALL_FILE.exists()) {
            Log.d(TAG, "UPDATE_LAST_INSTALL_FILE is exists");
            char[] buf = new char[128];
            int readCount = 0;
            ;
            try {
                FileReader reader = new FileReader(UPDATE_LAST_INSTALL_FILE);
                readCount = reader.read(buf, 0, buf.length);
                //Log.d(TAG, "readCount = " + readCount + " buf.length = " + buf.length);
            } catch (IOException e) {
                Log.e(TAG, "can not read " + ROOT_PATH + "/last_install! errMeg=" + e.getMessage());
            }
            StringBuilder sBuilder = new StringBuilder();
            for (int i = 0; i < readCount; i++) {
                if (buf[i] == 0) {
                    break;
                }
                sBuilder.append(buf[i]);
            }
            return sBuilder.toString();
        } else {
            Log.d(TAG, "UPDATE_LAST_INSTALL_FILE is not exists");
            return null;
        }
    }

    public static void writeFlagCommand(String path, String upFwVersion, String currentFwVersion) throws IOException {
        RECOVERY_DIR.mkdirs();
        UPDATE_LAST_UPDATE_FILE.delete();
        FileWriter writer = new FileWriter(UPDATE_LAST_UPDATE_FILE);
        try {
            String writeData = "updating;" + path + ";" + currentFwVersion + ";" + upFwVersion;
            Log.d(TAG, "writeFlagCommand: writeData=" + writeData);
            writer.write(writeData);
        } catch (Exception e) {
            Log.d(TAG, "writeFlagCommand: errMeg=" + e.getMessage());
        } finally {
            writer.close();
        }
    }

    /**
     * 获取当前固件版本
     *
     * @return
     */
    public static String getCurrentFwVersion() {
        String currentFwversion = Build.DISPLAY;
        String[] versionInfo = currentFwversion.split("_");
        String systemFwVersion = "";
        if (versionInfo.length >= 2) {
            systemFwVersion = versionInfo[2].replace("V", "");
        } else {
            systemFwVersion = currentFwversion;
        }
        return systemFwVersion;
    }

}
