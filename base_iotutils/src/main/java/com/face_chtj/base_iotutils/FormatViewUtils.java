package com.face_chtj.base_iotutils;

import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FormatViewUtils {
    /*红色*/
    public static final int COLOR_RED = 0x10;
    /*绿色*/
    public static final int COLOR_GREEN = 0x11;
    /*黑色*/
    public static final int COLOR_BLACK = 0x12;
    /*灰色*/
    public static final int COLOR_GREY = 0x13;
    /*橙色*/
    public static final int COLOR_ORANGE = 0x14;

    @IntDef({COLOR_RED, COLOR_GREEN, COLOR_BLACK, COLOR_GREY, COLOR_ORANGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IColor {
    }

    /**
     * A movement method that interprets movement keys by scrolling the text buffer.
     *
     * @param textView
     */
    public static void setMovementMethod(TextView textView) {
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * show data to Activity
     *
     * @param htmlStr Support html tags
     */
    public static void formatData(TextView textView, String htmlStr) {
        textView.append(Html.fromHtml(TimeUtils.getTodayDateHms("yy-MM-dd HH:mm:ss") + "：" + htmlStr));
        textView.append("\n");
        //刷新最新行显示
        int offset = textView.getLineCount() * textView.getLineHeight();
        int tvHeight = textView.getHeight();
        if (offset > 6000) {
            textView.setText("");
            textView.scrollTo(0, 0);
        } else {
            if (offset > tvHeight) {
                //Log.d(TAG, "showData: offset >> " + offset + " ,tvHeight >> " + tvHeight);
                textView.scrollTo(0, offset - tvHeight);
            }
        }
    }

    public static String formatColor(String content, @IColor int color) {
        if (ObjectUtils.isEmpty(content)) {
            return content;
        } else {
            return "<font color=\""+getColor(color)+"\">" + content + "</font>";
        }
    }

    public static String formatColor(String content, String color) {
        if (ObjectUtils.isEmpty(content)) {
            return content;
        } else {
            return "<font color=\""+color+"\">" + content + "</font>";
        }
    }

    private static String getColor(@IColor int color) {
        if (color == COLOR_RED) {
            return "#FF0000";
        } else if (color == COLOR_GREEN) {
            return "#00FF37";
        } else if (color == COLOR_BLACK) {
            return "#111010";
        } else if (color == COLOR_GREY) {
            return "#767876";
        } else if (color == COLOR_ORANGE) {
            return "#e17808";
        }
        return "#111010";
    }
}
