package com.face_chtj.base_iotutils.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.face_chtj.base_iotutils.R;

/**
 * 提供左中右文本点击控制
 */
public class TopTitleBar extends View {
    private static final String TAG = TopTitleBar.class.getSimpleName();
    private String centerText;
    private String leftText;
    private String rightText;
    private boolean leftBack;
    private float titleTextSize;
    private Paint paint;
    private Context mContext;
    private OnTextViewClickListener onTextViewClickListener;

    public TopTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        // 获取自定义属性的值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTitleBar);
        centerText = a.getString(R.styleable.CustomTitleBar_centerText);
        leftText = a.getString(R.styleable.CustomTitleBar_leftText);
        rightText = a.getString(R.styleable.CustomTitleBar_rightText);
        leftBack = a.getBoolean(R.styleable.CustomTitleBar_leftBack, false);
        titleTextSize = a.getDimension(R.styleable.CustomTitleBar_textSize, 16); // 默认字体大小为16sp
        a.recycle();
        // 在指定的高度内居中显示文本
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setTextLeft(String textLeft) {
        this.leftText = textLeft;
        invalidate();
    }

    public void setTextCenter(String textCenter) {
        this.centerText = textCenter;
        invalidate();
    }

    public void setTextRight(String textRight) {
        this.rightText = textRight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setTextSize(titleTextSize);

        // 获取视图的宽度和高度
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // 获取文本的宽度和高度
        float textLeftWidth = paint.measureText(leftText);
        float textCenterWidth = paint.measureText(centerText);
        float textRightWidth = paint.measureText(rightText);
        float textHeight = paint.descent() - paint.ascent();

        // 计算文本在垂直方向上的居中位置
        float y = (viewHeight - textHeight) / 2f - paint.ascent();

        // 绘制文本
        canvas.drawText(leftText, 0, y, paint);
        canvas.drawText(centerText, (viewWidth - textCenterWidth) / 2, y, paint);
        canvas.drawText(rightText, viewWidth - textRightWidth, y, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 检测点击位置并触发相应的回调
                if (x <= getWidth() / 3f && isInsideTextBounds(x, y, 0, (getHeight() - paint.descent() - paint.ascent()) / 2f, paint.measureText(leftText), paint.descent() - paint.ascent())) {
                    if (onTextViewClickListener != null) {
                        onTextViewClickListener.onTextLeftClick();
                    }
                    // 获取当前 Activity 对象并调用 finish
                    if (leftBack && mContext instanceof Activity) {
                        ((Activity) mContext).finish();
                    }
                } else if (x > getWidth() / 3f && x <= 2 * getWidth() / 3f && isInsideTextBounds(x, y, (getWidth() - paint.measureText(centerText)) / 2, (getHeight() - paint.descent() - paint.ascent()) / 2f, paint.measureText(centerText), paint.descent() - paint.ascent())) {
                    if (onTextViewClickListener != null) {
                        onTextViewClickListener.onTextCenterClick();
                    }
                } else if (x > 2 * getWidth() / 3f && isInsideTextBounds(x, y, getWidth() - paint.measureText(rightText), (getHeight() - paint.descent() - paint.ascent()) / 2f, paint.measureText(rightText), paint.descent() - paint.ascent())) {
                    if (onTextViewClickListener != null) {
                        onTextViewClickListener.onTextRightClick();
                    }
                }
                break;
        }

        return true;
    }

    private boolean isInsideTextBounds(float x, float y, float textX, float textY, float textWidth, float textHeight) {
        return x >= textX && x <= textX + textWidth && y >= textY - textHeight && y <= textY + textHeight;
    }

    // 设置回调接口
    public void setOnTextViewClickListener(OnTextViewClickListener listener) {
        this.onTextViewClickListener = listener;
    }

    // 回调接口
    public interface OnTextViewClickListener {
        void onTextLeftClick();

        void onTextCenterClick();

        void onTextRightClick();
    }
}
