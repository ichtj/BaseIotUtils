package com.wave_chtj.example.install;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.util.OptionTools;
import com.wave_chtj.example.util.TopAppUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Create on 2020/7/13
 * author chtj
 * desc
 */
public class InstallAPkAty extends BaseActivity {
    private static final String TAG = "InstallAPkAty";
    private static final String pkgName = "com.csdroid.pkg";
    private static final String appName = "pkgSearch.apk";
    private static final String apkPath = "/sdcard/" + appName;
    TextView tvNowVersion;
    private RadioButton rbOld,rbNew;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_apk);
        rbOld=findViewById(R.id.rbOld);
        rbNew=findViewById(R.id.rbNew);
        tvNowVersion=findViewById(R.id.tvNowVersion);
        tvNowVersion.setText("当前apk版本："+AppsUtils.getAppVersionName());
        initFile();
        AppManager.finishActivity(StartPageAty.class);
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
                KLog.d(TAG, apkPath + " exist");
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
     *
     * @param view
     */
    public void pmInstall(View view) {
        boolean isInstalled = OptionTools.pmInstallBySilent(apkPath);
        Log.d(TAG, "onCreate: isInstalled=" + isInstalled);
        if (isInstalled) {
            ToastUtils.success("安装成功！");
        } else {
            ToastUtils.error("安装失败！");
        }
    }
    /**
     * PM UNINSTALL
     *
     * @param view
     */
    public void pmUnInstall(View view) {
        OptionTools.deletePackage(this, pkgName, new OptionTools.IResult() {
            @Override
            public void getResult(boolean isComplete, String err) {
                Log.d(TAG, "systemApiUnInstall getResult: isComplete=" + isComplete + ",err=" + err);
            }
        });
    }


    /**
     * SYSTEM API INSTALL
     *
     * @param view
     */
    public void systemApiInstall(View view) {
        OptionTools.installPackageByJavaReflect(this, pkgName, apkPath, new OptionTools.IResult() {
            @Override
            public void getResult(boolean isComplete, String err) {
                Log.d(TAG, "systemApiInstall getResult: isComplete=" + isComplete + ",err=" + err);
            }
        });
    }

    /**
     * SYSTEM API UNINSTALL
     *
     * @param view
     */
    public void systemApiUnInstall(View view) {
        OptionTools.deletePackage(this, pkgName, new OptionTools.IResult() {
            @Override
            public void getResult(boolean isComplete, String err) {
                Log.d(TAG, "systemApiUnInstall getResult: isComplete=" + isComplete + ",err=" + err);
            }
        });
    }

    public void silenceInstall(View view) {
        boolean isInstalled = AppsUtils.installSilent(true, true, "pkgSearch.apk", apkPath);
        Log.d(TAG, "onCreate: isInstalled=" + isInstalled);
        if (isInstalled) {
            ToastUtils.success("安装成功！");
        } else {
            ToastUtils.error("安装失败！");
        }
    }

    public void silenceUnInstall(View view) {
        AppsUtils.uninstallSilent(true, true, appName, pkgName);
    }


    public void intentInstallApkClick(View view) {
        boolean isNew=rbNew.isChecked();
        File file=new File(isNew?"/sdcard/meituan/MTCabinet_new.apk":"/sdcard/meituan/MTCabinet_old.apk");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(photoURI, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    /**
     * 通过apk文件获取包名
     *
     * @param filePath apk文件路径
     * @return
     */
    private String getPackageName(String filePath) {
        if (filePath == null) {
            Log.e(TAG, "call method getPackageName, filePath is null, return null.");
            return null;
        }
        PackageManager pm = this.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            String packageName = appInfo.packageName;
            Log.d(TAG, "call method getPackageName, packageName = " + packageName);
            return packageName;
        }
        return null;
    }

    public void otherInstallApkClick(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File file=new File("/sdcard/meituan/pkgSearch.apk");
        Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(photoURI, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
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
