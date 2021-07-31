package com.face.keepsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.app.AppsUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;

import java.util.List;

/**
 * 如何其他app跨进程向base_keepalive中添加响应得保活呢？
 * 参考app Module中路径com.wave_chtj.example.keeplive.KeepLiveAty中的使用
 * 注意复制相应的aidl和实体类
 */
public class SelectServiceActivity extends BaseActivity {
    RecyclerView rv_info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_service);
        SelectAppAdapter selectAppAdapter=new SelectAppAdapter(AppsUtils.getDeskTopAppList());
        rv_info=findViewById(R.id.rv_info);
        rv_info.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        rv_info.setAdapter(selectAppAdapter);
    }

    class SelectAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final String TAG = "SelectAppAdapter";
        private List<AppEntity> list;

        public SelectAppAdapter(List<AppEntity> list) {
            this.list = list;

        }

        public void setList(List<AppEntity> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.item_service, null, false);
            RecyclerView.ViewHolder holder = null;
            holder = new ServiceViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ServiceViewHolder myViewHolder=(ServiceViewHolder) holder;
            myViewHolder.tvAppName.setText(list.get(position).getAppName());
            myViewHolder.tvPackName.setText(list.get(position).getPackageName());
            myViewHolder.ivAppIcon.setImageDrawable(list.get(position).getIcon());
            myViewHolder.tvVersion.setText("v:" +list.get(position).getVersionName()+"."+list.get(position).getVersionCode());
            ItemServiceAdapter itemServiceAdapter=new ItemServiceAdapter(list.get(position).getPackageName(),list.get(position).getmRunServiceList());
            myViewHolder.rv_service.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
            myViewHolder.rv_service.setAdapter(itemServiceAdapter);
            myViewHolder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> runServiceList=list.get(position).getmRunServiceList();
                    if(runServiceList==null||runServiceList.size()<=0){
                        ToastUtils.error("该APK没有Service服务,或应用未启动,无法查询！");
                    }else {
                        ToastUtils.info("请选择列出的Service服务！");
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        class ServiceViewHolder extends RecyclerView.ViewHolder {
            public TextView tvAppName, tvPackName,tvVersion;
            public ImageView ivAppIcon;
            public RelativeLayout ll_item;
            public RecyclerView rv_service;

            public ServiceViewHolder(View itemView) {
                super(itemView);
                tvAppName = itemView.findViewById(R.id.tvAppName);
                tvPackName = itemView.findViewById(R.id.tvPackName);
                tvVersion = itemView.findViewById(R.id.tvVersion);
                ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
                ll_item = itemView.findViewById(R.id.ll_item);
                rv_service = itemView.findViewById(R.id.rv_service);
            }
        }
    }
    class ItemServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private String packageName;
        private List<String> runServiceList;

        public ItemServiceAdapter(String packageName,List<String> runServiceList) {
            this.runServiceList = runServiceList;
            this.packageName = packageName;
        }

        public void setList(String packageName,List<String> runServiceList) {
            this.runServiceList = runServiceList;
            this.packageName = packageName;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.item_service_select, null, false);
            RecyclerView.ViewHolder holder = null;
            holder = new ItemServiceViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemServiceViewHolder myViewHolder=(ItemServiceViewHolder) holder;
            myViewHolder.tv_service.setText(runServiceList.get(position));
            myViewHolder.tv_service.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SelectServiceActivity.this, IndexActivity.class);
                    intent.putExtra("packageName",packageName);
                    intent.putExtra("service",runServiceList.get(position));
                    setResult(RESULT_OK,intent);
                    finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return runServiceList.size();
        }


        class ItemServiceViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_service;

            public ItemServiceViewHolder(View itemView) {
                super(itemView);
                tv_service = itemView.findViewById(R.id.tv_service);
            }
        }
    }
}
