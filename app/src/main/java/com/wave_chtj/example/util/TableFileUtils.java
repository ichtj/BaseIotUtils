package com.wave_chtj.example.util;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ZipUtils;

import java.io.File;

/**
 * Create on 2020/7/17
 * author chtj
 * desc
 */
public class TableFileUtils {
    /**
     * zip保存的路径
     */
    private static final String xlsPath = "/sdcard/table.xls";
    /**
     * 检查xls文件是否存在
     * @return
     */
    public static boolean checkXlsExist(){
        if(new File("/sdcard/table.xls").exists()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 检查xlsx文件是否存在
     * @return
     */
    public static boolean checkXlsxExist(){
        if(new File("/sdcard/table.xlsx").exists()){
            return true;
        }else{
            return false;
        }
    }
    //public void writeToSdcard(){
    //    //视频文件不存在时将文件保存到本地
    //    if(!new File(savePath).exists()){
    //        input = getAssets().open(fileName);
    //        writeToLocal(savePath, input);
    //        boolean isUnzip = ZipUtils.unzipFile(savePath, unZipPath);
    //        if(isUnzip&&new File(savePath).exists()){
    //            if (myService.mCallResult != null) {
    //                myService.mCallResult.unZipPlay();
    //            }
    //        }
    //    }else{
    //        KLog.d(TAG, "Aging_Test_Video.mp4 exist");
    //    }
    //}
}
