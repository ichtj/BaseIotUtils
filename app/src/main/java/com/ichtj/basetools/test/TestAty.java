package com.ichtj.basetools.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppManager;
import com.ichtj.basetools.util.PACKAGES;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@Route(path = PACKAGES.BASE + "testDemo")
public class TestAty extends BaseActivity {
    private static final String TAG = TestAty.class.getSimpleName();
    private static final String CONFIG_PATH = "/data/misc/package.conf";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        AppManager.finishActivity(StartPageAty.class);
        Log.d(TAG, "onCreate list>>: "+readAllPackages());
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo1"));
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo2"));
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo3"));
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo4"));
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo5"));
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo6"));
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo7"));
//        Log.d(TAG, "onCreate: add>>:"+addPackage("com.test.demo8"));
//        Log.d(TAG, "onCreate: remove>>:"+removePackage("com.test.dddd1"));
//        Log.d(TAG, "onCreate: remove>>:"+removePackage("com.test.demo3"));
//        Log.d(TAG, "onCreate: remove>>:"+removePackage("com.test.demo8"));
//        toggleStatusBar(false);
        sendKeepAliveBroadcast(this,"com.android.settings",false);
    }


    public static void sendKeepAliveBroadcast(Context context, String packageName, boolean enable) {
        Intent intent = new Intent("com.android.control.keepalive");
        intent.putExtra("packageName", packageName);
        intent.putExtra("enable", enable);//enable true开启 false关闭
        context.sendBroadcast(intent);
    }

    public void toggleStatusBar(boolean show) {
        Intent intent;
        if (show) {
            intent = new Intent("com.android.intent.showbar");
        } else {
            intent = new Intent("com.android.intent.hidebar");
        }
        sendBroadcast(intent);
    }

    // 添加一个包名到配置文件中（如果不存在）
    public static boolean addPackage(String packageName) {
        try {
            List<String> packages = readAllPackages();
            for (String pkg : packages) {
                if (pkg.trim().equals(packageName.trim())) {
                    return false; // 已存在
                }
            }
            File file = new File(CONFIG_PATH);
            boolean needsNewline = false;
            // 检查是否需要加换行符
            if (file.exists() && file.length() > 0) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    raf.seek(file.length() - 1);
                    byte lastByte = raf.readByte();
                    if (lastByte != '\n') {
                        needsNewline = true;
                    }
                }
            }
            try (FileWriter fw = new FileWriter(file, true)) {
                if (needsNewline) {
                    fw.write("\n");
                }
                fw.write(packageName.trim() + "\n");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除配置文件中的一个包名
    public static boolean removePackage(String packageName) {
        try {
            List<String> packages = readAllPackages();
            if (!packages.remove(packageName)) {
                return false; // 没找到，不删除
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_PATH, false))) {
                for (String pkg : packages) {
                    writer.write(pkg);
                    writer.newLine();
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 读取所有包名（辅助函数）
    private static List<String> readAllPackages() {
        List<String> result = new ArrayList<>();
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            return result;
        }
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        result.add(line);
                    }
                }
            }
        }catch (Throwable t){
            Log.e(TAG, "readAllPackages: ", t);
        }
        return result;
    }
}
