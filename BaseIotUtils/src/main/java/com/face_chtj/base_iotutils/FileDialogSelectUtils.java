package com.face_chtj.base_iotutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class FileDialogSelectUtils {
    private static final String EMPTY_PLACEHOLDER = "__EMPTY_PLACEHOLDER__";

    public interface FileSelectCallback {
        void onFileSelected(List<File> selectedFiles);
    }

    private final Context context;
    private final FileSelectCallback callback;
    private final List<File> fileList = new ArrayList<>();
    private final Set<String> selectedPathSet = new HashSet<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private File currentDir;
    private AlertDialog dialog;
    private FileListAdapter adapter;
    private ListView listView;
    private TextView titleView;
    private ExecutorService executor;

    private int itemTvSize = 16;
    private boolean singleSelect = false;
    private int paddingTb;
    private float widthRatio = 0.92f;
    private float heightRatio = 0.82f;
    private volatile int loadGeneration = 0;

    public FileDialogSelectUtils(Context context, File startDir, FileSelectCallback callback) {
        this.context = context;
        this.currentDir = normalizeStartDir(startDir);
        this.callback = callback;
        this.paddingTb = dp(10);
    }

    public FileDialogSelectUtils setSizeRatio(float widthRatio, float heightRatio, int itemTvSize) {
        setSizeRatio(widthRatio, heightRatio);
        if (itemTvSize > 0) {
            this.itemTvSize = itemTvSize;
        }
        return this;
    }

    public FileDialogSelectUtils setSingleSelect(boolean singleSelect) {
        this.singleSelect = singleSelect;
        if (singleSelect && selectedPathSet.size() > 1) {
            String firstPath = selectedPathSet.iterator().next();
            selectedPathSet.clear();
            selectedPathSet.add(firstPath);
        }
        return this;
    }

    public FileDialogSelectUtils setSizeRatio(float widthRatio, float heightRatio) {
        this.widthRatio = clamp(widthRatio, 0.5f, 1f);
        this.heightRatio = clamp(heightRatio, 0.4f, 1f);
        return this;
    }

    public void show() {
        if (!canShowDialog()) {
            return;
        }
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        ensureExecutor();

        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dp(18), dp(14), dp(18), dp(14));

        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        titleView.setGravity(Gravity.LEFT);
        titleView.setPadding(0, 0, 0, dp(10));
        titleView.setTextColor(Color.BLACK);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        rootLayout.addView(titleView);

        listView = new ListView(context);
        adapter = new FileListAdapter();
        listView.setAdapter(adapter);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
        rootLayout.addView(listView);

        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.RIGHT);
        buttonLayout.setPadding(0, dp(12), 0, 0);

        Button cancelButton = createDialogButton(context.getString(R.string.iot_cancel));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cancelParams.setMargins(0, 0, dp(12), 0);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        buttonLayout.addView(cancelButton);

        Button okButton = createDialogButton(context.getString(R.string.iot_ok));
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onFileSelected(buildSelectedFiles());
                }
                dismiss();
            }
        });
        buttonLayout.addView(okButton);
        rootLayout.addView(buttonLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        builder.setView(rootLayout);
        dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                shutdownWorker();
            }
        });
        dialog.show();
        adjustDialogSize();
        refreshFileList();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            shutdownWorker();
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
        return true;
    }

    private Button createDialogButton(String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
        button.setPadding(dp(16), dp(8), dp(16), dp(8));
        button.setTextColor(Color.BLACK);
        button.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_button_background));
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return button;
    }

    private void notifyViewChange(final int position) {
        if (position < 0 || position >= fileList.size()) {
            return;
        }
        File item = fileList.get(position);
        String name = item.getName();
        if (EMPTY_PLACEHOLDER.equals(name)) {
            return;
        }

        if ("..".equals(name)) {
            File parent = currentDir == null ? null : currentDir.getParentFile();
            if (parent != null && parent.canRead()) {
                currentDir = parent;
                refreshFileList();
            }
            return;
        }

        if (item.isDirectory()) {
            if (item.canRead()) {
                currentDir = item;
                refreshFileList();
            } else {
                ToastUtils.warning(context, "目录不可读").show();
            }
            return;
        }

        String path = item.getAbsolutePath();
        if (singleSelect) {
            selectedPathSet.clear();
            selectedPathSet.add(path);
        } else if (selectedPathSet.contains(path)) {
            selectedPathSet.remove(path);
        } else {
            selectedPathSet.add(path);
        }
        adapter.notifyDataSetChanged();
    }

    private void refreshFileList() {
        if (titleView == null || adapter == null) {
            return;
        }
        currentDir = normalizeStartDir(currentDir);
        final File targetDir = currentDir;
        final int generation = ++loadGeneration;

        titleView.setText(context.getString(R.string.iot_directory_title, targetDir.getAbsolutePath()));
        fileList.clear();
        fileList.add(new File(EMPTY_PLACEHOLDER));
        adapter.notifyDataSetChanged();

        ensureExecutor();
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final List<File> result = loadFiles(targetDir);
                    if (generation != loadGeneration) {
                        return;
                    }
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (generation != loadGeneration || dialog == null || !dialog.isShowing()) {
                                return;
                            }
                            fileList.clear();
                            fileList.addAll(result);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ignored) {
            // Dialog is closing; ignore stale refresh requests.
        }
    }

    private List<File> loadFiles(File dir) {
        List<File> result = new ArrayList<>();
        if (dir.getParentFile() != null) {
            result.add(new File(".."));
        }

        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            List<File> sorted = new ArrayList<>();
            Collections.addAll(sorted, files);
            Collections.sort(sorted, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            result.addAll(sorted);
        }

        if (result.isEmpty()) {
            result.add(new File(EMPTY_PLACEHOLDER));
        }
        return result;
    }

    private List<File> buildSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        for (String path : selectedPathSet) {
            selectedFiles.add(new File(path));
        }
        return selectedFiles;
    }

    private void adjustDialogSize() {
        if (dialog == null) return;

        Window window = dialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (metrics.widthPixels * widthRatio);
            lp.height = (int) (metrics.heightPixels * heightRatio);
            window.setAttributes(lp);
        }
    }

    private File normalizeStartDir(File startDir) {
        File fallback = Environment.getExternalStorageDirectory();
        if (startDir != null && startDir.exists() && startDir.isDirectory() && startDir.canRead()) {
            return startDir;
        }
        return fallback != null && fallback.exists() && fallback.isDirectory() && fallback.canRead()
                ? fallback
                : new File("/");
    }

    private void ensureExecutor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
    }

    private void shutdownWorker() {
        loadGeneration++;
        mainHandler.removeCallbacksAndMessages(null);
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        dialog = null;
    }

    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private int dp(int value) {
        if (context == null) {
            return value;
        }
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setPadding(dp(14), paddingTb, dp(14), paddingTb);
                layout.setGravity(Gravity.CENTER_VERTICAL);

                holder.checkBox = new CheckBox(context);
                holder.checkBox.setButtonDrawable(R.drawable.custom_checkbox);
                LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                checkBoxParams.setMargins(0, 0, dp(12), 0);
                holder.checkBox.setLayoutParams(checkBoxParams);
                layout.addView(holder.checkBox);

                holder.nameView = new TextView(context);
                holder.nameView.setSingleLine(false);
                holder.nameView.setMaxLines(2);
                holder.nameView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                holder.nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTvSize);
                holder.nameView.setTextColor(Color.BLACK);
                holder.nameView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                layout.addView(holder.nameView);

                convertView = layout;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            File file = fileList.get(position);
            String name = file.getName();
            boolean isPlaceholder = EMPTY_PLACEHOLDER.equals(name);
            boolean isBack = "..".equals(name);
            boolean selectableFile = !isPlaceholder && !isBack && !file.isDirectory();

            holder.checkBox.setOnClickListener(null);
            holder.checkBox.setVisibility(selectableFile ? View.VISIBLE : View.GONE);
            holder.checkBox.setChecked(selectableFile && selectedPathSet.contains(file.getAbsolutePath()));
            holder.nameView.setGravity(isPlaceholder ? Gravity.CENTER : Gravity.LEFT);
            holder.nameView.setTextColor(isPlaceholder ? Color.GRAY : Color.BLACK);
            holder.nameView.setText(isPlaceholder
                    ? context.getString(R.string.not_found_file_child_content)
                    : (isBack ? context.getString(R.string.iot_back_directory) : name));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyViewChange(position);
                }
            });
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyViewChange(position);
                }
            });
            return convertView;
        }
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView nameView;
    }
}
