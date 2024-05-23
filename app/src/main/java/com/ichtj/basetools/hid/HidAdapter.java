package com.ichtj.basetools.hid;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HidAdapter extends ArrayAdapter<UsbDevice> {
    public HidAdapter(Context context, List<UsbDevice> persons) {
        super(context, 0, persons);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取数据项位置上的 Person 对象
        UsbDevice person = getItem(position);

        // 检查是否已经有一个可重用的视图，如果没有，则从布局文件中创建一个新的视图
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        // 获取布局文件中的 TextView
        TextView tvName = convertView.findViewById(android.R.id.text1);

        // 将 Person 对象的姓名设置到 TextView 中
        tvName.setText(person.getDeviceName()+",pid："+person.getProductId()+",vid："+person.getVendorId());

        return convertView;
    }

    // 下拉列表的视图
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}