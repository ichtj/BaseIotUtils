package com.face_chtj.base_iotutils.network;

import com.face_chtj.base_iotutils.entity.NetTypeInfo;

/**
 * Create on 2020/1/3
 * author chtj
 * desc
 */
public interface OnNetChangeLinstener {
    void changed(NetTypeInfo type, boolean isNormal);
}
