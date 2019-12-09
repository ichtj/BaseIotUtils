package com.chtj.base_iotutils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.chtj.base_iotutils.keeplive.BaseIotUtils;

/**
 * 打开关闭软键盘
 * @author chtj
 */
public class KeyBoardUtils {
    public KeyBoardUtils() {
        throw new AssertionError();
    }

    /**
     * 打卡软键盘
     * @param mEditText 点击的输入框
     */
    public static void openKeybord(EditText mEditText) {
        InputMethodManager imm = (InputMethodManager)  BaseIotUtils.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     * @param mEditText 输入框
     */
    public static void closeKeybord(EditText mEditText) {
        InputMethodManager imm = (InputMethodManager)  BaseIotUtils.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }
}
