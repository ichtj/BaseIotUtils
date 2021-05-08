//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

class MqttTokenAndroid implements IMqttToken {
    private IMqttActionListener listener;
    private volatile boolean isComplete;
    private volatile MqttException lastException;
    private Object waitObject;
    private MqttAndroidClient client;
    private Object userContext;
    private String[] topics;
    private IMqttToken delegate;
    private MqttException pendingException;

    MqttTokenAndroid(MqttAndroidClient client, Object userContext, IMqttActionListener listener) {
        this(client, userContext, listener, (String[])null);
    }

    MqttTokenAndroid(MqttAndroidClient client, Object userContext, IMqttActionListener listener, String[] topics) {
        this.waitObject = new Object();
        this.client = client;
        this.userContext = userContext;
        this.listener = listener;
        this.topics = topics;
    }

    public void waitForCompletion() throws MqttException, MqttSecurityException {
        synchronized(this.waitObject) {
            try {
                this.waitObject.wait();
            } catch (InterruptedException var4) {
            }
        }

        if (this.pendingException != null) {
            throw this.pendingException;
        }
    }

    public void waitForCompletion(long timeout) throws MqttException, MqttSecurityException {
        synchronized(this.waitObject) {
            try {
                this.waitObject.wait(timeout);
            } catch (InterruptedException var6) {
            }

            if (!this.isComplete) {
                throw new MqttException(32000);
            } else if (this.pendingException != null) {
                throw this.pendingException;
            }
        }
    }

    void notifyComplete() {
        synchronized(this.waitObject) {
            this.isComplete = true;
            this.waitObject.notifyAll();
            if (this.listener != null) {
                this.listener.onSuccess(this);
            }

        }
    }

    void notifyFailure(Throwable exception) {
        synchronized(this.waitObject) {
            this.isComplete = true;
            if (exception instanceof MqttException) {
                this.pendingException = (MqttException)exception;
            } else {
                this.pendingException = new MqttException(exception);
            }

            this.waitObject.notifyAll();
            if (exception instanceof MqttException) {
                this.lastException = (MqttException)exception;
            }

            if (this.listener != null) {
                this.listener.onFailure(this, exception);
            }

        }
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    void setComplete(boolean complete) {
        this.isComplete = complete;
    }

    public MqttException getException() {
        return this.lastException;
    }

    void setException(MqttException exception) {
        this.lastException = exception;
    }

    public IMqttAsyncClient getClient() {
        return this.client;
    }

    public void setActionCallback(IMqttActionListener listener) {
        this.listener = listener;
    }

    public IMqttActionListener getActionCallback() {
        return this.listener;
    }

    public String[] getTopics() {
        return this.topics;
    }

    public void setUserContext(Object userContext) {
        this.userContext = userContext;
    }

    public Object getUserContext() {
        return this.userContext;
    }

    void setDelegate(IMqttToken delegate) {
        this.delegate = delegate;
    }

    public int getMessageId() {
        return this.delegate != null ? this.delegate.getMessageId() : 0;
    }

    public MqttWireMessage getResponse() {
        return this.delegate.getResponse();
    }

    public boolean getSessionPresent() {
        return this.delegate.getSessionPresent();
    }

    public int[] getGrantedQos() {
        return this.delegate.getGrantedQos();
    }
}
