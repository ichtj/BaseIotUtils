//package com.ichtj.basetools.util;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.util.DisplayMetrics;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.BaseAdapter;
//import android.widget.CheckBox;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//
//public class FileDialogSelectUtils {
//    public interface FileSelectCallback {
//        void onFileSelected(List<File> selectedFiles);
//    }
//
//    private final Context context;
//    private final FileSelectCallback callback;
//    private File currentDir;
//    private final List<File> fileList = new ArrayList<>();
//    private final List<File> selectedFiles = new ArrayList<>();
//    private AlertDialog dialog;
//    private FileListAdapter adapter;
//
//    private float widthRatio = 1f;
//    private float heightRatio = 1f;
//
//    public FileDialogSelectUtils(Context context, File startDir, FileSelectCallback callback) {
//        this.context = context;
//        this.currentDir = startDir;
//        this.callback = callback;
//    }
//
//    public FileDialogSelectUtils setSizeRatio(float widthRatio, float heightRatio) {
//        this.widthRatio = widthRatio;
//        this.heightRatio = heightRatio;
//        return this;
//    }
//
//    public void show() {
//        ListView listView = new ListView(context);
//        adapter = new FileListAdapter();
//        listView.setAdapter(adapter);
//
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            File item = fileList.get(position);
//            if (item.getName().equals("..")) {
//                currentDir = currentDir.getParentFile();
//                refreshFileList();
//            } else if (item.isDirectory()) {
//                currentDir = item;
//                refreshFileList();
//            } else {
//                // 文件：选中/取消
//                if (selectedFiles.contains(item)) {
//                    selectedFiles.remove(item);
//                } else {
//                    selectedFiles.add(item);
//                }
//                adapter.notifyDataSetChanged();
//            }
//        });
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("目录: " + currentDir.getAbsolutePath())
//                .setView(listView)
//                .setPositiveButton("确定", (d, w) -> callback.onFileSelected(new ArrayList<>(selectedFiles)))
//                .setNegativeButton("取消", null);
//        dialog = builder.create();
//        dialog.show();
//
//        // 设置 dialog 尺寸
//        Window window = dialog.getWindow();
//        if (window != null) {
//            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//            WindowManager.LayoutParams lp = window.getAttributes();
//            lp.width = (int) (metrics.widthPixels * widthRatio);
//            lp.height = (int) (metrics.heightPixels * heightRatio);
//            window.setAttributes(lp);
//        }
//
//        refreshFileList();
//    }
//
//    private void refreshFileList() {
//        fileList.clear();
//
//        if (currentDir.getParentFile() != null) {
//            fileList.add(new File(".."));
//        }
//
//        File[] files = currentDir.listFiles();
//        if (files != null) {
//            List<File> fileSorted = Arrays.asList(files);
//            Collections.sort(fileSorted, (f1, f2) -> {
//                if (f1.isDirectory() && !f2.isDirectory()) return -1;
//                else if (!f1.isDirectory() && f2.isDirectory()) return 1;
//                else return f1.getName().compareToIgnoreCase(f2.getName());
//            });
//            fileList.addAll(fileSorted);
//        }
//
//        if (adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
//
//        if (dialog != null) {
//            dialog.setTitle("目录: " + currentDir.getAbsolutePath());
//        }
//    }
//
//    private class FileListAdapter extends BaseAdapter {
//        @Override
//        public int getCount() {
//            return fileList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return fileList.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            File file = fileList.get(position);
//
//            LinearLayout layout = new LinearLayout(context);
//            layout.setOrientation(LinearLayout.HORIZONTAL);
//            layout.setPadding(30, 15, 30, 15);
//            layout.setGravity(Gravity.CENTER_VERTICAL);
//
//            TextView nameView = new TextView(context);
//            nameView.setTextSize(20);
//            nameView.setText(file.getName().equals("..") ? "↩ 返回上一级" : file.getName());
//            nameView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
//            layout.addView(nameView);
//
//            if (!file.isDirectory() && !file.getName().equals("..")) {
//                CheckBox checkBox = new CheckBox(context);
//                checkBox.setChecked(selectedFiles.contains(file));
//                checkBox.setEnabled(false); // 控制点击整行而不是 checkbox
//                layout.addView(checkBox);
//            }
//
//            return layout;
//        }
//    }
//}
