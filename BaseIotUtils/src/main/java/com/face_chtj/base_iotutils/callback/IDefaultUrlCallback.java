package com.face_chtj.base_iotutils.callback;


import com.face_chtj.base_iotutils.entity.BaseUrlBean;

import java.util.Map;

/**
 * 添加默认的baseUrl列表
 */
public interface IDefaultUrlCallback {
    Map<String, BaseUrlBean> baseUrlValues();
}
