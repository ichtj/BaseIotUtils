package com.wave_chtj.example.screen;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.app.ScreenInfoUtils;

import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * 直接去查看 activity_screen.xml
 */
public class ScreenActivity extends BaseActivity {
    private static final String TAG = "ScreenActivity";
    private TextView tvProgress;
    private TextView tvSysProgress;
    private SeekBar sbProgress;
    private SeekBar sbSysProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        //这里只是查看屏幕相关信息 与屏幕适配无关
        Log.e(TAG, ScreenInfoUtils.getScreenInfo(this));

        initView();
    }

    public void initView() {
        tvProgress = findViewById(R.id.tvProgress);
        sbProgress = findViewById(R.id.sbProgress);
        tvProgress.setText("App当前进度(0~255)："+ScreenInfoUtils.getScreenBrightness());
        sbProgress.setMax(255);
        sbProgress.setProgress(ScreenInfoUtils.getScreenBrightness());
        //进度
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                KLog.d(TAG, "progress: " + progress);
                tvProgress.setText("App当前进度(0~255)：" + progress);
                ScreenInfoUtils.setAppScreenBrightness(ScreenActivity.this, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        tvSysProgress = findViewById(R.id.tvSysProgress);
        sbSysProgress = findViewById(R.id.sbSysProgress);
        sbSysProgress.setMax(255);
        tvSysProgress.setText("系统当前进度(0~255)："+ScreenInfoUtils.getScreenBrightness());
        sbSysProgress.setProgress(ScreenInfoUtils.getScreenBrightness());

        //进度
        sbSysProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ScreenInfoUtils.setScreenMode(0);
                KLog.d(TAG, "progress: " + progress);
                tvSysProgress.setText("系统当前进度(0~255)：" + progress);
                ScreenInfoUtils.setSysScreenBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    /**
     * 1.获取系统默认屏幕亮度值 屏幕亮度值范围（0-255）
     * **/
    private int getScreenBrightness(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }

}
