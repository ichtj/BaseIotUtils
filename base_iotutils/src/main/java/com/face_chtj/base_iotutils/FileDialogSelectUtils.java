package com.face_chtj.base_iotutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileDialogSelectUtils {
    public interface FileSelectCallback {
        void onFileSelected(List<File> selectedFiles);
    }

    private final Context context;
    private final FileSelectCallback callback;
    private File currentDir;
    private final List<File> fileList = new ArrayList<>();
    private final List<File> selectedFiles = new ArrayList<>();
    private AlertDialog dialog;
    private FileListAdapter adapter;
    private int itemTvSize=20;
    private boolean singleSelect = false;
    private ListView listView;
    private TextView titleView;
    private LinearLayout rootLayout;

    private float widthRatio = 1f;
    private float heightRatio = 1f;

    public FileDialogSelectUtils(Context context, File startDir, FileSelectCallback callback) {
        this.context = context;
        this.currentDir = startDir;
        this.callback = callback;
    }

    public FileDialogSelectUtils setSizeRatio(float widthRatio, float heightRatio,int itemTvSize) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        this.itemTvSize = itemTvSize;
        return this;
    }

    public FileDialogSelectUtils setSingleSelect(boolean singleSelect) {
        this.singleSelect = singleSelect;
        return this;
    }

    public FileDialogSelectUtils setSizeRatio(float widthRatio, float heightRatio) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        return this;
    }

    public void show() {
        // 创建根布局
        rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(30, 20, 30, 20);

        // 创建标题
        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        titleView.setGravity(Gravity.LEFT);
        titleView.setPadding(0, 0, 0, 20);
        titleView.setTextColor(Color.BLACK);
        titleView.setTypeface(null, Typeface.BOLD);
        rootLayout.addView(titleView);

        // 创建ListView
        listView = new ListView(context);
        adapter = new FileListAdapter();
        listView.setAdapter(adapter);

        // 设置ListView的权重为1，占据剩余空间
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        listView.setLayoutParams(listParams);
        rootLayout.addView(listView);

        // 创建按钮布局
        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.RIGHT);
        buttonLayout.setPadding(0, 20, 0, 0);

        // 创建取消按钮
        Button cancelButton = new Button(context);
        cancelButton.setText(context.getString(R.string.iot_cancel));
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        cancelButton.setPadding(20, 15, 20, 15);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cancelParams.setMargins(0, 0, 12, 0);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        buttonLayout.addView(cancelButton);

        // 创建确定按钮
        Button okButton = new Button(context);
        okButton.setText(context.getString(R.string.iot_ok));
        okButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        okButton.setPadding(20, 15, 20, 15);
        okButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onFileSelected(new ArrayList<>(selectedFiles));
                dialog.dismiss();
            }
        });
        buttonLayout.addView(okButton);

        rootLayout.addView(buttonLayout);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File item = fileList.get(position);
                if (item.getName().equals("..")) {
                    currentDir = currentDir.getParentFile();
                    refreshFileList();
                } else if (item.isDirectory()) {
                    currentDir = item;
                    refreshFileList();
                } else {
                    // 文件：选中/取消
                    if (singleSelect) {
                        selectedFiles.clear();
                        selectedFiles.add(item);
                    } else {
                        if (selectedFiles.contains(item)) {
                            selectedFiles.remove(item);
                        } else {
                            selectedFiles.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setView(rootLayout);
        dialog = builder.create();
        dialog.show();

        refreshFileList();
    }

    private void refreshFileList() {
        fileList.clear();

        if (currentDir.getParentFile() != null) {
            fileList.add(new File(".."));
        }

        File[] files = currentDir.listFiles();
        if (files != null) {
            List<File> fileSorted = Arrays.asList(files);
            Collections.sort(fileSorted, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    else if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    else return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            fileList.addAll(fileSorted);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (titleView != null) {
            titleView.setText(context.getString(R.string.iot_directory_title, currentDir.getAbsolutePath()));

            // 动态调整Dialog高度
            adjustDialogHeight();
        }
    }

    private void adjustDialogHeight() {
        if (dialog == null || rootLayout == null) return;

        // 使用post确保布局已经完成
        rootLayout.post(new Runnable() {
            @Override
            public void run() {
                Window window = dialog.getWindow();
                if (window != null) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.width = (int) (metrics.widthPixels * widthRatio);

                    // 计算ListView的实际内容高度
                    int listViewHeight = calculateListViewHeight();

                    // 计算Dialog的其他组件高度（标题、按钮等）
                    int dialogExtraHeight = calculateDialogExtraHeight();

                    // 实际内容总高度
                    int actualContentHeight = listViewHeight + dialogExtraHeight;

                    // 设置的最大高度
                    int maxHeight = (int) (metrics.heightPixels * heightRatio);

                    // 选择较小的高度
                    lp.height = Math.min(actualContentHeight, maxHeight);

                    window.setAttributes(lp);
                }
            }
        });
    }

    private int calculateListViewHeight() {
        if (adapter == null || adapter.getCount() == 0) {
            return 0;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, listView);
            item.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += item.getMeasuredHeight();
        }

        // 加上ListView的分隔线高度
        if (adapter.getCount() > 1) {
            totalHeight += (adapter.getCount() - 1) * listView.getDividerHeight();
        }

        return totalHeight;
    }

    private int calculateDialogExtraHeight() {
        // 估算Dialog其他组件的高度
        // 包括：标题高度、按钮高度、内边距等

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        // 标题高度（根据itemTvSize估算）
        int titleHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, itemTvSize + 10, metrics);

        // 按钮高度（根据itemTvSize估算）
        int buttonHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, itemTvSize + 20, metrics);

        // 内边距
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80, metrics);

        return titleHeight + buttonHeight + padding;
    }

    private class FileListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return fileList.size();
        }

        @Override
        public Object getItem(int position) {
            return fileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            File file = fileList.get(position);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(24, 15, 24, 15);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            TextView nameView = new TextView(context);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
            nameView.setText(file.getName().equals("..") ? context.getString(R.string.iot_back_directory) : file.getName());
            nameView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            layout.addView(nameView);

            if (!file.isDirectory() && !file.getName().equals("..")) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setChecked(selectedFiles.contains(file));
                checkBox.setEnabled(false); // 控制点击整行而不是 checkbox
                checkBox.setButtonDrawable(R.drawable.custom_checkbox);
                layout.addView(checkBox);
            }

            return layout;
        }
    }
}