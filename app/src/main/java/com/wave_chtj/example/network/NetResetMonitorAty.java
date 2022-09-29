package com.wave_chtj.example.network;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.display.ToastUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.util.AppManager;

import java.util.Arrays;

public class NetResetMonitorAty extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = NetResetMonitorAty.class.getSimpleName() + "F";
    private boolean isBound = false;
    private NetResetMonitorService netResetMonitorService;
    private TextView tvResetMode;
    private TextView tvNowTime;
    private TextView tvNetResult;
    private TextView tvNetType;
    private TextView tvPingList;
    private TextView tvResetErrCount;
    private TextView tvInputAddrResult;
    private TextView tvTotalCount;
    private TextView tvDbm;
    private EditText etAddress;
    private RadioButton btnHard;
    private RadioButton btnSoft;
    private RadioButton rbOne;
    private RadioButton rbMore;
    private RadioButton rbRebootYes;
    private RadioButton rbRebootNo;
    private Button btnAirplaneMode;
    private Button btnRestartMode;
    private TextView tvModeResetTitle;
    private LinearLayout llFifteenView,llResetExeu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_reset_monitor);
        KLog.d(TAG, "onCreate ");
        rbRebootYes = findViewById(R.id.rbRebootYes);
        rbRebootYes.setOnCheckedChangeListener(this);
        rbRebootNo = findViewById(R.id.rbRebootNo);
        rbRebootNo.setOnCheckedChangeListener(this);;
        tvDbm = findViewById(R.id.tvDbm);
        tvResetMode = findViewById(R.id.tvResetMode);
        tvNowTime = findViewById(R.id.tvNowTime);
        tvNetResult = findViewById(R.id.tvNetResult);
        tvNetType = findViewById(R.id.tvNetType);
        tvPingList = findViewById(R.id.tvPingList);
        tvResetErrCount = findViewById(R.id.tvResetErrCount);
        etAddress = findViewById(R.id.etAddress);
        tvInputAddrResult = findViewById(R.id.tvInputAddrResult);
        btnHard = findViewById(R.id.btnHard);
        btnSoft = findViewById(R.id.btnSoft);
        rbOne = findViewById(R.id.rbOne);
        rbMore = findViewById(R.id.rbMore);
        rbOne.setOnCheckedChangeListener(this);
        rbMore.setOnCheckedChangeListener(this);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvModeResetTitle = findViewById(R.id.tvModeResetTitle);
        btnAirplaneMode = findViewById(R.id.btnAirplaneMode);
        btnRestartMode = findViewById(R.id.btnRestartMode);
        llFifteenView = findViewById(R.id.llFifteenView);
        llResetExeu = findViewById(R.id.llResetExeu);

        Intent intent = new Intent(this, NetResetMonitorService.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        AppManager.getAppManager().finishActivity(StartPageAty.class);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    BroadcastReceiver mReceiver = new AirplaneModeBroadcastReceiver();


    private class AirplaneModeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                KLog.d(TAG, "onReceive ACTION_AIRPLANE_MODE_CHANGED");
            }
        }
    }
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            KLog.d(TAG, "onServiceConnected ");
            isBound = true;
            NetResetMonitorService.NetMonitorBinder myBinder = (NetResetMonitorService.NetMonitorBinder) binder;
            netResetMonitorService = myBinder.getService();
            if (netResetMonitorService == null) {
                ToastUtils.error(getString(R.string.service_start_err));
                return;
            }
            changeResetModeTitle();
            int getCycleValue=netResetMonitorService.getCyclesCount();
            if(getCycleValue==0){
                rbMore.setChecked(true);
            }else{
                rbOne.setChecked(true);
            }
            boolean timerdAchieve=netResetMonitorService.getTimerdAchieve();
            if(timerdAchieve){
                rbRebootYes.setChecked(true);
            }else{
                rbRebootNo.setChecked(true);
            }
            netResetMonitorService.setNetMonitorCallBack(new NetMonitorCallBack() {
                @Override
                public void getPingResult(boolean isPing) {
                    KLog.d(TAG, "getPingResult isPing=" + isPing);
                    tvNetResult.setText(getString(isPing ? R.string.net_ok : R.string.net_err));
                }

                @Override
                public void getPingList(String[] pingList) {
                    KLog.d(TAG, "getPingList pingList=" + Arrays.toString(pingList));
                    tvPingList.setText(String.format(getString(R.string.ping_list), Arrays.toString(pingList)));
                }

                @Override
                public void getNowTime(String time) {
                    KLog.d(TAG, "getNowTime time=" + time);
                    tvNowTime.setText(String.format(getString(R.string.now_time), time));
                }

                @Override
                public void getNetType(String netType) {
                    KLog.d(TAG, "getNetType netType=" + netType);
                    tvNetType.setText(String.format(getString(R.string.net_type), netType));
                }

                @Override
                public void getResetErrCount(int errCount) {
                    KLog.d(TAG, "getResetErrCount errCount=" + errCount);
                    tvResetErrCount.setText(String.format(getString(R.string.net_reset_err_count), errCount + ""));
                }

                @Override
                public void getTotalCount(int totalCount) {
                    KLog.d(TAG, "getTotalCount totalCount=" + totalCount);
                    tvTotalCount.setText(String.format(getString(R.string.net_reset_total_count), totalCount + ""));
                }

                @Override
                public void getDbm(String dBm) {
                    KLog.d(TAG, "getDbm dBm=" + dBm);
                    tvDbm.setText(String.format(getString(R.string.net_dbm), dBm + ""));
                }
            });
            netResetMonitorService.getDataCallBack();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            KLog.d(TAG, "onServiceDisconnected ");
        }
    };

    public void loadInitData() {
        tvNetResult.setText(String.format(getString(R.string.net_status),"..."));
        tvNetType.setText(String.format(getString(R.string.net_type),"..."));
        tvResetErrCount.setText(String.format(getString(R.string.net_reset_err_count),"..."));
        tvTotalCount.setText(String.format(getString(R.string.net_reset_total_count),"..."));
        tvPingList.setText(String.format(getString(R.string.ping_list),"..."));
        tvNowTime.setText(String.format(getString(R.string.now_time),"..."));
        tvDbm.setText(String.format(getString(R.string.net_dbm),"..."));
    }

    /**
     * 改变标题的UI
     */
    public void changeResetModeTitle(){
        int resetMode = netResetMonitorService.getResetModeValue();
        KLog.d(TAG,"changeResetModeTitle resetMode="+resetMode);
        tvResetMode.setText(String.format(getString(R.string.net_reset_mode), netResetMonitorService.getResetMode()));
        if(resetMode==NetResetMonitorService.FLAG_MODE_HARD||resetMode==NetResetMonitorService.FLAG_MODE_SOFT){
            if(resetMode==NetResetMonitorService.FLAG_MODE_HARD){
                btnHard.setChecked(true);
            }else{
                btnSoft.setChecked(true);
            }
            btnAirplaneMode.setTypeface(Typeface.DEFAULT);
            btnAirplaneMode.setTextColor( Color.WHITE);

            tvModeResetTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD) );
            tvModeResetTitle.setTextColor( Color.GREEN);

            btnRestartMode.setTypeface(Typeface.DEFAULT);
            btnRestartMode.setTextColor( Color.WHITE);
            llFifteenView.setVisibility(View.VISIBLE);
            llResetExeu.setVisibility(View.VISIBLE);
        }else if(resetMode==NetResetMonitorService.FLAG_MODE_AIRPLANE){
            btnAirplaneMode.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            btnAirplaneMode.setTextColor( Color.GREEN);

            tvModeResetTitle.setTypeface(Typeface.DEFAULT);
            tvModeResetTitle.setTextColor( Color.WHITE);

            btnRestartMode.setTypeface(Typeface.DEFAULT);
            btnRestartMode.setTextColor( Color.WHITE);
            llFifteenView.setVisibility(View.VISIBLE);
            llResetExeu.setVisibility(View.VISIBLE);
        }else{
            btnAirplaneMode.setTypeface(Typeface.DEFAULT);
            btnAirplaneMode.setTextColor( Color.WHITE);

            tvModeResetTitle.setTypeface(Typeface.DEFAULT);
            tvModeResetTitle.setTextColor( Color.WHITE);

            btnRestartMode.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            btnRestartMode.setTextColor( Color.GREEN);
            llFifteenView.setVisibility(View.GONE);
            llResetExeu.setVisibility(View.GONE);
        }
    }

    /**
     * 重启模式
     *
     * @param view
     */
    public void btnRestartModeClick(View view) {
        if (isBound) {
            loadInitData();
            netResetMonitorService.setModeRestartCallBack(NetResetMonitorService.FLAG_MODE_REBOOT);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d(TAG, "btnAirplaneModeClick ");
        } else {
            ToastUtils.error(getString(R.string.service_start_err));
        }
    }

    /**
     * 飞行模式
     *
     * @param view
     */
    public void btnAirplaneModeClick(View view) {
        if (isBound) {
            loadInitData();
            netResetMonitorService.setModeRestartCallBack(NetResetMonitorService.FLAG_MODE_AIRPLANE);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d(TAG, "btnAirplaneModeClick ");
        } else {
            ToastUtils.error(getString(R.string.service_start_err));
        }
    }

    /**
     * 执行硬复位
     *
     * @param view
     */
    public void btnHardClick(View view) {
        if (isBound) {
            loadInitData();
            netResetMonitorService.setModeRestartCallBack(NetResetMonitorService.FLAG_MODE_HARD);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d(TAG, "btnHardClick ");
        } else {
            ToastUtils.error(getString(R.string.service_start_err));
        }
    }

    /**
     * 执行软复位
     *
     * @param view
     */
    public void btnSoftClick(View view) {
        if (isBound) {
            loadInitData();
            netResetMonitorService.setModeRestartCallBack(NetResetMonitorService.FLAG_MODE_SOFT);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d(TAG, "btnSoftClick ");
        } else {
            ToastUtils.error(getString(R.string.service_start_err));
        }
    }

    /**
     * 清除所有日志
     */
    public void clearAllLogClick(View view){
        ShellUtils.CommandResult commandResult=ShellUtils.execCommand("rm -rf "+NetResetMonitorService.NET_LOG_RECORD_PATH+"*.*",true);
        if(commandResult.result==0){
            ToastUtils.success(getString(R.string.del_cache_succ));
        }else{
            ToastUtils.success(getString(R.string.del_cache_failed));
        }
    }


    /**
     * 重置异常次数
     */
    public void resetErrCountClick(View view){
        if(netResetMonitorService!=null){
            netResetMonitorService.setErrCount(0);
            netResetMonitorService.setTotalCount(0);
            tvTotalCount.setText(String.format(getString(R.string.net_reset_total_count), netResetMonitorService.getTotalCount() + ""));
            tvResetErrCount.setText(String.format(getString(R.string.net_reset_err_count), netResetMonitorService.getErrCount() + ""));
            ToastUtils.success(getString(R.string.reset_first_errcount));
        }else{
            ToastUtils.error(getString(R.string.service_start_err));
        }
    }

    /**
     * ping一个地址
     *
     * @param view
     */
    public void pingClick(View view) {
        String address = etAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            ToastUtils.error(getString(R.string.input_add_err));
            return;
        }
        String[] dnsList = new String[]{address};
        boolean isPing = NetMonitorUtils.checkNetWork(dnsList, 2, 1);
        tvInputAddrResult.setText(String.format(getString(R.string.ping_addr_result), isPing ? getString(R.string.ping_ok) : getString(R.string.ping_err)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KLog.d(TAG, "onDestroy ");
        if (isBound || netResetMonitorService != null) {
            try {
                unbindService(conn);
            } catch (Throwable e) {
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            switch (buttonView.getId()){
                case R.id.rbOne:
                    Log.d(TAG, "onCheckedChanged: rbOne");
                    if(netResetMonitorService!=null){
                        netResetMonitorService.setCyclesCount(1);
                    }else{
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
                case R.id.rbMore:
                    Log.d(TAG, "onCheckedChanged: rbMore");
                    if(netResetMonitorService!=null){
                        netResetMonitorService.setCyclesCount(0);
                    }else{
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
                case R.id.rbRebootYes:
                    Log.d(TAG, "onCheckedChanged: rbRebootYes");
                    if(netResetMonitorService!=null){
                        netResetMonitorService.setDefaultTimerdAchieve(true);
                    }else{
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
                case R.id.rbRebootNo:
                    Log.d(TAG, "onCheckedChanged: rbRebootNo");
                    if(netResetMonitorService!=null){
                        netResetMonitorService.setDefaultTimerdAchieve(false);
                    }else{
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
            }
        }
    }
}
