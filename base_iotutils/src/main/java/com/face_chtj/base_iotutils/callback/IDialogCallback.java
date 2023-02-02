package com.face_chtj.base_iotutils.callback;

public interface IDialogCallback {
    void show();
    void onPositiveClick(String content);
    void onNegativeClick();
    void dismiss();
}
