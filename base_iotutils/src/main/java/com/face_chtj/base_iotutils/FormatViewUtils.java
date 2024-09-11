package com.face_chtj.base_iotutils;

import android.content.res.Resources;
import android.text.Html;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class FormatViewUtils {
    private static int MAXIMUM_ROW = 300;

    public static void setMaximumRow(int num) {
        MAXIMUM_ROW = num;
    }

    /**
     * A movement method that interprets movement keys by scrolling the text buffer.
     *
     * @param textView
     */
    public static void setMovementMethod(TextView textView) {
        textView.setMovementMethod (ScrollingMovementMethod.getInstance ( ));
    }

    /**
     * scroll back to top
     *
     * @param textView
     */
    public static void scrollBackToTop(TextView textView) {
        textView.scrollTo (0, 0);
        textView.setText ("");
    }

    /**
     * show data to Activity
     *
     * @param htmlStr Support html tags
     * @param pattern time format yyyyMMddHHmmss or ....
     */
    public static void formatData(TextView tv, String htmlStr, String pattern) {
        if (tv != null && !ObjectUtils.isEmpty (htmlStr)) {
            // 如果行数大于 MAXIMUM_ROW ，清空内容
            if (tv.getLineCount ( ) > MAXIMUM_ROW) {
                tv.setText ("");
            }
            boolean isNull = ObjectUtils.isEmpty (pattern);
            tv.append (isNull ? "" : TimeUtils.getTodayDateHms (pattern) + "：");
            tv.append (Html.fromHtml (htmlStr));
            tv.append ("\n");
            Layout layout = tv.getLayout ( );
            if (layout != null) {
                int scrollAmount = layout.getLineTop (tv.getLineCount ( )) - tv.getHeight ( );
                tv.scrollTo (0, scrollAmount > 0 ? scrollAmount : 0);
            }
        }
    }

    /**
     * show data to Activity
     *
     * @param htmlStr Support html tags
     */
    public static void formatData(TextView tv, String htmlStr) {
        formatData (tv, htmlStr, "");
    }

    public static String formatUnderline(int color, String content) {
        return "<u><font color='" + getHexColor (color) + "'>" + content + "</font></u>";
    }


    public static String formatColor(String content, int color) {
        if (ObjectUtils.isEmpty (content)) {
            return content;
        } else {
            return "<font color=\"" + getHexColor (color) + "\">" + content + "</font>";
        }
    }

    public static String getHexColor(int color) {
        // 获取 Resources 对象
        Resources res = BaseIotUtils.getContext ( ).getResources ( );
        // 通过 Resources 对象获取颜色值
        int colorAccentValue = res.getColor (color);
        // 将颜色值转换为十六进制表示的字符串
        return String.format ("#%06X", (0xFFFFFF & colorAccentValue));
    }
}
