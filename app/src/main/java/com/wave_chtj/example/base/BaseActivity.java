package com.wave_chtj.example.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wave_chtj.example.crash.AppManager;

/**
 * Create on 2019/10/12
 * author chtj
 * desc $
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将继承BaseActivity的Activity添加到堆栈 统一管理
        AppManager.getAppManager().addActivity(this);
    }
}
