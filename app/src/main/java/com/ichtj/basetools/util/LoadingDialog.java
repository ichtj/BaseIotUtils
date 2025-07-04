package com.ichtj.basetools.util;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.ichtj.basetools.R;

public class LoadingDialog extends Dialog {

    private FrameLayout container;
    private ImageView imageView;
    private AnimationDrawable frameAnimation;
    private ObjectAnimator rotateAnimator;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.TransparentDialogStyle);
        init(context);
    }

    private void init(Context context) {
        container = new FrameLayout(context);
        container.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        container.setBackgroundColor(Color.TRANSPARENT);
        setContentView(container);

        imageView = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dpToPx(80), dpToPx(80), Gravity.CENTER);
        imageView.setLayoutParams(params);
        container.addView(imageView);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    /**
     * 设置 PNG 列表。如果只有一张图，则自动执行旋转动画。
     */
    public void setLoadingImages(int[] resIds, int widthDp, int heightDp, int frameDurationMs) {
        imageView.clearAnimation();
        imageView.setBackground(null);

        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = dpToPx(widthDp);
        lp.height = dpToPx(heightDp);
        imageView.setLayoutParams(lp);

        if (resIds.length > 1) {
            frameAnimation = new AnimationDrawable();
            for (int resId : resIds) {
                frameAnimation.addFrame(imageView.getContext().getResources().getDrawable(resId), frameDurationMs);
            }
            frameAnimation.setOneShot(false);
            imageView.setBackground(frameAnimation);
        } else if (resIds.length == 1) {
            imageView.setImageResource(resIds[0]);
            rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
            rotateAnimator.setDuration(1000);
            rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
    }

    /**
     * 显示 loading（并启动动画）
     */
    public void showLoading() {
        if (!isShowing()) {
            show();
            if (frameAnimation != null && !frameAnimation.isRunning()) {
                frameAnimation.start();
            }
            if (rotateAnimator != null && !rotateAnimator.isRunning()) {
                rotateAnimator.start();
            }
        }
    }

    /**
     * 关闭 loading（停止动画）
     */
    public void hideLoading() {
        if (isShowing()) {
            if (frameAnimation != null && frameAnimation.isRunning()) {
                frameAnimation.stop();
            }
            if (rotateAnimator != null && rotateAnimator.isRunning()) {
                rotateAnimator.cancel();
                imageView.setRotation(0f);
            }
            dismiss();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * imageView.getContext().getResources().getDisplayMetrics().density);
    }
}

