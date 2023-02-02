package com.wave_chtj.example.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.DialogUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.callback.IDialogCallback;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

public class DialogAty extends BaseActivity {
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        handler.postDelayed(runnable, 5000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ToastUtils.info("即将关闭对话框！");
            DialogUtils.dismiss();
            handler.postDelayed(this, 5000);
        }
    };

    public void normalDialogClick(View view) {
        DialogUtils.setDialogCallback(new IDialogCallback() {
            @Override
            public void show() {
                KLog.d("show() >> ");
            }

            @Override
            public void onPositiveClick(String content) {
                KLog.d("onPositiveClick() etContent >> " + content);
            }

            @Override
            public void onNegativeClick() {
                KLog.d("onNegativeClick() >> ");
            }

            @Override
            public void dismiss() {
                KLog.d("dismiss() >> ");
            }
        }).show(this, "ichtj", "这是一个测试dialog");
    }

    public void editeDialogClick(View view) {
        DialogUtils.setDialogCallback(new IDialogCallback() {
            @Override
            public void show() {
                KLog.d("show() >> ");
            }

            @Override
            public void onPositiveClick(String content) {
                KLog.d("onPositiveClick() etContent >> " + content);
            }

            @Override
            public void onNegativeClick() {
                KLog.d("onNegativeClick() >> ");
            }

            @Override
            public void dismiss() {
                KLog.d("dismiss() >> ");
            }
        }).showEdite(this, "ichtj", "这是一个测试dialog");
    }

    public void dismissClick(View view) {
        DialogUtils.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
