package com.face_chtj.base_iotutils;

import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FormatViewUtils {
    /*红色*/
    public static final int C_RED = 0x10;
    /*绿色*/
    public static final int C_GREEN = 0x11;
    /*黑色*/
    public static final int C_BLACK = 0x12;
    /*灰色*/
    public static final int C_GREY = 0x13;
    /*橙色*/
    public static final int C_ORANGE = 0x14;
    /*蓝色*/
    public static final int C_BLUE = 0x15;

    @IntDef({C_RED, C_GREEN, C_BLACK, C_GREY, C_ORANGE, C_BLUE})
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
     * scroll back to top
     *
     * @param textView
     */
    public static void scrollBackToTop(TextView textView) {
        textView.scrollTo(0, 0);
        textView.setText("");
    }

    /**
     * show data to Activity
     *
     * @param htmlStr Support html tags
     */
    public static void formatData(TextView textView, String htmlStr) {
        if (textView != null && !ObjectUtils.isEmpty(htmlStr)) {
            textView.append(Html.fromHtml(TimeUtils.getTodayDateHms("yyyy-MM-dd-HHmmss-SS") + "：" + htmlStr));
            textView.append("\n");
            Layout layout = textView.getLayout();
            if (layout != null) {
                int scrollAmount = layout.getLineTop(textView.getLineCount()) - textView.getHeight();
                if (scrollAmount > 0) {
                    textView.scrollTo(0, scrollAmount);
                } else {
                    textView.scrollTo(0, 0);
                }
            }
        }
    }

    public static String formatUnderlin(@IColor int color, String content) {
        return "<u><font color='" + getColor(color) + "'>" + content + "</font></u>";
    }

    public static String formatColor(String content, @IColor int color) {
        if (ObjectUtils.isEmpty(content)) {
            return content;
        } else {
            return "<font color=\"" + getColor(color) + "\">" + content + "</font>";
        }
    }

    public static String formatColor(String content, String color) {
        if (ObjectUtils.isEmpty(content)) {
            return content;
        } else {
            return "<font color=\"" + color + "\">" + content + "</font>";
        }
    }

    private static String getColor(@IColor int color) {
        if (color == C_RED) {
            return "#FF0000";
        } else if (color == C_GREEN) {
            return "#00FF37";
        } else if (color == C_BLACK) {
            return "#111010";
        } else if (color == C_GREY) {
            return "#767876";
        } else if (color == C_ORANGE) {
            return "#e17808";
        } else if (color == C_BLUE) {
            return "#0000FB";
        }
        return "#111010";
    }
}
