package com.wave_chtj.example.test;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.util.AppManager;

public class TestAty  extends BaseActivity {
    TextView tvNowPkg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tvNowPkg=findViewById(R.id.tvNowPkg);
        String pkgName=getPackageName();
        if(pkgName.contains("com.zto.ztoexpresscabinet")){
            pkgName=pkgName+">>>>A";
        }else{
            pkgName=pkgName+">>>>B";
        }
        tvNowPkg.setText(pkgName);
        AppManager.getAppManager().finishActivity(StartPageAty.class);
    }
}
