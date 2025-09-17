package com.face_chtj.base_iotutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.face_chtj.base_iotutils.callback.IDialogCallback;

/**
 * 根据照不同的需求，封装不同的dialog
 * 根据callback返回限定的结果
 * @param <T>
 */
public class DialogUtils<T> {
    private static volatile DialogUtils mInstance;
    private AlertDialog mDialog;
    private TextView tvTitle;
    private EditText etContent;
    private boolean isShowBoard;
    private IDialogCallback<?> iCallback;
    private boolean isClickBtn;

    //单例模式
    private static DialogUtils instance() {
        if (mInstance == null) {
            synchronized (DialogUtils.class) {
                if (mInstance == null) {
                    mInstance = new DialogUtils();
                }
            }
        }
        return mInstance;
    }

    public static <T> DialogUtils<T> setDialogCallback(IDialogCallback<T> iCallback) {
        instance().iCallback = iCallback;
        return instance();
    }

    public static EditText getEditeContent() {
        return instance().etContent;
    }

    public static void show(Context context, String title, String content) {
        createDialog(context, R.drawable.ic_dialog_tool, "", title, content, false);
    }

    public static void show(Context context, @DrawableRes int icon, String title, String content) {
        createDialog(context, icon, "", title, content, false);
    }

    public static void showEdite(Context context, String title) {
        createDialog(context, R.drawable.ic_dialog_tool, "", title, "", true);
    }

    public static void showCheckedItem(Context context, String title, String[] arrays) {
        showCheckedItem (context,title,arrays,true);
    }

    public static void showCheckedItem(Context context, String title, String[] arrays, boolean returnPositions) {
        final boolean[] checkedItems = new boolean[arrays.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMultiChoiceItems(arrays, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        });
        builder.setPositiveButton(R.string.iot_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                instance().isClickBtn = true;
                if (instance().iCallback != null) {
                    int count = 0;
                    for (boolean checked : checkedItems) {
                        if (checked) count++;
                    }
                    if (returnPositions) {
                        int[] result = new int[count];
                        int idx = 0;
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                result[idx++] = i;
                            }
                        }
                        instance().iCallback.callback(result);
                    } else {
                        String[] result = new String[count];
                        int idx = 0;
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                result[idx++] = arrays[i];
                            }
                        }
                        instance().iCallback.callback(result);
                    }
                    instance().iCallback.onPositiveClick();
                }
            }
        });
        builder.setNegativeButton(R.string.iot_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                instance().isClickBtn = true;
                if (instance().iCallback != null) {
                    instance().iCallback.onNegativeClick();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("onDismiss", "onDismiss >>>");
                reset();
                if (!instance().isClickBtn && instance().iCallback != null) {
                    instance().iCallback.dismiss();
                }
            }
        });
        dialog.show();
        if (instance().iCallback != null) {
            instance().iCallback.show();
        }
    }


    public static void showEdite(Context context, String title, String etContent) {
        createDialog(context, R.drawable.ic_dialog_tool, "", title, etContent, true);
    }


    public static void showEdite(Context context, @DrawableRes int icon, String title,
                                 String etContent) {
        createDialog(context, icon, "", title, etContent, true);
    }


    public static void dismiss() {
        if (instance().mDialog != null) {
            instance().mDialog.dismiss();
        }
        if (instance().iCallback != null) {
            instance().iCallback.dismiss();
        }
        KeyBoardUtils.closeKeybord(instance().etContent);
        reset();
    }

    private static void reset() {
        instance().etContent = null;
        instance().mDialog = null;
    }

    private static void createDialog(final Context context, @DrawableRes int icon, String hint, String title,
                                     final String content, final boolean isInput) {
        if (instance().mDialog == null) {
            instance().isClickBtn = false;
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.iot_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    instance().isClickBtn = true;
                    if (instance().iCallback != null) {
                        String etContent = instance().etContent != null ? instance().etContent.getText().toString() : "";
                        instance().iCallback.callback (etContent);
                        instance().iCallback.onPositiveClick();
                    }
                }
            });
            builder.setNegativeButton(R.string.iot_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    instance().isClickBtn = true;
                    if (instance().iCallback != null) {
                        instance().iCallback.onNegativeClick();
                    }
                }
            });
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_edite, null);
            instance().mDialog = builder.setView(view).create();
            instance().mDialog.setCanceledOnTouchOutside(false);
            instance().mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d("onDismiss", "onDismiss >>>");
                    reset();
                    if (!instance().isClickBtn) {
                        instance().iCallback.dismiss();
                    }
                }
            });

            instance().etContent = view.findViewById(R.id.etContent);
            instance().etContent.setBackground(isInput ? ContextCompat.getDrawable(context, R.drawable.ic_dialogalert_bg) : null);
            instance().tvTitle = view.findViewById(R.id.tvTitle);
            if (!isInput) {
                instance().tvTitle.setFocusable(true);
                instance().etContent.setGravity(Gravity.CENTER);
                instance().etContent.setLongClickable(false);
                instance().etContent.setClickable(false);
                instance().etContent.setEnabled(false);
                instance().mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
            Drawable drawableLeft = ContextCompat.getDrawable(context, icon);
            instance().tvTitle.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
            instance().etContent.setText(isInput ? "" : content);
            instance().etContent.setHint(isInput ? hint : "");
            instance().tvTitle.setText(title);
            instance().etContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInput) {
                        if (instance().isShowBoard) {
                            KeyBoardUtils.openKeybord(instance().etContent);
                            instance().isShowBoard = false;
                        } else {
                            KeyBoardUtils.closeKeybord(instance().etContent);
                            instance().isShowBoard = true;
                        }
                    }
                }
            });
            Window window = instance().mDialog.getWindow();
            window.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));
            WindowManager.LayoutParams params = window.getAttributes();
            int[] size = DisplayUtils.getScreenSize(context);
            params.width = (int) (size[0] / 2);
            params.height = (int) (size[1] / 4);
            window.setAttributes(params);
            instance().mDialog.show();
            if (instance().iCallback != null) {
                instance().iCallback.show();
            }
        }
    }

}
