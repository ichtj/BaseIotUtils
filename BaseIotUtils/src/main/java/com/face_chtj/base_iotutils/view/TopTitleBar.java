package com.face_chtj.base_iotutils.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.face_chtj.base_iotutils.R;

/**
 * 提供左中右文本点击控制
 */
public class TopTitleBar extends View {
    private String centerText = "";
    private String leftText = "";
    private String rightText = "";
    private boolean leftBack;
    private float titleTextSize;
    private int textColor = Color.BLACK;
    private Drawable leftIcon;
    private int leftIconPadding = 0; // dp
    private int leftIconSize = 0; // px
    private int horizontalPadding = dp2px(12); // 控制左右边距
    private int backgroundColor = Color.WHITE; // 默认背景色

    private Paint paint;
    private Context mContext;
    private OnTextViewClickListener onTextViewClickListener;

    public TopTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTitleBar);
        centerText = safeString(a.getString(R.styleable.CustomTitleBar_centerText));
        leftText = safeString(a.getString(R.styleable.CustomTitleBar_leftText));
        rightText = safeString(a.getString(R.styleable.CustomTitleBar_rightText));
        leftBack = a.getBoolean(R.styleable.CustomTitleBar_leftBack, false);
        titleTextSize = a.getDimension(R.styleable.CustomTitleBar_textSize, sp2px(16));
        textColor = a.getColor(R.styleable.CustomTitleBar_textColor, Color.BLACK);
        leftIcon = a.getDrawable(R.styleable.CustomTitleBar_leftIcon);
        leftIconPadding = a.getDimensionPixelSize(R.styleable.CustomTitleBar_leftIconPadding, dp2px(4));
        leftIconSize = a.getDimensionPixelSize(R.styleable.CustomTitleBar_leftIconSize, dp2px(24));
        backgroundColor = a.getColor(R.styleable.CustomTitleBar_backgroundColor, Color.WHITE);
        a.recycle();

        if (leftIcon != null) {
            leftIcon.setBounds(0, 0, leftIconSize, leftIconSize);
        }

        setClickable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(backgroundColor);
        paint.setColor(textColor);
        paint.setTextSize(titleTextSize);

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float textHeight = paint.descent() - paint.ascent();
        float y = (viewHeight - textHeight) / 2f - paint.ascent();

        float leftStartX = horizontalPadding;

        // Draw left icon if exists
        if (leftIcon != null) {
            int iconTop = (viewHeight - leftIconSize) / 2;
            canvas.save();
            canvas.translate(leftStartX, iconTop);
            leftIcon.draw(canvas);
            canvas.restore();
            leftStartX += leftIconSize + leftIconPadding;
        }

        // Draw left text
        if (!leftText.isEmpty()) {
            canvas.drawText(leftText, leftStartX, y, paint);
        }

        // Draw center text
        float centerTextWidth = paint.measureText(centerText);
        canvas.drawText(centerText, (viewWidth - centerTextWidth) / 2, y, paint);

        // Draw right text
        if (!rightText.isEmpty()) {
            float rightTextWidth = paint.measureText(rightText);
            float rightStartX = viewWidth - horizontalPadding - rightTextWidth;
            canvas.drawText(rightText, rightStartX, y, paint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float w = getWidth();
            if (x <= w / 3f) {
                if (onTextViewClickListener != null) onTextViewClickListener.onTextLeftClick();
                if (leftBack && mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            } else if (x <= 2 * w / 3f) {
                if (onTextViewClickListener != null) onTextViewClickListener.onTextCenterClick();
            } else {
                if (onTextViewClickListener != null) onTextViewClickListener.onTextRightClick();
            }
        }
        return super.onTouchEvent(event);
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    public void setTextLeft(String textLeft) {
        this.leftText = safeString(textLeft);
        invalidate();
    }

    public void setTextCenter(String textCenter) {
        this.centerText = safeString(textCenter);
        invalidate();
    }

    public void setTextRight(String textRight) {
        this.rightText = safeString(textRight);
        invalidate();
    }

    public void setLeftIcon(Drawable drawable) {
        this.leftIcon = drawable;
        if (drawable != null) {
            drawable.setBounds(0, 0, leftIconSize, leftIconSize);
        }
        invalidate();
    }

    public void setOnTextViewClickListener(OnTextViewClickListener listener) {
        this.onTextViewClickListener = listener;
    }

    public interface OnTextViewClickListener {
        void onTextLeftClick();
        void onTextCenterClick();
        void onTextRightClick();
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
}
