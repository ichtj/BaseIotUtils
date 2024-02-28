package com.ichtj.basetools.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import cn.jzvd.JzvdStd;

/**
 * Create on 2020/7/11
 * author chtj
 * desc
 */
public class VideoPlayerView extends JzvdStd {
    public VideoPlayerView(Context context) {
        super(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        startButton.setVisibility(View.GONE);
        posterImageView.setVisibility(View.GONE);
        fullscreenButton.setVisibility(View.GONE);
        startVideo();
    }
}
