//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Build.VERSION;
import android.os.PowerManager.WakeLock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

@SuppressLint({"Registered"})
public class MqttService extends Service implements MqttTraceHandler {
    static final String TAG = "MqttService";
    private String traceCallbackId;
    private boolean traceEnabled = false;
    MessageStore messageStore;
    private MqttService.NetworkConnectionIntentReceiver networkConnectionMonitor;
    private MqttService.BackgroundDataPreferenceReceiver backgroundDataPreferenceMonitor;
    private volatile boolean backgroundDataEnabled = true;
    private MqttServiceBinder mqttServiceBinder;
    private Map<String, MqttConnection> connections = new ConcurrentHashMap();

    public MqttService() {
    }

    void callbackToActivity(String clientHandle, Status status, Bundle dataBundle) {
        Intent callbackIntent = new Intent("MqttService.callbackToActivity.v0");
        if (clientHandle != null) {
            callbackIntent.putExtra("MqttService.clientHandle", clientHandle);
        }

        callbackIntent.putExtra("MqttService.callbackStatus", status);
        if (dataBundle != null) {
            callbackIntent.putExtras(dataBundle);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(callbackIntent);
    }

    public String getClient(String serverURI, String clientId, String contextId, MqttClientPersistence persistence) {
        String clientHandle = serverURI + ":" + clientId + ":" + contextId;
        if (!this.connections.containsKey(clientHandle)) {
            MqttConnection client = new MqttConnection(this, serverURI, clientId, persistence, clientHandle);
            this.connections.put(clientHandle, client);
        }

        return clientHandle;
    }

    public void connect(String clientHandle, MqttConnectOptions connectOptions, String invocationContext, String activityToken) throws MqttSecurityException, MqttException {
        MqttConnection client = this.getConnection(clientHandle);
        client.connect(connectOptions, (String)null, activityToken);
    }

    void reconnect() {
        this.traceDebug("MqttService", "Reconnect to server, client size=" + this.connections.size());
        Iterator var1 = this.connections.values().iterator();

        while(var1.hasNext()) {
            MqttConnection client = (MqttConnection)var1.next();
            this.traceDebug("Reconnect Client:", client.getClientId() + '/' + client.getServerURI());
            if (this.isOnline()) {
                client.reconnect();
            }
        }

    }

    public void close(String clientHandle) {
        MqttConnection client = this.getConnection(clientHandle);
        client.close();
    }

    public void disconnect(String clientHandle, String invocationContext, String activityToken) {
        MqttConnection client = this.getConnection(clientHandle);
        client.disconnect(invocationContext, activityToken);
        this.connections.remove(clientHandle);
        this.stopSelf();
    }

    public void disconnect(String clientHandle, long quiesceTimeout, String invocationContext, String activityToken) {
        MqttConnection client = this.getConnection(clientHandle);
        client.disconnect(quiesceTimeout, invocationContext, activityToken);
        this.connections.remove(clientHandle);
        this.stopSelf();
    }

    public boolean isConnected(String clientHandle) {
        MqttConnection client = this.getConnection(clientHandle);
        return client.isConnected();
    }

    public IMqttDeliveryToken publish(String clientHandle, String topic, byte[] payload, int qos, boolean retained, String invocationContext, String activityToken) throws MqttPersistenceException, MqttException {
        MqttConnection client = this.getConnection(clientHandle);
        return client.publish(topic, payload, qos, retained, invocationContext, activityToken);
    }

    public IMqttDeliveryToken publish(String clientHandle, String topic, MqttMessage message, String invocationContext, String activityToken) throws MqttPersistenceException, MqttException {
        MqttConnection client = this.getConnection(clientHandle);
        return client.publish(topic, message, invocationContext, activityToken);
    }

    public void subscribe(String clientHandle, String topic, int qos, String invocationContext, String activityToken) {
        MqttConnection client = this.getConnection(clientHandle);
        client.subscribe(topic, qos, invocationContext, activityToken);
    }

    public void subscribe(String clientHandle, String[] topic, int[] qos, String invocationContext, String activityToken) {
        MqttConnection client = this.getConnection(clientHandle);
        client.subscribe(topic, qos, invocationContext, activityToken);
    }

    public void subscribe(String clientHandle, String[] topicFilters, int[] qos, String invocationContext, String activityToken, IMqttMessageListener[] messageListeners) {
        MqttConnection client = this.getConnection(clientHandle);
        client.subscribe(topicFilters, qos, invocationContext, activityToken, messageListeners);
    }

    public void unsubscribe(String clientHandle, String topic, String invocationContext, String activityToken) {
        MqttConnection client = this.getConnection(clientHandle);
        client.unsubscribe(topic, invocationContext, activityToken);
    }

    public void unsubscribe(String clientHandle, String[] topic, String invocationContext, String activityToken) {
        MqttConnection client = this.getConnection(clientHandle);
        client.unsubscribe(topic, invocationContext, activityToken);
    }

    public IMqttDeliveryToken[] getPendingDeliveryTokens(String clientHandle) {
        MqttConnection client = this.getConnection(clientHandle);
        return client.getPendingDeliveryTokens();
    }

    private MqttConnection getConnection(String clientHandle) {
        MqttConnection client = (MqttConnection)this.connections.get(clientHandle);
        if (client == null) {
            throw new IllegalArgumentException("Invalid ClientHandle");
        } else {
            return client;
        }
    }

    public Status acknowledgeMessageArrival(String clientHandle, String id) {
        return this.messageStore.discardArrived(clientHandle, id) ? Status.OK : Status.ERROR;
    }

    public void onCreate() {
        super.onCreate();
        this.mqttServiceBinder = new MqttServiceBinder(this);
        this.messageStore = new DatabaseMessageStore(this, this);
    }

    public void onDestroy() {
        Iterator var1 = this.connections.values().iterator();

        while(var1.hasNext()) {
            MqttConnection client = (MqttConnection)var1.next();
            client.disconnect((String)null, (String)null);
        }

        if (this.mqttServiceBinder != null) {
            this.mqttServiceBinder = null;
        }

        this.unregisterBroadcastReceivers();
        if (this.messageStore != null) {
            this.messageStore.close();
        }

        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        String activityToken = intent.getStringExtra("MqttService.activityToken");
        this.mqttServiceBinder.setActivityToken(activityToken);
        return this.mqttServiceBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.registerBroadcastReceivers();
        return 1;
    }

    public void setTraceCallbackId(String traceCallbackId) {
        this.traceCallbackId = traceCallbackId;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public boolean isTraceEnabled() {
        return this.traceEnabled;
    }

    public void traceDebug(String tag, String message) {
        this.traceCallback("debug", tag, message);
    }

    public void traceError(String tag, String message) {
        this.traceCallback("error", tag, message);
    }

    private void traceCallback(String severity, String tag, String message) {
        if (this.traceCallbackId != null && this.traceEnabled) {
            Bundle dataBundle = new Bundle();
            dataBundle.putString("MqttService.callbackAction", "trace");
            dataBundle.putString("MqttService.traceSeverity", severity);
            dataBundle.putString("MqttService.traceTag", tag);
            dataBundle.putString("MqttService.errorMessage", message);
            this.callbackToActivity(this.traceCallbackId, Status.ERROR, dataBundle);
        }

    }

    public void traceException(String tag, String message, Exception e) {
        if (this.traceCallbackId != null) {
            Bundle dataBundle = new Bundle();
            dataBundle.putString("MqttService.callbackAction", "trace");
            dataBundle.putString("MqttService.traceSeverity", "exception");
            dataBundle.putString("MqttService.errorMessage", message);
            dataBundle.putSerializable("MqttService.exception", e);
            dataBundle.putString("MqttService.traceTag", tag);
            this.callbackToActivity(this.traceCallbackId, Status.ERROR, dataBundle);
        }

    }

    private void registerBroadcastReceivers() {
        if (this.networkConnectionMonitor == null) {
            this.networkConnectionMonitor = new MqttService.NetworkConnectionIntentReceiver();
            this.registerReceiver(this.networkConnectionMonitor, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }

        if (VERSION.SDK_INT < 14) {
            ConnectivityManager cm = (ConnectivityManager)this.getSystemService("connectivity");
            this.backgroundDataEnabled = cm.getBackgroundDataSetting();
            if (this.backgroundDataPreferenceMonitor == null) {
                this.backgroundDataPreferenceMonitor = new MqttService.BackgroundDataPreferenceReceiver();
                this.registerReceiver(this.backgroundDataPreferenceMonitor, new IntentFilter("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED"));
            }
        }

    }

    private void unregisterBroadcastReceivers() {
        if (this.networkConnectionMonitor != null) {
            this.unregisterReceiver(this.networkConnectionMonitor);
            this.networkConnectionMonitor = null;
        }

        if (VERSION.SDK_INT < 14 && this.backgroundDataPreferenceMonitor != null) {
            this.unregisterReceiver(this.backgroundDataPreferenceMonitor);
        }

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService("connectivity");
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected() && this.backgroundDataEnabled;
    }

    private void notifyClientsOffline() {
        Iterator var1 = this.connections.values().iterator();

        while(var1.hasNext()) {
            MqttConnection connection = (MqttConnection)var1.next();
            connection.offline();
        }

    }

    public void setBufferOpts(String clientHandle, DisconnectedBufferOptions bufferOpts) {
        MqttConnection client = this.getConnection(clientHandle);
        client.setBufferOpts(bufferOpts);
    }

    public int getBufferedMessageCount(String clientHandle) {
        MqttConnection client = this.getConnection(clientHandle);
        return client.getBufferedMessageCount();
    }

    public MqttMessage getBufferedMessage(String clientHandle, int bufferIndex) {
        MqttConnection client = this.getConnection(clientHandle);
        return client.getBufferedMessage(bufferIndex);
    }

    public void deleteBufferedMessage(String clientHandle, int bufferIndex) {
        MqttConnection client = this.getConnection(clientHandle);
        client.deleteBufferedMessage(bufferIndex);
    }

    private class BackgroundDataPreferenceReceiver extends BroadcastReceiver {
        private BackgroundDataPreferenceReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager)MqttService.this.getSystemService("connectivity");
            MqttService.this.traceDebug("MqttService", "Reconnect since BroadcastReceiver.");
            if (cm.getBackgroundDataSetting()) {
                if (!MqttService.this.backgroundDataEnabled) {
                    MqttService.this.backgroundDataEnabled = true;
                    MqttService.this.reconnect();
                }
            } else {
                MqttService.this.backgroundDataEnabled = false;
                MqttService.this.notifyClientsOffline();
            }

        }
    }

    private class NetworkConnectionIntentReceiver extends BroadcastReceiver {
        private NetworkConnectionIntentReceiver() {
        }

        @SuppressLint({"Wakelock"})
        public void onReceive(Context context, Intent intent) {
            MqttService.this.traceDebug("MqttService", "Internal network status receive.");
            PowerManager pm = (PowerManager)MqttService.this.getSystemService("power");
            WakeLock wl = pm.newWakeLock(1, "MQTT");
            wl.acquire();
            MqttService.this.traceDebug("MqttService", "Reconnect for Network recovery.");
            if (MqttService.this.isOnline()) {
                MqttService.this.traceDebug("MqttService", "Online,reconnect.");
                MqttService.this.reconnect();
            } else {
                MqttService.this.notifyClientsOffline();
            }

            wl.release();
        }
    }
}
