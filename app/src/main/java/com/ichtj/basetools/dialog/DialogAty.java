package com.ichtj.basetools.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.DialogUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.LoadDialogUtils;
import com.face_chtj.base_iotutils.callback.IDialogCallback;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogAty extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
    }

    public void normalDialogClick(View view) {
        DialogUtils.setDialogCallback(new IDialogCallback<String>() {
            @Override
            public void show() {
                KLog.d("show() >> ");
            }

            @Override
            public void onPositiveClick() {
            }

            @Override
            public void callback(String content) {
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
        DialogUtils.setDialogCallback(new IDialogCallback<String>() {
            @Override
            public void show() {
                KLog.d("show() >> ");
            }

            @Override
            public void onPositiveClick() {
                KLog.d ("onPositiveClick() >> ");
            }

            @Override
            public void callback(String content) {
                KLog.d("callback >> " + content);
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

    public void moreSelectDialogClick(View view) {
        DialogUtils.setDialogCallback(new IDialogCallback<int[]>() {
            @Override
            public void show() {
                KLog.d("show() >> ");
            }

            @Override
            public void onPositiveClick() {
                KLog.d ("onPositiveClick() >> ");
            }

            @Override
            public void callback(int[] data) {
                KLog.d("callback >> " + Arrays.toString (data ));
            }

            @Override
            public void onNegativeClick() {
                KLog.d("onNegativeClick() >> ");
            }

            @Override
            public void dismiss() {
                KLog.d("dismiss() >> ");
            }
        }).showCheckedItem (this, "ichtj", new String[]{"选项一", "选项二", "选项三", "选项四"});
    }

    public void dismissClick(View view) {
        DialogUtils.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
