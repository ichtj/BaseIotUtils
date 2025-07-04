package com.ichtj.basetools.network;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.DialogUtils;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.face_chtj.base_iotutils.GlobalDialogUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.ObjectUtils;
import com.face_chtj.base_iotutils.RegularTools;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.callback.IDialogCallback;
import com.face_chtj.base_iotutils.view.OnPopupItemClickListener;
import com.face_chtj.base_iotutils.view.PopupWindowTools;
import com.ichtj.basetools.MainActivity;
import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.callback.INetTimerCallback;
import com.ichtj.basetools.entity.NetBean;
import com.ichtj.basetools.util.AppManager;
import com.face_chtj.base_iotutils.view.TopTitleBar;
import com.ichtj.basetools.util.PACKAGES;

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
        tvPingAddr.setPaintFlags(tvPingAddr.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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
        builder.setTitle(R.string.net_record_dns_title);
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
                    timerService.replacePingDns(selectedItemsList.toArray(new String[0]));
                }else{
                    ToastUtils.error(getString(R.string.net_record_more_than_one));
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
            if (timerService!=null){
                tvPingAddr.setText(Arrays.toString(timerService.getPingDns()));
            }
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
        String netConnectResult = FormatViewUtils.formatColor(netBean.netConnect + "", netBean.netConnect ? R.color.green : R.color.red);
//        String dnsResult = FormatViewUtils.formatUnderline (R.color.blue, Arrays.toString(netBean.pingDns));
        FormatViewUtils.formatData(tvResult, /*"dns：" + dnsResult + */"dns：" + Arrays.toString(netBean.pingResult) + ", dbm：" + netBean.dbm + ", localIp：" + netBean.localIp + ", netType：" + netBean.netType + ", On4G：" + netBean.isNet4G + ", netResult：" + netConnectResult,"yyyyMMddHHmmss");
        tvDbm.setText("信号：" + netBean.dbm);
        String content=getString(R.string.net_record_in_dns,Arrays.toString(netBean.pingDns));
        setTextWithLimit(tvPingAddr,content,25);
    }

    public static void setTextWithLimit(TextView textView, String content, int maxLength) {
        content=content.replace("[","").replace("]","");
        if (content.length() > maxLength) {
            String truncated = content.substring(0, maxLength - 1) + "………"; // 中文省略号也可以用
            textView.setText(truncated);
        } else {
            textView.setText(content);
        }
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
                List<String> options = Arrays.asList("自定义", "内部列表");
                PopupWindowTools.showDropdownPopup(this, tvPingAddr, options, new OnPopupItemClickListener() {
                    @Override
                    public void onItemClick(int position, String itemText) {
                        // 这里你可以接收到点击项的 position 和文本
                        if (position==0){
                            CustomDynamicDialog.showDialog(NetRecordAty.this, "请输入IP", new CustomDynamicDialog.OnConfirmListener() {
                                @Override
                                public void onConfirm(List<String> inputs) {
                                    if (inputs!=null&&inputs.size()>0){
                                        Log.d(TAG, "onConfirm: ipList>>"+inputs);
                                        timerService.replacePingDns(inputs.toArray(new String[0]));
                                    }
                                }
                            });
                        }else{
                            showItemSelectionDialog();
                        }
                    }
                });
                break;
            case R.id.btnClear:
                tvResult.setText("");
                tvResult.scrollTo(0, 0);
                break;
        }
    }
}
