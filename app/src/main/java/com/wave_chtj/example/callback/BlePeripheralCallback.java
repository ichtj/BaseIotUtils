package com.wave_chtj.example.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

public interface BlePeripheralCallback {
    /**
     * 连接状态改变
     * @param device 设备信息
     * @param status 状态
     * @param newState
     */
    void onConnectionStateChange(BluetoothDevice device, int status, int newState);

    /**
     * 写特征回调
     * @param device
     * @param requestId
     * @param characteristic
     * @param preparedWrite
     * @param responseNeeded
     * @param offset
     * @param requestBytes
     */
    void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes);
}
