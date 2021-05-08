//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class MqttDeliveryTokenAndroid extends MqttTokenAndroid implements IMqttDeliveryToken {
    private MqttMessage message;

    MqttDeliveryTokenAndroid(MqttAndroidClient client, Object userContext, IMqttActionListener listener, MqttMessage message) {
        super(client, userContext, listener);
        this.message = message;
    }

    public MqttMessage getMessage() throws MqttException {
        return this.message;
    }

    void setMessage(MqttMessage message) {
        this.message = message;
    }

    void notifyDelivery(MqttMessage delivered) {
        this.message = delivered;
        super.notifyComplete();
    }
}
