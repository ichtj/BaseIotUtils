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
    private MqttCallback mCallback;
    private MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private Context context;
    private InitParams params;
    private boolean isInitconnect = false;// 是否初始化重连


    private MqttManager() {
        mCallback = new MqttCallbackBus();
    }


    public static MqttManager getInstance() {
        if (null == mInstance) {
            mInstance = new MqttManager();
        }
        return mInstance;
    }



    /**
     * 检查网络是否正常
     *
     * @param context
     * @return
     */
    protected boolean isConnectIsNormal(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected() && info.isAvailable()) {
            String name = info.getTypeName();
            //Log4J.info(TAG, "isConnectIsNormal", "isConnectIsNormal MQTT Current network name：" + name + "type-->" + Utils.GetNetworkType(info));
            return true;
        } else {
            //Log4J.info(TAG, "isConnectIsNormal", "isConnectIsNormal MQTT No network available");
            return false;
        }
    }

    /**
     * 创建Mqtt 连接
     *
     * @return
     */

    public boolean creatConnect(Context context, InitParams params, Register register) {
        Log.d(TAG.getSimpleName(), "doConntect: use old client");
        this.context = context;
        this.params = params;
        boolean flag = false;
        isInitconnect = true;
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        try {
            conOpt = MqConnectionFactory.getMqttConnectOptions(params, register);
            // Construct an MQTT blocking mode client ;clientId需要修改为设备sn
            client = new MqttAndroidClient(context, register.mqttBroker, params.sn, dataStore);
            // Set this wrapper as the callback handler
            client.setCallback(mCallback);
            flag = doConnect(context);//todo
            Log.d(TAG.getSimpleName(), "creatConnect: flag=" + flag);
        } catch (Exception e) {
            e.printStackTrace();
            Log4J.crash(getClass(), "creatConnect", e);
            boolean isNetConn = isConnectIsNormal(context);
            Log.d(TAG.getSimpleName(), "creatConnect: errMeg=" + e.getMessage() + ",isNetConn==" + isNetConn);
            //XBus.post(new Carrier(Carrier.TYPE_REMOTE_TIME_OUT, ConnectType.CONNECT_RESPONSE_TIMEOUT));
        }
        return flag;
    }

    /**
     * 建立连接
     *
     * @return
     */

    public boolean doConnect(Context context) throws MqttException {
        Log.d(TAG.getSimpleName(), "doConntect: use old client");
        boolean flag = false;
        //&&isConnectIsNormal(context)
        if (client != null && !client.isConnected()) {
            IMqttToken itoken = client.connect(conOpt, context, iMqttActionListener);
            itoken.waitForCompletion();
            Log4J.info(TAG, "doConnect", "doConnect" + "Connected to " + client.getServerURI() + " with client ID " + client.getClientId() + "connected==" + client.isConnected());
            flag = true;

        }
        return flag;
    }

    public boolean doConntect(Context context, InitParams params, Register register) throws MqttException {
        if (client == null) {
            //client为空时代表需要重新建立连接
            return creatConnect(context, params, register);
        } else {
            //client不为空时表明 利用原有的连接信息
            this.context = context;
            return doConnect(context);
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
            Log.d(TAG.getSimpleName(), "onFailure: params.automaticReconnect=" + params.automaticReconnect + ",isInitconnect=" + isInitconnect);
            if (params.automaticReconnect) {
                //只在客户端主动创建初始化连接时回调
                if (isInitconnect) {
                    if(arg1.getMessage().contains("无权连接")){
                        //可能是此设备在其他产品中，或者设备已被删除 本地的缓存需要重新生成
                        XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECTED, ConnectType.CONNECT_NO_PERMISSION));
                        try {
                            //删除配置文件
                            String path=GlobalConfig.SYS_ROOT_PATH+ Utils.getPackageName(context) + File.separator + params.sn + File.separator+GlobalConfig.MY_PROPERTIES;
                            boolean isDel=new File(path).delete();
                            Log.d(TAG.getName(), "onFailure: isDel="+isDel);
                        }catch (Exception e){
                            Log.d(TAG.getName(), "onFailure: errMeg="+e.getMessage());
                        }
                    }else{
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
        isInitconnect = false;//isConnectIsNormal(context) 判断网络是否正常然后在发送 不然没有意义
        //Log.d(TAG.getSimpleName(), "publish: data");
        if (client != null && client.isConnected() && isConnectIsNormal(context)) {
            Log4J.info(TAG, "publish", "Publishing to topic \"" + topicName + "\" qos " + qos);
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
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
            // Subscribe to the requested topic
            // The QoS specified is the maximum level that messages will be sent to the client at.
            // For instance if QoS 1 is specified, any messages originally published at QoS 2 will
            // be downgraded to 1 when delivering to the client but messages published at 1 and 0
            // will be received at the same level they were published at.

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
     */

    public void release() {
        try {
            if (client != null && client.isConnected()) {
                Log4J.info(TAG, "release", "释放了mqtt连接");
                conOpt = null;
                context = null;
                client.disconnect();
                client.unregisterResources();
                client.close();
                //Log4J.info(TAG, "disConnect", "取消了mqtt连接" + (client == null));
                client = null;
            }
            if (mInstance != null) {
                mInstance = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Log4J.crash(MqttManager.class, "release", e);
        }
    }
}