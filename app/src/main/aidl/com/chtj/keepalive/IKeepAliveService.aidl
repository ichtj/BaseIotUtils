// IKeepAliveService.aidl

//外部需要拉起的Activity或者Service
//必须要求Service设置属性 用于其他进程访问
//android:exported="true"
//android:enabled="true"
//addKeepLiveInfo()  用于外部添加需要保活的Activity或者Service
//getKeepLiveInfo()  获取所有的保活的内容
package com.chtj.keepalive;

// Declare any non-default types here with import statements

import com.chtj.keepalive.IKeepAliveListener;
import com.chtj.keepalive.entity.KeepAliveData;

interface IKeepAliveService {
       boolean addKeepLiveInfo(in KeepAliveData info,IKeepAliveListener listener);//
       List<KeepAliveData> getKeepLiveInfo();
       boolean clearAllKeepAliveInfo();
}
