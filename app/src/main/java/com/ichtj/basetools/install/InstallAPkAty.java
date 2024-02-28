package com.ichtj.basetools.install;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppManager;
import com.ichtj.basetools.util.OptionTools;

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

    TextView tvNowVersion;
    private RadioButton rbOld,rbNew;
    private static final int SILENT_INSTALLATION=1000;
    private static final int SILENT_UNINSTALLATION=1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_apk);
        rbOld=findViewById(R.id.rbOld);
        rbNew=findViewById(R.id.rbNew);
        tvNowVersion=findViewById(R.id.tvNowVersion);
        tvNowVersion.setText("当前apk版本："+AppsUtils.getAppVersionName());
        AppManager.finishActivity(StartPageAty.class);
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
        boolean isInstalled = OptionTools.pmInstallBySilent("apkPath");
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
        OptionTools.deletePackage(this, "pkgName", new OptionTools.IResult() {
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
        OptionTools.installPackageByJavaReflect(this, "pkgName", "apkPath", new OptionTools.IResult() {
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
        OptionTools.deletePackage(this, "pkgName", new OptionTools.IResult() {
            @Override
            public void getResult(boolean isComplete, String err) {
                Log.d(TAG, "systemApiUnInstall getResult: isComplete=" + isComplete + ",err=" + err);
            }
        });
    }

    public void silenceInstall(View view) {
        // 启动文件选择器
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive"); // 选择apk文件类型
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, SILENT_INSTALLATION);
    }


    public void silenceUnInstall(View view) {
        AppsUtils.uninstallSilent(true, true, "IotCloud", "com.face.baseiotcloud");
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if (data != null) {
                Uri uri = data.getData();
                String filePath = uri.getPath(); // 获取文件路径
                String fileName= TimeUtils.getTodayDateHms("yyyyMMddHHmmss");
                if (filePath != null) {
                    File file = new File(filePath);
                    fileName = file.getName();
                }
                ToastUtils.success("File selected: " + filePath);
                if(requestCode==SILENT_INSTALLATION){
                    boolean isInstalled = AppsUtils.installSilent(true, true, fileName, filePath);
                    Log.d(TAG, "onCreate: isInstalled=" + isInstalled);
                    if (isInstalled) {
                        ToastUtils.success("安装成功！");
                    } else {
                        ToastUtils.error("安装失败！");
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
