package com.wave_chtj.example.screen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.face_chtj.base_iotutils.ScreenInfoUtils;

import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * 直接去查看 activity_screen.xml
 *
 */
public class ScreenActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        //这里只是查看屏幕相关信息 与屏幕适配无关
        Log.e("TAG>>>", ScreenInfoUtils.getScreenInfo(this));
    }
}
