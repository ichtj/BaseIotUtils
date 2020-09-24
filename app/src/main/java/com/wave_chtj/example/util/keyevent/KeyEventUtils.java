package com.wave_chtj.example.util.keyevent;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.keeplive.BaseIotUtils;

/**
 * Create on 2020/6/18
 * author chtj
 * desc 监听Usb设备
 * 目前只针对android7.1,5.1使用
 */
public class KeyEventUtils extends BroadcastReceiver  {
    private static final String TAG = "KeyEventUtils";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static KeyEventUtils keyEventUtils;

    IUsbDeviceListener mIUsbDeviceListener;
    UsbManager mUsbManager;

    public static KeyEventUtils getInstance() {
        if (keyEventUtils == null) {
            synchronized (KeyEventUtils.class) {
                if (keyEventUtils == null) {
                    keyEventUtils = new KeyEventUtils();
                }
            }
        }
        return keyEventUtils;
    }

    public void setIUsbDeviceListener(IUsbDeviceListener IUsbDeviceListener) {
        mIUsbDeviceListener = IUsbDeviceListener;
    }


    /**
     * 注册广播 用于监听usb设备接入
     */
    public void registerReceiver() {
        mUsbManager = (UsbManager)BaseIotUtils.getContext(). getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        BaseIotUtils.getContext().registerReceiver(this, filter);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(BaseIotUtils.getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        //KLog.d(TAG, "devicelist: "+mUsbManager.getDeviceList());
        //here do emulation to ask all connected usb device for permission
        for (UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if(mUsbManager.hasPermission(usbDevice)){
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
            }else{
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }



    private void afterGetUsbPermission(UsbDevice usbDevice){
        //call method to set up device communication
        //Toast.makeText(this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();

        doYourOpenUsbDevice(usbDevice);
    }
    private void doYourOpenUsbDevice(UsbDevice usbDevice){
        //now follow line will NOT show: User has not given permission to device UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        //add your operation code here
    }

    /**
     * 销毁监听usb设备的广播
     */
    public void unRegisterReceiver() {
        try{
            if (BaseIotUtils.getContext() != null&&keyEventUtils!=null) {
                BaseIotUtils.getContext().unregisterReceiver(keyEventUtils);
            }
        }catch(Exception e){
            e.printStackTrace();
            KLog.e(TAG,"errMeg:this broadcastReceiver not register");
        }
        if (mIUsbDeviceListener != null) {
            mIUsbDeviceListener = null;
        }
        if (keyEventUtils != null) {
            keyEventUtils = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        KLog.d(TAG, "onReceive: action="+action);
        synchronized (this) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                if (device != null) {
                    //call method to set up device communication
                    afterGetUsbPermission(device);
                }
            } else {
                if(keyEventUtils!=null&&keyEventUtils.mIUsbDeviceListener!=null||device!=null){
                    KLog.d(TAG, "device: "+device.toString());
                    if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                        keyEventUtils.mIUsbDeviceListener.deviceInfo(device,true);
                    } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                        keyEventUtils.mIUsbDeviceListener.deviceInfo(device,false);
                    }
                }
            }
        }
    }
}
