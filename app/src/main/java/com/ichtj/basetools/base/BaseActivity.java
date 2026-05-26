package com.ichtj.basetools.base;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.util.AppManager;

import io.reactivex.functions.Consumer;


/**
 * Create on 2019/10/12
 * author chtj
 * desc $
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected static final int FILE_SELECT_CODE = 10000;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将继承BaseActivity的Activity添加到堆栈 统一管理
        AppManager.addActivity(this);//
        setStatusBar();
    }

    protected void setStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void startAty(Class classes){
        startActivity(new Intent(this,classes));
    }
}
