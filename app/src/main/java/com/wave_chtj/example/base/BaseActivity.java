package com.wave_chtj.example.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.app.StatusBarUtil;
import com.wave_chtj.example.util.AppManager;


/**
 * Create on 2019/10/12
 * author chtj
 * desc $
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG="BaseActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将继承BaseActivity的Activity添加到堆栈 统一管理
        AppManager.getAppManager().addActivity(this);
        //统一状态栏
        setStatusBar();
    }

    protected void setStatusBar() {
        //StatusBarUtil.setColor(this, getResources().getColor(R.color.white));
        StatusBarUtil.setTranslucent(this,255);
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
