package com.wave_chtj.example.allapp;

import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chtj.base_framework.FIPTablesTools;
import com.chtj.base_framework.network.FNetworkTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.display.AppsUtils;
import com.face_chtj.base_iotutils.display.ToastUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.util.List;

/**
 * Create on 2020/6/29
 * author chtj
 * desc app列表
 */
public class AllAppAty extends BaseActivity {
    private static final String TAG = "AllAppInfo";
    private RecyclerView rvList;
    AllAppAdapter newsAdapter = null;
    private TextView tvCount, tvTotal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allapp);
        tvCount = findViewById(R.id.tvCount);
        rvList = findViewById(R.id.rvList);
        tvTotal = findViewById(R.id.tvTotal);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        List<AppEntity> appEntityList = AppsUtils.getDeskTopAppList();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter = new AllAppAdapter(appEntityList);
        rvList.setLayoutManager(manager);
        //添加Android自带的分割线
        rvList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvList.setAdapter(newsAdapter);
        try {
            //7.1.2系统才可以使用此方式获取总流量
            long total = FNetworkTools.getEthTotalUsage(FNetworkTools.getTimesMonthMorning(), FNetworkTools.getNow());
            String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), total);
            tvTotal.setText("总流量：" + totalPhrase);
        }catch (Exception e){
            e.printStackTrace();
            KLog.e(TAG,"errMeg:"+e.getMessage());
            tvTotal.setText("总流量：计算异常");
        }
    }


    /**
     * 查询桌面应用
     *
     * @param view
     */
    public void getDeskTopApp(View view) {
        List<AppEntity> appEntityList = AppsUtils.getDeskTopAppList();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter.setList(appEntityList);
    }

    /**
     * 启用全部应用的网络访问
     */
    public void enableAllAppNetClick(View view){
        boolean isPass=FIPTablesTools.clearAllRule();
        if(isPass){
            ToastUtils.success("启用成功！");
        }else{
            ToastUtils.error("启用失败！");
        }
    }

    /**
     * 查询可卸载应用
     *
     * @param view
     */
    public void getNormalApp(View view) {
        List<AppEntity> appEntityList = AppsUtils.getNormalAppList();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter.setList(appEntityList);
    }

    /**
     * 查询系统应用
     *
     * @param view
     */
    public void getSystemApp(View view) {
        List<AppEntity> appEntityList = AppsUtils.getSystemAppList();
        for (int i = 0; i < appEntityList.size(); i++) {
            for (int j = 0; j < appEntityList.get(i).getmProcessEntity().size(); j++) {
                Log.d(TAG, "onCreate: process pid="+appEntityList.get(i).getmProcessEntity().get(j).getPid()+",pkgName="+appEntityList.get(i).getmProcessEntity().get(j).getProcessName());
            }
        }
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter.setList(appEntityList);
    }
}
