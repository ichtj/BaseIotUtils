package com.ichtj.basetools.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class Dbean implements MultiItemEntity {
    private int flag;
    private String item;
    private int itemType;

    public Dbean(int flag, String item, int itemType) {
        this.flag = flag;
        this.item = item;
        this.itemType=itemType;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
