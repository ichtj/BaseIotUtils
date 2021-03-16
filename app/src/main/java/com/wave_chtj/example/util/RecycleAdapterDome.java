package com.wave_chtj.example.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.wave_chtj.example.R;

import java.util.List;

public class RecycleAdapterDome extends RecyclerView.Adapter<RecycleAdapterDome.MyViewHolder>{
    private Context context;

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * 更新列表数据某一项的packageName
     *
     * @param position    下标
     * @param str 要替换的文本名
     */
    public void updateItem(int position, String str) {
        if (position >= 0 && position < list.size()) {
            list.set(position,str);
        }
        notifyDataSetChanged();
    }

    private List<String> list;
    private View inflater;
    //构造方法，传入数据
    public RecycleAdapterDome(Context context, List<String> list){
        this.context = context;
        this.list = list;
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_rvinfo,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(inflater);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //将数据和控件绑定
        holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        //返回Item总条数
        return list.size();
    }

    //内部类，绑定控件
    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_view);
        }
    }
}