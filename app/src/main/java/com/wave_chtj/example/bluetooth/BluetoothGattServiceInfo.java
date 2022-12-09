package com.wave_chtj.example.bluetooth;


import java.util.UUID;

public class BluetoothGattServiceInfo {

    //Service的UUID
    private UUID uuid;
    //服务的类型 SERVICE_TYPE_PRIMARY /SERVICE_TYPE_SECONDARY
    private int serviceType;
    //该service下的characteristic
    private BluetoothGattCharacteristicInfo[] characteristicInfos;



    public BluetoothGattServiceInfo(){

    }
    public BluetoothGattServiceInfo(UUID uuid,int serviceType,BluetoothGattCharacteristicInfo[] characteristicInfos){
        this.uuid = uuid;
        this.serviceType = serviceType;
        this.characteristicInfos = characteristicInfos;

    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public BluetoothGattCharacteristicInfo[] getCharacteristicInfos() {
        return characteristicInfos;
    }

    public void setCharacteristicInfos(BluetoothGattCharacteristicInfo[] characteristicInfos) {
        this.characteristicInfos = characteristicInfos;
    }


}
