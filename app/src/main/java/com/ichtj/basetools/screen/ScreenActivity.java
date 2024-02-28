package com.ichtj.basetools.screen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.DisplayUtils;

import com.face_chtj.base_iotutils.view.TopTitleBar;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

/**
 * 直接去查看 activity_screen.xml
 */
public class ScreenActivity extends BaseActivity {
    private TextView tvProgress;
    private TextView tvSysProgress;
    private SeekBar sbProgress;
    private SeekBar sbSysProgress;
    private TopTitleBar ctTopView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        //这里只是查看屏幕相关信息 与屏幕适配无关
        initView();
    }

    public void initView() {
        ctTopView = findViewById(R.id.ctTopView);
        KLog.d(DisplayUtils.getScreenInfo(this));
        ctTopView.setOnTextViewClickListener(new TopTitleBar.OnTextViewClickListener() {
            @Override
            public void onTextLeftClick() {
            }

            @Override
            public void onTextCenterClick() {
            }

            @Override
            public void onTextRightClick() {
                int orientation=getRequestedOrientation();
                if(orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                    KLog.d("onClick() orientation >> "+orientation);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }else{
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });
        tvProgress = findViewById(R.id.tvProgress);
        sbProgress = findViewById(R.id.sbProgress);
        tvProgress.setText("App当前进度(0~255)：" + DisplayUtils.getScreenBrightness());
        sbProgress.setMax(255);
        sbProgress.setProgress(DisplayUtils.getScreenBrightness());
        //进度
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                KLog.d( "progress: " + progress);
                tvProgress.setText("App当前进度(0~255)：" + progress);
                DisplayUtils.setAppScreenBrightness(ScreenActivity.this, progress);
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
        tvSysProgress.setText("系统当前进度(0~255)：" + DisplayUtils.getScreenBrightness());
        sbSysProgress.setProgress(DisplayUtils.getScreenBrightness());

        //进度
        sbSysProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DisplayUtils.setScreenMode(0);
                KLog.d("progress: " + progress);
                tvSysProgress.setText("系统当前进度(0~255)：" + progress);
                DisplayUtils.setSysScreenBrightness(progress);
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
     **/
    private int getScreenBrightness(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }
}
