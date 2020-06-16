package com.wave_chtj.example.util;

import android.view.View;

/**
 * Create on 2020/5/26
 * author chtj
 * desc   防止View重复多次点击
 */
public abstract class IClickListener implements View.OnClickListener {
    private long mLastClickTime;
    private long timeInterval = 1000L;

    public IClickListener() {

    }

    public IClickListener(long interval) {
        this.timeInterval = interval;
    }

    @Override
    public void onClick(View v) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > timeInterval) {
            // 单次点击事件
            onSingleClick();
            mLastClickTime = nowTime;
        } else {
            // 快速点击事件
            onFastClick();
        }
    }

    protected abstract void onSingleClick();

    protected abstract void onFastClick();
}