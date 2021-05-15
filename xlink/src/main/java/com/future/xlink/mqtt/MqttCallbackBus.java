package com.future.xlink.mqtt;



import com.future.xlink.logs.Log4J;
import com.future.xlink.utils.Carrier;
import com.future.xlink.utils.XBus;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCallbackBus implements MqttCallbackExtended {
    private  final  Class TAG=MqttCallbackBus.class;

    @Override
    public void connectionLost(Throwable cause) {
        Log4J.info(TAG, "connectionLost", cause!=null?cause.getMessage():"");
        XBus.post(new Carrier(Carrier.TYPE_MODE_CONNECT_LOST,cause));
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log4J.info(TAG, "connectComplete", "reconnect =="+reconnect+"     serverURI=="+serverURI);
        XBus.post(new Carrier(Carrier.TYPE_MODE_RECONNECT_COMPLETE,serverURI,reconnect));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Log4J.info(TAG, "messageArrived", topic + "====" + message.toString());
        XBus.post(new Carrier(Carrier.TYPE_REMOTE_RX,topic,message));
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log4J.info(TAG, "deliveryComplete", "token   =====" + token.getMessageId());
    }


}