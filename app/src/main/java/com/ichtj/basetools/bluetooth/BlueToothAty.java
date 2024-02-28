package com.ichtj.basetools.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.TimeUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.callback.BlePeripheralCallback;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import top.wuhaojie.bthelper.BtHelperClient;
import top.wuhaojie.bthelper.OnSearchDeviceListener;

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
    BluetoothDevice bindDevice;

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
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
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
     * 显示数据到UI
     *
     * @param htmlStr
     */
    public void showData(String htmlStr) {
        tvResult.append(Html.fromHtml(TimeUtils.getTodayDateHms("yy-MM-dd HH:mm:ss") + "：" + htmlStr));
        tvResult.append("\n");
        //刷新最新行显示
        int offset = tvResult.getLineCount() * tvResult.getLineHeight();
        int tvHeight = tvResult.getHeight();
        if (offset > 6000) {
            tvResult.setText("");
            tvResult.scrollTo(0, 0);
        } else {
            if (offset > tvHeight) {
                //Log.d(TAG, "showData: offset >> " + offset + " ,tvHeight >> " + tvHeight);
                tvResult.scrollTo(0, offset - tvHeight);
            }
        }
    }

    /**
     * -------->>>>从设备开发
     * 判断是否支持从设备开发
     */
    public void multipleAdvertisementSupportedClick(View view) {
        boolean isSupported = blePeripheralUtils.isMultipleAdvertisementSupported();
        showData("是否支持从设备开发：" + isSupported + "\n");
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
        showData("从设备开启\n");
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
        showData("从设备发送通知 writeData >> " + writeData + "\n");
    }

    /**
     * 清空日志
     */
    public void clearResultClick(View view) {
        tvResult.setText("");
    }

    /**
     * 是否支持ble开发
     */
    public void supportBleClick(View view) {
        boolean isSupportBle = BleManager.getInstance().isSupportBle();
        showData("是否支持ble开发：" + isSupportBle + "\n");
    }

    /**
     * 启用蓝牙
     */
    public void enableBluetoothClick(View view) {
        BleManager.getInstance().enableBluetooth();
        showData("开启蓝牙\n");
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetoothClick(View view) {
        BleManager.getInstance().disableBluetooth();
        showData("关闭蓝牙\n");
    }


    /**
     * BT蓝牙设备扫描
     * i7mini, MAC >> E6:0D:CB:B5:39:38, BondState >> 10
     */
    public void btDeviceScanClick(View view) {
        showData("BT蓝牙扫描\n");
        BtHelperClient.from(this).searchDevices(new OnSearchDeviceListener() {
            @Override
            public void onStartDiscovery() {
                showData("BT开始查询\n");
            }

            @Override
            public void onNewDeviceFounded(BluetoothDevice bluetoothDevice) {
            }

            @Override
            public void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList) {
                for (int i = 0; i < bondedList.size(); i++) {
                    String name=bondedList.get(i).getName();
                    String content = "BT>>bonded>>NAME >> " + name + ", MAC >> " + bondedList.get(i).getAddress() + ", BondState >> " + bondedList.get(i).getBondState();
                    if (!TextUtils.isEmpty(name)&&name.contains("i7mini")) {
                        bindDevice=newList.get(i);
                    }
                    KLog.d(content);
                    showData(content);
                }
                for (int i = 0; i < newList.size(); i++) {
                    String name=newList.get(i).getName();
                    if (!TextUtils.isEmpty(name)&&name.contains("i7mini")) {
                        bindDevice=newList.get(i);
                    }
                    String content = "BT>>newList>>NAME >> " + name + ", MAC >> " + newList.get(i).getAddress() + ", BondState >> " + newList.get(i).getBondState();
                    KLog.d(content);
                    showData(content);
                }
                showData("BLE>>扫描结束数量 " + (newList.size() + bondedList.size()));
                BtHelperClient.from(BlueToothAty.this).close();
            }

            @Override
            public void onError(Exception e) {
                showData("经典蓝牙查询错误\n");
            }
        });
    }


    public void getBatteryValueClick(View view) {
        try {
            // 根据设备的名称或地址来确定你需要的设备
            if (bindDevice.getName().contains("i7mini")) {
                // 连接到设备
                // 你可能需要实现 BluetoothGattCallback 来处理连接状态和数据通信
                // 这里只是简单的示例，具体实现可能会更复杂
                BluetoothGatt gatt = bindDevice.connectGatt(this, false, gattCallback);
            }
        } catch (Throwable throwable) {
            KLog.d(throwable.getMessage());
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            KLog.d("onConnectionStateChange>>" + gatt.toString() + ",status>>" + status + ",newState>>" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                // 连接成功后开始发现服务
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            KLog.d("onServicesDiscovered>>" + gatt.toString() + ",status>>" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 获取服务列表
                List<BluetoothGattService> services = gatt.getServices();

                // 遍历服务列表查找你需要的特定服务和特征
                for (BluetoothGattService service : services) {
                    KLog.d("onServicesDiscovered>>uuid>>" + service.getUuid());
                    // 这里的 UUID 需要根据你需要获取电量的设备来确定
                    if (service.getUuid().equals(UUID.fromString("7a68d6d7-755c-4dbc-9613-2a11d062dba1"))) {
                        // 获取特定特征
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("edbc66bc-8e55-4776-b93b-d9b4afdde5ff"));
                        // 读取特征的值，这可能需要先设置相应的 descriptor
                        gatt.readCharacteristic(characteristic);
                        break;
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            KLog.d("onCharacteristicRead>>" + gatt.toString() + ",characteristic>>" + characteristic.toString() + ",status>>" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 获取特征的值
                byte[] value = characteristic.getValue();
                KLog.d("onCharacteristicRead>>value" + value);
                // 解析特征的值来获取电量信息
                // 这取决于你所连接的蓝牙设备的协议和数据格式
            }
        }
    };


    /**
     * 初始化配置
     */
    public void initConfigurationClick(View view) {
        BleManager.getInstance().enableLog(true).setReConnectCount(1, 5000).setSplitWriteNum(20).setConnectOverTime(10000).setOperateTimeout(5000);
        showData("初始化配置\n");
    }

    /**
     * 初始化扫描规则
     */
    public void initScanRuleClick(View view) {
        BleManager.getInstance().enableLog(true).setReConnectCount(1, 5000).setSplitWriteNum(20).setConnectOverTime(10000).setOperateTimeout(5000);
        showData("初始化扫描规则\n");
    }

    /**
     * 本机蓝牙作为master主机模式扫描周围蓝牙设备
     */
    public void getScanClick(View view) {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                KLog.d("onScanStarted() success >> " + success);
                showData("BLE>>开始扫描 >> " + success + "\n");
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                KLog.d("onScanning>>NAME >> " + bleDevice.getName() + ", MAC >> " + bleDevice.getMac() + ", Rssi >> " + bleDevice.getRssi());
                showData("BLE>>onScanning>> " + bleDevice.getName() + " >> " + bleDevice.getMac() + "\n");
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                KLog.d("onScanFinished() bleDevice >> " + scanResultList.toString());
                showData("BLE>>扫描结束 数量 >> " + scanResultList.size() + "\n");
            }
        });
    }
}
