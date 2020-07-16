package com.wave_chtj.example.play;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * Create on 2020/7/13
 * author chtj
 * desc
 */
public class VideoPlayAty extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
