package com.ichtj.basetools.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class BasicTools {
    public static void showTwoScaledBitmapsDialog(Context context, Bitmap bitmap1, Bitmap bitmap2) {
        if (bitmap1 == null || bitmap2 == null) return;

        // 获取屏幕尺寸
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        int maxDialogWidth = (int) (screenWidth * 0.9f);  // 最大宽度为屏幕宽度的90%
        int maxDialogHeight = (int) (screenHeight * 0.9f); // 最大高度为屏幕高度的90%

        // ==== 缩放 bitmap1 ====
        float scale1 = Math.min(1f, (float) maxDialogWidth / bitmap1.getWidth());
        int targetWidth1 = (int) (bitmap1.getWidth() * scale1);
        int targetHeight1 = (int) (bitmap1.getHeight() * scale1);

        ImageView imageView1 = new ImageView(context);
        imageView1.setImageBitmap(bitmap1);
        imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView1.setLayoutParams(new LinearLayout.LayoutParams(targetWidth1, targetHeight1));

        // ==== 缩放 bitmap2 ====
        float scale2 = Math.min(1f, (float) maxDialogWidth / bitmap2.getWidth());
        int targetWidth2 = (int) (bitmap2.getWidth() * scale2);
        int targetHeight2 = (int) (bitmap2.getHeight() * scale2);

        ImageView imageView2 = new ImageView(context);
        imageView2.setImageBitmap(bitmap2);
        imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView2.setLayoutParams(new LinearLayout.LayoutParams(targetWidth2, targetHeight2));

        // === 创建 LinearLayout，垂直方向 ===
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setPadding(20, 20, 20, 20);
        linearLayout.addView(imageView1);
        linearLayout.addView(imageView2);

        // ==== 计算总高度，如果超出最大高度则按比例缩放 ====
        int totalHeight = targetHeight1 + targetHeight2 + 40; // +padding
        float overallScale = totalHeight > maxDialogHeight ? (float) maxDialogHeight / totalHeight : 1f;

        if (overallScale < 1f) {
            // 重新按整体缩放
            targetWidth1 *= overallScale;
            targetHeight1 *= overallScale;
            targetWidth2 *= overallScale;
            targetHeight2 *= overallScale;

            imageView1.setLayoutParams(new LinearLayout.LayoutParams(targetWidth1, targetHeight1));
            imageView2.setLayoutParams(new LinearLayout.LayoutParams(targetWidth2, targetHeight2));
        }

        // ==== 创建 Dialog ====
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(linearLayout)
                .setCancelable(true)
                .create();

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        // 设置 Dialog 尺寸
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = Math.max(targetWidth1, targetWidth2) + 40;
            params.height = targetHeight1 + targetHeight2 + 40;
            params.width = Math.min(params.width, maxDialogWidth);
            params.height = Math.min(params.height, maxDialogHeight);
            window.setAttributes(params);
        }
    }


}
