package com.ichtj.basetools.touch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TouchDetectGridView extends View {

    private static final int NUM_COLUMNS = 20; // Number of columns
    private static final int NUM_ROWS = 20; // Number of rows
    private Paint paint;
    private int[][] gridStatus;
    private int cellWidth;
    private int cellHeight;
    private String touchStatus;

    public TouchDetectGridView(Context context) {
        super(context);
        init();
    }

    public TouchDetectGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchDetectGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        gridStatus = new int[NUM_COLUMNS][NUM_ROWS];
        touchStatus = "Touch the grid to detect";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        cellWidth = getWidth() / NUM_COLUMNS;
        cellHeight = getHeight() / NUM_ROWS;

        // Draw grid
        for (int i = 0; i < NUM_COLUMNS; i++) {
            for (int j = 0; j < NUM_ROWS; j++) {
                if (gridStatus[i][j] == 1) {
                    paint.setColor(Color.GREEN); // Touched cells are green
                } else {
                    paint.setColor(Color.WHITE); // Untouched cells are white
                }
                canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, paint);

                // Draw cell borders
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }

        // Draw touch status
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(touchStatus, getWidth() / 2, getHeight() - 50, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            int column = (int) (event.getX() / cellWidth);
            int row = (int) (event.getY() / cellHeight);

            if (column >= 0 && column < NUM_COLUMNS && row >= 0 && row < NUM_ROWS) {
                gridStatus[column][row] = 1; // Mark the cell as touched
                if (areAllCellsGreen()) {
                    touchStatus = "Touch the grid to detect - All cells are green!";
                } else {
                    touchStatus = "Touch the grid to detect";
                }
                invalidate(); // Request to redraw the view
            }
        }
        return true;
    }

    private boolean areAllCellsGreen() {
        for (int i = 0; i < NUM_COLUMNS; i++) {
            for (int j = 0; j < NUM_ROWS; j++) {
                if (gridStatus[i][j] == 0) {
                    return false; // Found an untouched cell
                }
            }
        }
        return true; // All cells are green
    }
}
