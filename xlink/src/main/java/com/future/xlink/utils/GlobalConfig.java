package com.future.xlink.utils;

import java.io.File;

public class GlobalConfig {

    /**
     * Log4j
     */
    public static String LOG_INFO_NAME="info.log";
    public static String LOG_ERROR_NAME="error.log";
    public static final String SYS_ROOT_PATH = "/sdcard" + File.separator + "xlink" + File.separator;
    public static String PATH_LOG_INFO = File.separator + "log" + File.separator + "info" + File.separator + LOG_INFO_NAME;
    public static String PATH_LOG_ERROR = File.separator + "log" + File.separator + "error" + File.separator +LOG_ERROR_NAME;
    public static String PROPERT_URL = "";
    public static final String PATH_LOG_SUFFIX = ".log";
    public static final String MY_PROPERTIES = "my.properties"; //参数存储文件

    public static final String HTTP_SERVER = "http://iot.frozenmoment.cn:10130/"; //生产测试环境
    public static final String AGENT_SERVER_LIST = "api/iot/reg/device/servers"; //代理服务端地址
    public static final String AGENT_REGISTER = "/api/iot/reg/device/register"; //注册服务器
    public static final String PRODUCT_UNIQUE = "api/iot/reg/device/unique"; //设备sn唯一性验证
    public static final String UPLOAD_LOGURL = "api/iot/reg/device/uploadLogUrl"; //获取上传日志信息

    public static int MQTT_OutTime = 10;   //设置超时时间，单位：秒
    public static int MQTT_KeepAliveTime = 20;  //心跳包发送间隔，单位：秒
    public static final int OVER_TIME = 10 * 60 * 1000; //消息处理超时，默认30分钟
    public static int MQTT_RECONNECT_TIME = 10; //默认自动超时反馈时长 单位：分钟
    /**
     * 设置阿里云的 AccessKey，用于鉴权
     */
    public static final String MQTT_ACESS_KEY = "LTAIaJSHZCG80Qo1";
}
