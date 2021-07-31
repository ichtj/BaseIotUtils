package com.face.keepsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.app.AppsUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;

import java.util.List;

/**
 * 如何其他app跨进程向base_keepalive中添加响应得保活呢？
 * 参考app Module中路径com.wave_chtj.example.keeplive.KeepLiveAty中的使用
 * 注意复制相应的aidl和实体类
 */
public class SelectAppActivity extends BaseActivity {
    RecyclerView rv_info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_app);
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
            View v = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.item_app_select, null, false);
            RecyclerView.ViewHolder holder = null;
            holder = new MyViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder=(MyViewHolder) holder;
            myViewHolder.tvAppName.setText(list.get(position).getAppName());
            myViewHolder.tvPackName.setText(list.get(position).getPackageName());
            myViewHolder.ivAppIcon.setImageDrawable(list.get(position).getIcon());
            myViewHolder.tvVersion.setText("v:" +list.get(position).getVersionName()+"."+list.get(position).getVersionCode());
            myViewHolder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(list.get(position).getPackageName().equals(getPackageName())){
                        ToastUtils.error("不能选择本应用!");
                    }else{
                        Intent intent = new Intent(SelectAppActivity.this, IndexActivity.class);
                        intent.putExtra("packageName",list.get(position).getPackageName());
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvAppName, tvPackName,tvVersion;
            public ImageView ivAppIcon;
            public LinearLayout ll_item;

            public MyViewHolder(View itemView) {
                super(itemView);
                tvAppName = itemView.findViewById(R.id.tvAppName);
                tvPackName = itemView.findViewById(R.id.tvPackName);
                tvVersion = itemView.findViewById(R.id.tvVersion);
                ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
                ll_item = itemView.findViewById(R.id.ll_item);
            }
        }
    }
}
