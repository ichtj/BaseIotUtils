package com.wave_chtj.example.network;

import android.os.Bundle;
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
        NetMonitorUtils.register();
        NetMonitorUtils.addCallBack(this);
        tvDbm = findViewById(R.id.tvDbm);
        tvStatus = findViewById(R.id.tvStatus);
        tvType = findViewById(R.id.tvType);
        if(NetUtils.getNetWorkType()==NetUtils.NETWORK_NO){
            ToastUtils.error("当前无网络连接！");
            tvType.setText("无" );
            tvStatus.setText("false");
        }
    }

    //开始监听
    public void startLinstener(View view) {
        FLteTools.instance().init4GDbm(new NetDbmListener() {
            @Override
            public void getDbm(String dbmAsu) {
                tvDbm.setText("信号值："+dbmAsu);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetMonitorUtils.removeCallback(this);
        NetMonitorUtils.unRegister();
    }

    @Override
    public void netChange(int netType, boolean pingResult) {
        //isNormal 网络经过ping后 true为网络正常 false为网络异常
        String netTypeName=NetUtils.convertNetTypeName(netType);
        tvType.setText("" + netTypeName);
        tvStatus.setText("" + pingResult);
    }
}
