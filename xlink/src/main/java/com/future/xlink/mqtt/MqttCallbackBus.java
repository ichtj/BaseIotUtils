package com.future.xlink.mqtt;


import android.util.Log;

import com.future.xlink.utils.Carrier;
import com.future.xlink.utils.XBus;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCallbackBus implements MqttCallbackExtended {
    private static final String TAG = "MqttCallbackBus";
    /**
     * 连接丢失后 回调
     *
     * @param cause
     */
    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost " + cause != null ? cause.getMessage() : "");
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
        Log.d(TAG, "connectComplete reconnect ==" + reconnect + "     serverURI==" + serverURI);
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
        Log.d(TAG, "messageArrived" + topic + "====" + message.toString());
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
            Log.d(TAG, "deliveryComplete token isComplete=" + isComplete + ",errMeg=" + (isComplete ? "" : token.getException().toString()));
            Log.d(TAG, "deliveryComplete token message=" + token.getMessage().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "deliveryComplete errMeg=" + e.toString());
        }
    }


}