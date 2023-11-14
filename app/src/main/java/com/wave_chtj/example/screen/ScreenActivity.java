package com.wave_chtj.example.screen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.DisplayUtils;

import com.face_chtj.base_iotutils.view.TopTitleBar;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

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
        screenInfo();
    }

    public void screenInfo() {
        WindowManager windowManager = getWindowManager();

        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics displayMetrics = new DisplayMetrics();

        display.getMetrics(displayMetrics);

        int mWidthPixels = displayMetrics.widthPixels;
        int mHeightPixels = displayMetrics.heightPixels;
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);

                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);

            } catch (Exception ignored) {
            }
        }

        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (Exception ignored) {
            }
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            double x = Math.pow(mWidthPixels / dm.xdpi, 2);
            double y = Math.pow(mHeightPixels / dm.ydpi, 2);
            double screenInches = Math.sqrt(x + y);
            KLog.d("Screen inches : " + screenInches);
        }
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
