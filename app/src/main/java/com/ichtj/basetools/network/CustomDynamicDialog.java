package com.ichtj.basetools.network;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.core.content.ContextCompat;

import com.face_chtj.base_iotutils.DisplayUtils;
import com.face_chtj.base_iotutils.RegularTools;
import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;

import java.util.ArrayList;
import java.util.List;

public class CustomDynamicDialog {
    private static int IP_COUNT=0;
    public interface OnConfirmListener {
        void onConfirm(List<String> inputs);
    }

    public static void showDialog(Context context,String titleStr, OnConfirmListener listener) {
        IP_COUNT=0;
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.CustomDialogTheme);
        // 根布局
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(25, 25, 25, 25);

        // ===== 1. 标题 =====
        TextView title = new TextView(context);
        title.setText(titleStr);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.START);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = 30;
        rootLayout.addView(title, titleParams);

        // ===== 2. ScrollView + 输入框容器 =====
        ScrollView scrollView = new ScrollView(context);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // 设置最大高度（如 400dp）
        int maxHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 400,
                context.getResources().getDisplayMetrics()
        );
        scrollView.setLayoutParams(scrollParams);
        scrollView.setFillViewport(true);
        scrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

        // 限制 ScrollView 的最大高度
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                maxHeightPx
        ));

        // 输入框容器
        LinearLayout inputContainer = new LinearLayout(context);
        inputContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(inputContainer);

        rootLayout.addView(scrollView);

        TextView tvCount = new TextView(context);
        tvCount.setTextSize(23);

        // 添加首个输入框
        addInputRow(context,tvCount, inputContainer);
        tvCount.setText(context.getString(R.string.net_record_ip_count,IP_COUNT+""));

        // ===== 3. 底部按钮区域 =====
        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.END);
        buttonLayout.setPadding(0, 40, 0, 0);

        Button cancelBtn = new Button(context);
        cancelBtn.setText(R.string.dialog_cancel);
        cancelBtn.setTextSize(23);

        Button confirmBtn = new Button(context);
        confirmBtn.setText(R.string.dialog_confirm);
        confirmBtn.setTextSize(23);

        buttonLayout.addView(tvCount);
        buttonLayout.addView(cancelBtn);
        buttonLayout.addView(confirmBtn);
        rootLayout.addView(buttonLayout);

        // ===== 4. 创建并显示 Dialog =====
        AlertDialog dialog = builder.setView(rootLayout).create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        confirmBtn.setOnClickListener(v -> {
            List<String> inputs = new ArrayList<>();
            for (int i = 0; i < inputContainer.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) inputContainer.getChildAt(i);
                EditText et = (EditText) row.getChildAt(0);
                String etContent=et.getText().toString().trim();
                if (!TextUtils.isEmpty(etContent)){
                    inputs.add(etContent);
                }
            }
            listener.onConfirm(inputs);
            dialog.dismiss();
        });
        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        int[] size = DisplayUtils.getScreenSize(context);
        params.width = (int) (size[0] / 2);
//        params.height = (int) (size[1] / 4);
        window.setAttributes(params);
    }

    // 添加一个输入框行（EditText + [+]按钮）
    private static void addInputRow(Context context,TextView tvCount, LinearLayout container) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 20, 0, 0);

        EditText editText = new EditText(context);
        editText.setHint("请输入内容");
        editText.setTextSize(28);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));

        Button addBtn = new Button(context);
        addBtn.setText("+");
        addBtn.setTextSize(28);

        Button reduceBtn = new Button(context);
        reduceBtn.setText("-");
        reduceBtn.setTextSize(28);

        addBtn.setOnClickListener(v -> {
            if (RegularTools.isValidIpOrUrl(editText.getText().toString())){
                addBtn.setVisibility(View.GONE); // 隐藏当前按钮
                reduceBtn.setVisibility(View.GONE); // 隐藏当前按钮
                addInputRow(context,tvCount, container); // 添加新行
                tvCount.setText(context.getString(R.string.net_record_ip_count,IP_COUNT+""));
            }else{
                ToastUtils.error("输入格式错误!");
            }
        });

        reduceBtn.setOnClickListener(v -> {
            if (IP_COUNT>1){
                int index = container.indexOfChild(row);
                container.removeView(row);
                IP_COUNT--;
                tvCount.setText(context.getString(R.string.net_record_ip_count, IP_COUNT + ""));

                // 如果被删的是最后一行，恢复上一行的按钮
                int childCount = container.getChildCount();
                if (childCount > 0 && index - 1 >= 0 && index == childCount) {
                    LinearLayout prevRow = (LinearLayout) container.getChildAt(index - 1);
                    View prevAddBtn = prevRow.getChildAt(1);
                    View prevReduceBtn = prevRow.getChildAt(2);
                    prevAddBtn.setVisibility(View.VISIBLE);
                    prevReduceBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        row.addView(editText);
        row.addView(addBtn);
        row.addView(reduceBtn);
        container.addView(row);
        IP_COUNT++;
    }
}
