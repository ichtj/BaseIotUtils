package com.wave_chtj.example.network;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.chtj.base_framework.FBaseTools;
import com.chtj.base_framework.network.NetDbmListener;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.network.NetUtils;

import java.lang.reflect.Method;

public class NetMonitorUtils {
    //硬复位指令
    public static final String[] CMD_HARD_RESET = new String[]{
            "echo 1 > /dev/lte_state",//新固件地址
            "echo 1 > /sys/devices/platform/imx6q_sim/sim_sysfs/state"/*,*///旧固件地址
            /*"svc data enable"*/
    };
    //软复位指令
    public static final String[] CMD_SOFT_RESET = new String[]{
            "echo \"AT+QPOWD=1\\r\" > /dev/ttyUSB2",//EC20
            "setprop rild.simcom.reboot 1"/*,*///SIMCOM
            /*"svc data enable"*/
    };

    //软复位指令
    public static final String CMD_START_RILL = "start ril-daemon";
    public static final String CMD_STOP_RILL = "stop ril-daemon";



    //AirplaneMode on
    public static final String[] CMD_ARIPLANEMODE_ON=new String[]{"settings put global airplane_mode_on 1","am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
    //AirplaneMode off
    public static final String[] CMD_ARIPLANEMODE_OFF=new String[]{"settings put global airplane_mode_on 0","am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"};

    /**
     * dns列表
     */
    public static final String[] DNS_LIST = new String[]{
            "114.114.114.114", "223.5.5.5", "223.6.6.6", "180.76.76.76"/*, "8.8.8.8"*/,
            "114.114.115.115", "119.29.29.29", "210.2.4.8","182.254.116.116"/*, "9.9.9.9"*/, "199.91.73.222",
            "101.226.4.6", "1.2.4.8"};


    /**
     * 判断网络是否异常
     */
    public static boolean checkNetWork(String[] dnsList, int count, int deadline) {
        for (String pingAddr : dnsList) {
            if (NetUtils.ping(pingAddr, count, deadline)) {
                //If it is abnormal when entering the program network at the beginning, then only prompt once
                return true;
            }
        }
        return false;
    }
    /**
     * 获取指定数量的dns列表
     * @param randomSize 数量
     */
    public static String[] getPingDns(int randomSize,String [] availableList){
        //每次随机获取三个dns进行网络判断 其中一个通过 那么跳出循环
        int[] randomArray = randomCommon(0, availableList.length - 1, randomSize);
        String[] dns = new String[randomArray.length];
        for (int i = 0; i < randomArray.length; i++) {
            dns[i] = availableList[randomArray[i]];
        }
        return dns;
    }

    /**
     * 重启系统
     */
    public static void rebootSystem() {
        try {
            //adb命令进行重启
            ShellUtils.execCommand("reboot",true);
            //调用系统接口进行重启
            Intent intent = new Intent(Intent.ACTION_REBOOT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BaseIotUtils.getContext().startActivity(intent);
        } catch (Throwable ex) {
            KLog.e(ex);
        }
    }
    /**
     * 最简单最易理解的两重循环去重
     * 随机指定范围内N个不重复的数
     *
     * @param min        指定范围最小值
     * @param max        指定范围最大值
     * @param randomSize 随机数个数
     */
    public static int[] randomCommon(int min, int max, int randomSize) {
        if (randomSize > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[randomSize];
        int count = 0;
        while (count < randomSize) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < randomSize; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }
}
