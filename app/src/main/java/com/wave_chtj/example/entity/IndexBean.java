package com.wave_chtj.example.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class IndexBean implements MultiItemEntity {
    private int flag;
    private String[] item;
    private int itemType;

    public IndexBean(int flag, String[] item,int itemType) {
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

    public String[] getItem() {
        return item;
    }

    public void setItem(String[] item) {
        this.item = item;
    }


    @Override
    public int getItemType() {
        return itemType;
    }
}
