package com.ichtj.basetools.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.List;
import java.util.Map;

public class CustomButtonGridView extends ScrollView {

    private LinearLayout container;
    private Map<Integer, String> btnMap;
    private int numColumns = 2; // 默认每列显示2个按钮
    private OnButtonClickListener onButtonClickListener;

    public CustomButtonGridView(Context context) {
        super(context);
        init(context);
    }

    public CustomButtonGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomButtonGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        addView(container);
    }

    public void setButtonMap(Map<Integer, String> btnMap) {
        this.btnMap = btnMap;
        displayButtons();
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        displayButtons();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.onButtonClickListener = listener;
    }

    private void displayButtons() {
        container.removeAllViews();

        if (btnMap == null || btnMap.isEmpty()) {
            return;
        }

        int totalButtons = btnMap.size();
        int numRows = (int) Math.ceil((double) totalButtons / numColumns);

        for (int i = 0; i < numRows; i++) {
            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER); // 设置行居中显示

            for (int j = 0; j < numColumns; j++) {
                int position = i * numColumns + j;
                if (position < totalButtons) {
                    String btnText = btnMap.get(position);
                    Button button = createButton(btnText, position);
                    button.setPadding(px2dip(3),px2dip(3),px2dip(3),px2dip(3));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1); // 设置权重为1，均分宽度
                    layoutParams.setMargins(px2dip(3), px2dip(3), px2dip(3), px2dip(3)); // 设置外边距
                    button.setLayoutParams(layoutParams);
                    row.addView(button);
                } else {
                    break;
                }
            }

            container.addView(row);
        }
    }

    private Button createButton(String text, final int position) {
        Button button = new Button(getContext());
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // 根据列数动态调整文字大小
        int textSizeInSp = px2dip(calculateTextSize(numColumns));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);

        button.setText(text);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onButtonClick(position, btnMap.get(position));
                }
            }
        });
        return button;
    }

    private int calculateTextSize(int numColumns) {
        // 根据列数动态计算文字大小，这里仅提供一个简单的示例，你可以根据实际需求调整逻辑
        switch (numColumns) {
            case 1:
                return 22; // 单列时文字大小为18sp
            case 2:
                return 20; // 两列时文字大小为16sp
            case 3:
                return 18; // 三列时文字大小为14sp
            default:
                return 16; // 其他情况文字大小为12sp
        }
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     */
    public int px2dip(float pxValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public interface OnButtonClickListener {
        void onButtonClick(int position, String buttonText);
    }
}
