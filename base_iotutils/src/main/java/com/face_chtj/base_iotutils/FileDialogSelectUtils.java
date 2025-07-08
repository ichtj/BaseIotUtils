package com.face_chtj.base_iotutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
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
    private static final String TAG = FileDialogSelectUtils.class.getSimpleName();
    private static final String EMPTY_PLACEHOLDER = "__EMPTY_PLACEHOLDER__";

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
    private int itemTvSize = 20;
    private boolean singleSelect = false;
    private ListView listView;
    private TextView titleView;
    private LinearLayout buttonLayout;
    private LinearLayout rootLayout;

    private float widthRatio = 1f;
    private float heightRatio = 1f;

    public FileDialogSelectUtils(Context context, File startDir, FileSelectCallback callback) {
        this.context = context;
        this.currentDir = startDir;
        this.callback = callback;
    }

    public FileDialogSelectUtils setSizeRatio(float widthRatio, float heightRatio, int itemTvSize) {
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
        rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(30, 20, 30, 20);

        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        titleView.setGravity(Gravity.LEFT);
        titleView.setPadding(0, 0, 0, 20);
        titleView.setTextColor(Color.BLACK);
        titleView.setTypeface(null, Typeface.BOLD);
        rootLayout.addView(titleView);

        listView = new ListView(context);
        adapter = new FileListAdapter();
        listView.setAdapter(adapter);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
        rootLayout.addView(listView);

        buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.RIGHT);
        buttonLayout.setPadding(0, 20, 0, 0);

        Button cancelButton = new Button(context);
        cancelButton.setText(context.getString(R.string.iot_cancel));
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        cancelButton.setPadding(20, 10, 20, 10);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cancelParams.setMargins(0, 0, 12, 0);
        cancelButton.setTextColor(Color.BLACK); // 可根据你设计风格设置
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setBackground(ContextCompat.getDrawable(context,R.drawable.custom_button_background));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        buttonLayout.addView(cancelButton);

        Button okButton = new Button(context);
        okButton.setText(context.getString(R.string.iot_ok));
        okButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        okButton.setPadding(20, 10, 20, 10);
        okButton.setTextColor(Color.BLACK); // 可根据你设计风格设置
        okButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        okButton.setBackground(ContextCompat.getDrawable(context,R.drawable.custom_button_background));
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
                if (EMPTY_PLACEHOLDER.equals(item.getName())) return;

                if ("..".equals(item.getName())) {
                    currentDir = currentDir.getParentFile();
                    FileDialogSelectUtils.this.refreshFileList();
                } else if (item.isDirectory()) {
                    currentDir = item;
                    FileDialogSelectUtils.this.refreshFileList();
                } else {
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
        if (files != null && files.length > 0) {
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
        } else {
            fileList.add(new File(EMPTY_PLACEHOLDER));
        }

        adapter.notifyDataSetChanged();
        titleView.setText(context.getString(R.string.iot_directory_title, currentDir.getAbsolutePath()));
        listView.post(new Runnable() {
            @Override
            public void run() {
                FileDialogSelectUtils.this.adjustDialogHeight();
            }
        });
    }

    private void adjustDialogHeight() {
        if (dialog == null || rootLayout == null) return;

        Window window = dialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (metrics.widthPixels * widthRatio);

            int listViewHeight = calculateListViewHeight();
            int dialogExtraHeight = calculateDialogExtraHeight();
            int actualContentHeight = listViewHeight + dialogExtraHeight;
            int maxHeight = (int) (metrics.heightPixels * heightRatio);

            lp.height = Math.min(actualContentHeight, maxHeight);
            window.setAttributes(lp);
        }
    }

    private int calculateListViewHeight() {
        if (adapter == null || adapter.getCount() == 0) return 0;

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, listView);
            item.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += item.getMeasuredHeight();
        }

        if (adapter.getCount() > 1) {
            totalHeight += (adapter.getCount() - 1) * listView.getDividerHeight();
        }

        return totalHeight;
    }

    private int calculateDialogExtraHeight() {
        int total = 0;

        if (titleView != null) {
            titleView.measure(
                    View.MeasureSpec.makeMeasureSpec(rootLayout.getWidth(), View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            total += titleView.getMeasuredHeight();
        }

        if (buttonLayout != null) {
            buttonLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(rootLayout.getWidth(), View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            total += buttonLayout.getMeasuredHeight();
        }

        // 加上内边距（上下的 padding）
        total += rootLayout.getPaddingTop() + rootLayout.getPaddingBottom();

        return total;
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
            layout.setPadding(22, 12, 22, 12);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            TextView nameView = new TextView(context);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
            nameView.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            if (EMPTY_PLACEHOLDER.equals(file.getName())) {
                nameView.setText(R.string.not_found_file_child_content);
                nameView.setTextColor(Color.GRAY);
                nameView.setGravity(Gravity.CENTER);
                layout.setGravity(Gravity.CENTER);
            } else {
                nameView.setText(file.getName().equals("..") ?
                        context.getString(R.string.iot_back_directory) : file.getName());

                if (!file.isDirectory() && !file.getName().equals("..")) {
                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setChecked(selectedFiles.contains(file));
                    checkBox.setEnabled(false);
                    checkBox.setButtonDrawable(R.drawable.custom_checkbox);

                    // 设置右边距（比如 20dp）
                    LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    int marginRightPx = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
                    checkBoxParams.setMargins(0, 0, marginRightPx, 0);
                    checkBox.setLayoutParams(checkBoxParams);

                    layout.addView(checkBox);
                }
            }

            layout.addView(nameView);
            return layout;
        }
    }
}
