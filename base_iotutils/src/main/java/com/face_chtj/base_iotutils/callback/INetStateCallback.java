package com.face_chtj.base_iotutils.callback;
/**
 * Create on 2020/1/3
 * author chtj
 * desc
 */
public interface INetStateCallback {
    void changed(int netType, boolean isNormal);
}
