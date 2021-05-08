//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.paho.android.service.MessageStore.StoredMessage;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

class MqttConnection implements MqttCallbackExtended {
    private static final String TAG = "MqttConnection";
    private static final String NOT_CONNECTED = "not connected";
    private String serverURI;
    private String clientId;
    private MqttClientPersistence persistence = null;
    private MqttConnectOptions connectOptions;
    private String clientHandle;
    private String reconnectActivityToken = null;
    private MqttAsyncClient myClient = null;
    private AlarmPingSender alarmPingSender = null;
    private MqttService service = null;
    private volatile boolean disconnected = true;
    private boolean cleanSession = true;
    private volatile boolean isConnecting = false;
    private Map<IMqttDeliveryToken, String> savedTopics = new HashMap();
    private Map<IMqttDeliveryToken, MqttMessage> savedSentMessages = new HashMap();
    private Map<IMqttDeliveryToken, String> savedActivityTokens = new HashMap();
    private Map<IMqttDeliveryToken, String> savedInvocationContexts = new HashMap();
    private WakeLock wakelock = null;
    private String wakeLockTag = null;
    private DisconnectedBufferOptions bufferOpts = null;

    public String getServerURI() {
        return this.serverURI;
    }

    public void setServerURI(String serverURI) {
        this.serverURI = serverURI;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public MqttConnectOptions getConnectOptions() {
        return this.connectOptions;
    }

    public void setConnectOptions(MqttConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
    }

    public String getClientHandle() {
        return this.clientHandle;
    }

    public void setClientHandle(String clientHandle) {
        this.clientHandle = clientHandle;
    }

    MqttConnection(MqttService service, String serverURI, String clientId, MqttClientPersistence persistence, String clientHandle) {
        this.serverURI = serverURI;
        this.service = service;
        this.clientId = clientId;
        this.persistence = persistence;
        this.clientHandle = clientHandle;
        StringBuilder stringBuilder = new StringBuilder(this.getClass().getCanonicalName());
        stringBuilder.append(" ");
        stringBuilder.append(clientId);
        stringBuilder.append(" ");
        stringBuilder.append("on host ");
        stringBuilder.append(serverURI);
        this.wakeLockTag = stringBuilder.toString();
    }

    public void connect(MqttConnectOptions options, String invocationContext, String activityToken) {
        this.connectOptions = options;
        this.reconnectActivityToken = activityToken;
        if (options != null) {
            this.cleanSession = options.isCleanSession();
        }

        if (this.connectOptions.isCleanSession()) {
            this.service.messageStore.clearArrivedMessages(this.clientHandle);
        }

        this.service.traceDebug("MqttConnection", "Connecting {" + this.serverURI + "} as {" + this.clientId + "}");
        final Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        resultBundle.putString("MqttService.callbackAction", "connect");

        try {
            if (this.persistence == null) {
                File myDir = this.service.getExternalFilesDir("MqttConnection");
                if (myDir == null) {
                    myDir = this.service.getDir("MqttConnection", 0);
                    if (myDir == null) {
                        resultBundle.putString("MqttService.errorMessage", "Error! No external and internal storage available");
                        resultBundle.putSerializable("MqttService.exception", new MqttPersistenceException());
                        this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
                        return;
                    }
                }

                this.persistence = new MqttDefaultFilePersistence(myDir.getAbsolutePath());
            }

            IMqttActionListener listener = new MqttConnection.MqttConnectionListener(resultBundle) {
                public void onSuccess(IMqttToken asyncActionToken) {
                    MqttConnection.this.doAfterConnectSuccess(resultBundle);
                    MqttConnection.this.service.traceDebug("MqttConnection", "connect success!");
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    resultBundle.putString("MqttService.errorMessage", exception.getLocalizedMessage());
                    resultBundle.putSerializable("MqttService.exception", exception);
                    MqttConnection.this.service.traceError("MqttConnection", "connect fail, call connect to reconnect.reason:" + exception.getMessage());
                    MqttConnection.this.doAfterConnectFail(resultBundle);
                }
            };
            if (this.myClient != null) {
                if (this.isConnecting) {
                    this.service.traceDebug("MqttConnection", "myClient != null and the client is connecting. Connect return directly.");
                    this.service.traceDebug("MqttConnection", "Connect return:isConnecting:" + this.isConnecting + ".disconnected:" + this.disconnected);
                } else if (!this.disconnected) {
                    this.service.traceDebug("MqttConnection", "myClient != null and the client is connected and notify!");
                    this.doAfterConnectSuccess(resultBundle);
                } else {
                    this.service.traceDebug("MqttConnection", "myClient != null and the client is not connected");
                    this.service.traceDebug("MqttConnection", "Do Real connect!");
                    this.setConnectingState(true);
                    this.myClient.connect(this.connectOptions, invocationContext, listener);
                }
            } else {
                this.alarmPingSender = new AlarmPingSender(this.service);
                this.myClient = new MqttAsyncClient(this.serverURI, this.clientId, this.persistence, this.alarmPingSender);
                this.myClient.setCallback(this);
                this.service.traceDebug("MqttConnection", "Do Real connect!");
                this.setConnectingState(true);
                this.myClient.connect(this.connectOptions, invocationContext, listener);
            }
        } catch (Exception var6) {
            this.service.traceError("MqttConnection", "Exception occurred attempting to connect: " + var6.getMessage());
            this.setConnectingState(false);
            this.handleException(resultBundle, var6);
        }

    }

    private void doAfterConnectSuccess(Bundle resultBundle) {
        this.acquireWakeLock();
        this.service.callbackToActivity(this.clientHandle, Status.OK, resultBundle);
        this.deliverBacklog();
        this.setConnectingState(false);
        this.disconnected = false;
        this.releaseWakeLock();
    }

    public void connectComplete(boolean reconnect, String serverURI) {
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "connectExtended");
        resultBundle.putBoolean("MqttService.reconnect", reconnect);
        resultBundle.putString("MqttService.serverURI", serverURI);
        this.service.callbackToActivity(this.clientHandle, Status.OK, resultBundle);
    }

    private void doAfterConnectFail(Bundle resultBundle) {
        this.acquireWakeLock();
        this.disconnected = true;
        this.setConnectingState(false);
        this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        this.releaseWakeLock();
    }

    private void handleException(Bundle resultBundle, Exception e) {
        resultBundle.putString("MqttService.errorMessage", e.getLocalizedMessage());
        resultBundle.putSerializable("MqttService.exception", e);
        this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
    }

    private void deliverBacklog() {
        Iterator backlog = this.service.messageStore.getAllArrivedMessages(this.clientHandle);

        while(backlog.hasNext()) {
            StoredMessage msgArrived = (StoredMessage)backlog.next();
            Bundle resultBundle = this.messageToBundle(msgArrived.getMessageId(), msgArrived.getTopic(), msgArrived.getMessage());
            resultBundle.putString("MqttService.callbackAction", "messageArrived");
            this.service.callbackToActivity(this.clientHandle, Status.OK, resultBundle);
        }

    }

    private Bundle messageToBundle(String messageId, String topic, MqttMessage message) {
        Bundle result = new Bundle();
        result.putString("MqttService.messageId", messageId);
        result.putString("MqttService.destinationName", topic);
        result.putParcelable("MqttService.PARCEL", new ParcelableMqttMessage(message));
        return result;
    }

    void close() {
        this.service.traceDebug("MqttConnection", "close()");

        try {
            if (this.myClient != null) {
                this.myClient.close();
            }
        } catch (MqttException var2) {
            this.handleException(new Bundle(), var2);
        }

    }

    void disconnect(long quiesceTimeout, String invocationContext, String activityToken) {
        this.service.traceDebug("MqttConnection", "disconnect()");
        this.disconnected = true;
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        resultBundle.putString("MqttService.callbackAction", "disconnect");
        if (this.myClient != null && this.myClient.isConnected()) {
            MqttConnection.MqttConnectionListener listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                this.myClient.disconnect(quiesceTimeout, invocationContext, listener);
            } catch (Exception var8) {
                this.handleException(resultBundle, var8);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("disconnect", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

        if (this.connectOptions != null && this.connectOptions.isCleanSession()) {
            this.service.messageStore.clearArrivedMessages(this.clientHandle);
        }

        this.releaseWakeLock();
    }

    void disconnect(String invocationContext, String activityToken) {
        this.service.traceDebug("MqttConnection", "disconnect()");
        this.disconnected = true;
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        resultBundle.putString("MqttService.callbackAction", "disconnect");
        if (this.myClient != null && this.myClient.isConnected()) {
            MqttConnection.MqttConnectionListener listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                this.myClient.disconnect(invocationContext, listener);
            } catch (Exception var6) {
                this.handleException(resultBundle, var6);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("disconnect", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

        if (this.connectOptions != null && this.connectOptions.isCleanSession()) {
            this.service.messageStore.clearArrivedMessages(this.clientHandle);
        }

        this.releaseWakeLock();
    }

    public boolean isConnected() {
        return this.myClient != null && this.myClient.isConnected();
    }

    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained, String invocationContext, String activityToken) {
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "send");
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        IMqttDeliveryToken sendToken = null;
        if (this.myClient != null && this.myClient.isConnected()) {
            MqttConnection.MqttConnectionListener listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                MqttMessage message = new MqttMessage(payload);
                message.setQos(qos);
                message.setRetained(retained);
                sendToken = this.myClient.publish(topic, payload, qos, retained, invocationContext, listener);
                this.storeSendDetails(topic, message, sendToken, invocationContext, activityToken);
            } catch (Exception var11) {
                this.handleException(resultBundle, var11);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("send", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

        return sendToken;
    }

    public IMqttDeliveryToken publish(String topic, MqttMessage message, String invocationContext, String activityToken) {
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "send");
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        IMqttDeliveryToken sendToken = null;
        MqttConnection.MqttConnectionListener listener;
        if (this.myClient != null && this.myClient.isConnected()) {
            listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                sendToken = this.myClient.publish(topic, message, invocationContext, listener);
                this.storeSendDetails(topic, message, sendToken, invocationContext, activityToken);
            } catch (Exception var10) {
                this.handleException(resultBundle, var10);
            }
        } else if (this.myClient != null && this.bufferOpts != null && this.bufferOpts.isBufferEnabled()) {
            listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                sendToken = this.myClient.publish(topic, message, invocationContext, listener);
                this.storeSendDetails(topic, message, sendToken, invocationContext, activityToken);
            } catch (Exception var9) {
                this.handleException(resultBundle, var9);
            }
        } else {
            Log.i("MqttConnection", "Client is not connected, so not sending message");
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("send", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

        return sendToken;
    }

    public void subscribe(String topic, int qos, String invocationContext, String activityToken) {
        this.service.traceDebug("MqttConnection", "subscribe({" + topic + "}," + qos + ",{" + invocationContext + "}, {" + activityToken + "}");
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "subscribe");
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        if (this.myClient != null && this.myClient.isConnected()) {
            MqttConnection.MqttConnectionListener listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                this.myClient.subscribe(topic, qos, invocationContext, listener);
            } catch (Exception var8) {
                this.handleException(resultBundle, var8);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("subscribe", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

    }

    public void subscribe(String[] topic, int[] qos, String invocationContext, String activityToken) {
        this.service.traceDebug("MqttConnection", "subscribe({" + Arrays.toString(topic) + "}," + Arrays.toString(qos) + ",{" + invocationContext + "}, {" + activityToken + "}");
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "subscribe");
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        if (this.myClient != null && this.myClient.isConnected()) {
            MqttConnection.MqttConnectionListener listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                this.myClient.subscribe(topic, qos, invocationContext, listener);
            } catch (Exception var8) {
                this.handleException(resultBundle, var8);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("subscribe", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

    }

    public void subscribe(String[] topicFilters, int[] qos, String invocationContext, String activityToken, IMqttMessageListener[] messageListeners) {
        this.service.traceDebug("MqttConnection", "subscribe({" + Arrays.toString(topicFilters) + "}," + Arrays.toString(qos) + ",{" + invocationContext + "}, {" + activityToken + "}");
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "subscribe");
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        if (this.myClient != null && this.myClient.isConnected()) {
            new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                this.myClient.subscribe(topicFilters, qos, messageListeners);
            } catch (Exception var9) {
                this.handleException(resultBundle, var9);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("subscribe", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

    }

    void unsubscribe(String topic, String invocationContext, String activityToken) {
        this.service.traceDebug("MqttConnection", "unsubscribe({" + topic + "},{" + invocationContext + "}, {" + activityToken + "})");
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "unsubscribe");
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        if (this.myClient != null && this.myClient.isConnected()) {
            MqttConnection.MqttConnectionListener listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                this.myClient.unsubscribe(topic, invocationContext, listener);
            } catch (Exception var7) {
                this.handleException(resultBundle, var7);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("subscribe", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

    }

    void unsubscribe(String[] topic, String invocationContext, String activityToken) {
        this.service.traceDebug("MqttConnection", "unsubscribe({" + Arrays.toString(topic) + "},{" + invocationContext + "}, {" + activityToken + "})");
        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "unsubscribe");
        resultBundle.putString("MqttService.activityToken", activityToken);
        resultBundle.putString("MqttService.invocationContext", invocationContext);
        if (this.myClient != null && this.myClient.isConnected()) {
            MqttConnection.MqttConnectionListener listener = new MqttConnection.MqttConnectionListener(resultBundle);

            try {
                this.myClient.unsubscribe(topic, invocationContext, listener);
            } catch (Exception var7) {
                this.handleException(resultBundle, var7);
            }
        } else {
            resultBundle.putString("MqttService.errorMessage", "not connected");
            this.service.traceError("subscribe", "not connected");
            this.service.callbackToActivity(this.clientHandle, Status.ERROR, resultBundle);
        }

    }

    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return this.myClient.getPendingDeliveryTokens();
    }

    public void connectionLost(Throwable why) {
        this.service.traceDebug("MqttConnection", "connectionLost(" + why.getMessage() + ")");
        this.disconnected = true;

        try {
            if (!this.connectOptions.isAutomaticReconnect()) {
                this.myClient.disconnect((Object)null, new IMqttActionListener() {
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    }
                });
            } else {
                this.alarmPingSender.schedule(100L);
            }
        } catch (Exception var3) {
        }

        Bundle resultBundle = new Bundle();
        resultBundle.putString("MqttService.callbackAction", "onConnectionLost");
        if (why != null) {
            resultBundle.putString("MqttService.errorMessage", why.getMessage());
            if (why instanceof MqttException) {
                resultBundle.putSerializable("MqttService.exception", why);
            }

            resultBundle.putString("MqttService.exceptionStack", Log.getStackTraceString(why));
        }

        this.service.callbackToActivity(this.clientHandle, Status.OK, resultBundle);
        this.releaseWakeLock();
    }

    public void deliveryComplete(IMqttDeliveryToken messageToken) {
        this.service.traceDebug("MqttConnection", "deliveryComplete(" + messageToken + ")");
        MqttMessage message = (MqttMessage)this.savedSentMessages.remove(messageToken);
        if (message != null) {
            String topic = (String)this.savedTopics.remove(messageToken);
            String activityToken = (String)this.savedActivityTokens.remove(messageToken);
            String invocationContext = (String)this.savedInvocationContexts.remove(messageToken);
            Bundle resultBundle = this.messageToBundle((String)null, topic, message);
            if (activityToken != null) {
                resultBundle.putString("MqttService.callbackAction", "send");
                resultBundle.putString("MqttService.activityToken", activityToken);
                resultBundle.putString("MqttService.invocationContext", invocationContext);
                this.service.callbackToActivity(this.clientHandle, Status.OK, resultBundle);
            }

            resultBundle.putString("MqttService.callbackAction", "messageDelivered");
            this.service.callbackToActivity(this.clientHandle, Status.OK, resultBundle);
        }

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.service.traceDebug("MqttConnection", "messageArrived(" + topic + ",{" + message.toString() + "})");
        String messageId = this.service.messageStore.storeArrived(this.clientHandle, topic, message);
        Bundle resultBundle = this.messageToBundle(messageId, topic, message);
        resultBundle.putString("MqttService.callbackAction", "messageArrived");
        resultBundle.putString("MqttService.messageId", messageId);
        this.service.callbackToActivity(this.clientHandle, Status.OK, resultBundle);
    }

    private void storeSendDetails(String topic, MqttMessage msg, IMqttDeliveryToken messageToken, String invocationContext, String activityToken) {
        this.savedTopics.put(messageToken, topic);
        this.savedSentMessages.put(messageToken, msg);
        this.savedActivityTokens.put(messageToken, activityToken);
        this.savedInvocationContexts.put(messageToken, invocationContext);
    }

    private void acquireWakeLock() {
        if (this.wakelock == null) {
            PowerManager pm = (PowerManager)this.service.getSystemService("power");
            this.wakelock = pm.newWakeLock(1, this.wakeLockTag);
        }

        this.wakelock.acquire();
    }

    private void releaseWakeLock() {
        if (this.wakelock != null && this.wakelock.isHeld()) {
            this.wakelock.release();
        }

    }

    void offline() {
        if (!this.disconnected && !this.cleanSession) {
            Exception e = new Exception("Android offline");
            this.connectionLost(e);
        }

    }

    synchronized void reconnect() {
        if (this.myClient == null) {
            this.service.traceError("MqttConnection", "Reconnect myClient = null. Will not do reconnect");
        } else if (this.isConnecting) {
            this.service.traceDebug("MqttConnection", "The client is connecting. Reconnect return directly.");
        } else if (!this.service.isOnline()) {
            this.service.traceDebug("MqttConnection", "The network is not reachable. Will not do reconnect");
        } else {
            final Bundle resultBundle;
            if (this.connectOptions.isAutomaticReconnect()) {
                Log.i("MqttConnection", "Requesting Automatic reconnect using New Java AC");
                resultBundle = new Bundle();
                resultBundle.putString("MqttService.activityToken", this.reconnectActivityToken);
                resultBundle.putString("MqttService.invocationContext", (String)null);
                resultBundle.putString("MqttService.callbackAction", "connect");

                try {
                    this.myClient.reconnect();
                } catch (MqttException var6) {
                    Log.e("MqttConnection", "Exception occurred attempting to reconnect: " + var6.getMessage());
                    this.setConnectingState(false);
                    this.handleException(resultBundle, var6);
                }
            } else if (this.disconnected && !this.cleanSession) {
                this.service.traceDebug("MqttConnection", "Do Real Reconnect!");
                resultBundle = new Bundle();
                resultBundle.putString("MqttService.activityToken", this.reconnectActivityToken);
                resultBundle.putString("MqttService.invocationContext", (String)null);
                resultBundle.putString("MqttService.callbackAction", "connect");

                try {
                    IMqttActionListener listener = new MqttConnection.MqttConnectionListener(resultBundle) {
                        public void onSuccess(IMqttToken asyncActionToken) {
                            MqttConnection.this.service.traceDebug("MqttConnection", "Reconnect Success!");
                            MqttConnection.this.service.traceDebug("MqttConnection", "DeliverBacklog when reconnect.");
                            MqttConnection.this.doAfterConnectSuccess(resultBundle);
                        }

                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            resultBundle.putString("MqttService.errorMessage", exception.getLocalizedMessage());
                            resultBundle.putSerializable("MqttService.exception", exception);
                            MqttConnection.this.service.callbackToActivity(MqttConnection.this.clientHandle, Status.ERROR, resultBundle);
                            MqttConnection.this.doAfterConnectFail(resultBundle);
                        }
                    };
                    this.myClient.connect(this.connectOptions, (Object)null, listener);
                    this.setConnectingState(true);
                } catch (MqttException var4) {
                    this.service.traceError("MqttConnection", "Cannot reconnect to remote server." + var4.getMessage());
                    this.setConnectingState(false);
                    this.handleException(resultBundle, var4);
                } catch (Exception var5) {
                    this.service.traceError("MqttConnection", "Cannot reconnect to remote server." + var5.getMessage());
                    this.setConnectingState(false);
                    MqttException newEx = new MqttException(6, var5.getCause());
                    this.handleException(resultBundle, newEx);
                }
            }

        }
    }

    private synchronized void setConnectingState(boolean isConnecting) {
        this.isConnecting = isConnecting;
    }

    public void setBufferOpts(DisconnectedBufferOptions bufferOpts) {
        this.bufferOpts = bufferOpts;
        this.myClient.setBufferOpts(bufferOpts);
    }

    public int getBufferedMessageCount() {
        return this.myClient.getBufferedMessageCount();
    }

    public MqttMessage getBufferedMessage(int bufferIndex) {
        return this.myClient.getBufferedMessage(bufferIndex);
    }

    public void deleteBufferedMessage(int bufferIndex) {
        this.myClient.deleteBufferedMessage(bufferIndex);
    }

    private class MqttConnectionListener implements IMqttActionListener {
        private final Bundle resultBundle;

        private MqttConnectionListener(Bundle resultBundle) {
            this.resultBundle = resultBundle;
        }

        public void onSuccess(IMqttToken asyncActionToken) {
            MqttConnection.this.service.callbackToActivity(MqttConnection.this.clientHandle, Status.OK, this.resultBundle);
        }

        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            this.resultBundle.putString("MqttService.errorMessage", exception.getLocalizedMessage());
            this.resultBundle.putSerializable("MqttService.exception", exception);
            MqttConnection.this.service.callbackToActivity(MqttConnection.this.clientHandle, Status.ERROR, this.resultBundle);
        }
    }
}
