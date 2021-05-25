package com.wave_chtj.example.util.keyevent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;

/**
 * Create on 2020/6/18
 * author chtj
 * desc 监听Usb设备
 */
public class UsbHubTools extends BroadcastReceiver  {
    private static final String TAG = "UsbHubDeviceTools";

    private static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";

    private static UsbHubTools usbHubTools;

    IUsbHubListener mIUsbHubListener;

    public static UsbHubTools getInstance() {
        if (usbHubTools == null) {
            synchronized (UsbHubTools.class) {
                if (usbHubTools == null) {
                    usbHubTools = new UsbHubTools();
                }
            }
        }
        return usbHubTools;
    }

    public void setIUsbDeviceListener(IUsbHubListener IUsbHubListener) {
        mIUsbHubListener = IUsbHubListener;
    }


    /**
     * 注册广播 用于监听usb设备接入
     */
    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_STATE);
        filter.addDataScheme("file");
        BaseIotUtils.getContext().registerReceiver(this, filter);
    }


    /**
     * 销毁监听usb设备的广播
     */
    public void unRegisterReceiver() {
        try{
            if (BaseIotUtils.getContext() != null&& usbHubTools !=null) {
                BaseIotUtils.getContext().unregisterReceiver(usbHubTools);
            }
        }catch(Exception e){
            e.printStackTrace();
            KLog.e(TAG,"errMeg:this broadcastReceiver not register");
        }
        if (mIUsbHubListener != null) {
            mIUsbHubListener = null;
        }
        if (usbHubTools != null) {
            usbHubTools = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        KLog.d(TAG,"onReceive:>action="+action);
        switch (action){
            case Intent.ACTION_MEDIA_MOUNTED://设备接入
                String inOtaPath = intent.getData().toString().replace("file://","");
                mIUsbHubListener.deviceInfo(action,inOtaPath,true);
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED://设备卸载
                String unOtaPath = intent.getData().toString().replace("file://","");
                mIUsbHubListener.deviceInfo(action,unOtaPath,false);
                break;
            case ACTION_USB_STATE://usb状态
                Log.d(TAG, "onReceive: action=" + action);
                break;
            case UsbManager.ACTION_USB_DEVICE_ATTACHED://usb设备已接入
            case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                //UsbDevice idevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                //Log.d(TAG, "onReceive: action=" + action + ", USB Connected.. idevice=" + idevice);
                //Log.d(TAG, "onReceive: device ATTACHED");
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED://usb设备已卸载
            case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                //UsbDevice odevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                //Log.d(TAG, "onReceive: action=" + action + ", USB DisConnected..");
                //Log.d(TAG, "onReceive: device DETACHED");
                break;
        }

    }
}
