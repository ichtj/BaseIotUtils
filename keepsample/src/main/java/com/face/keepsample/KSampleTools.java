package com.face.keepsample;

import com.chtj.keepalive.FKeepAliveTools;
import com.chtj.keepalive.entity.KeepAliveData;

import java.util.ArrayList;
import java.util.List;

public class KSampleTools {
    /**
     * 应用首次启动默认添加一些数据
     * @return
     */
    public static List<KeepAliveData> getDefaultInitData() {
        List<KeepAliveData> keepAliveDataList = new ArrayList<>();
        //默认添加IotCloud APK
        keepAliveDataList.add(new KeepAliveData("com.face.baseiotcloud", FKeepAliveTools.TYPE_SERVICE,"com.face.baseiotcloud.service.OtherService",true));
        //客户应用
        //keepAliveDataList.add(new KeepAliveData("com.ss.testserial", FKeepAliveTools.TYPE_ACTIVITY, true));
        //这里可以添加其他的应用
        return keepAliveDataList;
    }
}
