package com.face_chtj.base_iotutils.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import com.face_chtj.base_iotutils.R;

/**
 * 气泡布局
 */
public class BubbleLayout extends LinearLayout {
    //先定义 常量
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;
    public static final int NONE = 4;

    /**
     * 气泡尖角方向
     */
    @IntDef({TOP, LEFT, RIGHT, BOTTOM, NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BubbleOrientation {
    }

    public static float dip2Px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }

    public static int PADDING;
    public static int DEFAULT_PADDING;
    public static int LEG_HALF_BASE;
    public static float STROKE_WIDTH;
    public static float CORNER_RADIUS;
    public static float MIN_ARROW_DISTANCE;
    public static int DEFAULT_WIDTH;
    public static int DEFAULT_HEIGHT;

    private Paint mFillPaint = null;
    private final Path mPath = new Path();
    private final Path mBubbleArrowPath = new Path();


    private RectF mRoundRect;
    private float mWidth;
    private float mHeight;

    private float mBubbleArrowOffset = 0.75f;
    @BubbleOrientation
    private int mBubbleOrientation = LEFT;

    public BubbleLayout(Context context) {
        this(context, null);
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        PADDING = (int) dip2Px(context, 7);
        DEFAULT_PADDING = (int) dip2Px(context, 10);
        LEG_HALF_BASE = (int) dip2Px(context, 6);
        STROKE_WIDTH = 2.0f;
        CORNER_RADIUS = dip2Px(context, 6);
        MIN_ARROW_DISTANCE = PADDING + LEG_HALF_BASE;
        DEFAULT_WIDTH = (int) dip2Px(context, 50);
        DEFAULT_HEIGHT = (int) dip2Px(context, 46);
        //setGravity(Gravity.CENTER);

        mFillPaint = new Paint();
        mFillPaint.setStyle(Style.FILL);
        mFillPaint.setStrokeCap(Cap.BUTT);
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStrokeWidth(STROKE_WIDTH);
        mFillPaint.setStrokeJoin(Paint.Join.MITER);
//        mFillPaint.setPathEffect(new CornerPathEffect(CORNER_RADIUS));
        mFillPaint.setColor(context.getResources().getColor(R.color.s6_80));

        setLayerType(LAYER_TYPE_SOFTWARE, mFillPaint);
        renderBubbleLegPrototype();
        setBackgroundColor(Color.TRANSPARENT);
        setClipChildren(false);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 尖角path
     */
    private void renderBubbleLegPrototype() {
        mBubbleArrowPath.moveTo(0, 0);
        mBubbleArrowPath.lineTo(PADDING, -PADDING);
        mBubbleArrowPath.lineTo(PADDING, PADDING);
        mBubbleArrowPath.close();
    }

    public void setBubbleParams(final @BubbleOrientation int bubbleOrientation, final float bubbleOffset) {
        mBubbleArrowOffset = bubbleOffset;
        mBubbleOrientation = bubbleOrientation;
    }

    /**
     * 根据显示方向，获取尖角位置矩阵
     *
     * @param width
     * @param height
     * @return
     */
    private Matrix renderBubbleArrowMatrix(final float width, final float height) {

        final float offset = Math.max(mBubbleArrowOffset, MIN_ARROW_DISTANCE);

        float dstX = 0;
        float dstY = Math.min(offset, height - MIN_ARROW_DISTANCE);
        final Matrix matrix = new Matrix();

        switch (mBubbleOrientation) {
            case TOP:
                dstX = Math.min(offset, width - MIN_ARROW_DISTANCE);
                dstY = 0;
                matrix.postRotate(90);
                setPadding(0, PADDING, 0, 0);
                setGravity(Gravity.CENTER);
                mRoundRect = new RectF(0, PADDING, mWidth, mHeight);
                break;

            case RIGHT:
                dstX = width;
                dstY = Math.min(offset, height - MIN_ARROW_DISTANCE);
                matrix.postRotate(180);
                setPadding(0, 0, PADDING, 0);
                setGravity(Gravity.CENTER);
                mRoundRect = new RectF(0, 0, mWidth - PADDING, mHeight);
                break;

            case LEFT:
                dstX = 0;
                dstY = Math.min(offset, height - MIN_ARROW_DISTANCE);
                setPadding(PADDING, 0, 0, 0);
                setGravity(Gravity.CENTER);
                mRoundRect = new RectF(PADDING, 0, mWidth, mHeight);
                break;

            case BOTTOM:
                dstX = Math.min(offset, width - MIN_ARROW_DISTANCE);
                dstY = height;
                matrix.postRotate(270);
                setPadding(0, 0, 0, PADDING);
                setGravity(Gravity.CENTER);
                mRoundRect = new RectF(0, 0, mWidth, mHeight - PADDING);
                break;
        }

        matrix.postTranslate(dstX, dstY);
        return matrix;
    }

    public float getBubbleOffset() {
        final float offset = Math.max(mBubbleArrowOffset, MIN_ARROW_DISTANCE);
        float bubbleOffset = 0;
        switch (mBubbleOrientation) {
            case TOP:
                bubbleOffset = Math.min(offset, mWidth - MIN_ARROW_DISTANCE);
                break;

            case RIGHT:
                bubbleOffset = Math.min(offset, mHeight - MIN_ARROW_DISTANCE);
                break;

            case LEFT:
                bubbleOffset = Math.min(offset, mHeight - MIN_ARROW_DISTANCE);
                break;

            case BOTTOM:
                bubbleOffset = Math.min(offset, mWidth - MIN_ARROW_DISTANCE);
                break;
        }
        return bubbleOffset;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        TextView view = null;
        if (getChildAt(0) instanceof TextView) {
            view = (TextView) getChildAt(0);
        }
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = 0;
        if (view != null) {
            size = (int) view.getPaint().measureText(view.getText().toString()) + view.getPaddingLeft() + view.getPaddingRight();
        }
        int width;
        int height;
        if (mBubbleOrientation == RIGHT || mBubbleOrientation == LEFT) {
            width = (size > DEFAULT_WIDTH ? (size + DEFAULT_PADDING * 2) : DEFAULT_WIDTH) + PADDING;
            height = DEFAULT_HEIGHT;
        } else {
            width = size > DEFAULT_WIDTH ? (size + DEFAULT_PADDING * 2) : DEFAULT_WIDTH;
            height = DEFAULT_HEIGHT;
        }


        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, height);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, height);
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Matrix matrix = renderBubbleArrowMatrix(mWidth, mHeight);
        mPath.rewind();
        mPath.addRoundRect(mRoundRect, CORNER_RADIUS, CORNER_RADIUS, Direction.CW);
        mPath.addPath(mBubbleArrowPath, matrix);

        canvas.drawPath(mPath, mFillPaint);
    }
}
