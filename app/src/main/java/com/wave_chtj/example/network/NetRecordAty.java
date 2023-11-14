package com.wave_chtj.example.network;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.ObjectUtils;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.callback.INetTimerCallback;
import com.wave_chtj.example.entity.NetBean;
import com.wave_chtj.example.util.AppManager;
import com.face_chtj.base_iotutils.view.TopTitleBar;
import com.wave_chtj.example.util.PACKAGES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route(path = PACKAGES.BASE + "netrecord")
public class NetRecordAty extends BaseActivity implements INetTimerCallback, View.OnClickListener {
    private static final String TAG=NetRecordAty.class.getSimpleName();
    private NetTimerService timerService;
    private boolean isBound = false;
    private Button btnRefresh;
    private Button btnStart;
    private Button btnClear;
    private Button btnClose;
    private Button btnClearCount;
    private TextView tvResult;
    private TextView tvSuccCount;
    private TextView tvErrCount;
    private TextView tvDbm;
    private TextView tvPingAddr;
    private EditText etTimerd;
    private TopTitleBar ctTopView;
    private boolean[] selectedItems = new boolean[NetUtils.getDnsList().length];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netrecord);
        ctTopView = findViewById(R.id.ctTopView);
        etTimerd = findViewById(R.id.etTimerd);
        etTimerd.setText(SPUtils.getInt(NetTimerService.KEY_INTERVAL, NetTimerService.DEFAULT_INTERVAL) + "");
        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);
        tvPingAddr = findViewById(R.id.tvPingAddr);
        tvPingAddr.setOnClickListener(this);
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

    private void showItemSelectionDialog() {
        if (timerService != null) {
            String[] lastChoices = timerService.getPingDns();
            if (lastChoices != null && lastChoices.length != 0) {
                for (int i = 0; i < lastChoices.length; i++) {
                    for (int j = 0; j < NetUtils.getDnsList().length; j++) {
                        if (lastChoices[i].equals(NetUtils.getDnsList()[j])) {
                            selectedItems[j] = true;
                        }
                    }
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select DNS");
        builder.setMultiChoiceItems(NetUtils.getDnsList(), selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedItems[which] = isChecked;
            }
        });
        builder.setPositiveButton(R.string.iot_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击了“OK”按钮，处理选中的项目
                List<String> selectedItemsList = new ArrayList<>();
                for (int i = 0; i < selectedItems.length; i++) {
                    if (selectedItems[i]) {
                        selectedItemsList.add(NetUtils.getDnsList()[i]);
                    }
                }
                // 在这里可以处理或显示选中的项目列表
                if (selectedItemsList.size() > 0) {
                    timerService.setPingDns(selectedItemsList.toArray(new String[0]));
                }
            }
        });

        builder.setNegativeButton(R.string.iot_cancel, null);
        builder.show();
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
    public void refreshNet(NetBean netBean) {
        String netConnectResult = FormatViewUtils.formatColor(netBean.netConnect + "", netBean.netConnect ? FormatViewUtils.C_GREEN : FormatViewUtils.C_RED);
        String dnsResult = FormatViewUtils.formatUnderlin(FormatViewUtils.C_BLUE, Arrays.toString(netBean.pingDns));
        FormatViewUtils.formatData(tvResult, "dns：" + dnsResult + ", pingResult：" + Arrays.toString(netBean.pingResult) + ", dbm：" + netBean.dbm + ", localIp：" + netBean.localIp + ", netType：" + netBean.netType + ", isNet4G：" + netBean.isNet4G + ", netConnect：" + netConnectResult);
        tvDbm.setText("信号：" + netBean.dbm);
        tvPingAddr.setText("DNS地址：" + Arrays.toString(netBean.pingDns));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                String interval = etTimerd.getText().toString();
                if (!ObjectUtils.isEmpty(interval)) {
                    SPUtils.putInt(NetTimerService.KEY_INTERVAL, Integer.parseInt(interval));
                    timerService.cancel();
                    timerService.startNetCheck();
                } else {
                    ToastUtils.error("请填写正确的参数：毫秒！");
                }
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
            case R.id.tvPingAddr:
                showItemSelectionDialog();
                break;
            case R.id.btnClear:
                tvResult.setText("");
                tvResult.scrollTo(0, 0);
                break;
        }
    }
}
