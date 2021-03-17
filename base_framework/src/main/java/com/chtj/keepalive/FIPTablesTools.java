package com.chtj.keepalive;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * iptables 网络访问管理
 * 开启网络 禁用网络等
 */
public class FIPTablesTools {
    private static final String TAG = "FIPTablesTools";

    /**
     * 根据包名获得程序UID
     *
     * @param context 上下文
     * @param apkPkg  包名
     * @return uid
     */
    private static int getUid(Context context, String apkPkg) {
        PackageManager pManager = context.getPackageManager();
        int uid = -1;
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            Log.d(TAG, "getUid: pkg="+pak.applicationInfo.packageName+",uid="+pak.applicationInfo.uid);
            //判断是否为非系统预装的应用程序
            if (apkPkg.equals(pak.applicationInfo.packageName)) {
                // customs applications
                uid = pak.applicationInfo.uid;
                break;
            }
        }
        return uid;
    }

    /**
     * 根据uid判断规则是否添加过
     *
     * @param uid 用户名
     * @return 是否已添加
     */
    private static boolean isAdded(int uid) {
        String[] cmdArry = new String[]{
                "iptables -L OUTPUT -nv  --line-number"
        };
        FCmdTools.CommandResult result = FCmdTools.execCommand(cmdArry, true);
        if (result.result == 0 && result.successMsg.indexOf("owner UID match " + uid) != -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取uid对应得output链中的下标
     *
     * @param uid 用户id
     * @return position
     */
    public static int getOutPutRulePostion(int uid) {
        String[] cmdArry = new String[]{
                "iptables -L OUTPUT -nv  --line-number"
        };
        FCmdTools.CommandResult result = FCmdTools.execCommand(cmdArry, true);
        Log.d(TAG, "getOutPutRulePostion: result.successMsg="+result.successMsg);
        if (result.result == 0) {
            int position = -1;
            Pattern pattern = Pattern.compile("owner UID match [0-9]{5}");
            Matcher matcher = pattern.matcher(result.successMsg);
            int count = 0;
            boolean isExist = false;
            while (matcher.find()) {
                Log.d(TAG, "getOutPutRulePostion: result=" + matcher.group());
                if (matcher.group().indexOf("" + uid) != -1) {
                    position = count;
                    isExist = true;
                    break;
                }
                count++;
            }
            if (isExist) {
                return position + 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * 根据包名获取uid后添加规则
     *
     * @param context 上下文
     * @param pkgName 包名
     * @return 是否添加成功
     */
    public static boolean putDisableRule(Context context, String pkgName) {
        int uid = getUid(context, pkgName);
        return putDisableRule(uid);

    }

    /**
     * 根据uid后添加规则
     *
     * @return 是否添加成功
     */
    public static boolean putDisableRule(int uid) {
        if (!isAdded(uid)) {
            String[] cmdArray = new String[]{
                    "iptables -N " + uid,
                    "iptables -A OUTPUT -p all -m owner --uid-owner " + uid + " -j DROP"
            };
            FCmdTools.CommandResult result = FCmdTools.execCommand(cmdArray, true);
            if (result.result == 0) {
                return true;
            } else {
                Log.d(TAG, "putDisableRule: errMeg=" + result.errorMsg);
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     * 删除指定uid的规则
     *
     * @param context 上下文
     * @param apkPkg  apk包名
     * @return 添加规则是否成功true|false
     */
    public static boolean clearRule(Context context, String apkPkg) {
        int uid = getUid(context, apkPkg);
        return clearRule(uid);
    }

    /**
     * 删除指定uid的规则
     *
     * @return 添加规则是否成功true|false
     */
    public static boolean clearRule(int uid) {
        int position = getOutPutRulePostion(uid);
        Log.d(TAG, "clearRule: position=" + position);
        if (position != -1) {
            String[] cmdArray = new String[]{
                    "iptables -D OUTPUT " + position
            };
            FCmdTools.CommandResult result = FCmdTools.execCommand(cmdArray, true);
            if (result.result == 0) {
                return true;
            } else {
                Log.d(TAG, "putDisableRule: errMeg=" + result.errorMsg);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 删除所有规则
     *
     * @return 删除规则是否成功true|false
     */
    public static boolean clearAllRule() {
        String[] cmdArray = new String[]{
                "iptables -F "
        };
        FCmdTools.CommandResult result = FCmdTools.execCommand(cmdArray, true);
        if (result.result == 0) {
            return true;
        } else {
            Log.d(TAG, "putDisableRule: errMeg=" + result.errorMsg);
            return false;
        }
    }
}
