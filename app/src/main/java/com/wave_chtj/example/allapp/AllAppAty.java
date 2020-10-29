package com.wave_chtj.example.allapp;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.app.PackagesUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.util.List;

/**
 * Create on 2020/6/29
 * author chtj
 * desc
 */
public class AllAppAty extends BaseActivity {
    private static final String TAG = "AllAppInfo";
    private RecyclerView rvList;
    AllAppAdapter newsAdapter = null;
    private TextView tvCount,tvTotal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allapp);
        tvCount = findViewById(R.id.tvCount);
        rvList = findViewById(R.id.rvList);
        tvTotal = findViewById(R.id.tvTotal);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        List<AppEntity> appEntityList = PackagesUtils.getDeskTopAppList();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter = new AllAppAdapter(appEntityList);
        rvList.setLayoutManager(manager);
        //添加Android自带的分割线
        rvList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvList.setAdapter(newsAdapter);

        long total=EthDataUsageUtils.getInstance().getSystemTotalUsageData(DataUsageTime.getTimesMonthMorning(), DataUsageTime.getNow());
        String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), total);
        tvTotal.setText("总流量消耗："+totalPhrase);

    }


    /**
     * 查询桌面应用
     *
     * @param view
     */
    public void getDeskTopApp(View view) {
        List<AppEntity> appEntityList = PackagesUtils.getDeskTopAppList();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter.setList(appEntityList);
    }

    /**
     * 查询可卸载应用
     *
     * @param view
     */
    public void getNormalApp(View view) {
        List<AppEntity> appEntityList = PackagesUtils.getNormalAppList();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter.setList(appEntityList);
    }

    /**
     * 查询系统应用
     *
     * @param view
     */
    public void getSystemApp(View view) {
        List<AppEntity> appEntityList = PackagesUtils.getSystemAppList();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter.setList(appEntityList);
    }
}
