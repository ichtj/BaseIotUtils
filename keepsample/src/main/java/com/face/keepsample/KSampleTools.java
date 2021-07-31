package com.face.keepsample;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.chtj.keepalive.FKeepAliveTools;
import com.chtj.keepalive.entity.KeepAliveData;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;

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
        keepAliveDataList.add(new KeepAliveData("com.zgkx.change", FKeepAliveTools.TYPE_ACTIVITY, true));
        //这里可以添加其他的应用
        return keepAliveDataList;
    }

    /**
     * 通过包名获取应用程序的名称。
     *
     * @param packageName
     *            包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public static String getAppNameByPkg(String packageName) {
        PackageManager pm = BaseIotUtils.getContext().getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 通过包名获取应用程序的名称。
     *
     * @param packageName
     *            包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public static Drawable getAppIconByPkg(String packageName) {
        try {
            ApplicationInfo aInfo =BaseIotUtils.getContext(). getPackageManager().getApplicationInfo(packageName,PackageManager.GET_META_DATA);
            Drawable icon = BaseIotUtils.getContext().getPackageManager().getApplicationIcon(aInfo);
            return icon;
        }catch (Exception e){
            e.printStackTrace();
            return ContextCompat.getDrawable(BaseIotUtils.getContext(),R.mipmap.load_err);
        }
    }

}
