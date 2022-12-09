package com.wave_chtj.example.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.face_chtj.base_iotutils.KLog;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

public class BlueToothAty extends BaseActivity {
    public final static String TAG = BlueToothAty.class.getSimpleName();
    public final static UUID UUID_SERVER = UUID.fromString("0000181C-0000-1000-8000-00805F9B34FB");
    public final static UUID UUID_SERVER1 = UUID.fromString("0000181C-0000-1000-8000-00805F9B34FA");
    public final static UUID UUID_CHARREAD = UUID.fromString("0000C101-0000-1000-8000-00805F9B3401");
    public final static UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHARWRITE = UUID.fromString("0000C101-0000-1000-8000-00805F9B3402");
    public final static UUID UUID_WRITE = UUID.fromString("0000C101-0000-1000-8000-00805F9B3403");

    //从设备开发相关调用
    BluetoothGattCharacteristic characteristicnotify;
    BlePeripheralUtils blePeripheralUtils;
    TextView tvResult;
    /**
     * -------->>>>从设备开发相关回调
     */
    BlePeripheralCallback callback = new BlePeripheralCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            KLog.d("onConnectionStateChange() NAME >> " + device.getName() + ",MAC >> " + device.getAddress() + ",status >> " + status + ",newState >> " + blePeripheralUtils.getFormatState(newState));
            handler.sendMessage(handler.obtainMessage(0x100, "master mac >> " + device.getAddress() + " connect this Peripheral"));
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            try {
                String readStr = new String(requestBytes, "UTF-8");
                KLog.d("onCharacteristicWriteRequest() requestId >> " + requestId + ",characteristic >> " + characteristic + ",preparedWrite >> " + preparedWrite + ", responseNeeded >> " + responseNeeded + ", offset >> " + offset + ", readStr >> " + readStr);
                handler.sendMessage(handler.obtainMessage(0x100, "master writeDat >> " + readStr));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, "onCharacteristicWriteRequest: ", e);
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            tvResult.append(msg.obj.toString() + "\n");
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        tvResult = findViewById(R.id.tvResult);
        //初始化本机蓝牙设备作为master主机的蓝牙工具 按需使用 不需要主机master模式可不使用
        //在build.gradle中引用即可implementation 'com.github.Jasonchenlijian:FastBle:2.4.0'
        BleManager.getInstance().init(getApplication());

        //初始化从设备工具
        //不需要引用任何工具 androidApi自带此接口
        blePeripheralUtils = new BlePeripheralUtils(this);
        blePeripheralUtils.init();
        blePeripheralUtils.setBlePeripheralCallback(callback);
    }

    /**
     * -------->>>>从设备开发
     * 判断是否支持从设备开发
     */
    public void multipleAdvertisementSupportedClick(View view) {
        boolean isSupported = blePeripheralUtils.isMultipleAdvertisementSupported();
        tvResult.append("是否支持从设备开发：" + isSupported + "\n");
    }

    /**
     * -------->>>>从设备开发
     * 开启蓝牙模块的从设备功能
     */
    public void slaveDeviceStartClick(View view) {
        blePeripheralUtils.startBluetoothLeAdvertiser("ichtj_bla", "1111111".getBytes(), UUID_SERVER1);
        BluetoothGattCharacteristicInfo[] bluetoothGattCharacteristicInfos = new BluetoothGattCharacteristicInfo[3];
        BluetoothGattDescriptorInfo descriptorInfo = new BluetoothGattDescriptorInfo(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE);
        bluetoothGattCharacteristicInfos[0] = new BluetoothGattCharacteristicInfo(UUID_CHARREAD, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ, null);
        bluetoothGattCharacteristicInfos[1] = new BluetoothGattCharacteristicInfo(UUID_WRITE, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE, null);
        bluetoothGattCharacteristicInfos[2] = new BluetoothGattCharacteristicInfo(UUID_CHARWRITE, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PROPERTY_NOTIFY, descriptorInfo);
        BluetoothGattServiceInfo bluetoothGattServiceInfo1 = new BluetoothGattServiceInfo(UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY, bluetoothGattCharacteristicInfos);
        blePeripheralUtils.addServices(bluetoothGattServiceInfo1);
        tvResult.append("从设备开启\n");
    }

    /**
     * -------->>>>从设备开发
     * 发送通知到蓝牙连接master主机
     */
    public void notifyClick(View view) {
        String writeData = "AA BB CC DD EE FF";
        if (characteristicnotify == null) {
            characteristicnotify = blePeripheralUtils.getCharacteristic(UUID_SERVER, UUID_CHARWRITE);
        }
        if (characteristicnotify != null && blePeripheralUtils.getDeviceArrayList().size() > 0) {
            blePeripheralUtils.notify(blePeripheralUtils.getDeviceArrayList().get(0), characteristicnotify, writeData.getBytes());
        }
        tvResult.append("从设备发送通知 writeData >> " + writeData + "\n");
    }

    /**
     * 是否支持ble开发
     */
    public void supportBleClick(View view) {
        boolean isSupportBle = BleManager.getInstance().isSupportBle();
        tvResult.append("是否支持ble开发：" + isSupportBle + "\n");
    }

    /**
     * 启用蓝牙
     */
    public void enableBluetoothClick(View view) {
        BleManager.getInstance().enableBluetooth();
        tvResult.append("开启蓝牙\n");
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetoothClick(View view) {
        BleManager.getInstance().disableBluetooth();
        tvResult.append("关闭蓝牙\n");
    }

    /**
     * 初始化配置
     */
    public void initConfigurationClick(View view) {
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);
        tvResult.append("初始化配置\n");
    }

    /**
     * 初始化扫描规则
     */
    public void initScanRuleClick(View view) {
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);
        tvResult.append("初始化扫描规则\n");
    }

    /**
     * 本机蓝牙作为master主机模式扫描周围蓝牙设备
     */
    public void getScanClick(View view) {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                KLog.d("onScanStarted() success >> " + success);
                tvResult.append("开始扫描 >> " + success + "\n");
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                KLog.d("onScanStarted() NAME >> " + bleDevice.getName() + ", MAC >> " + bleDevice.getMac() + ", Rssi >> " + bleDevice.getRssi());
                tvResult.append("扫描中 >> " + bleDevice.getName() + " >> " + bleDevice.getMac() + "\n");
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                KLog.d("onScanStarted() bleDevice >> " + scanResultList.toString());
                tvResult.append("扫描结束 数量 >> " + scanResultList.size() + "\n");
            }
        });
    }

}
