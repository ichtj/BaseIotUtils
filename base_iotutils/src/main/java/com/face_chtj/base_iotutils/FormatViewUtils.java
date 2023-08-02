package com.face_chtj.base_iotutils;

import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class FormatViewUtils {
    /**
     * A movement method that interprets movement keys by scrolling the text buffer.
     * @param textView
     */
    public static void setMovementMethod(TextView textView){
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * show data to Activity
     * @param htmlStr 支持html标签的字符串
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
}
