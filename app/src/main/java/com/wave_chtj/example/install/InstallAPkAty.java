package com.wave_chtj.example.install;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.ZipUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.util.InstallTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Create on 2020/7/13
 * author chtj
 * desc
 */
public class InstallAPkAty extends BaseActivity {
    private static final String TAG = "InstallAPkAty";
    private static final String pkgName="com.csdroid.pkg";
    private static final String apkPath="/sdcard/pkgSearch.apk";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_apk);
        initFile();
    }


    //zip保存的路径
    private static final String savePath = "/sdcard/pkgSearch.apk";
    // 文件名
    private static final String fileName = "pkgSearch.apk";

    public void initFile() {
        try {
            //视频文件不存在时将文件保存到本地
            if (!new File(savePath).exists()) {
                InputStream input = getAssets().open(fileName);
                writeToLocal(savePath, input);
            } else {
                KLog.d(TAG, apkPath+" exist");
                ToastUtils.success("加载成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    /**
     * 将InputStream写入本地文件
     *
     * @param destDirPath 写入本地目录
     * @param input       输入流
     * @throws IOException
     */
    public static void writeToLocal(String destDirPath, InputStream input)
            throws IOException {

        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destDirPath);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }

    /**
     * PM INSTALL
     * @param view
     */
    public void pmInstall(View view) {
        boolean isInstalled= InstallTools.pmInstallBySilent(apkPath);
        Log.d(TAG, "onCreate: isInstalled="+isInstalled);
        if(isInstalled){
            ToastUtils.success("安装成功！");
        }else{
            ToastUtils.error("安装失败！");
        }
    }

    /**
     * PM UNINSTALL
     * @param view
     */
    public void pmUnInstall(View view) {
        boolean isInstalled= InstallTools.pmUnInstallBySilent(pkgName);
        Log.d(TAG, "onCreate: isInstalled="+isInstalled);
    }


    /**
     * SYSTEM API INSTALL
     * @param view
     */
    public void systemApiInstall(View view) {
        InstallTools.installPackageByJavaReflect(this, pkgName, apkPath, new InstallTools.IResult() {
            @Override
            public void getResult(boolean isComplete, String err) {
                Log.d(TAG, "systemApiInstall getResult: isComplete="+isComplete+",err="+err);
            }
        });
    }

    /**
     * SYSTEM API UNINSTALL
     * @param view
     */
    public void systemApiUnInstall(View view) {
        InstallTools.deletePackage(this, pkgName, new InstallTools.IResult() {
            @Override
            public void getResult(boolean isComplete, String err) {
                Log.d(TAG, "systemApiUnInstall getResult: isComplete="+isComplete+",err="+err);
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
