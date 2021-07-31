package com.face.keepsample;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.chtj.keepalive.FKeepAliveTools;
import com.chtj.keepalive.entity.CommonValue;
import com.chtj.keepalive.entity.KeepAliveData;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.StringUtils;
import com.face_chtj.base_iotutils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.IndexViewHolder> {
    private static final String TAG = "IndexAdapter";
    private List<SimpleKeepAlive> list;
    private Context mContext;

    public IndexAdapter(List<SimpleKeepAlive> list,Context mContext) {
        this.list = list;
        this.mContext=mContext;
    }

    public void setList(List<SimpleKeepAlive> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IndexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IndexViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_index, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IndexViewHolder holder, int position) {
        String pkgName = list.get(position).getAppName();
        holder.tvAppName.setText(StringUtils.isEmpty(pkgName) ? "NULL" : pkgName);
        holder.tvPackName.setText(list.get(position).getPackageName());
        holder.ivAppIcon.setImageDrawable(list.get(position).getIcon());
        holder.tvType.setText(list.get(position).getType() == FKeepAliveTools.TYPE_SERVICE ? "SERVICE" : "ACTIVITY");
        holder.cbEnable.setChecked(list.get(position).getIsEnable());
        holder.cbEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    list.get(position).setIsEnable(isChecked);
                    saveItemInfo();
                    notifyItemChanged(position, 1);
                }
            }
        });
        holder.tv_service.setText(list.get(position).getServiceName());
        holder.ll_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.get(position).getIsEnable()) {
                    list.get(position).setIsEnable(false);
                } else {
                    list.get(position).setIsEnable(true);
                }
                saveItemInfo();
                notifyItemChanged(position, 1);
            }
        });
        holder.ivDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("提示");
                builder.setMessage("确认删除对 "+list.get(position).getAppName()+" 的保活吗?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        list.remove(position);
                        KLog.d(TAG, "onClick:>list.size=" + list.size());
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        saveItemInfo();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    public void saveItemInfo() {
        List<KeepAliveData> keepAliveDataList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            keepAliveDataList.add(new KeepAliveData(list.get(i).getPackageName(), list.get(i).getType(), list.get(i).getServiceName(), list.get(i).getIsEnable()));
        }
        FKeepAliveTools.clearKeepLive();
        FKeepAliveTools.addMoreData(keepAliveDataList);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class IndexViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAppName, tvPackName, tvType, tv_service;
        public ImageView ivAppIcon, ivDel;
        public CheckBox cbEnable;
        public LinearLayout ll_item;

        public IndexViewHolder(View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvPackName = itemView.findViewById(R.id.tvPackName);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvType = itemView.findViewById(R.id.tvType);
            cbEnable = itemView.findViewById(R.id.cbEnable);
            tv_service = itemView.findViewById(R.id.tv_service);
            ll_item = itemView.findViewById(R.id.ll_item);
            ivDel = itemView.findViewById(R.id.ivDel);
        }
    }
}
