package com.wave_chtj.example.base;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.face_chtj.base_iotutils.NetMonitorUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.callback.INetChangeCallBack;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wave_chtj.example.util.AppManager;

import io.reactivex.functions.Consumer;


/**
 * Create on 2019/10/12
 * author chtj
 * desc $
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将继承BaseActivity的Activity添加到堆栈 统一管理
        AppManager.addActivity(this);
        setStatusBar();
    }

    protected void requestPermission(){
        new RxPermissions(this).request(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
        }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
                if (granted) { // Always true pre-M
                    // I can control the camera now
                    ToastUtils.success("已通过权限");
                } else {
                    // Oups permission denied
                    ToastUtils.error("未通过权限");
                }
            }
        });
    }

    protected void setStatusBar() {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //KLog.d(TAG,"onDestroy");
    }

    protected void startAty(Class classes){
        startActivity(new Intent(this,classes));
    }
}
