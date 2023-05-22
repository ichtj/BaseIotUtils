package com.face_chtj.base_iotutils;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.face_chtj.base_iotutils.BaseIotUtils;

import java.lang.reflect.Method;

/**
 *
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:打开关闭软键盘
 * --打卡软键盘 {@link #openKeybord(EditText mEditText)}
 * --关闭软键盘 {@link #closeKeybord(EditText mEditText)}
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

    /**
     * 禁用显示输入的操作
     * @param etView 输入框
     */
    public static void disableShowInput(EditText etView) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            etView.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(etView, false);
            } catch (
                    Exception e) {//TODO: handle exception}try {method = cls.getMethod
                // ("setSoftInputShownOnFocus", boolean.class);method.setAccessible(true);method
                // .invoke(editText, false);} catch (Exception e) {//TODO: handle exception}}}

            }
        }
    }

}
