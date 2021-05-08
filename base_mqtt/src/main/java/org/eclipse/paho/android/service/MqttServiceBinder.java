//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import android.os.Binder;

class MqttServiceBinder extends Binder {
    private MqttService mqttService;
    private String activityToken;

    MqttServiceBinder(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    public MqttService getService() {
        return this.mqttService;
    }

    void setActivityToken(String activityToken) {
        this.activityToken = activityToken;
    }

    public String getActivityToken() {
        return this.activityToken;
    }
}
