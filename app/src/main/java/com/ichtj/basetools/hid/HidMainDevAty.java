package com.ichtj.basetools.hid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.PACKAGES;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Route(path = PACKAGES.BASE + "hidMainDev")
public class HidMainDevAty extends BaseActivity {
    private static final String TAG = HidMainDevAty.class.getSimpleName();
    Spinner spSubDev;
    TextView tvResult;
    EditText etData;
    RadioButton rbHex;
    RadioButton rbAscii;
    boolean isReceiveData=true;
    List<UsbDevice> subDevList = new ArrayList<>();
    private static final String ACTION_USB_PERMISSION = "com.example.ACTION_USB_PERMISSION";

    private UsbManager usbManager;
    private UsbAccessory usbAccessory;
    private PendingIntent permissionIntent;
    private ParcelFileDescriptor fileDescriptor;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action>>"+action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "Permission denied for accessory " + accessory);
                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hid_main_dev);
        spSubDev = findViewById(R.id.spSubDev);
        rbAscii = findViewById(R.id.rbAscii);
        rbHex = findViewById(R.id.rbHex);
        etData = findViewById(R.id.etData);
        tvResult = findViewById(R.id.tvResult);
        FormatViewUtils.setMovementMethod(tvResult);
        initSubDev();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] usbAccessories=usbManager.getAccessoryList();
        Log.d(TAG, "onCreate: "+usbAccessories);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        // 注册 USB 权限广播接收器
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
    }

    private void initSubDev() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devMap = manager.getDeviceList();
        if (devMap != null && devMap.size() > 0) {
            subDevList = new ArrayList<>();
            Set<String> usbDeviceSet = devMap.keySet();
            for (String key : usbDeviceSet) {
                subDevList.add(devMap.get(key));
            }
            HidAdapter adapter = new HidAdapter(this, subDevList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSubDev.setAdapter(adapter);
            FormatViewUtils.formatData(tvResult, "subDevList>>" + devMap.size());
        } else {
            FormatViewUtils.formatData(tvResult, "HID子设备列表获取为空,请确认是否支持。");
        }
    }


    public void connectDevClick(View view) {
        UsbDevice usbDevice = (UsbDevice) spSubDev.getSelectedItem();
        // 请求USB权限
        for (int i = 0; i < subDevList.size(); i++) {
            UsbDevice device = subDevList.get(i);
            if (device.getVendorId() == usbDevice.getVendorId() && device.getProductId() == usbDevice.getProductId()) {
                usbDevice = device;
                PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(usbDevice, permissionIntent);
                break;
            }
        }
    }

    public void sendCmdClick(View view) {

    }

    // 向 USB 主机端发送数据
    private void sendDataToHost(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                outputStream.flush();
                Log.d(TAG, "Sent data: " + data);
            } catch (Throwable e) {
                Log.e(TAG, "Error writing to accessory", e);
            }
        }
    }


    private void openAccessory(UsbAccessory accessory) {
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);

            // 创建读取数据的线程
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[16384];
                    try {
                        while (true) {
                            int bytesRead = inputStream.read(buffer);
                            if (bytesRead > 0) {
                                // 处理从 USB 主机端接收到的数据
                                final String receivedData = new String(buffer, 0, bytesRead);
                                Log.d(TAG, "Received data: " + receivedData);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading from accessory", e);
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }
}
