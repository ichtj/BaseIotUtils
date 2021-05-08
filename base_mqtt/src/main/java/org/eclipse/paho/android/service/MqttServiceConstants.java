//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

interface MqttServiceConstants {
    String VERSION = "v0";
    String DUPLICATE = "duplicate";
    String RETAINED = "retained";
    String QOS = "qos";
    String PAYLOAD = "payload";
    String DESTINATION_NAME = "destinationName";
    String CLIENT_HANDLE = "clientHandle";
    String MESSAGE_ID = "messageId";
    String SEND_ACTION = "send";
    String UNSUBSCRIBE_ACTION = "unsubscribe";
    String SUBSCRIBE_ACTION = "subscribe";
    String DISCONNECT_ACTION = "disconnect";
    String CONNECT_ACTION = "connect";
    String CONNECT_EXTENDED_ACTION = "connectExtended";
    String MESSAGE_ARRIVED_ACTION = "messageArrived";
    String MESSAGE_DELIVERED_ACTION = "messageDelivered";
    String ON_CONNECTION_LOST_ACTION = "onConnectionLost";
    String TRACE_ACTION = "trace";
    String CALLBACK_TO_ACTIVITY = "MqttService.callbackToActivity.v0";
    String CALLBACK_ACTION = "MqttService.callbackAction";
    String CALLBACK_STATUS = "MqttService.callbackStatus";
    String CALLBACK_CLIENT_HANDLE = "MqttService.clientHandle";
    String CALLBACK_ERROR_MESSAGE = "MqttService.errorMessage";
    String CALLBACK_EXCEPTION_STACK = "MqttService.exceptionStack";
    String CALLBACK_INVOCATION_CONTEXT = "MqttService.invocationContext";
    String CALLBACK_ACTIVITY_TOKEN = "MqttService.activityToken";
    String CALLBACK_DESTINATION_NAME = "MqttService.destinationName";
    String CALLBACK_MESSAGE_ID = "MqttService.messageId";
    String CALLBACK_RECONNECT = "MqttService.reconnect";
    String CALLBACK_SERVER_URI = "MqttService.serverURI";
    String CALLBACK_MESSAGE_PARCEL = "MqttService.PARCEL";
    String CALLBACK_TRACE_SEVERITY = "MqttService.traceSeverity";
    String CALLBACK_TRACE_TAG = "MqttService.traceTag";
    String CALLBACK_TRACE_ID = "MqttService.traceId";
    String CALLBACK_ERROR_NUMBER = "MqttService.ERROR_NUMBER";
    String CALLBACK_EXCEPTION = "MqttService.exception";
    String PING_SENDER = "MqttService.pingSender.";
    String PING_WAKELOCK = "MqttService.client.";
    String WAKELOCK_NETWORK_INTENT = "MqttService";
    String TRACE_ERROR = "error";
    String TRACE_DEBUG = "debug";
    String TRACE_EXCEPTION = "exception";
    int NON_MQTT_EXCEPTION = -1;
}
