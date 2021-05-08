//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.Build.VERSION;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;

class AlarmPingSender implements MqttPingSender {
    private static final String TAG = "AlarmPingSender";
    private ClientComms comms;
    private MqttService service;
    private BroadcastReceiver alarmReceiver;
    private AlarmPingSender that;
    private PendingIntent pendingIntent;
    private volatile boolean hasStarted = false;

    public AlarmPingSender(MqttService service) {
        if (service == null) {
            throw new IllegalArgumentException("Neither service nor client can be null.");
        } else {
            this.service = service;
            this.that = this;
        }
    }

    public void init(ClientComms comms) {
        this.comms = comms;
        this.alarmReceiver = new AlarmPingSender.AlarmReceiver();
    }

    public void start() {
        String action = "MqttService.pingSender." + this.comms.getClient().getClientId();
        //Log.d("AlarmPingSender", "Register alarmreceiver to MqttService" + action);
        this.service.registerReceiver(this.alarmReceiver, new IntentFilter(action));
        this.pendingIntent = PendingIntent.getBroadcast(this.service, 0, new Intent(action), 134217728);
        this.schedule(this.comms.getKeepAlive());
        this.hasStarted = true;
    }

    public void stop() {
        //Log.d("AlarmPingSender", "Unregister alarmreceiver to MqttService" + this.comms.getClient().getClientId());
        if (this.hasStarted) {
            if (this.pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager)this.service.getSystemService("alarm");
                alarmManager.cancel(this.pendingIntent);
            }

            this.hasStarted = false;

            try {
                this.service.unregisterReceiver(this.alarmReceiver);
            } catch (IllegalArgumentException var2) {
            }
        }

    }

    public void schedule(long delayInMilliseconds) {
        long nextAlarmInMilliseconds = System.currentTimeMillis() + delayInMilliseconds;
        //Log.d("AlarmPingSender", "Schedule next alarm at " + nextAlarmInMilliseconds);
        AlarmManager alarmManager = (AlarmManager)this.service.getSystemService("alarm");
        if (VERSION.SDK_INT >= 23) {
            //Log.d("AlarmPingSender", "Alarm scheule using setExactAndAllowWhileIdle, next: " + delayInMilliseconds);
            alarmManager.setExactAndAllowWhileIdle(0, nextAlarmInMilliseconds, this.pendingIntent);
        } else if (VERSION.SDK_INT >= 19) {
            //Log.d("AlarmPingSender", "Alarm scheule using setExact, delay: " + delayInMilliseconds);
            alarmManager.setExact(0, nextAlarmInMilliseconds, this.pendingIntent);
        } else {
            alarmManager.set(0, nextAlarmInMilliseconds, this.pendingIntent);
        }

    }

    class AlarmReceiver extends BroadcastReceiver {
        private WakeLock wakelock;
        private final String wakeLockTag;

        AlarmReceiver() {
            this.wakeLockTag = "MqttService.client." + AlarmPingSender.this.that.comms.getClient().getClientId();
        }

        @SuppressLint({"Wakelock"})
        public void onReceive(Context context, Intent intent) {
            //Log.d("AlarmPingSender", "Sending Ping at:" + System.currentTimeMillis());
            PowerManager pm = (PowerManager)AlarmPingSender.this.service.getSystemService("power");
            this.wakelock = pm.newWakeLock(1, this.wakeLockTag);
            this.wakelock.acquire();
            IMqttToken token = AlarmPingSender.this.comms.checkForActivity(new IMqttActionListener() {
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Log.d("AlarmPingSender", "Success. Release lock(" + AlarmReceiver.this.wakeLockTag + "):" + System.currentTimeMillis());
                    AlarmReceiver.this.wakelock.release();
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //Log.d("AlarmPingSender", "Failure. Release lock(" + AlarmReceiver.this.wakeLockTag + "):" + System.currentTimeMillis());
                    AlarmReceiver.this.wakelock.release();
                }
            });
            if (token == null && this.wakelock.isHeld()) {
                this.wakelock.release();
            }

        }
    }
}
