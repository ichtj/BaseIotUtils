package com.wave_chtj.example.network;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chtj.base_framework.network.FLteTools;
import com.chtj.base_framework.network.NetDbmListener;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NetMonitorUtils;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.callback.INetChangeCallBack;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * Create on 2020/1/3
 * author chtj
 * desc 网络监听
 */
public class NetChangeAty extends BaseActivity implements INetChangeCallBack {
    private static final String TAG = "NetChangeAty";
    private TextView tvStatus;
    private TextView tvType;
    private TextView tvDbm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        tvDbm = findViewById(R.id.tvDbm);
        tvStatus = findViewById(R.id.tvStatus);
        tvType = findViewById(R.id.tvType);
        if(NetUtils.getNetWorkType()==NetUtils.NETWORK_NO){
            ToastUtils.error("当前无网络连接！");
            tvType.setText("无" );
            tvStatus.setText("false");
        }
        FLteTools.init();
        NetMonitorUtils.register();
        NetMonitorUtils.addCallBack(this);
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshNet();
                handler.postDelayed(this,3500);
            }
        },0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetMonitorUtils.removeCallback(this);
        NetMonitorUtils.unRegister();
    }

    public void getDbmClick(View view){
        tvDbm.setText("信号值：" +FLteTools.getDbm());
    }

    public void refreshNetClick(View view){
        refreshNet();
    }

    private void refreshNet() {
        boolean pingResult=NetUtils.reloadDnsPing();
        String netWorkTypeName=NetUtils.getNetWorkTypeName();
        KLog.d("refreshNetClick>>pingResult >> "+pingResult+",netWorkTypeName >> "+netWorkTypeName);
        tvStatus.setText(""+pingResult);
        tvType.setText(netWorkTypeName);
    }

    @Override
    public void netChange(int netType, boolean pingResult) {
        //isNormal 网络经过ping后 true为网络正常 false为网络异常
        String netTypeName=NetUtils.convertNetTypeName(netType);
        tvType.setText("" + netTypeName);
        tvStatus.setText("" + pingResult);
        tvDbm.setText("信号值：" +FLteTools.getDbm());
    }
}
