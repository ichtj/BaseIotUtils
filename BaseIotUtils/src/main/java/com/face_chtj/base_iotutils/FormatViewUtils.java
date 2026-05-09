package com.face_chtj.base_iotutils;

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

public class FormatViewUtils {
    private static int MAXIMUM_ROW = 300;
    private static int MAXIMUM_LENGTH = 10000;
    private static int MAXIMUM_SINGLE_INPUT_LENGTH = 4000;

    public static void setMaximumRow(int num) {
        MAXIMUM_ROW = Math.max(1, num);
    }

    public static void setMaximumLength(int num) {
        MAXIMUM_LENGTH = Math.max(1, num);
    }

    public static void setMaximumSingleInputLength(int num) {
        MAXIMUM_SINGLE_INPUT_LENGTH = Math.max(1, num);
    }

    public static void setMovementMethod(TextView textView) {
        if (textView != null) {
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
    }

    public static void scrollBackToTop(TextView textView) {
        if (textView == null) {
            return;
        }
        textView.setText("");
        textView.scrollTo(0, 0);
    }

    public static void formatData(TextView tv, String str, String pattern) {
        formatData(tv, str, pattern, false);
    }

    public static void formatData(TextView tv, String str, String pattern, boolean jumpFirstLine) {
        if (tv == null || ObjectUtils.isEmpty(str)) {
            return;
        }

        SpannableStringBuilder nextText = new SpannableStringBuilder();
        CharSequence currentText = tv.getText();
        if (!ObjectUtils.isEmpty(currentText)) {
            nextText.append(currentText);
        }

        nextText.append(buildEntry(str, pattern));
        trimToMaximumLength(nextText);
        tv.setText(nextText, TextView.BufferType.SPANNABLE);
        trimToMaximumRows(tv, jumpFirstLine);
    }

    public static void formatData(TextView tv, String htmlStr) {
        formatData(tv, htmlStr, "", false);
    }

    public static String formatUnderline(int color, String content) {
        return "<u><font color='" + getHexColor(color) + "'>" + content + "</font></u>";
    }

    public static String formatColor(String content, int color) {
        if (ObjectUtils.isEmpty(content)) {
            return content;
        }
        return "<font color=\"" + getHexColor(color) + "\">" + content + "</font>";
    }

    public static String getHexColor(int color) {
        int colorValue = ContextCompat.getColor(BaseIotUtils.getContext(), color);
        return String.format("#%06X", (0xFFFFFF & colorValue));
    }

    private static CharSequence buildEntry(String str, String pattern) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!ObjectUtils.isEmpty(pattern)) {
            builder.append(TimeUtils.getTodayDateHms(pattern)).append(":");
        }
        String safeHtml = normalizeLineBreaks(limitSingleInput(str));
        builder.append(HtmlCompat.fromHtml(safeHtml, HtmlCompat.FROM_HTML_MODE_LEGACY));
        builder.append('\n');
        return builder;
    }

    private static String limitSingleInput(String str) {
        if (str.length() <= MAXIMUM_SINGLE_INPUT_LENGTH) {
            return str;
        }
        return str.substring(0, MAXIMUM_SINGLE_INPUT_LENGTH) + "\n...[truncated]";
    }

    private static String normalizeLineBreaks(String str) {
        return str.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\n", "<br>");
    }

    private static void trimToMaximumLength(SpannableStringBuilder builder) {
        int overflow = builder.length() - MAXIMUM_LENGTH;
        if (overflow > 0) {
            builder.delete(0, overflow);
        }
    }

    private static void trimToMaximumRows(final TextView tv, final boolean jumpFirstLine) {
        tv.post(new Runnable() {
            @Override
            public void run() {
                Layout layout = tv.getLayout();
                if (layout != null && layout.getLineCount() > MAXIMUM_ROW) {
                    int keepFrom = layout.getLineStart(layout.getLineCount() - MAXIMUM_ROW);
                    if (keepFrom > 0) {
                        CharSequence text = tv.getText();
                        tv.setText(text.subSequence(keepFrom, text.length()), TextView.BufferType.SPANNABLE);
                    }
                }
                applyScroll(tv, jumpFirstLine);
            }
        });
    }

    private static void applyScroll(TextView tv, boolean jumpFirstLine) {
        Layout layout = tv.getLayout();
        if (layout == null) {
            return;
        }
        if (jumpFirstLine) {
            tv.scrollTo(0, 0);
            return;
        }
        int scrollAmount = layout.getLineTop(tv.getLineCount()) - tv.getHeight();
        tv.scrollTo(0, Math.max(scrollAmount, 0));
    }
}
