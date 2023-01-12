package com.wave_chtj.example.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wave_chtj.example.R;

/**
 * Created by ZLM on 2018/5/17.
 * Describe 自定义RelativeLayout
 */

public class TopTitleView extends RelativeLayout {
    private static final String TAG = "TopTitleView";
    private ImageView mItem_product_icon;
    private TextView tvTopCenter;
    private TextView tvTopRight;
    private LinearLayout layoutView;
    private Context mContext;

    public TopTitleView(Context context) {
        this(context, null);
    }

    public TopTitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView(LayoutInflater.from(context).inflate(R.layout.top_view, this));
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TopTitleView);
        Drawable back = typedArray.getDrawable(R.styleable.TopTitleView_ivback);
        String name = typedArray.getString(R.styleable.TopTitleView_name);
        String rightName = typedArray.getString(R.styleable.TopTitleView_rightName);
        Log.d(TAG, "initAttrs: back=" + back + ",name=" + name);
        setProductIcon(back);
        setProductTitle(name);
        setRightName(rightName);
        typedArray.recycle();
    }

    private void initView(View view) {
        tvTopRight = view.findViewById(R.id.tvTopRight);
        layoutView = view.findViewById(R.id.layoutview);
        mItem_product_icon = view.findViewById(R.id.iv_back);
        layoutView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //在自定义View中关闭Activity
                ((Activity) mContext).finish();
            }
        });
        //标题
        tvTopCenter = view.findViewById(R.id.tvTitle);
    }

    public void setProductIcon(Drawable productIcon) {
        mItem_product_icon.setImageDrawable(productIcon);
    }

    public void setRightName(CharSequence productTitle) {
        tvTopRight.setText(productTitle);
    }

    public void setProductTitle(CharSequence productTitle) {
        tvTopCenter.setText(productTitle);
    }

    public void setCenterClick(OnClickListener onCenter){
        tvTopCenter.setOnClickListener(onCenter);
    }
    public void setRightClick(OnClickListener onRight){
        tvTopRight.setOnClickListener(onRight);
    }
}