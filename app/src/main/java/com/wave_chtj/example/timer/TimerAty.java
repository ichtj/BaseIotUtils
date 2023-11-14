package com.wave_chtj.example.timer;

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
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerAty extends BaseActivity {
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_finish)
    Button btnFinish;
    @BindView(R.id.tvResult)
    TextView tvResult;
    long baseTimer;
    Handler myhandler;
    @BindView(R.id.btn_countdown)
    Button btnCountdown;
    @BindView(R.id.btn_countdown_finish)
    Button btnCountdownFinish;
    @BindView(R.id.tvCountdownResult)
    TextView tvCountdownResult;
    @BindView(R.id.et_CountDownTime)
    EditText etCountDownTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        ButterKnife.bind(this);

    }

    /**
     * 开启定时
     * System.currentTimeMillis()获取当前日期有意义，如当前是xxxx年xx月xx时xx分xx秒xxx毫秒，这个值在系统设置中可以更改的
     * SystemClock.elapsedRealtime()计算某个时间经历了多长时间有意义，例如通话经历了多长时间，这个值是系统设置无关
     */
    public void startTimer() {
        baseTimer = SystemClock.elapsedRealtime();
        tvResult = (TextView) this.findViewById(R.id.tvResult);
        myhandler = new Handler() {
            public void handleMessage(Message msg) {
                if (0 == baseTimer) {
                    //计算某个时间经历了多长时间有意义，例如通话经历了多长时间，这个值是系统设置无关
                    baseTimer = SystemClock.elapsedRealtime();
                }

                int time = (int) ((SystemClock.elapsedRealtime() - baseTimer) / 1000);
                String mm = new DecimalFormat("00").format(time / 60);
                String ss = new DecimalFormat("00").format(time % 60);
                if (null != tvResult) {
                    tvResult.setText(mm + ":" + ss);
                }
                Message message = Message.obtain();
                message.what = 0x0;
                sendMessageDelayed(message, 1000);
            }
        };
        myhandler.sendMessageDelayed(Message.obtain(myhandler, 1), 1000);
    }

    /**
     * 关闭计时器
     */
    public void stopTimer() {
        if (myhandler != null) {
            myhandler.removeMessages(0x0);
        }
    }


    @OnClick({R.id.btn_start, R.id.btn_finish, R.id.btn_countdown, R.id.btn_countdown_finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                tvResult.setText("");
                startTimer();
                break;
            case R.id.btn_finish:
                stopTimer();
                break;
            case R.id.btn_countdown://倒计时
                //开启倒计时前先把之前的及时关闭
                stopCountDown();
                tvCountdownResult.setText("");
                if (etCountDownTime.getText().toString() == null || etCountDownTime.getText().toString().equals("")) {
                    ToastUtils.error("请填写倒计时时间");
                    return;
                }
                int millisInFuture = Integer.parseInt(etCountDownTime.getText().toString().trim());
                startCountDown(millisInFuture);
                break;
            case R.id.btn_countdown_finish:
                stopCountDown();
                break;
        }
    }

    private CountDownTimer mTimer;

    /**
     * 开始倒计时
     *
     * @param millisInFuture
     */
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

    /**
     * 关闭倒计时
     */
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
