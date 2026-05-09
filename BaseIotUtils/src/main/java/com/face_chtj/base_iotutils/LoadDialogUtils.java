package com.face_chtj.base_iotutils;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Loading animation helper.
 * One image rotates continuously; multiple images are played as frame animation.
 */
public class LoadDialogUtils {
    private static final int DEFAULT_ROTATE_DURATION_MS = 1000;
    private static final int DEFAULT_FRAME_DURATION_MS = 100;

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Dialog dialog;
    private FrameLayout rootView;
    private ImageView imageView;
    private AnimationDrawable frameAnimation;
    private ObjectAnimator rotateAnimator;
    private List<Integer> pngList;

    private int frameDuration = DEFAULT_FRAME_DURATION_MS;
    private int imageSizeWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int imageSizeHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
    private boolean showing = false;
    private boolean cancelable = false;
    private float dimAmount = 0.25f;

    public LoadDialogUtils(Context context) {
        this.context = context;
        initView();
    }

    public LoadDialogUtils(Context context, List<Integer> pngList) {
        this.context = context;
        setPngList(pngList);
        initView();
    }

    private void initView() {
        if (context == null) {
            return;
        }
        rootView = new FrameLayout(context);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootView.setBackgroundColor(Color.TRANSPARENT);
        rootView.setClickable(true);

        imageView = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                imageSizeWidth,
                imageSizeHeight,
                Gravity.CENTER);
        imageView.setLayoutParams(params);
        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        rootView.addView(imageView);
    }

    /**
     * Set loading image resources.
     * One image rotates; multiple images are played frame by frame.
     */
    public void setPngList(List<Integer> pngList) {
        this.pngList = pngList == null ? null : new ArrayList<>(pngList);
    }

    public void setPngList(List<Integer> pngList, int frameDurationMs) {
        setPngList(pngList);
        this.frameDuration = Math.max(16, frameDurationMs);
    }

    public void setImageSize(int widthDp, int heightDp) {
        imageSizeWidth = widthDp > 0 ? dpToPx(widthDp) : ViewGroup.LayoutParams.WRAP_CONTENT;
        imageSizeHeight = heightDp > 0 ? dpToPx(heightDp) : ViewGroup.LayoutParams.WRAP_CONTENT;

        if (imageView != null) {
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = imageSizeWidth;
            lp.height = imageSizeHeight;
            imageView.setLayoutParams(lp);
        }
    }

    public LoadDialogUtils setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        if (dialog != null) {
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(cancelable);
        }
        return this;
    }

    public LoadDialogUtils setDimAmount(float dimAmount) {
        if (dimAmount < 0f) {
            dimAmount = 0f;
        } else if (dimAmount > 1f) {
            dimAmount = 1f;
        }
        this.dimAmount = dimAmount;
        applyWindowLayout();
        return this;
    }

    public void showLoading() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    showLoading();
                }
            });
            return;
        }
        if (showing || pngList == null || pngList.isEmpty() || !canShowDialog()) {
            return;
        }

        clearAnimations();
        ensureDialog();

        try {
            dialog.show();
            showing = true;
            applyWindowLayout();
            if (pngList.size() == 1) {
                imageView.setImageResource(pngList.get(0));
                startRotateAnimation();
            } else {
                startFrameAnimation();
            }
        } catch (RuntimeException ignored) {
            showing = true;
            hideLoading();
        }
    }

    public void hideLoading() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    hideLoading();
                }
            });
            return;
        }
        if (!showing && dialog == null) {
            return;
        }

        clearAnimations();
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (IllegalArgumentException ignored) {
            // Activity window is gone; state is cleaned below.
        } finally {
            showing = false;
            dialog = null;
        }
    }

    private boolean canShowDialog() {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing()
                    && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
        }
        return false;
    }

    private void ensureDialog() {
        if (dialog != null) {
            return;
        }
        dialog = new Dialog(context, R.style.TransparentDialogStyle);
        dialog.setContentView(rootView);
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(android.content.DialogInterface dialogInterface) {
                clearAnimations();
                showing = false;
            }
        });
    }

    private void applyWindowLayout() {
        if (dialog == null) {
            return;
        }
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setGravity(Gravity.CENTER);
        window.setDimAmount(dimAmount);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        window.setAttributes(params);
    }

    private void startRotateAnimation() {
        if (imageView == null) return;

        rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        rotateAnimator.setDuration(DEFAULT_ROTATE_DURATION_MS);
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
        if (frameAnimation.getNumberOfFrames() == 0) {
            hideLoading();
            return;
        }

        frameAnimation.setOneShot(false);
        imageView.setImageDrawable(frameAnimation);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (showing && frameAnimation != null) {
                    frameAnimation.start();
                }
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
            imageView.setRotation(0f);
            imageView.setImageDrawable(null);
        }
    }

    private int dpToPx(int dp) {
        if (context == null) {
            return dp;
        }
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public boolean isShowing() {
        return showing;
    }

    public void refresh() {
        if (isShowing()) {
            hideLoading();
            showLoading();
        }
    }
}
