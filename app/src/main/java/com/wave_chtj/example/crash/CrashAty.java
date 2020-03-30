package com.wave_chtj.example.crash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * Create on 2020/3/20
 * author chtj
 * desc crash控制
 */
public class CrashAty extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
    }

    /**
     * 重新启动app
     * @param view
     */
    public void reStartApp(View view){
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
