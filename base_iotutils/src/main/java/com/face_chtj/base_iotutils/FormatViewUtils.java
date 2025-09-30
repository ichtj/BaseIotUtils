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

    private static final int MAX_CHAR_LIMIT = 200000; // 超过此长度开始截断 (20万字符)
    private static final int TRIM_TO_LENGTH = 150000; // 截断后保留的长度 (15万字符)

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

    public static void formatContent(final TextView tv, String str, String pattern, boolean jumpFirstLine) {
        if (tv == null || ObjectUtils.isEmpty(str)) {
            return;
        }

        // --- 换行处理：将所有常见换行符替换为 <br> ---
        String htmlStr = str.replace("\r\n", "<br>")
                .replace("\n", "<br>")
                .replace("\r", "<br>");

        // ---------------------- 🌟 滚动窗口逻辑 (多显示/防OOM) ----------------------
        if (tv.length() > MAX_CHAR_LIMIT) {

            // 1. 计算截断点：保留最新的 150,000 字符
            int trimIndex = tv.length() - TRIM_TO_LENGTH;

            // 2. 寻找安全截断点（下一个日志行的开头）
            // 必须转为 String 才能高效查找 '\n'
            String fullText = tv.getText().toString();
            int firstNewline = fullText.indexOf('\n', trimIndex);

            if (firstNewline != -1 && firstNewline < tv.length() - 1) {
                // 找到安全点：从下一行开始保留，使用 subSequence 以保持 Span 格式
                tv.setText(tv.getText().subSequence(firstNewline + 1, tv.length()));
            } else {
                // 找不到安全点：直接从 trimIndex 截断
                tv.setText(tv.getText().subSequence(trimIndex, tv.length()));
            }
        }
        // ---------------------- 滚动窗口逻辑结束 ----------------------

        // --- 追加新内容 ---
        boolean isNull = ObjectUtils.isEmpty(pattern);
        String prefix = isNull ? "" : (TimeUtils.getTodayDateHms(pattern) + "：");

        tv.append(prefix);
        tv.append(HtmlCompat.fromHtml(htmlStr, HtmlCompat.FROM_HTML_MODE_LEGACY));
        tv.append("\n"); // 在整个日志条目末尾追加换行

        // --- 滚动逻辑 ---
        Layout layout = tv.getLayout();
        if (layout != null) {
            if (jumpFirstLine){
                // 滚动到顶部
                tv.scrollTo(0, 0);
            } else {
                // 滚动到底部 (确保显示最新内容)
                final int scrollAmount = layout.getLineTop(tv.getLineCount()) - tv.getHeight();
                // 使用 post 确保滚动发生在文本布局完成后，更加可靠
                tv.post (new Runnable ( ) {
                    @Override
                    public void run() {
                        tv.scrollTo(0, scrollAmount > 0 ? scrollAmount : 0);
                    }
                });
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
