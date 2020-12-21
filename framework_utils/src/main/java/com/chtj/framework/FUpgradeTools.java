package com.chtj.framework;

import android.annotation.SuppressLint;
import android.os.RecoverySystem;
import android.util.Log;

import com.chtj.framework.entity.InstallStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * 更新工具类
 * 针对固件升级 APK等
 */
public class FUpgradeTools {

    public interface UpgradeInterface {
        /**
         * 固件安装前的执行操作回调
         * @param installStatus
         */
        void operating(InstallStatus installStatus);

        /**
         * 固件安装前的过程执行失败
         * @param throwable
         */
        void error(Throwable throwable);
    }


    private static final String TAG = "FirmwareUtils";
    /**
     * 固件最终存放的地址
     */
    public static final String SAVA_FW_COPY_PATH = "/data/update.zip";

    /**
     * 固件系统升级
     *
     * @param filePath
     */
    public static void firmwareUpgrade(String filePath, UpgradeInterface upgradeInterface) {
        try {
            upgradeInterface.operating(InstallStatus.CHECK);
            RecoverySystem.verifyPackage(new File(filePath), new RecoverySystem.ProgressListener() {
                @SuppressLint("WrongConstant")
                @Override
                public void onProgress(int progress) {
                    upgradeInterface.operating(InstallStatus.COPY);
                    Log.d(TAG, "onProgress() called with: progress = [" + progress + "]");
                    if (progress == 100) {
                        copyFile(filePath, SAVA_FW_COPY_PATH);
                        try {
                            upgradeInterface.operating(InstallStatus.INSTALL);
                            RecoverySystem.installPackage(FBaseTools.getContext(), new File("/data/update.zip"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            upgradeInterface.error(e);
                        }
                    }
                }
            }, null);
        } catch (Throwable e) {
            e.printStackTrace();
            upgradeInterface.error(e);
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    /**
     * 复制文件到data目录下
     *
     * @param oldPath 源地址
     * @param newPath 目标地址
     */
    private static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                try {
                    Class<?> c = Class.forName("android.os.SystemProperties");
                    Method set = c.getMethod("set", String.class, String.class);
                    set.invoke(c, "persist.sys.firstRun", "true");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "copyFile:fail" + e);
            e.printStackTrace();
        }
    }


    /**
     * App安装升级
     *
     * @param filePath 文件存放地址
     * @param isSys    是否是系统应用
     * @return 操作结果
     */
    public static FCmdTools.CommandResult installApk(String filePath, boolean isSys) {
        return installApk(filePath, isSys, false);
    }

    /**
     * App安装升级
     *
     * @param filePath 文件存放地址
     * @param isSys    是否是系统应用
     * @param isReboot 是否执行重启
     * @return 操作结果
     */
    public static FCmdTools.CommandResult installApk(String filePath, boolean isSys, boolean isReboot) {
        FCmdTools.CommandResult cmdResult = new FCmdTools.CommandResult();
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        String[] commands = null;
        try {
            if (isSys) {
                String[] fileInfo = filePath.split("/");
                String fileName = fileInfo[fileInfo.length - 1].replace(".apk", "");
                commands = new String[]{
                        "rm -rf /system/priv-app/" + fileName + "*",
                        "cp -rf " + filePath + " /system/priv-app/",
                        "chmod 777 /system/priv-app/" + fileName + "*",
                        isReboot ? "reboot" : "",
                };
            } else {
                commands = new String[]{
                        "pm install -rf " + filePath,
                        isReboot ? "reboot" : "",
                };
            }
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command != null) {
                    os.write(command.getBytes());
                    os.writeBytes("\n");
                    os.flush();
                }
            }
            os.writeBytes("exit\n");
            os.flush();
            cmdResult.result=process.waitFor();
            //获取错误信息
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
            cmdResult.successMsg=successMsg.toString();
            cmdResult.errorMsg=errorMsg.toString();
            Log.i(TAG, cmdResult.result + " | " + cmdResult.successMsg
                    + " | " + cmdResult.errorMsg);
        } catch (IOException e) {
            String errmsg = e.getMessage();
            if (errmsg != null) {
                Log.e(TAG, errmsg);
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            String errmsg = e.getMessage();
            if (errmsg != null) {
                Log.e(TAG, errmsg);
            } else {
                e.printStackTrace();
            }
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                String errmsg = e.getMessage();
                if (errmsg != null) {
                    Log.e(TAG, errmsg);
                } else {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return cmdResult;
    }
}
