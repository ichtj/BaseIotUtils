package com.wave_chtj.example.network;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.NetUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.callback.INetMonitor;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.util.PACKAGES;

import java.util.Arrays;

@Route(path = PACKAGES.BASE+"netmonitor")
public class NetMonitorAty extends BaseActivity implements CompoundButton.OnCheckedChangeListener, INetMonitor {
    private boolean isBound = false;
    private NetMonitorService nService;
    private TextView tvResetMode, tvNowTime, tvNetResult, tvNetType, tvPingList,
            tvResetErrCount, tvInputAddrResult, tvTotalCount, tvDbm, tvModeResetTitle, tvTaskStatus;
    private RadioButton btnHard, btnSoft, rbOne, rbMore, rbRebootYes, rbRebootNo;
    private Button btnAirplaneMode, btnRestartMode;
    private EditText etAddress;
    private LinearLayout llFifteenView, llResetExeu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_reset);
        rbRebootYes = findViewById(R.id.rbRebootYes);
        rbRebootYes.setOnCheckedChangeListener(this);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        rbRebootNo = findViewById(R.id.rbRebootNo);
        rbRebootNo.setOnCheckedChangeListener(this);
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
        startBindService();
    }

    /**
     * 获取一个随机数
     *
     * @param start
     * @param end
     * @return
     */
    public static int getRandomNum(int start, int end) {
        return (int) Math.floor(Math.random() * (start - end) + end);
    }

    public void startBindService() {
        Intent intent = new Intent(this, NetMonitorService.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        AppManager.finishActivity(StartPageAty.class);
    }


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            KLog.d("onServiceConnected ");
            isBound = true;
            NetMonitorService.NetBinder myBinder = (NetMonitorService.NetBinder) binder;
            nService = myBinder.getService();
            if (nService == null) {
                KLog.d("service_start_err() 1");
                ToastUtils.error(getString(R.string.service_start_err));
                return;
            }
            changeResetModeTitle();
            int getCycleValue = nService.getCyclesCount();
            if (getCycleValue == 0) {
                rbMore.setChecked(true);
            } else {
                rbOne.setChecked(true);
            }
            boolean timerdAchieve = nService.getTimerdAchieve();
            if (timerdAchieve) {
                rbRebootYes.setChecked(true);
            } else {
                rbRebootNo.setChecked(true);
            }
            nService.setInetMonitor(NetMonitorAty.this);
            nService.getDataCallBack();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            KLog.d("onServiceDisconnected ");
        }
    };

    public void initViewTitle() {
        tvNetResult.setText(String.format(getString(R.string.net_status), "..."));
        tvNetType.setText(String.format(getString(R.string.net_type), "..."));
        tvResetErrCount.setText(String.format(getString(R.string.net_reset_err_count), "..."));
        tvTotalCount.setText(String.format(getString(R.string.net_reset_total_count), "..."));
        tvPingList.setText(String.format(getString(R.string.ping_list), "..."));
        tvNowTime.setText(String.format(getString(R.string.now_time), "..."));
        tvDbm.setText(String.format(getString(R.string.net_dbm), "..."));
        tvTaskStatus.setText(String.format(getString(R.string.task_status), "..."));
    }

    /**
     * 改变标题的UI
     */
    public void changeResetModeTitle() {
        int resetMode = nService.getResetModeValue();
        tvResetMode.setText(String.format(getString(R.string.net_reset_mode), nService.getResetMode()));
        if (resetMode == NetMtools.MODE_HARD || resetMode == NetMtools.MODE_SOFT) {
            if (resetMode == NetMtools.MODE_HARD) {
                btnHard.setChecked(true);
            } else {
                btnSoft.setChecked(true);
            }
            btnAirplaneMode.setTypeface(Typeface.DEFAULT);
            btnAirplaneMode.setTextColor(Color.WHITE);

            tvModeResetTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tvModeResetTitle.setTextColor(Color.GREEN);

            btnRestartMode.setTypeface(Typeface.DEFAULT);
            btnRestartMode.setTextColor(Color.WHITE);
            llFifteenView.setVisibility(View.VISIBLE);
            llResetExeu.setVisibility(View.VISIBLE);
        } else if (resetMode == NetMtools.MODE_AIRPLANE) {
            btnAirplaneMode.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            btnAirplaneMode.setTextColor(Color.GREEN);

            tvModeResetTitle.setTypeface(Typeface.DEFAULT);
            tvModeResetTitle.setTextColor(Color.WHITE);

            btnRestartMode.setTypeface(Typeface.DEFAULT);
            btnRestartMode.setTextColor(Color.WHITE);
            llFifteenView.setVisibility(View.VISIBLE);
            llResetExeu.setVisibility(View.VISIBLE);
        } else {
            btnAirplaneMode.setTypeface(Typeface.DEFAULT);
            btnAirplaneMode.setTextColor(Color.WHITE);

            tvModeResetTitle.setTypeface(Typeface.DEFAULT);
            tvModeResetTitle.setTextColor(Color.WHITE);

            btnRestartMode.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            btnRestartMode.setTextColor(Color.GREEN);
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
            initViewTitle();
            nService.setModeRestartCallBack(NetMtools.MODE_REBOOT);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d("btnAirplaneModeClick ");
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
            initViewTitle();
            nService.setModeRestartCallBack(NetMtools.MODE_AIRPLANE);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d("btnAirplaneModeClick ");
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
            initViewTitle();
            nService.setModeRestartCallBack(NetMtools.MODE_HARD);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d("btnHardClick ");
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
            initViewTitle();
            nService.setModeRestartCallBack(NetMtools.MODE_SOFT);
            changeResetModeTitle();
            //ToastUtils.success(getString(R.string.service_start_succ));
            KLog.d("btnSoftClick ");
        } else {
            ToastUtils.error(getString(R.string.service_start_err));
        }
    }

    /**
     * 清除所有日志
     */
    public void clearAllLogClick(View view) {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("rm -rf " + NetMtools.LOG_PATH + "*.*", true);
        if (commandResult.result == 0) {
            ToastUtils.success(getString(R.string.del_cache_succ));
        } else {
            ToastUtils.success(getString(R.string.del_cache_failed));
        }
    }


    /**
     * 重置异常次数
     */
    public void resetErrCountClick(View view) {
        if (nService != null) {
            nService.setErrCount(0);
            nService.setTotalCount(0);
            tvTotalCount.setText(String.format(getString(R.string.net_reset_total_count), nService.getTotalCount() + ""));
            tvResetErrCount.setText(String.format(getString(R.string.net_reset_err_count), nService.getErrCount() + ""));
            ToastUtils.success(getString(R.string.reset_first_errcount));
        } else {
            ToastUtils.error(getString(R.string.service_start_err));
        }
    }

    /**
     * ping一个地址
     */
    public void pingClick(View view) {
        String address = etAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            ToastUtils.error(getString(R.string.input_add_err));
            return;
        }
        String[] dnsList = new String[]{address};
        boolean isPing = NetUtils.checkNetWork(dnsList, 1, 1);
        tvInputAddrResult.setText(String.format(getString(R.string.ping_addr_result), (isPing ? getString(R.string.ping_ok) : getString(R.string.ping_err)), TimeUtils.getTodayDateHms("yyyy-MM-dd HH:mm:ss")));
    }

    @Override
    public void getPingList(String[] pingList) {
        tvPingList.setText(String.format(getString(R.string.ping_list), Arrays.toString(pingList)));
    }

    @Override
    public void getNowTime(String time) {
        tvNowTime.setText(String.format(getString(R.string.now_time), time));
    }

    @Override
    public void getNetType(String netType, boolean isPing) {
        tvNetType.setText(String.format(getString(R.string.net_type), netType));
        tvNetResult.setText(getString(isPing ? R.string.net_ok : R.string.net_err));
    }

    @Override
    public void getResetErrCount(int errCount) {
        tvResetErrCount.setText(String.format(getString(R.string.net_reset_err_count), errCount + ""));
    }

    @Override
    public void getTotalCount(int totalCount) {
        tvTotalCount.setText(String.format(getString(R.string.net_reset_total_count), totalCount + ""));
    }

    @Override
    public void getDbm(String dBm) {
        tvDbm.setText(String.format(getString(R.string.net_dbm), dBm + ""));
    }

    @Override
    public void taskStatus(boolean isRunning) {
        tvTaskStatus.setText(String.format(getString(R.string.task_status), isRunning ? "正在运行" : "已停止"));
    }

    @Override
    public void onCheckedChanged(CompoundButton bv, boolean isChecked) {
        if (isChecked && bv.isPressed()) {
            switch (bv.getId()) {
                case R.id.rbOne:
                    KLog.d("onCheckedChanged: rbOne");
                    if (nService != null) {
                        nService.setCyclesCount(1);
                    } else {
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
                case R.id.rbMore:
                    KLog.d("onCheckedChanged: rbMore");
                    if (nService != null) {
                        nService.setCyclesCount(0);
                    } else {
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
                case R.id.rbRebootYes:
                    KLog.d("onCheckedChanged: rbRebootYes");
                    if (nService != null) {
                        nService.setDefaultTimerdAchieve(true);
                    } else {
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
                case R.id.rbRebootNo:
                    KLog.d("onCheckedChanged: rbRebootNo");
                    if (nService != null) {
                        nService.setDefaultTimerdAchieve(false);
                    } else {
                        ToastUtils.error(getString(R.string.service_start_err));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KLog.d("onDestroy >> ");
        try {
            unbindService(conn);
        } catch (Throwable e) {
        }
    }
}
