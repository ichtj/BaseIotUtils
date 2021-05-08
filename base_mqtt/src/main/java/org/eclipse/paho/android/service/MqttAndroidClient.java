//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.SparseArray;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class MqttAndroidClient extends BroadcastReceiver implements IMqttAsyncClient {
    private static final String SERVICE_NAME = "org.eclipse.paho.android.service.MqttService";
    private static final int BIND_SERVICE_FLAG = 0;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private final MqttAndroidClient.MyServiceConnection serviceConnection;
    private MqttService mqttService;
    private String clientHandle;
    private Context myContext;
    private final SparseArray<IMqttToken> tokenMap;
    private int tokenNumber;
    private final String serverURI;
    private final String clientId;
    private MqttClientPersistence persistence;
    private MqttConnectOptions connectOptions;
    private IMqttToken connectToken;
    private MqttCallback callback;
    private MqttTraceHandler traceCallback;
    private final MqttAndroidClient.Ack messageAck;
    private boolean traceEnabled;
    private volatile boolean receiverRegistered;
    private volatile boolean bindedService;

    public MqttAndroidClient(Context context, String serverURI, String clientId) {
        this(context, serverURI, clientId, (MqttClientPersistence)null, MqttAndroidClient.Ack.AUTO_ACK);
    }

    public MqttAndroidClient(Context ctx, String serverURI, String clientId, MqttAndroidClient.Ack ackType) {
        this(ctx, serverURI, clientId, (MqttClientPersistence)null, ackType);
    }

    public MqttAndroidClient(Context ctx, String serverURI, String clientId, MqttClientPersistence persistence) {
        this(ctx, serverURI, clientId, persistence, MqttAndroidClient.Ack.AUTO_ACK);
    }

    public MqttAndroidClient(Context context, String serverURI, String clientId, MqttClientPersistence persistence, MqttAndroidClient.Ack ackType) {
        this.serviceConnection = new MqttAndroidClient.MyServiceConnection();
        this.tokenMap = new SparseArray();
        this.tokenNumber = 0;
        this.persistence = null;
        this.traceEnabled = false;
        this.receiverRegistered = false;
        this.bindedService = false;
        this.myContext = context;
        this.serverURI = serverURI;
        this.clientId = clientId;
        this.persistence = persistence;
        this.messageAck = ackType;
    }

    public boolean isConnected() {
        return this.clientHandle != null && this.mqttService != null && this.mqttService.isConnected(this.clientHandle);
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getServerURI() {
        return this.serverURI;
    }

    public void close() {
        if (this.mqttService != null) {
            if (this.clientHandle == null) {
                this.clientHandle = this.mqttService.getClient(this.serverURI, this.clientId, this.myContext.getApplicationInfo().packageName, this.persistence);
            }

            this.mqttService.close(this.clientHandle);
        }

    }

    public IMqttToken connect() throws MqttException {
        return this.connect((Object)null, (IMqttActionListener)null);
    }

    public IMqttToken connect(MqttConnectOptions options) throws MqttException {
        return this.connect(options, (Object)null, (IMqttActionListener)null);
    }

    public IMqttToken connect(Object userContext, IMqttActionListener callback) throws MqttException {
        return this.connect(new MqttConnectOptions(), userContext, callback);
    }

    public IMqttToken connect(MqttConnectOptions options, Object userContext, IMqttActionListener callback) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback);
        this.connectOptions = options;
        this.connectToken = token;
        if (this.mqttService == null) {
            Intent serviceStartIntent = new Intent();
            serviceStartIntent.setClassName(this.myContext, "org.eclipse.paho.android.service.MqttService");
            Object service = this.myContext.startService(serviceStartIntent);
            if (service == null) {
                IMqttActionListener listener = token.getActionCallback();
                if (listener != null) {
                    listener.onFailure(token, new RuntimeException("cannot start service org.eclipse.paho.android.service.MqttService"));
                }
            }

            this.myContext.bindService(serviceStartIntent, this.serviceConnection, 1);
            if (!this.receiverRegistered) {
                this.registerReceiver(this);
            }
        } else {
            pool.execute(new Runnable() {
                public void run() {
                    MqttAndroidClient.this.doConnect();
                    if (!MqttAndroidClient.this.receiverRegistered) {
                        MqttAndroidClient.this.registerReceiver(MqttAndroidClient.this);
                    }

                }
            });
        }

        return token;
    }

    private void registerReceiver(BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MqttService.callbackToActivity.v0");
        LocalBroadcastManager.getInstance(this.myContext).registerReceiver(receiver, filter);
        this.receiverRegistered = true;
    }

    private void doConnect() {
        if (this.clientHandle == null) {
            this.clientHandle = this.mqttService.getClient(this.serverURI, this.clientId, this.myContext.getApplicationInfo().packageName, this.persistence);
        }

        this.mqttService.setTraceEnabled(this.traceEnabled);
        this.mqttService.setTraceCallbackId(this.clientHandle);
        String activityToken = this.storeToken(this.connectToken);

        try {
            this.mqttService.connect(this.clientHandle, this.connectOptions, (String)null, activityToken);
        } catch (MqttException var4) {
            IMqttActionListener listener = this.connectToken.getActionCallback();
            if (listener != null) {
                listener.onFailure(this.connectToken, var4);
            }
        }

    }

    public IMqttToken disconnect() throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, (Object)null, (IMqttActionListener)null);
        String activityToken = this.storeToken(token);
        this.mqttService.disconnect(this.clientHandle, (String)null, activityToken);
        return token;
    }

    public IMqttToken disconnect(long quiesceTimeout) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, (Object)null, (IMqttActionListener)null);
        String activityToken = this.storeToken(token);
        this.mqttService.disconnect(this.clientHandle, quiesceTimeout, (String)null, activityToken);
        return token;
    }

    public IMqttToken disconnect(Object userContext, IMqttActionListener callback) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback);
        String activityToken = this.storeToken(token);
        this.mqttService.disconnect(this.clientHandle, (String)null, activityToken);
        return token;
    }

    public IMqttToken disconnect(long quiesceTimeout, Object userContext, IMqttActionListener callback) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback);
        String activityToken = this.storeToken(token);
        this.mqttService.disconnect(this.clientHandle, quiesceTimeout, (String)null, activityToken);
        return token;
    }

    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException, MqttPersistenceException {
        return this.publish(topic, payload, qos, retained, (Object)null, (IMqttActionListener)null);
    }

    public IMqttDeliveryToken publish(String topic, MqttMessage message) throws MqttException, MqttPersistenceException {
        return this.publish(topic, message, (Object)null, (IMqttActionListener)null);
    }

    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained, Object userContext, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        MqttDeliveryTokenAndroid token = new MqttDeliveryTokenAndroid(this, userContext, callback, message);
        String activityToken = this.storeToken(token);
        IMqttDeliveryToken internalToken = this.mqttService.publish(this.clientHandle, topic, payload, qos, retained, (String)null, activityToken);
        token.setDelegate(internalToken);
        return token;
    }

    public IMqttDeliveryToken publish(String topic, MqttMessage message, Object userContext, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        MqttDeliveryTokenAndroid token = new MqttDeliveryTokenAndroid(this, userContext, callback, message);
        String activityToken = this.storeToken(token);
        IMqttDeliveryToken internalToken = this.mqttService.publish(this.clientHandle, topic, message, (String)null, activityToken);
        token.setDelegate(internalToken);
        return token;
    }

    public IMqttToken subscribe(String topic, int qos) throws MqttException, MqttSecurityException {
        return this.subscribe(topic, qos, (Object)null, (IMqttActionListener)null);
    }

    public IMqttToken subscribe(String[] topic, int[] qos) throws MqttException, MqttSecurityException {
        return this.subscribe(topic, qos, (Object)null, (IMqttActionListener)null);
    }

    public IMqttToken subscribe(String topic, int qos, Object userContext, IMqttActionListener callback) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback, new String[]{topic});
        String activityToken = this.storeToken(token);
        this.mqttService.subscribe(this.clientHandle, topic, qos, (String)null, activityToken);
        return token;
    }

    public IMqttToken subscribe(String[] topic, int[] qos, Object userContext, IMqttActionListener callback) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback, topic);
        String activityToken = this.storeToken(token);
        this.mqttService.subscribe(this.clientHandle, topic, qos, (String)null, activityToken);
        return token;
    }

    public IMqttToken subscribe(String topicFilter, int qos, Object userContext, IMqttActionListener callback, IMqttMessageListener messageListener) throws MqttException {
        return this.subscribe(new String[]{topicFilter}, new int[]{qos}, userContext, callback, new IMqttMessageListener[]{messageListener});
    }

    public IMqttToken subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) throws MqttException {
        return this.subscribe(topicFilter, qos, (Object)null, (IMqttActionListener)null, messageListener);
    }

    public IMqttToken subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException {
        return this.subscribe(topicFilters, qos, (Object)null, (IMqttActionListener)null, messageListeners);
    }

    public IMqttToken subscribe(String[] topicFilters, int[] qos, Object userContext, IMqttActionListener callback, IMqttMessageListener[] messageListeners) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback, topicFilters);
        String activityToken = this.storeToken(token);
        this.mqttService.subscribe(this.clientHandle, topicFilters, qos, (String)null, activityToken, messageListeners);
        return null;
    }

    public IMqttToken unsubscribe(String topic) throws MqttException {
        return this.unsubscribe((String)topic, (Object)null, (IMqttActionListener)null);
    }

    public IMqttToken unsubscribe(String[] topic) throws MqttException {
        return this.unsubscribe((String[])topic, (Object)null, (IMqttActionListener)null);
    }

    public IMqttToken unsubscribe(String topic, Object userContext, IMqttActionListener callback) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback);
        String activityToken = this.storeToken(token);
        this.mqttService.unsubscribe(this.clientHandle, topic, (String)null, activityToken);
        return token;
    }

    public IMqttToken unsubscribe(String[] topic, Object userContext, IMqttActionListener callback) throws MqttException {
        IMqttToken token = new MqttTokenAndroid(this, userContext, callback);
        String activityToken = this.storeToken(token);
        this.mqttService.unsubscribe(this.clientHandle, topic, (String)null, activityToken);
        return token;
    }

    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return this.mqttService.getPendingDeliveryTokens(this.clientHandle);
    }

    public void setCallback(MqttCallback callback) {
        this.callback = callback;
    }

    public void setTraceCallback(MqttTraceHandler traceCallback) {
        this.traceCallback = traceCallback;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
        if (this.mqttService != null) {
            this.mqttService.setTraceEnabled(traceEnabled);
        }

    }

    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        String handleFromIntent = data.getString("MqttService.clientHandle");
        if (handleFromIntent != null && handleFromIntent.equals(this.clientHandle)) {
            String action = data.getString("MqttService.callbackAction");
            if ("connect".equals(action)) {
                this.connectAction(data);
            } else if ("connectExtended".equals(action)) {
                this.connectExtendedAction(data);
            } else if ("messageArrived".equals(action)) {
                this.messageArrivedAction(data);
            } else if ("subscribe".equals(action)) {
                this.subscribeAction(data);
            } else if ("unsubscribe".equals(action)) {
                this.unSubscribeAction(data);
            } else if ("send".equals(action)) {
                this.sendAction(data);
            } else if ("messageDelivered".equals(action)) {
                this.messageDeliveredAction(data);
            } else if ("onConnectionLost".equals(action)) {
                this.connectionLostAction(data);
            } else if ("disconnect".equals(action)) {
                this.disconnected(data);
            } else if ("trace".equals(action)) {
                this.traceAction(data);
            } else {
                this.mqttService.traceError("MqttService", "Callback action doesn't exist.");
            }

        }
    }

    public boolean acknowledgeMessage(String messageId) {
        if (this.messageAck == MqttAndroidClient.Ack.MANUAL_ACK) {
            Status status = this.mqttService.acknowledgeMessageArrival(this.clientHandle, messageId);
            return status == Status.OK;
        } else {
            return false;
        }
    }

    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        throw new UnsupportedOperationException();
    }

    public void setManualAcks(boolean manualAcks) {
        throw new UnsupportedOperationException();
    }

    private void connectAction(Bundle data) {
        IMqttToken token = this.connectToken;
        this.removeMqttToken(data);
        this.simpleAction(token, data);
    }

    private void disconnected(Bundle data) {
        this.clientHandle = null;
        IMqttToken token = this.removeMqttToken(data);
        if (token != null) {
            ((MqttTokenAndroid)token).notifyComplete();
        }

        if (this.callback != null) {
            this.callback.connectionLost((Throwable)null);
        }

    }

    private void connectionLostAction(Bundle data) {
        if (this.callback != null) {
            Exception reason = (Exception)data.getSerializable("MqttService.exception");
            this.callback.connectionLost(reason);
        }

    }

    private void connectExtendedAction(Bundle data) {
        if (this.callback instanceof MqttCallbackExtended) {
            boolean reconnect = data.getBoolean("MqttService.reconnect", false);
            String serverURI = data.getString("MqttService.serverURI");
            ((MqttCallbackExtended)this.callback).connectComplete(reconnect, serverURI);
        }

    }

    private void simpleAction(IMqttToken token, Bundle data) {
        if (token != null) {
            Status status = (Status)data.getSerializable("MqttService.callbackStatus");
            if (status == Status.OK) {
                ((MqttTokenAndroid)token).notifyComplete();
            } else {
                Exception exceptionThrown = (Exception)data.getSerializable("MqttService.exception");
                ((MqttTokenAndroid)token).notifyFailure(exceptionThrown);
            }
        } else {
            this.mqttService.traceError("MqttService", "simpleAction : token is null");
        }

    }

    private void sendAction(Bundle data) {
        IMqttToken token = this.getMqttToken(data);
        this.simpleAction(token, data);
    }

    private void subscribeAction(Bundle data) {
        IMqttToken token = this.removeMqttToken(data);
        this.simpleAction(token, data);
    }

    private void unSubscribeAction(Bundle data) {
        IMqttToken token = this.removeMqttToken(data);
        this.simpleAction(token, data);
    }

    private void messageDeliveredAction(Bundle data) {
        IMqttToken token = this.removeMqttToken(data);
        if (token != null && this.callback != null) {
            Status status = (Status)data.getSerializable("MqttService.callbackStatus");
            if (status == Status.OK && token instanceof IMqttDeliveryToken) {
                this.callback.deliveryComplete((IMqttDeliveryToken)token);
            }
        }

    }

    private void messageArrivedAction(Bundle data) {
        if (this.callback != null) {
            String messageId = data.getString("MqttService.messageId");
            String destinationName = data.getString("MqttService.destinationName");
            ParcelableMqttMessage message = (ParcelableMqttMessage)data.getParcelable("MqttService.PARCEL");

            try {
                if (this.messageAck == MqttAndroidClient.Ack.AUTO_ACK) {
                    this.callback.messageArrived(destinationName, message);
                    this.mqttService.acknowledgeMessageArrival(this.clientHandle, messageId);
                } else {
                    message.messageId = messageId;
                    this.callback.messageArrived(destinationName, message);
                }
            } catch (Exception var6) {
            }
        }

    }

    private void traceAction(Bundle data) {
        if (this.traceCallback != null) {
            String severity = data.getString("MqttService.traceSeverity");
            String message = data.getString("MqttService.errorMessage");
            String tag = data.getString("MqttService.traceTag");
            if ("debug".equals(severity)) {
                this.traceCallback.traceDebug(tag, message);
            } else if ("error".equals(severity)) {
                this.traceCallback.traceError(tag, message);
            } else {
                Exception e = (Exception)data.getSerializable("MqttService.exception");
                this.traceCallback.traceException(tag, message, e);
            }
        }

    }

    private synchronized String storeToken(IMqttToken token) {
        this.tokenMap.put(this.tokenNumber, token);
        return Integer.toString(this.tokenNumber++);
    }

    private synchronized IMqttToken removeMqttToken(Bundle data) {
        String activityToken = data.getString("MqttService.activityToken");
        if (activityToken != null) {
            int tokenNumber = Integer.parseInt(activityToken);
            IMqttToken token = (IMqttToken)this.tokenMap.get(tokenNumber);
            this.tokenMap.delete(tokenNumber);
            return token;
        } else {
            return null;
        }
    }

    private synchronized IMqttToken getMqttToken(Bundle data) {
        String activityToken = data.getString("MqttService.activityToken");
        return (IMqttToken)this.tokenMap.get(Integer.parseInt(activityToken));
    }

    public void setBufferOpts(DisconnectedBufferOptions bufferOpts) {
        this.mqttService.setBufferOpts(this.clientHandle, bufferOpts);
    }

    public int getBufferedMessageCount() {
        return this.mqttService.getBufferedMessageCount(this.clientHandle);
    }

    public MqttMessage getBufferedMessage(int bufferIndex) {
        return this.mqttService.getBufferedMessage(this.clientHandle, bufferIndex);
    }

    public void deleteBufferedMessage(int bufferIndex) {
        this.mqttService.deleteBufferedMessage(this.clientHandle, bufferIndex);
    }

    public SSLSocketFactory getSSLSocketFactory(InputStream keyStore, String password) throws MqttSecurityException {
        try {
            SSLContext ctx = null;
            SSLSocketFactory sslSockFactory = null;
            KeyStore ts = KeyStore.getInstance("BKS");
            ts.load(keyStore, password.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ts);
            TrustManager[] tm = tmf.getTrustManagers();
            ctx = SSLContext.getInstance("TLSv1");
            ctx.init((KeyManager[])null, tm, (SecureRandom)null);
            sslSockFactory = ctx.getSocketFactory();
            return sslSockFactory;
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException var8) {
            throw new MqttSecurityException(var8);
        }
    }

    public void disconnectForcibly() throws MqttException {
        throw new UnsupportedOperationException();
    }

    public void disconnectForcibly(long disconnectTimeout) throws MqttException {
        throw new UnsupportedOperationException();
    }

    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        throw new UnsupportedOperationException();
    }

    public void unregisterResources() {
        if (this.myContext != null && this.receiverRegistered) {
            synchronized(this) {
                LocalBroadcastManager.getInstance(this.myContext).unregisterReceiver(this);
                this.receiverRegistered = false;
            }

            if (this.bindedService) {
                try {
                    this.myContext.unbindService(this.serviceConnection);
                    this.bindedService = false;
                } catch (IllegalArgumentException var3) {
                }
            }
        }

    }

    public void registerResources(Context context) {
        if (context != null) {
            this.myContext = context;
            if (!this.receiverRegistered) {
                this.registerReceiver(this);
            }
        }

    }

    private final class MyServiceConnection implements ServiceConnection {
        private MyServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder binder) {
            MqttAndroidClient.this.mqttService = ((MqttServiceBinder)binder).getService();
            MqttAndroidClient.this.bindedService = true;
            MqttAndroidClient.this.doConnect();
        }

        public void onServiceDisconnected(ComponentName name) {
            MqttAndroidClient.this.mqttService = null;
        }
    }

    public static enum Ack {
        AUTO_ACK,
        MANUAL_ACK;

        private Ack() {
        }
    }
}
