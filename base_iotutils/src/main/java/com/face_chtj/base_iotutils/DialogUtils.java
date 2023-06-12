package com.face_chtj.base_iotutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.face_chtj.base_iotutils.callback.IDialogCallback;

public class DialogUtils {
    private static volatile DialogUtils mInstance;
    private AlertDialog mDialog;
    private TextView tvTitle;
    private EditText etContent;
    private boolean isShowlBottomView;
    private boolean isShowBoard;
    private IDialogCallback iCallback;
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

    public static DialogUtils setDialogCallback(IDialogCallback iCallback) {
        instance().iCallback = iCallback;
        return instance();
    }

    public static DialogUtils setBottomVisible(boolean isShow) {
        instance().isShowlBottomView=isShow;
        return instance();
    }

    public static EditText getEditeContent() {
        return instance().etContent;
    }

    public static void show(Context context, String title, String content) {
        createDialog(context, -1, title, content, false);
    }

    public static void show(Context context, @DrawableRes int icon, String title, String content) {
        createDialog(context, icon, title, content, false);
    }

    public static void showEdite(Context context, String title) {
        createDialog(context, -1, title, "", true);
    }


    public static void showEdite(Context context, String title, String etContent) {
        createDialog(context, -1, title, etContent, true);
    }


    public static void showEdite(Context context, @DrawableRes int icon, String title,
                                 String etContent) {
        createDialog(context, icon, title, etContent, true);
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

    private static void createDialog(Context context, @DrawableRes int icon, String title,
                                     String content, final boolean needEnter) {
        if (instance().mDialog == null) {
            instance().isClickBtn=false;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_edite, null);
            instance().mDialog = builder.setView(view).create();
            instance().mDialog.setCanceledOnTouchOutside(true);
            instance().mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d("onDismiss","onDismiss >>>");
                    reset();
                    if(!instance().isClickBtn){
                        instance().iCallback.dismiss();
                    }
                }
            });
            instance().etContent = view.findViewById(R.id.etContent);
            instance().etContent.setBackground(needEnter?ContextCompat.getDrawable(context,R.drawable.ic_dialogalert_bg):null);
            instance().tvTitle = view.findViewById(R.id.tvTitle);
            view.findViewById(R.id.lBottomView).setVisibility(instance().isShowlBottomView?View.VISIBLE:View.GONE);
            if(!needEnter){
                instance().tvTitle.setFocusable(true);
                instance().etContent.setLongClickable(false);
                instance().etContent.setClickable(false);
                instance().etContent.setEnabled(false);
                instance().mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
            instance().etContent.setText(needEnter?"":content);
            instance().tvTitle.setText(title);
            instance().etContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(needEnter){
                        if(instance().isShowBoard){
                            KeyBoardUtils.openKeybord(instance().etContent);
                            instance().isShowBoard=false;
                        }else{
                            KeyBoardUtils.closeKeybord(instance().etContent);
                            instance().isShowBoard=true;
                        }
                    }
                }
            });
            view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instance().isClickBtn=true;
                    if (instance().iCallback != null) {
                        String etContent = instance().etContent != null ? instance().etContent.getText().toString() : "";
                        instance().iCallback.onPositiveClick(etContent);
                    }
                    dismiss();
                }
            });
            view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instance().isClickBtn=true;
                    if (instance().iCallback != null) {
                        instance().iCallback.onNegativeClick();
                    }
                    dismiss();
                }
            });
            Window window=instance().mDialog.getWindow();
            window.setBackgroundDrawable(ContextCompat.getDrawable(context,android.R.color.transparent));
            WindowManager.LayoutParams params = window.getAttributes();
            int[] size=DisplayUtils.getScreenSize(context);
            params.width = (int) (size[0] /2);
            params.height=(int)(size[1]/4);
            window.setAttributes(params);
            instance().mDialog.show();
            //window.setContentView(view);
            if (instance().iCallback != null) {
                instance().iCallback.show();
            }
        }
    }
}
