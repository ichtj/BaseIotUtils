package com.chtj.base_framework;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.RecoverySystem;
import android.util.Log;

import com.chtj.base_framework.entity.CommonValue;
import com.chtj.base_framework.entity.InstallStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新工具类
 * 针对固件升级 APK等
 */
public class FUpgradeTools {

    public interface UpgradeInterface {
        /**
         * 固件安装前的执行操作回调
         *
         * @param installStatus
         */
        void operating(InstallStatus installStatus);

        /**
         * 固件安装前的过程执行失败
         */
        void error(String errInfo);
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
            File file = new File(filePath);
            if (file.exists()) {
                upgradeInterface.operating(InstallStatus.CHECK);
                RecoverySystem.verifyPackage(file, new RecoverySystem.ProgressListener() {
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
                                upgradeInterface.error(e.getMessage());
                            }
                        }
                    }
                }, null);
            } else {
                upgradeInterface.error("Firmware does not exist");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            upgradeInterface.error(e.getMessage());
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
     * 卸载普通应用(第三方应用)
     *
     * @param packageName 包名
     * @param isRebootNow 是否立即重启
     * @return
     */
    public CommonValue uninstallNormalApp(String packageName, boolean isRebootNow) {
        String[] commands = new String[]{
                "pm uninstall  " + packageName,
                isRebootNow ? FCommonTools.CMD_REBOOT : "",
        };
        FCmdTools.CommandResult commandResult = FCmdTools.execCommand(commands, true);
        if (commandResult.result == 0) {
            return CommonValue.EXEU_COMPLETE;
        } else {
            return CommonValue.EXEU_UNKNOWN_ERR;
        }
    }

    /**
     * 卸载系统应用
     *
     * @param apkName     包名
     * @param isRebootNow 是否立即重启
     * @return
     */
    public CommonValue uninstallSystemApp(String apkName, boolean isRebootNow) {
        String replaceStr = apkName.replace(".apk", "") + "*";
        String[] commands = new String[]{
                "rm -rf /system/priv-app/" + replaceStr,
                isRebootNow ? FCommonTools.CMD_REBOOT : "",
        };
        FCmdTools.CommandResult commandResult = FCmdTools.execCommand(commands, true);
        if (commandResult.result == 0) {
            return CommonValue.EXEU_COMPLETE;
        } else {
            if (commandResult.errorMsg != null && commandResult.errorMsg.contains("Read-only file system")) {
                return CommonValue.CMD_READ_ONLY;
            } else {
                return CommonValue.EXEU_UNKNOWN_ERR;
            }
        }
    }


    /**
     * App安装升级
     *
     * @param apkPath apk存放地址
     * @param isSys   是否是系统应用
     * @return 操作结果
     */
    public static CommonValue installApk(String apkPath, boolean isSys) {
        return installApk(apkPath, isSys, false);
    }

    /**
     * App安装升级
     *
     * @param apkPath     apk存放地址
     * @param isSys       是否是系统应用
     * @param isRebootNow 是否执行重启
     * @return 操作结果
     */
    public static CommonValue installApk(String apkPath, boolean isSys, boolean isRebootNow) {
        String[] commands = null;
        if (isSys) {
            String[] fileInfo = apkPath.split("/");
            String fileName = fileInfo[fileInfo.length - 1].replace(".apk", "");
            commands = new String[]{
                    "rm -rf /system/priv-app/" + fileName + "*",
                    "cp -rf " + apkPath + " /system/priv-app/",
                    "chmod 777 /system/priv-app/" + fileName + "*",
                    isRebootNow ? FCommonTools.CMD_REBOOT : "",
            };
        } else {
            commands = new String[]{
                    "pm install -rf " + apkPath,
                    isRebootNow ? FCommonTools.CMD_REBOOT : "",
            };
        }
        FCmdTools.CommandResult commandResult = FCmdTools.execCommand(commands, true);
        if (commandResult.result == 0) {
            return CommonValue.EXEU_COMPLETE;
        } else {
            if (commandResult.errorMsg != null && commandResult.errorMsg.contains("Read-only file system")) {
                return CommonValue.CMD_READ_ONLY;
            } else {
                return CommonValue.EXEU_UNKNOWN_ERR;
            }
        }
    }

    /**
     * 获取U盘tf等ota包路径
     *
     * @return 存在update.zip的路径 并且这个update.zip这个文件大于100M
     */
    public static List<String> getDeviceUpdatePathList() {
        List<String> filePath = new ArrayList<>();
        int sdk = Build.VERSION.SDK_INT;
        String fileRootPath = "";
        File file = null;
        if (sdk >= 24) {
            fileRootPath = "/storage/";
            file = new File(fileRootPath);
            File[] fDevicesList = file.listFiles();
            if (fDevicesList != null && fDevicesList.length > 0) {
                for (int i = 0; i < fDevicesList.length; i++) {
                    if (!fDevicesList[i].getName().equals("emulated") && !fDevicesList[i].getName().equals("self")) {
                        File[] fDeviceInfo = fDevicesList[i].listFiles();
                        for (int j = 0; j < fDeviceInfo.length; j++) {
                            if (fDeviceInfo[j].getName().equals("update.zip") && fDeviceInfo[j].length() > 104857600) {
                                //文件大于一百兆 并且名称为update.zip
                                filePath.add(fDeviceInfo[j].getAbsolutePath());
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            fileRootPath = "/mnt/media_rw/";//extsd udisk
            file = new File(fileRootPath);
            File[] fDevicesList = file.listFiles();
            if (fDevicesList != null && fDevicesList.length > 0) {
                for (int i = 0; i < fDevicesList.length; i++) {
                    if (fDevicesList[i].getName().equals("sdisk") && fDevicesList[i].getName().equals("udisk")) {
                        File[] fDeviceInfo = fDevicesList[i].listFiles();
                        for (int j = 0; j < fDeviceInfo.length; j++) {
                            if (fDeviceInfo[j].getName().equals("update.zip") && fDeviceInfo[j].length() > 104857600) {
                                //文件大于一百兆 并且名称为update.zip
                                filePath.add(fDeviceInfo[j].getAbsolutePath());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return filePath;
    }


}
