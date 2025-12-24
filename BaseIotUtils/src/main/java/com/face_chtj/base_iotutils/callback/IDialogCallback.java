package com.face_chtj.base_iotutils.callback;

public interface IDialogCallback<T> {
    void show();
    void onPositiveClick();
    void callback(T data);
    void onNegativeClick();
    void dismiss();
}
