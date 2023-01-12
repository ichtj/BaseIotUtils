package com.wave_chtj.example.callback;

import android.hardware.usb.UsbDevice;

/**
 * Create on 2020/6/22
 * author chtj
 * desc 监听usb设备回调接口
 */
public interface IUsbHubListener {
    /**
     * 设备信息
     * @param isConn true 接入 | false 拔出
     */
    void deviceInfo(String action,String path,boolean isConn);

}
