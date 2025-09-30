package com.face_chtj.base_iotutils;

import android.content.res.Resources;
import android.text.Html;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

public class FormatViewUtils {
    private static int MAXIMUM_ROW = 300;
    private static int MAXIMUM_LENGTH = 10000;

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

    public static void formatData(TextView tv, String str, String pattern){
        formatData(tv, str, pattern,false);
    }

    /**
     * show data to Activity
     *
     * @param str Support html tags
     * @param pattern time format yyyyMMddHHmmss or ....
     */
    public static void formatData(TextView tv, String str, String pattern,boolean jumpFirstLine) {
        // 假设 ObjectUtils.isEmpty 和 TimeUtils.getTodayDateHms 已经定义
        if (tv != null && !ObjectUtils.isEmpty(str)) {

            // --- 换行处理的核心优化 ---
            // 1. 处理 Windows/DOS 换行：将 "\r\n" 替换为 "<br>"
            String htmlStr = str.replace("\r\n", "<br>");

            // 2. 处理 Unix/Linux/Android 换行：将单个 "\n" 替换为 "<br>"
            htmlStr = htmlStr.replace("\n", "<br>");

            // 3. (可选) 处理旧 Mac 换行：将单个 "\r" 替换为 "<br>"
            htmlStr = htmlStr.replace("\r", "<br>");
            // ------------------------

            // 如果行数大于 MAXIMUM_ROW ，清空内容
            CharSequence currentText = tv.getText();
            if (currentText.length() > MAXIMUM_LENGTH || tv.getLineCount() > MAXIMUM_ROW) {
                tv.setText("");
            }

            boolean isNull = ObjectUtils.isEmpty(pattern);
            tv.append(isNull ? "" : TimeUtils.getTodayDateHms(pattern) + "：");

            // 2. 使用 Html.fromHtml() 处理替换后的 HTML 字符串
            // 推荐使用兼容性更好的 HtmlCompat
            tv.append(HtmlCompat.fromHtml(htmlStr, HtmlCompat.FROM_HTML_MODE_LEGACY));

            // 3. 在日志条目末尾追加的换行符 (保持不变)
            tv.append("\n");

            // 滚动逻辑 (保持不变)
            Layout layout = tv.getLayout();
            if (layout != null) {
                if (jumpFirstLine){
                    tv.scrollTo(0, 0);
                }else{
                    int scrollAmount = layout.getLineTop(tv.getLineCount()) - tv.getHeight();
                    tv.scrollTo(0, scrollAmount > 0 ? scrollAmount : 0);
                }
            }
        }
    }

    /**
     * show data to Activity
     *
     * @param htmlStr Support html tags
     */
    public static void formatData(TextView tv, String htmlStr) {
        formatData (tv, htmlStr, "",false);
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
