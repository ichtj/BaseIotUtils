package com.chtj.keepalive;

import android.util.Log;

import com.chtj.keepalive.entity.CommonValue;
import com.chtj.keepalive.entity.KeepAliveData;
import com.chtj.keepalive.service.FKeepAliveService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 应用保活
 * 1.用于本地调用添加Activity或者Service保活
 * 2.用于跨进程请使用aidl服务进行添加Activity或者Service保活
 * ----将src/main/aidl目录复制到自己的项目中，然后初始化后调用
 */
public class FKeepAliveTools {


    private static final String TAG = "FKeepAliveTools";
    /**
     * 保活的类型为Activity
     */
    public static final int TYPE_ACTIVITY = 0;
    /**
     * 保活的类型为服务
     */
    public static final int TYPE_SERVICE = 1;

    /**
     * 添加多个保活对象
     * @param keepAliveDataList
     * @return 执行结果
     */
    public static CommonValue addMoreData(List<KeepAliveData> keepAliveDataList) {
        Gson gson=new Gson();
        File file = new File(FileCommonTools.SAVE_KEEPLIVE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
        }
        boolean isWrite = FileCommonTools.writeFileData(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME, gson.toJson(keepAliveDataList), true);
        if (!isWrite) {
            return CommonValue.KL_FILE_WRITE_ERR;
        } else {
            //如果是立即启用 那么需要判断服务是否开启
            return CommonValue.EXEU_COMPLETE;
        }
    }

    /**
     * 添加需要拉起的界面 界面只能拉起一个 多个界面会造成混乱
     * 所以调用多次这个方法会覆盖之前那个需要拉起的界面
     *
     * @param keepAliveData 当前需要添加的记录
     * @return 是否执行成功
     */
    public static CommonValue addActivity(KeepAliveData keepAliveData) {
        Gson gson = new Gson();
        keepAliveData.setServiceName("");
        String readJson = FileCommonTools.readFileData(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        List<KeepAliveData> keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
        }.getType());
        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
            Iterator<KeepAliveData> it = keepAliveDataList.iterator();
            while (it.hasNext()) {
                KeepAliveData keepData = it.next();
                if (keepData.getType() == TYPE_ACTIVITY) {
                    //如果记录中存在Activity的记录 则清除
                    it.remove();
                }
            }
        }
        return toWrite(keepAliveData, keepAliveDataList, gson);
    }


    /**
     * 添加需要保活的服务
     * [包名,服务]一起校验  下一次添加时不可与之前记录中的[包名,服务]一致
     *
     * @param keepAliveData 当前需要添加的记录
     * @return 是否执行成功
     */
    public static CommonValue addService(KeepAliveData keepAliveData) {
        Gson gson = new Gson();
        String readJson = FileCommonTools.readFileData(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        List<KeepAliveData> keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
        }.getType());
        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
            boolean isFind = false;
            for (int i = 0; i < keepAliveDataList.size(); i++) {
                if (keepAliveDataList.get(i).getServiceName() != null && !keepAliveDataList.get(i).getServiceName().equals("")) {
                    if (keepAliveDataList.get(i).getType() == TYPE_SERVICE && keepAliveDataList.get(i).getServiceName().contains(keepAliveData.getServiceName())) {
                        isFind = true;
                        break;
                    }
                }
            }
            if (!isFind) {
                return toWrite(keepAliveData, keepAliveDataList, gson);
            } else {
                return CommonValue.KL_SERVICE_REPEAT;
            }
        } else {
            return toWrite(keepAliveData, null, gson);
        }
    }


    /**
     * 添加拉起的界面,服务
     *
     * @param keepAliveData     当前需要添加的记录
     * @param keepAliveDataList 以前添加的记录
     */
    private static CommonValue toWrite(KeepAliveData
                                               keepAliveData, List<KeepAliveData> keepAliveDataList, Gson gson) {
        if (keepAliveDataList == null) {
            keepAliveDataList = new ArrayList<>();
        }
        keepAliveDataList.add(keepAliveData);
        File file = new File(FileCommonTools.SAVE_KEEPLIVE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
        }
        boolean isWrite = FileCommonTools.writeFileData(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME, gson.toJson(keepAliveDataList), true);
        if (!isWrite) {
            return CommonValue.KL_FILE_WRITE_ERR;
        } else {
            //如果是立即启用 那么需要判断服务是否开启
            return CommonValue.EXEU_COMPLETE;
        }
    }

    /**
     * 清除所有的记录
     */
    public static CommonValue clearKeepLive() {
        File file = new File(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        if (file.exists()) {
            return file.delete() ? CommonValue.EXEU_COMPLETE : CommonValue.KL_FILE_DEL_ERR;
        } else {
            return CommonValue.KL_DATA_ISNULL;
        }
    }


    /**
     * 获取全部的记录Activity+Service
     *
     * @return
     */
    public static List<KeepAliveData> getKeepLive() {
        Gson gson = new Gson();
        String readJson = FileCommonTools.readFileData(FileCommonTools.SAVE_KEEPLIVE_PATH + FileCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        List<KeepAliveData> keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
        }.getType());
        return keepAliveDataList;
    }


    /**
     * 关闭保活服务
     */
    public static void stopKeepLive() {
        FKeepAliveService.isKeepAliveStatus = false;
    }


}
