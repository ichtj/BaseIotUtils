package com.future.xlink;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.future.xlink.bean.InitParams;
import com.future.xlink.bean.Protocal;
import com.future.xlink.bean.common.ConnectLostType;
import com.future.xlink.bean.common.RespType;
import com.future.xlink.bean.mqtt.RespStatus;
import com.future.xlink.listener.MessageListener;
import com.future.xlink.mqtt.MqttManager;
import com.future.xlink.mqtt.RxMqttService;
import com.future.xlink.utils.Carrier;
import com.future.xlink.utils.GlobalConfig;
import com.future.xlink.utils.JckJsonHelper;
import com.future.xlink.utils.PropertiesUtil;
import com.future.xlink.utils.Utils;
import com.future.xlink.utils.XBus;

import java.io.File;
import java.net.ConnectException;

public class XLink {
    /**
     * 单例
     */
    private static XLink mInstance = null;
    /**
     * 上下文参数
     */
    private Context context;
    /**
     * 消息回调接口
     */
    MessageListener listener;
    //Intent intent = null;

    private XLink() {

    }


    public MessageListener getListener() {
        return listener;
    }

    public static XLink getInstance() {
        if (null == mInstance) {
            mInstance = new XLink();
        }
        return mInstance;
    }

    /**
     * xlink初始化
     *
     * @param context  初始化句柄
     * @param params   初始化参数类
     * @param listener 初始化回调函数
     */
    public void init(@NonNull Context context, @NonNull InitParams params, @NonNull MessageListener listener) {
        //日志文件路径设置
        String pkgName = Utils.getPackageName(context);
        GlobalConfig.PATH_LOG_INFO = GlobalConfig.SYS_ROOT_PATH + pkgName + GlobalConfig.PATH_LOG_INFO;
        GlobalConfig.PATH_LOG_ERROR = GlobalConfig.SYS_ROOT_PATH + pkgName + GlobalConfig.PATH_LOG_ERROR;
        GlobalConfig.PROPERT_URL = GlobalConfig.SYS_ROOT_PATH + pkgName + File.separator + params.sn + File.separator;
        createFile(pkgName);//创建info,error日志的存储路径和.log文件my.properties文件
        this.context = context;
        this.listener = listener;
        Intent intent = new Intent(context, RxMqttService.class);
        intent.putExtra(RxMqttService.INIT_PARAM, params);
        context.startService(intent);
    }

    /**
     * 初始化时创建配置文件
     */
    private void createFile(String pkgName) {
        String logInfo = GlobalConfig.SYS_ROOT_PATH + pkgName + File.separator + "log" + File.separator + "info";
        String logError = GlobalConfig.SYS_ROOT_PATH + pkgName + File.separator + "log" + File.separator + "error";
        File infoLogFile = new File(logInfo);
        if (!infoLogFile.exists()) {
            infoLogFile.mkdirs();
        }
        File errLogFile = new File(logError);
        if (!errLogFile.exists()) {
            errLogFile.mkdirs();
        }
        File snConfigPath = new File(GlobalConfig.PROPERT_URL);
        if (!snConfigPath.exists()) {
            snConfigPath.mkdirs();
        }
        File configBySn = new File(GlobalConfig.PROPERT_URL, GlobalConfig.MY_PROPERTIES);
        if (!configBySn.exists()) {
            try {
                configBySn.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File logInfoFile = new File(logInfo, GlobalConfig.LOG_INFO_NAME);
        if (!logInfoFile.exists()) {
            try {
                logInfoFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File logErrorFile = new File(logError, GlobalConfig.LOG_ERROR_NAME);
        if (!logErrorFile.exists()) {
            try {
                logErrorFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建连接函数
     */
    public void connect() {
        XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECT));
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        XBus.post(new Carrier(Carrier.TYPE_MODE_DISCONNECT));
    }


    /**
     * 注销函数，注销后，重连mqtt需要重新调用init()函数
     */
    public void unInit() {
        context.stopService(new Intent(context, RxMqttService.class));
        // 清空保存在文件夹中的数据
        PropertiesUtil.getInstance(context).clearProperties(context);
        this.listener = null;
        this.context = null;
    }

    /**
     * 上报服务属性函数
     *
     * @param protocal 属性消息封装
     */
    public void upService(Protocal protocal) {
        publish(Carrier.TYPE_REMOTE_TX_SERVICE, protocal);
    }

    /**
     * 上报事件函数
     *
     * @param protocal 事件消息封装
     */
    public void upEvent(Protocal protocal) {
        publish(Carrier.TYPE_REMOTE_TX_EVENT, protocal);
    }

    /**
     * 代理服务端请求响应事件
     *
     * @param protocal
     */
    public void upResponse(Protocal protocal) {
        publish(Carrier.TYPE_REMOTE_TX, protocal);
    }

    private void publish(int type, Protocal protocal) {
        if (MqttManager.getInstance().isConnect())
            XBus.post(new Carrier(type, protocal));
        else {
            if (this.listener != null) {
                protocal.rx = JckJsonHelper.toJson(new RespStatus(RespType.RESP_CONNECT_LOST.getTye(), RespType.RESP_CONNECT_LOST.getValue()));
                this.listener.messageArrived(protocal);
            }
        }
    }
}
