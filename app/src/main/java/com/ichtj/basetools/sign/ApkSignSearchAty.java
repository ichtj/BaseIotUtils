package com.ichtj.basetools.sign;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApkSignSearchAty extends BaseActivity {
    private static final String TAG=ApkSignSearchAty.class.getSimpleName();
    TextView tvPosition;
    TextView tvResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_sign);
        tvPosition = findViewById(R.id.tvPosition);
        tvResult = findViewById(R.id.tvResult);
        FormatViewUtils.setMovementMethod(tvResult);
    }

    public void selectApkClick(View view) {
        FormatViewUtils.scrollBackToTop(tvResult);
        List<String> apkList =findAPKFiles(new File("/sdcard/"));
        if (apkList != null && apkList.size() > 0) {
            for (int i = 0; i < apkList.size(); i++) {
                String path = apkList.get(i);
                String sha256=AppsUtils.getSHA256FromAPK(path);
                Log.d(TAG, "selectApkClick: path>>"+path+",sha256>>"+sha256);
                FormatViewUtils.formatData(tvResult, path + ",签名：" + sha256);
            }
        } else {
            ToastUtils.success(getString(R.string.sdcard_not_exist_apklist));
        }
    }

    public void saveResultClick(View view) {
        List<String> apkList =findAPKFiles(new File("/sdcard/"));
        if (apkList != null && apkList.size() > 0) {
            List<String> resultList=new ArrayList<>();
            for (int i = 0; i < apkList.size(); i++) {
                String path =apkList.get(i);
                String sha256=AppsUtils.getSHA256FromAPK(path);
                resultList.add("apk："+path+",签名："+sha256);
            }
            if (resultList!=null&&resultList.size()>0){
                boolean isComplete = FileUtils.writeFileData("/sdcard/sign.txt", resultList.toString(), true);
                ToastUtils.successOrError(isComplete,isComplete?"保存成功！":"保存失败！");
            }else{
                ToastUtils.error("保存失败！");
            }
        } else {
            ToastUtils.success("/sdcard/根目录不存在apk文件！");
        }
    }

    private List<String> findAPKFiles(File directory) {
        List<String> apkFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    apkFiles.addAll(findAPKFiles(file));
                } else if (file.getName().toLowerCase().endsWith(".apk")) {
                    apkFiles.add(file.getAbsolutePath());
                }
            }
        }
        return apkFiles;
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    filePath = cursor.getString(index);
                }
                cursor.close();
            }
        } else if ("file".equals(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }
}
