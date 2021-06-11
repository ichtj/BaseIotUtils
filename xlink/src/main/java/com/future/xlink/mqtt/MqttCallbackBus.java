package com.future.xlink.mqtt;


import android.util.Log;

import com.future.xlink.logs.Log4J;
import com.future.xlink.utils.Carrier;
import com.future.xlink.utils.XBus;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCallbackBus implements MqttCallbackExtended {
    private final Class TAG = MqttCallbackBus.class;

    /**
     * 连接丢失后 回调
     *
     * @param cause
     */
    @Override
    public void connectionLost(Throwable cause) {
        Log4J.info(TAG, "connectionLost", cause != null ? cause.getMessage() : "");
        XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECT_LOST, cause));
    }

    /**
     * 连接完成后 回调
     *
     * @param reconnect
     * @param serverURI
     */
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log4J.info(TAG, "connectComplete", "reconnect ==" + reconnect + "     serverURI==" + serverURI);
        XBus.post(new Carrier(Carrier.TYPE_MODE_RECONNECT_COMPLETE, serverURI, reconnect));
    }

    /**
     * 消息到达后 回调
     *
     * @param topic
     * @param message
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Log4J.info(TAG, "messageArrived", topic + "====" + message.toString());
        XBus.post(new Carrier(Carrier.TYPE_REMOTE_RX, topic, message));
    }

    /**
     * 消息发送成功后 回调
     *
     * @param token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            boolean isComplete = token.isComplete();
            Log4J.info(TAG, "deliveryComplete", "token isComplete=" + isComplete + ",errMeg=" + (isComplete ? "" : token.getException().toString()));
            Log4J.info(TAG, "deliveryComplete", "token message=" + token.getMessage().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log4J.info(TAG, "deliveryComplete", "errMeg=" + e.toString());
        }
    }


}