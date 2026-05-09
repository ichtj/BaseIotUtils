package com.ichtj.basetools.allapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.chtj.base_framework.network.FNetworkTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.LoadDialogUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;
import com.face_chtj.base_iotutils.view.TopTitleBar;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.PACKAGES;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Create on 2020/6/29
 * author chtj
 * desc app列表
 */
@Route(path = PACKAGES.BASE + "allApp")
public class AllAppAty extends BaseActivity implements TopTitleBar.OnTextViewClickListener {
    private static final String TAG = AllAppAty.class.getSimpleName();
    private RecyclerView rvList;
    private AllAppAdapter newsAdapter = null;
    private TextView tvCount, tvDosage;
    private TopTitleBar ctTopView;
    private LoadDialogUtils loadDialogUtils;
    private static final int TYPE_ALL=0x01;
    private static final int TYPE_DESKTOP=0x02;
    private static final int TYPE_SYSTEM=0x03;
    private static final int TYPE_UNINSTALLABLE=0x04;
    private static final int TYPE_RUNNING=0x05;
    private static final int FLAG_LOAD_LIST=0x06;
    private static final int FLAG_DOSAGE=0x07;
    private int currentType=TYPE_ALL;
    private Executor executor=Executors.newSingleThreadExecutor();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what==FLAG_LOAD_LIST){
                List<AppEntity> loadList= (List<AppEntity>) msg.obj;
                boolean isSucc=loadList!=null&&loadList.size()>0;
                newsAdapter.setList(isSucc?loadList:new ArrayList<>());
                tvCount.setText(getString(R.string.allapp_sum,isSucc?loadList.size()+"":0+""));
                loadDialogUtils.hideLoading();
            }else if(msg.what==FLAG_DOSAGE){
                tvDosage.setText(msg.obj.toString());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allapp);
        ctTopView = findViewById(R.id.ctTopView);
        ctTopView.setOnTextViewClickListener(this);
        loadDialogUtils = new LoadDialogUtils(this);
        tvCount = findViewById(R.id.tvCount);
        rvList = findViewById(R.id.rvList);
        tvDosage = findViewById(R.id.tvDosage);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        List<AppEntity> appEntityList = new ArrayList<>();
        tvCount.setText(getString(R.string.allapp_sum,appEntityList.size()+""));
        newsAdapter = new AllAppAdapter(this,appEntityList);
        rvList.setLayoutManager(manager);
        //添加Android自带的分割线
        rvList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvList.setAdapter(newsAdapter);
    }

//    @Override
//    public Resources getResources() {
//        //需要升级到 v1.1.2 及以上版本才能使用 AutoSizeCompat
//        AutoSizeCompat.autoConvertDensity(super.getResources(), 1080, true);//如果有自定义需求就用这个方法
//        return super.getResources();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(currentType);
    }

    public void refreshData(int type) {
        Log.d(TAG, "refreshData: type>>"+type);
        List<Integer> pngList = new ArrayList<>();
        pngList.add(com.ichtj.drawable.R.drawable.ic_loading);
        // 情况一：PNG 列表
        loadDialogUtils.setPngList(pngList);
        loadDialogUtils.showLoading();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long total = FNetworkTools.getEthTotalUsage(FNetworkTools.getTimesMonthMorning(), FNetworkTools.getNow());
                    String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), total);
                    handler.sendMessage(handler.obtainMessage(FLAG_DOSAGE,totalPhrase));
                } catch (Exception e) {
                    handler.sendMessage(handler.obtainMessage(FLAG_DOSAGE,getString(R.string.allapp_sum_dosage)));
                }
                List<AppEntity> loadList=new ArrayList<>();
                if (type==TYPE_ALL){
                    loadList=AppsUtils.getAllApp();
                }else if(type==TYPE_DESKTOP){
                    loadList=AppsUtils.getDeskTopAppList();
                }else if(type==TYPE_SYSTEM){
                    List<AppEntity> appEntityList = AppsUtils.getAllApp();
                    for (int i = 0; i < appEntityList.size(); i++) {
                        if (appEntityList.get(i).isSystemApp){
                            loadList.add(appEntityList.get(i));
                        }
                    }
                }else if(type==TYPE_UNINSTALLABLE){
                    List<AppEntity> appEntityList = AppsUtils.getAllApp();
                    for (int i = 0; i < appEntityList.size(); i++) {
                        if (appEntityList.get(i).sourceDir.contains("data/")){
                            loadList.add(appEntityList.get(i));
                        }
                    }
                }else if(type==TYPE_RUNNING){
                    List<AppEntity> appEntityList = AppsUtils.getAllApp ();
                    Log.d (TAG, "runAS: "+appEntityList.size ());
                    for (int i = 0; i < appEntityList.size(); i++) {
                        if (appEntityList.get(i).isRunning){
                            loadList.add(appEntityList.get(i));
                        }
                    }
                }
                handler.sendMessage(handler.obtainMessage(FLAG_LOAD_LIST,loadList));
            }
        });
    }

    /**
     * 查询全部应用
     */
    public void getAllAppClick(View view) {
        currentType=TYPE_ALL;
        refreshData(TYPE_ALL);
    }

    /**
     * 查询运行中的应用
     */
    public void getRunningAppClick(View view) {
        currentType=TYPE_RUNNING;
        refreshData(TYPE_RUNNING);
    }

    /**
     * 查询桌面应用
     */
    public void getDeskAppClick(View view) {
        currentType=TYPE_DESKTOP;
        refreshData(TYPE_DESKTOP);
    }

    /**
     * 查询可卸载应用
     */
    public void getNormalApp(View view) {
        currentType=TYPE_UNINSTALLABLE;
        refreshData(TYPE_UNINSTALLABLE);
    }

    /**
     * 查询系统应用
     */
    public void getSystemApp(View view) {
        currentType=TYPE_SYSTEM;
        refreshData(TYPE_SYSTEM);
    }

    /**
     * 启用全部应用的网络访问
     */
    public void enableAllAppNetClick(View view) {

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
                ToastUtils.error(getString(R.string.allapp_reboot_failed));
            }
        }
    }


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
}
