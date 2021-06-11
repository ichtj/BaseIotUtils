package com.future.xlink.mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.future.xlink.bean.InitParams;
import com.future.xlink.bean.Register;
import com.future.xlink.bean.common.ConnectType;
import com.future.xlink.logs.Log4J;
import com.future.xlink.utils.Carrier;
import com.future.xlink.utils.GlobalConfig;
import com.future.xlink.utils.Utils;
import com.future.xlink.utils.XBus;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;

/**
 * 管理mqtt的连接,发布,订阅,断开连接, 断开重连等操作
 */

public class MqttManager {
    private final Class TAG = MqttManager.class;
    // 单例
    private static MqttManager mInstance = null;
    // 回调
    private MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private Context context;
    private InitParams params;
    private boolean isInitconnect = false;// 是否初始化重连
    private MqttCallback mCallback;

    private MqttManager() {
        mCallback = new MqttCallbackBus();
    }


    public static MqttManager getInstance() {
        if (mInstance == null) {
            synchronized (MqttManager.class) {
                if (mInstance == null) {
                    mInstance = new MqttManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 创建Mqtt 连接
     *
     * @return
     */

    public void creatConnect(Context context, InitParams params, Register register) {
        this.context = context;
        this.params = params;
        isInitconnect = true;
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        try {
            conOpt = MqConnectionFactory.getMqttConnectOptions(params, register);
            //解析注册时服务器返回的用户名密码 如果解析异常 ，可能是无权限
            if(conOpt.getUserName()==null||conOpt.getPassword()==null||conOpt.getUserName().equals("")||conOpt.getPassword().equals("")){
                XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECTED, ConnectType.CONNECT_NO_PERMISSION));
                //删除配置文件
                String path = GlobalConfig.SYS_ROOT_PATH + Utils.getPackageName(context) + File.separator + params.sn + File.separator + GlobalConfig.MY_PROPERTIES;
                boolean isDel = new File(path).delete();
                Log.d(TAG.getName(), "creatConnect: isDel my.properties=" + isDel);
                return;
            }
            // Construct an MQTT blocking mode client ;clientId需要修改为设备sn
            client = new MqttAndroidClient(context, register.mqttBroker, params.sn, dataStore);
            Log4J.info(getClass(), "creatConnect", "client id=" + client.getClientId() + ",dataStore=" + tmpDir);
            // Set this wrapper as the callback handler
            client.setCallback(mCallback);
            connAndListener(context);
        } catch (Exception e) {
            e.printStackTrace();
            Log4J.crash(getClass(), "creatConnect", e);
        }
    }

    /**
     * 建立连接 并监听连接回调
     * 连接结果将在 iMqttActionListener中进行回调 使用旧连接
     */
    public void connAndListener(Context context) {
        try {
            if (client != null && !client.isConnected()) {
                IMqttToken itoken = client.connect(conOpt, context, iMqttActionListener);
                Log4J.info(TAG, "connAndListener", "Waiting for connection to complete！");
                //阻止当前线程，直到该令牌关联的操作完成
                itoken.waitForCompletion();
                Log4J.info(TAG, "connAndListener", "Connected to " + client.getServerURI() + " with client ID " + client.getClientId() + " connected==" + client.isConnected());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log4J.crash(TAG, "connAndListener", e);
        }
    }

    public void doConntect(Context context, InitParams params, Register register) {
        if (client == null) {
            //client为空时代表需要重新建立连接
            creatConnect(context, params, register);
        } else {
            //client不为空时表明 利用原有的连接信息
            this.context = context;
            connAndListener(context);
        }
    }

    public boolean isConnect() {
        if (client != null) {
            return client.isConnected();
        }
        return false;
    }

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log4J.info(TAG, "onSuccess", "connection onSuccess");
            DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
            disconnectedBufferOptions.setBufferEnabled(params.bufferEnable);
            disconnectedBufferOptions.setBufferSize(params.bufferSize);
            disconnectedBufferOptions.setPersistBuffer(false);
            disconnectedBufferOptions.setDeleteOldestMessages(false);
            if (client != null)
                client.setBufferOpts(disconnectedBufferOptions);
            XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECTED, ConnectType.CONNECT_SUCCESS));
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            Log4J.info(TAG, "onFailure", "onFailure-->" + arg1.getMessage());
            if (params.automaticReconnect) {
                //只在客户端主动创建初始化连接时回调
                if (isInitconnect) {
                    if (arg1.getMessage().contains("无权连接")) {
                        try {
                            //1.可能是此设备在其他产品中 2.或者设备已被删除 3.该sn未添加到平台
                            XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECTED, ConnectType.CONNECT_NO_PERMISSION));
                            //删除配置文件
                            String path = GlobalConfig.SYS_ROOT_PATH + Utils.getPackageName(context) + File.separator + params.sn + File.separator + GlobalConfig.MY_PROPERTIES;
                            boolean isDel = new File(path).delete();
                            Log.d(TAG.getName(), "onFailure: isDel my.properties=" + isDel);
                        } catch (Exception e) {
                            Log4J.crash(TAG, "onFailure", e);
                        }
                    } else {
                        XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECTED, ConnectType.CONNECT_FAIL));
                    }
                }
            } else {
                XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECTED, ConnectType.CONNECT_FAIL));
            }
        }
    };

    /**
     * Publish / send a message to an MQTT server
     *
     * @param topicName the name of the topic to publish to
     * @param qos       the quality of service to delivery the message at (0,1,2)
     * @param payload   the set of bytes to send to the MQTT server
     * @return boolean
     */

    public boolean publish(String topicName, int qos, byte[] payload) {
        boolean flag = false;
        //有消息发送之后，isInitconnect 状态设置为false,系统重连之后不再回调onFailure
        isInitconnect = false;
        if (client != null && client.isConnected()) {
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            Log4J.info(TAG, "publish", "Publishing to topic \"" + topicName + "\" qos " + qos + ",messageId=" + message.getId());
            // Send the message to the server, control is not returned until
            // it has been delivered to the server meeting the specified
            // quality of service.
            try {
                client.publish(topicName, message);
                flag = true;
            } catch (MqttException e) {
            }
        } else {
            Log.d(TAG.getSimpleName(), "publish: client == null && !client.isConnected() || !isConnectIsNormal(context)");
        }
        return flag;
    }


    /**
     * Subscribe to a topic on an MQTT server
     * <p>
     * Once subscribed this method waits for the messages to arrive from the server
     * <p>
     * that match the subscription. It continues listening for messages until the enter key is
     * <p>
     * pressed.
     *
     * @param topicName to subscribe to (can be wild carded)
     * @param qos       the maximum quality of service to receive messages at for this subscription
     * @return boolean
     */

    public boolean subscribe(String topicName, int qos) {
        boolean flag = false;
        if (client != null && client.isConnected()) {
            Log4J.info(TAG, "subscribe", "subscribe" + "Subscribing to topic \"" + topicName + "\" qos " + qos);
            try {
                client.subscribe(topicName, qos);
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return flag;

    }

    /**
     * 释放单例, 及其所引用的资源
     * 连接主动断开
     * 重新连接时
     */
    public void release() {
        if (mInstance != null) {
            mInstance.disConnect();
            mInstance = null;
        }
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        try {
            if (client != null && client.isConnected()) {
                Log4J.info(TAG, "release", "Released the mqtt connection");
                client.unregisterResources();
                client.close();
                client = null;
                conOpt = null;
                context = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log4J.crash(MqttManager.class, "release", e);
        }
    }

}