package com.wave_chtj.example.util;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.wave_chtj.example.R;
import com.wave_chtj.example.entity.Dbean;

import java.util.List;

public class IndexAdapter extends BaseMultiItemQuickAdapter<Dbean, BaseViewHolder> {
    public static final int L_NO_BG = 1;
    public static final int L_ONE = 2;

    //构造方法，传入数据
    public IndexAdapter(List<Dbean> itemList) {
        super(itemList);
        addItemType(L_NO_BG,R.layout.item_nobg);
        addItemType(L_ONE,R.layout.item_one);
    }


    @Override
    public int getItemViewType(int position) {
        if (position < 12) {
            return L_NO_BG;
        } else {
            return L_ONE;
        }
    }


    @Override
    protected void convert(BaseViewHolder helper, Dbean dbean) {
        switch (helper.getItemViewType()) {
            case IndexAdapter.L_NO_BG:
                break;
            case IndexAdapter.L_ONE:
                break;
        }
        helper.setText(R.id.tvOneStr, dbean.getItem());
    }
}