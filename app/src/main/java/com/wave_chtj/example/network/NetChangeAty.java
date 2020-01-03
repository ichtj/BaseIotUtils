package com.wave_chtj.example.network;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.chtj.base_iotutils.KLog;
import com.chtj.base_iotutils.NetUtils;
import com.wave_chtj.example.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Create on 2020/1/3
 * author chtj
 * desc 网络检测
 */
public class NetChangeAty extends AppCompatActivity {
    public static final String TAG = "NetChangeAty";
    TextView tvStatus;
    TextView tvType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        tvStatus = findViewById(R.id.tvStatus);
        tvType = findViewById(R.id.tvType);

    }

    //开始监听
    public void startLinstener(View view) {
        NetChangeReceiver.getInstance().registerReceiver();
        NetChangeReceiver.getInstance().setOnNetChangeLinstener(new OnNetChangeLinstener() {
            @Override
            public void changed(NetTypeInfo type, boolean isNormal) {
                KLog.e(TAG, "network type=" + type.name() + ",isNormal=" + isNormal);
                tvType.setText("" + type.name());
                tvStatus.setText("" + isNormal);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetChangeReceiver.getInstance().unRegisterReceiver();
    }
}
