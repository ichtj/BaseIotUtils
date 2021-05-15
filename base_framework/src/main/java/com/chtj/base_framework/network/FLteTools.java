package com.chtj.base_framework.network;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.chtj.base_framework.FBaseTools;

import java.lang.reflect.Method;

public class FLteTools {
    private static final String TAG = "FLteTools";
    private static FLteTools sInstance;
    PhoneStateListener phoneStateListener;
    TelephonyManager tm;

    /**
     * 单例模式
     *
     * @return
     */
    public static FLteTools instance() {
        if (sInstance == null) {
            synchronized (FLteTools.class) {
                if (sInstance == null) {
                    sInstance = new FLteTools();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取4G信号
     * 这里说的大于>-90 是指越接近正数信号越好
     * @param netDbmListener
     */
    public void init4GDbm(NetDbmListener netDbmListener) {
        tm = (TelephonyManager) FBaseTools.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String dbmAsu = 0 + " dBm " + 0 + " asu";
                try {
                    Method method1 = signalStrength.getClass().getMethod("getDbm");
                    int signalDbm = (int) method1.invoke(signalStrength);
                    method1 = signalStrength.getClass().getMethod("getAsuLevel");
                    int signalAsu = (int) method1.invoke(signalStrength);
                    if (-1 == signalDbm) {
                        signalDbm = 0;
                    }
                    if (-1 == signalAsu) {
                        signalAsu = 0;
                    }
                    dbmAsu = signalDbm + " dBm " + signalAsu + " asu";
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "errMeg:" + e.getMessage());
                    dbmAsu = 0 + " dBm " + 0 + " asu";
                }
                if (netDbmListener != null) {
                    netDbmListener.getDbm(dbmAsu);
                }
            }
        };
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                | PhoneStateListener.LISTEN_SERVICE_STATE);
    }

    /**
     * 关闭4G信号监听
     */
    public void cancelTelephonyListener() {
        tm.listen(phoneStateListener,
                PhoneStateListener.LISTEN_NONE);
        tm = null;
        phoneStateListener = null;
    }

}
