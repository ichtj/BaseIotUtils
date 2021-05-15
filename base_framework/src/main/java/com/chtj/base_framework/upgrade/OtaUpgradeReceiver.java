package com.chtj.base_framework.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class OtaUpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = "OtaUpgradeReceiver";
    private static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED://开机完成
                Log.d(TAG, "onReceive: action=" + action);
                Intent bootIntent = new Intent(context, FUpgradeService.class);
                bootIntent.putExtra("action", "reboot");
                context.startService(bootIntent);
                break;
            case Intent.ACTION_MEDIA_MOUNTED://设备接入
                String inOtaPath = intent.getData().toString().replace("file://","");
                if(!inOtaPath.contains("storage/emulated")){//防止系统重启完成之后挂载了sdcard对此服务造成影响
                    Log.d(TAG, "onReceive: action=" + action + ", inOtaPath=" + inOtaPath);
                    Intent serviceIntent = new Intent(context, FUpgradeService.class);
                    serviceIntent.putExtra("action", "connect");
                    serviceIntent.putExtra("otaPath", inOtaPath);
                    context.startService(serviceIntent);
                }
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED://设备卸载
                String unOtaPath = intent.getData().toString().replace("file://","");
                Log.d(TAG, "onReceive: action=" + action + ", unOtaPath=" + unOtaPath);
                Intent unMounted = new Intent(context, FUpgradeService.class);
                unMounted.putExtra("action", "disconnect");
                unMounted.putExtra("otaPath", unOtaPath);
                context.startService(unMounted);
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
