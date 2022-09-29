package com.wave_chtj.example.network;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.face_chtj.base_iotutils.display.ToastUtils;
import com.face_chtj.base_iotutils.network.NetChangeMonitor;
import com.face_chtj.base_iotutils.network.callback.INetChangeCallback;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * Create on 2020/1/3
 * author chtj
 * desc 网络监听
 */
public class NetChangeAty extends BaseActivity {
    private static final String TAG = "NetChangeAty";
    private TextView tvStatus;
    private TextView tvType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
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
        NetChangeMonitor.instance().registerReceiver(new INetChangeCallback() {
            @Override
            public void changed(int netType, boolean isNormal) {
                //isNormal 网络经过ping后 true为网络正常 false为网络异常
                String netTypeName=NetUtils.convertNetTypeName(netType);
                KLog.e(TAG, "network type=" + netTypeName + ",isNormal=" + isNormal);
                tvType.setText("" + netTypeName);
                tvStatus.setText("" + isNormal);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetChangeMonitor.instance().unRegisterReceiver();
    }
}
