package com.face_chtj.base_iotutils.network;

import com.face_chtj.base_iotutils.enums.NET_TYPE;

/**
 * Create on 2020/1/3
 * author chtj
 * desc
 */
public interface OnNetChangeLinstener {
    void changed(NET_TYPE type, boolean isNormal);
}
