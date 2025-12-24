package com.ichtj.basetools.timer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.text.DecimalFormat;

public class TimerAty extends BaseActivity {
    private Button btnStart, btnFinish, btnCountdown, btnCountdownFinish;
    private TextView tvResult, tvCountdownResult;
    private EditText etCountDownTime;
    private long baseTimer;
    private Handler myhandler;
    private CountDownTimer mTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        initView();
        setupListeners();
    }

    private void initView() {
        btnStart = findViewById(R.id.btn_start);
        btnFinish = findViewById(R.id.btn_finish);
        btnCountdown = findViewById(R.id.btn_countdown);
        btnCountdownFinish = findViewById(R.id.btn_countdown_finish);
        tvResult = findViewById(R.id.tvResult);
        tvCountdownResult = findViewById(R.id.tvCountdownResult);
        etCountDownTime = findViewById(R.id.et_CountDownTime);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(this::onViewClicked);
        btnFinish.setOnClickListener(this::onViewClicked);
        btnCountdown.setOnClickListener(this::onViewClicked);
        btnCountdownFinish.setOnClickListener(this::onViewClicked);
    }

    public void startTimer() {
        baseTimer = SystemClock.elapsedRealtime();
        myhandler = new Handler() {
            public void handleMessage(Message msg) {
                if (0 == baseTimer) {
                    baseTimer = SystemClock.elapsedRealtime();
                }
                int time = (int) ((SystemClock.elapsedRealtime() - baseTimer) / 1000);
                String mm = new DecimalFormat("00").format(time / 60);
                String ss = new DecimalFormat("00").format(time % 60);
                if (tvResult != null) {
                    tvResult.setText(mm + ":" + ss);
                }
                Message message = Message.obtain();
                message.what = 0x0;
                sendMessageDelayed(message, 1000);
            }
        };
        myhandler.sendMessageDelayed(Message.obtain(myhandler, 1), 1000);
    }

    public void stopTimer() {
        if (myhandler != null) {
            myhandler.removeMessages(0x0);
        }
    }

    public void onViewClicked(View view) {
        int id = view.getId();
        if (id == R.id.btn_start) {
            tvResult.setText("");
            startTimer();
        } else if (id == R.id.btn_finish) {
            stopTimer();
        } else if (id == R.id.btn_countdown) {
            stopCountDown();
            tvCountdownResult.setText("");
            if (etCountDownTime.getText().toString() == null || etCountDownTime.getText().toString().equals("")) {
                ToastUtils.error("请填写倒计时时间");
                return;
            }
            int millisInFuture = Integer.parseInt(etCountDownTime.getText().toString().trim());
            startCountDown(millisInFuture);
        } else if (id == R.id.btn_countdown_finish) {
            stopCountDown();
        }
    }

    public void startCountDown(int millisInFuture) {
        if (mTimer == null) {
            mTimer = new CountDownTimer(millisInFuture * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int remainTime = (int) (millisUntilFinished / 1000L);
                    tvCountdownResult.setText("" + remainTime);
                }

                @Override
                public void onFinish() {
                    tvCountdownResult.setText("onFinish");
                }
            };
            mTimer.start();
        }
    }

    public void stopCountDown() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        stopCountDown();
    }
}