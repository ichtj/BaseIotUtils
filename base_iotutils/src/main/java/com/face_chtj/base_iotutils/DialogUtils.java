package com.face_chtj.base_iotutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.DrawableRes;

import com.face_chtj.base_iotutils.callback.IDialogCallback;

public class DialogUtils {
    private static volatile DialogUtils mInstance;
    private AlertDialog mDialog;
    private EditText etContent;
    private IDialogCallback iCallback;

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

    public static DialogUtils setDialogCallback(IDialogCallback iCallback) {
        instance().iCallback = iCallback;
        return instance();
    }

    public static Button getPositive() {
        return instance().mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
    }

    public static Button getNegative() {
        return instance().mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    }

    public static EditText getEditeContent() {
        return instance().etContent;
    }

    public static void show(Context context, String title, String content) {
        createDialog(context, -1, title, content, 0);
    }

    public static void show(Context context, @DrawableRes int icon, String title, String content) {
        createDialog(context, icon, title, content, 0);
    }

    public static void showEdite(Context context, String title) {
        createDialog(context, -1, title, "", 1);
    }


    public static void showEdite(Context context, String title, String etContent) {
        createDialog(context, -1, title, etContent, 1);
    }


    public static void showEdite(Context context, @DrawableRes int icon, String title, String etContent) {
        createDialog(context, icon, title, etContent, 1);
    }


    public static void dismiss() {
        if (instance().mDialog != null) {
            instance().mDialog.dismiss();
        }
        if (instance().iCallback != null) {
            instance().iCallback.dismiss();
        }
        reset();
    }

    private static void reset() {
        instance().etContent = null;
        instance().mDialog = null;
    }

    private static void createDialog(Context context, @DrawableRes int icon, String title, String content, int caseValue) {
        if(instance().mDialog==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.dialog_bg);
            builder.setTitle(title);
            if (caseValue == 0) {
                builder.setMessage(content);
            }
            if (icon != -1) {
                builder.setIcon(icon);
            } else {
                builder.setIcon(R.drawable.ic_dialog_tool);
            }
            builder.setCancelable(true);
            builder.setPositiveButton(context.getString(R.string.iot_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (instance().iCallback != null) {
                        String etContent = instance().etContent != null ? instance().etContent.getText().toString() : "";
                        instance().iCallback.onPositiveClick(etContent);
                    }
                    dismiss();
                }
            });
            builder.setNegativeButton(context.getString(R.string.iot_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (instance().iCallback != null) {
                        instance().iCallback.onNegativeClick();
                    }
                    dismiss();
                }
            });
            instance().mDialog = builder.create();
            if (caseValue == 1) {
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_edite, null);
                instance().mDialog.setView(view);
            }
            instance().mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    reset();
                }
            });
            instance().mDialog.show();
            if (caseValue == 1) {
                instance().etContent = instance().mDialog.findViewById(R.id.etContent);
            }
            instance().mDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            if(instance().iCallback!=null){
                instance().iCallback.show();
            }
        }
    }
}