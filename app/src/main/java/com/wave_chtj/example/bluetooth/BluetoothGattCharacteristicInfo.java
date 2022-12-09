package com.wave_chtj.example.bluetooth;

import java.util.UUID;

public class BluetoothGattCharacteristicInfo {
    //Characteristic的UUID
    private UUID uuid;
    //Characteristic的属性
    private int properties;
    //Characteristic的权限
    private int permissions;
    //该service下的Descriptor
    private  BluetoothGattDescriptorInfo bluetoothGattDescriptorInfo;

    public BluetoothGattCharacteristicInfo(UUID uuid,int properties,int permissions,BluetoothGattDescriptorInfo bluetoothGattDescriptorInfo){
        this.uuid = uuid;
        this.properties = properties;
        this.permissions = permissions;
        this.bluetoothGattDescriptorInfo = bluetoothGattDescriptorInfo;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getProperties() {
        return properties;
    }

    public void setProperties(int properties) {
        this.properties = properties;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public BluetoothGattDescriptorInfo getBluetoothGattDescriptorInfo() {
        return bluetoothGattDescriptorInfo;
    }

    public void setBluetoothGattDescriptorInfo(BluetoothGattDescriptorInfo bluetoothGattDescriptorInfo) {
        this.bluetoothGattDescriptorInfo = bluetoothGattDescriptorInfo;
    }
}
