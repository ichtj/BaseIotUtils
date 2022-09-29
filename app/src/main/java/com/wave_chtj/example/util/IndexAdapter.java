package com.wave_chtj.example.util;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.wave_chtj.example.R;
import com.wave_chtj.example.entity.IndexBean;

import java.util.List;

public class IndexAdapter extends BaseMultiItemQuickAdapter<IndexBean, BaseViewHolder> {
    private static final String TAG = "BaseIotRvAdapter";
    public static final int LAYOUT_NO_BG = 1;
    public static final int LAYOUT_ONE = 2;
    public static final int LAYOUT_TWO = 3;

    private List<IndexBean> itemList;

    public void setList(List<IndexBean> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    //构造方法，传入数据
    public IndexAdapter(List<IndexBean> itemList) {
        super(itemList);
        this.itemList = itemList;
        addItemType(LAYOUT_NO_BG,R.layout.item_nobg);
        addItemType(LAYOUT_ONE,R.layout.item_one);
    }


    @Override
    public int getItemViewType(int position) {
        if (position < 10) {
            return LAYOUT_NO_BG;
        } else {
            return LAYOUT_ONE;
        } /*else if (position <= 15) {
            return LAYOUT_ONE;
        } else {
            return LAYOUT_TWO;
        }*/
    }


    @Override
    protected void convert(BaseViewHolder helper, IndexBean indexBean) {
        switch (helper.getItemViewType()) {
            case IndexAdapter.LAYOUT_NO_BG:
                break;
            case IndexAdapter.LAYOUT_ONE:
                break;
        }
        helper.setText(R.id.tvOneStr, indexBean.getItem()[0]);
    }


}