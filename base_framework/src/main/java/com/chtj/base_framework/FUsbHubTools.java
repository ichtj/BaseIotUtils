package com.chtj.base_framework;

import android.content.Context;
import android.os.Build;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;

import com.chtj.base_framework.entity.EhciInfo;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FUsbHubTools {
    private static final String TAG = "FUsbHubTools";

    /**
     * 获取usbhub中的usb设备信息
     *
     * @return
     */
    public static List<EhciInfo> getUsbInfo() {
        List<EhciInfo> ehciInfoList = new ArrayList<>();
        int sdk = Build.VERSION.SDK_INT;
        //先获取usb设备的路径信息 用于遍历出来里面的设备 因为存在多个usbhub
        List<String> searchPathList = new ArrayList<>();
        String rootPath = "/sys/devices/platform/";
        File file = new File(rootPath);
        File[] files = file.listFiles();
        Pattern patternFirst = null;
        Pattern rkPattern1 = Pattern.compile("ff[0-9]*.usb");//填写正则表达式 ff500000.usb
        Pattern freeScalePattern1 = Pattern.compile("[a-z]*-ehci.1");//填写正则表达式 fsl-ehci.1
        Pattern pattern = Pattern.compile("[0-9]-[0-9]");//填写正则表达式 n-n
        Pattern pattern2 = Pattern.compile("[0-9]-[0-9](.\\d+)?");//填写正则表达式 一级hub
        Pattern pattern3 = Pattern.compile("[0-9]-[0-9].[0-9](.\\d+)?");//填写正则表达式 二级hub|[0-9]-[0-9].[0-9]:[0-9](.\d+)?
        if (sdk >= 24) { patternFirst = rkPattern1; } else { patternFirst = freeScalePattern1; }
        for (int i = 0; i < files.length; i++) {
            if (patternFirst.matcher(files[i].getName()).matches()) {
                File[] ehciFileInfo = files[i].listFiles();
                for (int j = 0; j < ehciFileInfo.length; j++) {
                    if (ehciFileInfo[j].getName().indexOf("usb1") != -1 || ehciFileInfo[j].getName().indexOf("usb2") != -1) {
                        File[] usbNumInfo = ehciFileInfo[j].listFiles();
                        for (int k = 0; k < usbNumInfo.length; k++) {
                            Matcher match = pattern.matcher(usbNumInfo[k].getName());//2-1
                            boolean isExist = match.matches();
                            if (isExist) {
                                File[] usbInfo = usbNumInfo[k].listFiles();
                                for (int l = 0; l < usbInfo.length; l++) {
                                    if (pattern2.matcher(usbInfo[l].getName()).matches()) {//2-1.7
                                        boolean levelTwohasData = false;
                                        //得到改文件夹中目录
                                        File[] level2Info = usbInfo[l].listFiles();
                                        if (level2Info != null && level2Info.length > 0) {
                                            //便利文件夹中的目录文件内容
                                            for (int m = 0; m < level2Info.length; m++) {
                                                //判断是否存在我们需要的设备信息 找到规则2-1.7.2
                                                Matcher match3 = pattern3.matcher(level2Info[m].getName());
                                                boolean isExist3 = match3.matches();
                                                if (isExist3) {
                                                    if (levelTwohasData == false) {
                                                        levelTwohasData = true;
                                                    }
                                                    searchPathList.add(level2Info[m].getAbsolutePath());
                                                }
                                            }
                                        }
                                        searchPathList.add(usbInfo[l].getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (searchPathList != null && searchPathList.size() > 0) {
            for (int i = 0; i < searchPathList.size(); i++) {
                FCmdTools.CommandResult commandResult1 = FCmdTools.execCommand("cat " + searchPathList.get(i) + "/product", true);
                FCmdTools.CommandResult commandResult2 = FCmdTools.execCommand("cat " + searchPathList.get(i) + "/manufacturer", true);
                if (commandResult1.result == 0 && commandResult2.result == 0 && commandResult1.successMsg.indexOf("Android") == -1 && commandResult2.successMsg.indexOf("Android") == -1) {
                    ehciInfoList.add(new EhciInfo(commandResult1.successMsg, commandResult2.successMsg, searchPathList.get(i)));
                }
            }
        }
        return ehciInfoList;
    }
}
