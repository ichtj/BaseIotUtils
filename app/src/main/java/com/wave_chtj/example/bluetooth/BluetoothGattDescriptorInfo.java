package com.wave_chtj.example.bluetooth;

import java.util.UUID;

public class BluetoothGattDescriptorInfo {
    //描述者的UUID
    private UUID uuid;
    //描述者的权限
    int permissions;

    public BluetoothGattDescriptorInfo(UUID uuid,int permissions){
        this.uuid = uuid;
        this.permissions = permissions;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }
}
