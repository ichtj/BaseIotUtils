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
 * Create on 2020/5/15
 * author chtj
 * desc 加载动画工具类 支持单张图片旋转动画和多张图片帧动画
 * --单张图片旋转动画 {@link #showLoading()}
 * --多张图片帧动画 {@link #showLoading()}
 * --设置图片大小 {@link #setImageSize(int, int)}
 * --隐藏加载动画 {@link #hideLoading()}
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
    private int imageSizeWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int imageSizeHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

    public LoadDialogUtils(Context context) {
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        init();
    }

    public LoadDialogUtils(Context context, List<Integer> pngList) {
        this.context = context.getApplicationContext();
        this.pngList = pngList;
        this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
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
        rootView.setClickable(true);

        // 创建ImageView
        imageView = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                imageSizeWidth,
                imageSizeHeight,
                Gravity.CENTER
        );
        imageView.setLayoutParams(params);
        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        ((FrameLayout) rootView).addView(imageView);
    }

    /**
     * 设置加载图片资源列表
     * 数量等于1时，执行旋转动画
     * 数量大于1时，执行帧动画
     * @param pngList
     */
    public void setPngList(List<Integer> pngList) {
        this.pngList = pngList;
    }

    public void setPngList(List<Integer> pngList, int frameDurationMs) {
        this.pngList = pngList;
        this.frameDuration = frameDurationMs;
    }

    public void setImageSize(int widthDp, int heightDp) {
        imageSizeWidth = dpToPx(widthDp);
        imageSizeHeight = dpToPx(heightDp);
        
        if (imageView != null) {
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = imageSizeWidth;
            lp.height = imageSizeHeight;
            imageView.setLayoutParams(lp);
        }
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
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );

        layoutParams.gravity = Gravity.CENTER;
        layoutParams.format = PixelFormat.RGBA_8888;

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

    /**
     * 隐藏加载对话框
     *
     * 该方法用于隐藏当前显示的加载对话框，会清除相关动画并从窗口管理器中移除视图
     */
    public void hideLoading() {
        // 如果当前未显示加载对话框，则直接返回
        if (!isShowing) {
            return;
        }

        try {
            // 清除所有动画效果
            clearAnimations();
            // 从窗口管理器中立即移除根视图
            if (rootView != null && rootView.getParent() != null) {
                windowManager.removeViewImmediate(rootView);
            }
            // 更新显示状态为false
            isShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startRotateAnimation() {
        if (imageView == null) return;
        
        rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1000);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.start();
    }

    private void startFrameAnimation() {
        if (imageView == null || pngList == null) return;
        
        frameAnimation = new AnimationDrawable();

        for (int resId : pngList) {
            Drawable drawable = ContextCompat.getDrawable(context, resId);
            if (drawable != null) {
                frameAnimation.addFrame(drawable, frameDuration);
            }
        }

        frameAnimation.setOneShot(false);
        imageView.setImageDrawable(frameAnimation);

        imageView.post(() -> {
            if (frameAnimation != null) {
                frameAnimation.start();
            }
        });
    }

    private void clearAnimations() {
        if (rotateAnimator != null) {
            rotateAnimator.cancel();
            rotateAnimator = null;
        }

        if (frameAnimation != null) {
            frameAnimation.stop();
            frameAnimation = null;
        }
        
        if (imageView != null) {
            imageView.clearAnimation();
            imageView.setImageDrawable(null);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public boolean isShowing() {
        return isShowing;
    }
    
    /**
     * 更新配置并刷新显示
     */
    public void refresh() {
        if (isShowing) {
            hideLoading();
            showLoading();
        }
    }
}
