package com.wave_chtj.example.screen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chtj.base_iotutils.ScreenUtils;

import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

public class ScreenActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        Log.e("TAG>>>", ScreenUtils.getScreenInfo(this));
    }
}
