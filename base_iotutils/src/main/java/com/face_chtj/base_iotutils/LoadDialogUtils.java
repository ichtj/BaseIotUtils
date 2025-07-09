package com.face_chtj.base_iotutils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import java.util.List;
/**
 * 使用示例：
 * List<Integer> pngList = Arrays.asList(
 *    R.drawable.loading_1,
 *    R.drawable.loading_2,
 *    R.drawable.loading_3
 * );
 *
 * LoadDialogUtils dialog = new LoadDialogUtils(this, pngList);
 * dialog.setImageSize(80, 80);
 * dialog.showLoading();
 *
 * 隐藏
 * dialog.hideLoading();
*/
public class LoadDialogUtils {
    private Context context;
    private WindowManager windowManager;
    private View rootView;
    private ImageView imageView;
    private AnimationDrawable frameAnimation;
    private ObjectAnimator rotateAnimator;
    private List<Integer> pngList;
    private int frameDuration = 100;
    private boolean isShowing = false;

    public LoadDialogUtils(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        init();
    }

    public LoadDialogUtils(Context context, List<Integer> pngList) {
        this.context = context;
        this.pngList = pngList;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        init();
    }

    private void init() {
        // 创建根布局
        rootView = new FrameLayout(context);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootView.setBackgroundColor(Color.TRANSPARENT);

        // 创建ImageView
        imageView = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        imageView.setLayoutParams(params);
        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        ((FrameLayout) rootView).addView(imageView);
    }

    public void setPngList(List<Integer> pngList) {
        this.pngList = pngList;
    }

    public void setPngList(List<Integer> pngList, int frameDurationMs) {
        this.pngList = pngList;
        this.frameDuration = frameDurationMs;
    }

    public void setImageSize(int widthDp, int heightDp) {
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = dpToPx(widthDp);
        lp.height = dpToPx(heightDp);
        imageView.setLayoutParams(lp);
    }

    public void showLoading() {
        if (isShowing || pngList == null || pngList.isEmpty()) {
            return;
        }

        // 清除之前的动画
        clearAnimations();

        // 创建窗口布局参数
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        layoutParams.gravity = Gravity.CENTER;

        try {
            // 添加到窗口
            windowManager.addView(rootView, layoutParams);
            isShowing = true;

            if (pngList.size() == 1) {
                // 单张图片：执行旋转动画
                imageView.setImageResource(pngList.get(0));
                startRotateAnimation();
            } else {
                // 多张图片：执行帧动画
                startFrameAnimation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideLoading() {
        if (!isShowing) {
            return;
        }

        try {
            clearAnimations();
            windowManager.removeView(rootView);
            isShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRotateAnimation() {
        rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1000);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.start();
    }

    private void startFrameAnimation() {
        frameAnimation = new AnimationDrawable();

        for (int resId : pngList) {
            Drawable drawable = ContextCompat.getDrawable(context, resId);
            if (drawable != null) {
                frameAnimation.addFrame(drawable, frameDuration);
            }
        }

        frameAnimation.setOneShot(false);
        imageView.setImageDrawable(frameAnimation);

        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (frameAnimation != null) {
                    frameAnimation.start();
                }
            }
        });
    }

    private void clearAnimations() {
        if (rotateAnimator != null && rotateAnimator.isRunning()) {
            rotateAnimator.cancel();
            imageView.setRotation(0f);
            rotateAnimator = null;
        }

        if (frameAnimation != null && frameAnimation.isRunning()) {
            frameAnimation.stop();
            frameAnimation = null;
        }

        imageView.clearAnimation();
        imageView.setImageDrawable(null);
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public boolean isShowing() {
        return isShowing;
    }
}
