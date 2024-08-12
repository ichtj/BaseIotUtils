package com.ichtj.basetools.mqtt;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.jrummyapps.android.shell.CommandResult;
import com.jrummyapps.android.shell.Shell;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttTestAty extends BaseActivity {
    private static final String TAG=MqttTestAty.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        String topic        = "MQTT Examples";
        String content      = "Message from MqttPublishSample";
        int qos             = 2;
        String broker       = "tcp://835db6b12b.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883";
        String clientId     = "SN-007_0_0_2024072611";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("SN-007");
            char[] pwd = "8dd9358e7d18112f5a4874b8e93f4e07edb84d39a32db48f42b8d5367e22c016".toCharArray();
            connOpts.setPassword(pwd);
            connOpts.setCleanSession(true);
            Log.d(TAG,"Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            Log.d(TAG,"Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            Log.d(TAG,"Message published");
//            sampleClient.disconnect();
            Log.d(TAG,"Disconnected");
//            System.exit(0);
        } catch(MqttException me) {
            Log.d(TAG,"reason "+me.getReasonCode());
            Log.d(TAG,"msg "+me.getMessage());
            Log.d(TAG,"loc "+me.getLocalizedMessage());
            Log.d(TAG,"cause "+me.getCause());
            Log.d(TAG,"excep "+me);
            me.printStackTrace();
        }catch(Throwable me) {
            Log.d(TAG,"2>msg "+me.getMessage());
            Log.d(TAG,"2>loc "+me.getLocalizedMessage());
            Log.d(TAG,"2>cause "+me.getCause());
            Log.d(TAG,"2>excep "+me);
            me.printStackTrace();
        }
    }
}
