package com.wave_chtj.example.network;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.KLog;
import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.callback.INetTimerCallback;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.util.PACKAGES;

import java.util.Arrays;

@Route(path = PACKAGES.BASE+"netrecord")
public class NetRecordAty extends BaseActivity implements INetTimerCallback, View.OnClickListener {
    NetTimerService timerService;
    private boolean isBound = false;
    Button btnRefresh;
    Button btnStart;
    Button btnClose;
    Button btnClearCount;
    TextView tvResult;
    TextView tvSuccCount;
    TextView tvErrCount;
    TextView tvDbm;
    TextView tvPingAddr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netrecord);
        tvPingAddr = findViewById(R.id.tvPingAddr);
        btnClearCount = findViewById(R.id.btnClearCount);
        btnClearCount.setOnClickListener(this);
        tvDbm = findViewById(R.id.tvDbm);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnStart = findViewById(R.id.btnStart);
        tvSuccCount = findViewById(R.id.tvSuccCount);
        tvErrCount = findViewById(R.id.tvErrCount);
        btnStart.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        tvResult = findViewById(R.id.tvResult);
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        startBindService();
        AppManager.finishActivity(StartPageAty.class);
    }

    public void startBindService() {
        timerService = new NetTimerService();
        timerService.setiNetTimerCallback(NetRecordAty.this);
        Intent intent = new Intent(this, timerService.getClass());
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBound = true;
            NetTimerService.NetTimerBinder myBinder = (NetTimerService.NetTimerBinder) binder;
            timerService = myBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            KLog.d("onServiceDisconnected ");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(conn);
        } catch (Throwable e) {
        }
    }

    @Override
    public void refreshNet(String time, String[] dns, String dbm, String localIp, String netType,
                           boolean isNet4G, boolean pingResult) {
        tvResult.append("\ntime：" + time + ", dns：" + Arrays.toString(dns) + ", dbm：" + dbm + ", localIp：" + localIp + ", netType：" + netType + ", isNet4G：" + isNet4G + ", pingResult：" + pingResult);
        tvDbm.setText("信号：" + dbm);
        tvPingAddr.setText("ping固定地址："+ Arrays.toString(dns));
        //刷新最新行显示
        int offset = tvResult.getLineCount() * tvResult.getLineHeight();
        int tvHeight = tvResult.getHeight();
        if (offset > 6000) {
            tvResult.setText("");
            tvResult.scrollTo(0, 0);
        } else {
            if (offset > tvHeight) {
                //Log.d(TAG, "showData: offset >> " + offset + " ,tvHeight >> " + tvHeight);
                tvResult.scrollTo(0, offset - tvHeight);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                timerService.startNetCheck();
                break;
            case R.id.btnClose:
                timerService.cancel();
                break;
            case R.id.btnRefresh:
                tvErrCount.setText("异常次数：" + timerService.getErrCount());
                tvSuccCount.setText("正常次数：" + timerService.getSuccCount());
                break;
            case R.id.btnClearCount:
                timerService.clearCount();
                break;
        }
    }
}
