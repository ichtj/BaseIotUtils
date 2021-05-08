//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import java.util.Iterator;
import org.eclipse.paho.client.mqttv3.MqttMessage;

interface MessageStore {
    String storeArrived(String var1, String var2, MqttMessage var3);

    boolean discardArrived(String var1, String var2);

    Iterator<MessageStore.StoredMessage> getAllArrivedMessages(String var1);

    void clearArrivedMessages(String var1);

    void close();

    public interface StoredMessage {
        String getMessageId();

        String getClientHandle();

        String getTopic();

        MqttMessage getMessage();
    }
}
