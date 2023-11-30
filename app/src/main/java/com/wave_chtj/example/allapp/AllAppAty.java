package com.wave_chtj.example.allapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
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
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;
import com.face_chtj.base_iotutils.view.TopTitleBar;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.util.ArrayList;
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
    private TopTitleBar ctTopView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allapp);
        ctTopView = findViewById(R.id.ctTopView);
        ctTopView.setOnTextViewClickListener(new TopTitleBar.OnTextViewClickListener() {
            @Override
            public void onTextLeftClick() {

            }

            @Override
            public void onTextCenterClick() {

            }

            @Override
            public void onTextRightClick() {
                int orientation=getRequestedOrientation();
                if(orientation== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                    KLog.d("onClick() orientation >> "+orientation);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }else{
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });
        tvCount = findViewById(R.id.tvCount);
        rvList = findViewById(R.id.rvList);
        tvTotal = findViewById(R.id.tvTotal);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        List<AppEntity> appEntityList = new ArrayList<>();
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter = new AllAppAdapter(appEntityList);
        rvList.setLayoutManager(manager);
        //添加Android自带的分割线
        rvList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvList.setAdapter(newsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //7.1.2系统才可以使用此方式获取总流量
                    long total = FNetworkTools.getEthTotalUsage(FNetworkTools.getTimesMonthMorning(), FNetworkTools.getNow());
                    String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), total);
                    tvTotal.setText("总流量：" + totalPhrase);
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                    tvTotal.setText("总流量：计算异常");
                }
                newsAdapter.setList(AppsUtils.getAllApp(true));
            }
        });

    }

    /**
     * 查询桌面应用
     *
     * @param view
     */
    public void getAllAppClick(View view) {
        List<AppEntity> appEntityList = AppsUtils.getAllApp(true);
        tvCount.setText("总数：" + appEntityList.size());
        newsAdapter.setList(appEntityList);
    }

    /**
     * 启用全部应用的网络访问
     */
    public void enableAllAppNetClick(View view) {
//        boolean isPass = FIPTablesTools.clearAllRule();
//        if (isPass) {
//            ToastUtils.success("启用成功！");
//        } else {
//            ToastUtils.error("启用失败！");
//        }
    }

    /**
     * 启用全部应用的网络访问
     */
    public void rebootClick(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_REBOOT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BaseIotUtils.getContext().startActivity(intent);
        } catch (Throwable e) {
            KLog.e("errMeg:" + e.getMessage());
            ShellUtils.CommandResult commandResult = ShellUtils.execCommand("reboot", true);
            if (commandResult.result != 0) {
                ToastUtils.error("重启失败,请重试！");
            }
        }
    }

    /**
     * 查询可卸载应用
     *
     * @param view
     */
    public void getNormalApp(View view) {
        List<AppEntity> appEntityList = AppsUtils.getAllApp(true);
        List<AppEntity> normalAppList=new ArrayList<>();
        for (int i = 0; i < appEntityList.size(); i++) {
            if (!appEntityList.get(i).isSystemApp){
                normalAppList.add(appEntityList.get(i));
            }
        }
        tvCount.setText("总数：" + normalAppList.size());
        newsAdapter.setList(normalAppList);
    }

    /**
     * 查询系统应用
     *
     * @param view
     */
    public void getSystemApp(View view) {
        List<AppEntity> appEntityList = AppsUtils.getAllApp(true);
        List<AppEntity> systemAppList=new ArrayList<>();
        for (int i = 0; i < appEntityList.size(); i++) {
            if (appEntityList.get(i).isSystemApp){
                systemAppList.add(appEntityList.get(i));
            }
        }
        tvCount.setText("总数：" + systemAppList.size());
        newsAdapter.setList(systemAppList);
    }
}
