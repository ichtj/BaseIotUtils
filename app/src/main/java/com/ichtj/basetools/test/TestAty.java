package com.ichtj.basetools.test;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppManager;

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
        AppManager.finishActivity(StartPageAty.class);
    }
}
