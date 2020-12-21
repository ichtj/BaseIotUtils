package com.chtj.framework;

import android.content.Intent;
import android.util.Log;

import com.chtj.framework.entity.CommonValue;
import com.chtj.framework.task.FBaseService;
import com.chtj.framework.entity.KeepLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 应用保活
 */
public class FKeepLiveTools {

    private static final String TAG = "FKeepLiveTools";
    /**
     * 保活的类型为Activity
     */
    public static final String TYPE_ACTIVITY = "0";
    /**
     * 保活的类型为服务
     */
    public static final String TYPE_SERVICE = "1";


    /**
     * 添加需要拉起的界面 界面只能拉起一个 多个界面会造成混乱
     * 所以调用多次这个方法会覆盖之前那个需要拉起的界面
     *
     * @param keepLiveData 当前需要添加的记录
     * @param isEnableNow  是否立即启用
     * @return 是否执行成功
     */
    public static CommonValue addActivity(KeepLiveData keepLiveData, boolean isEnableNow) {
        Gson gson = new Gson();
        keepLiveData.setServiceName("");
        String readJson = FCommonTools.readFileData(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        List<KeepLiveData> keepLiveDataList = gson.fromJson(readJson, new TypeToken<List<KeepLiveData>>() {
        }.getType());
        if (keepLiveDataList != null && keepLiveDataList.size() > 0) {
            Iterator<KeepLiveData> it = keepLiveDataList.iterator();
            while (it.hasNext()) {
                KeepLiveData keepData = it.next();
                if (keepData.getType().equals(TYPE_ACTIVITY)) {
                    //如果记录中存在Activity的记录 则清除
                    it.remove();
                }
            }
        }
        return toWrite(keepLiveData, null, gson, isEnableNow);
    }


    /**
     * 添加需要保活的服务
     * [包名,服务]一起校验  下一次添加时不可与之前记录中的[包名,服务]一致
     *
     * @param keepLiveData 当前需要添加的记录
     * @param isEnableNow  是否立即启用
     * @return 是否执行成功
     */
    public static CommonValue addService(KeepLiveData keepLiveData, boolean isEnableNow) {
        Gson gson = new Gson();
        String readJson = FCommonTools.readFileData(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        List<KeepLiveData> keepLiveDataList = gson.fromJson(readJson, new TypeToken<List<KeepLiveData>>() {
        }.getType());
        if (keepLiveDataList != null && keepLiveDataList.size() > 0) {
            boolean isFind = false;
            for (int i = 0; i < keepLiveDataList.size(); i++) {
                if (keepLiveDataList.get(i).getServiceName() != null && !keepLiveDataList.get(i).getServiceName().equals("")) {
                    if (keepLiveDataList.get(i).getType().equals(TYPE_SERVICE) && keepLiveDataList.get(i).getServiceName().contains(keepLiveData.getServiceName())) {
                        isFind = true;
                        break;
                    }
                }
            }
            if (!isFind) {
                return toWrite(keepLiveData, keepLiveDataList, gson, isEnableNow);
            } else {
                return CommonValue.KL_SERVICE_REPEAT;
            }
        } else {
            return toWrite(keepLiveData, null, gson, isEnableNow);
        }
    }


    /**
     * 添加拉起的界面,服务
     *
     * @param keepLiveData     当前需要添加的记录
     * @param keepLiveDataList 以前添加的记录
     * @param isEnableNow      是否立即启用
     */
    private static CommonValue toWrite(KeepLiveData
                                               keepLiveData, List<KeepLiveData> keepLiveDataList, Gson gson, boolean isEnableNow) {
        if (keepLiveDataList == null) {
            keepLiveDataList = new ArrayList<>();
        }
        keepLiveDataList.add(keepLiveData);
        File file = new File(FCommonTools.SAVE_KEEPLIVE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "errMeg:" + e.getMessage());
            }
        }
        boolean isWrite = FCommonTools.writeFileData(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME, gson.toJson(keepLiveDataList), true);
        if (!isWrite) {
            return CommonValue.KL_FILE_WRITE_ERR;
        } else {
            //如果是立即启用 那么需要判断服务是否开启
            if (isEnableNow) {
                FBaseTools.getContext().startService(new Intent(FBaseTools.sApp, FBaseService.class));
                boolean isRunning = FCommonTools.isWorked(FBaseService.class.getName());
                if (isRunning) {
                    return CommonValue.EXEU_COMPLETE;
                } else {
                    return CommonValue.KL_SERVICE_ACTIVATE_ERR;
                }
            } else {
                return CommonValue.EXEU_COMPLETE;
            }
        }
    }

    /**
     * 清除所有的记录
     */
    public static CommonValue clearKeepLive() {
        File file = new File(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME);
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
    public static List<KeepLiveData> getKeepLive() {
        Gson gson = new Gson();
        String readJson = FCommonTools.readFileData(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME);
        List<KeepLiveData> keepLiveDataList = gson.fromJson(readJson, new TypeToken<List<KeepLiveData>>() {
        }.getType());
        return keepLiveDataList;
    }


    /**
     * 关闭保活服务
     */
    public static void stopKeepLive() {
        FBaseService.isStopTask = true;
        FBaseTools.getContext().stopService(new Intent(FBaseTools.getContext(), FBaseService.class));
    }

}
