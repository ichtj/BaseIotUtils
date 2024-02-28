package com.ichtj.basetools.network;

import android.content.Intent;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ShellUtils;

public class NetMtools {
    public static final int MODE_HARD = 0;//------0为硬复位
    public static final int MODE_SOFT = 1;//------1为软复位
    public static final int MODE_AIRPLANE = 2;//--2为飞行模式
    public static final int MODE_REBOOT = 3;//----3为纯重启模式
    public final static String LOG_PATH = "/sdcard/Documents/";
    public static final String KEY_ERR_COUNT = "netMonitorErrCount";
    public static final String KEY_RESET_MOED = "netResetMode";
    public static final String KEY_TOTAL_COUNT = "totalCount";
    public static final String KEY_TIME_ACHIEVE = "timeAchieve";
    public static final String KEY_CYCLES_BREAK = "cyclesbreak";
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
}
