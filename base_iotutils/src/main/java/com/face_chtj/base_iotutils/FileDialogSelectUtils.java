package com.face_chtj.base_iotutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

    private float widthRatio = 1f;
    private float heightRatio = 1f;

    public FileDialogSelectUtils(Context context, File startDir, FileSelectCallback callback) {
        this.context = context;
        this.currentDir = startDir;
        this.callback = callback;
    }

    public FileDialogSelectUtils setSizeRatio(float widthRatio, float heightRatio) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        return this;
    }

    public void show() {
        ListView listView = new ListView(context);
        adapter = new FileListAdapter();
        listView.setAdapter(adapter);

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
                    if (selectedFiles.contains(item)) {
                        selectedFiles.remove(item);
                    } else {
                        selectedFiles.add(item);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.iot_directory_title,currentDir.getAbsolutePath()))
                .setView(listView)
                .setPositiveButton(context.getString(R.string.iot_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onFileSelected(new ArrayList<>(selectedFiles));
                    }
                })
                .setNegativeButton(context.getString(R.string.iot_cancel), null);
        dialog = builder.create();
        dialog.show();

        // 设置 dialog 尺寸
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (metrics.widthPixels * widthRatio);
            lp.height = (int) (metrics.heightPixels * heightRatio);
            window.setAttributes(lp);
        }

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

        if (dialog != null) {
            dialog.setTitle(context.getString(R.string.iot_directory_title,currentDir.getAbsolutePath()));
        }
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
            layout.setPadding(30, 15, 30, 15);
            layout.setGravity(Gravity.CENTER_VERTICAL);

            TextView nameView = new TextView(context);
            nameView.setTextSize(20);
            nameView.setText(file.getName().equals("..") ? context.getString(R.string.iot_back_directory) : file.getName());
            nameView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            layout.addView(nameView);

            if (!file.isDirectory() && !file.getName().equals("..")) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setChecked(selectedFiles.contains(file));
                checkBox.setEnabled(false); // 控制点击整行而不是 checkbox
                layout.addView(checkBox);
            }

            return layout;
        }
    }
}
