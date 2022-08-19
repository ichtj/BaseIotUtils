package com.wave_chtj.example.serialport;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.DataConvertUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.serialport.SerialPort;
import com.face_chtj.base_iotutils.serialport.SerialPortFinder;
import com.wave_chtj.example.R;
import com.wave_chtj.example.StartPageAty;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.util.AppManager;
import com.wave_chtj.example.util.SwitchUtils;
import com.wave_chtj.example.util.TopTitleView;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SerialPortAty extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "SerialPortAty";
    @BindView(R.id.sp_com)
    Spinner spCom;
    @BindView(R.id.sp_burate)
    Spinner spBurate;
    @BindView(R.id.btn_init)
    Button btnInit;
    @BindView(R.id.etCommand)
    EditText etCommand;
    @BindView(R.id.btn_test_send)
    Button btnTestSend;
    @BindView(R.id.btn_clear)
    Button btnClear;
    @BindView(R.id.tvResult)
    TextView tvResult;
    @BindView(R.id.ttView)
    TopTitleView ttView;
    @BindView(R.id.sp_com2)
    Spinner spCom2;
    @BindView(R.id.sp_burate2)
    Spinner spBurate2;
    @BindView(R.id.btn_init2)
    Button btnInit2;
    @BindView(R.id.etCommand2)
    EditText etCommand2;
    @BindView(R.id.btn_test_send2)
    Button btnTestSend2;
    @BindView(R.id.btn_clear2)
    Button btnClear2;
    @BindView(R.id.tvResult2)
    TextView tvResult2;
    @BindView(R.id.rbTxt)
    RadioButton rbTxt;
    @BindView(R.id.rbHex)
    RadioButton rbHex;
    @BindView(R.id.etAuto)
    EditText etAuto;
    @BindView(R.id.cbMs)
    CheckBox cbMs;
    @BindView(R.id.etAuto2)
    EditText etAuto2;
    @BindView(R.id.cbMs2)
    CheckBox cbMs2;
    private SerialPort serialOne;//串口控制
    private SerialPort serialTwo;//串口控制
    private List<String> list_serialcom = null;//串口地址
    private String[] arrays_burate;//波特率
    CustomSerialOne customSerialOne;
    CustomSerialTwo customSerialTwo;
    boolean isRun = false;
    boolean isRun2 = false;
    boolean isHexCmd = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialport_normal);
        ButterKnife.bind(this);
        //初始化控件
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvResult2.setMovementMethod(ScrollingMovementMethod.getInstance());
        //获取所有串口地址
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        KLog.d(TAG, "onCreate:>devices=[" + Arrays.toString(entryValues) + "]");
        list_serialcom = Arrays.asList(entryValues);
        //获取所有的波特率 可在R.array.burate 中手动添加需要的波特率
        arrays_burate = getResources().getStringArray(R.array.burate);
        //添加到适配器中显示
        ArrayAdapter arr_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list_serialcom);
        spCom.setAdapter(arr_adapter);
        spCom2.setAdapter(arr_adapter);
        rbTxt.setOnCheckedChangeListener(this);
        rbHex.setOnCheckedChangeListener(this);
        String getPkgName = getPackageName();
        KLog.d("onCreate:>getPkgName=" + getPkgName);
        if (getPkgName.contains(SwitchUtils.FLAG_SERIALPORT_PKG)) {
            AppManager.getAppManager().finishActivity(StartPageAty.class);
        }
    }

    /**
     * 开启串口
     */
    public void initOpenSerial(Button btnInit, Spinner spCom, Spinner spBurate, int position) {
        try {
            String com = spCom.getSelectedItem().toString();
            int baudrate = Integer.parseInt(spBurate.getSelectedItem().toString());
            switch (position) {
                case 0:
                    serialOne = new SerialPort(new File(com), baudrate, 0);
                    isRun = true;
                    customSerialOne = new CustomSerialOne(0, "init1");
                    customSerialOne.start();
                    //mThreadPool2.execute(customSerialOne);
                    break;
                case 1:
                    serialTwo = new SerialPort(new File(com), baudrate, 0);
                    isRun2 = true;
                    customSerialTwo = new CustomSerialTwo(1, "init2");
                    //mThreadPool2.execute(customSerialTwo);
                    customSerialTwo.start();
                    break;
            }
            KLog.d(TAG, "serialport param com=" + com + ",baudrate=" + baudrate);
            ToastUtils.success("开启串口成功！" + (position + 1));
            btnInit.setText("关闭串口");
            btnInit.setTextColor(Color.GREEN);
        } catch (Exception e) {
            Log.e(TAG, "errMeg:", e);
            ToastUtils.error("开启串口失败,请查看日志！");
            btnInit.setText("开启串口");
            btnInit.setTextColor(Color.BLACK);
            if (position == 0) {
                isRun = false;
            } else {
                isRun2 = false;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (buttonView.getId() == R.id.rbTxt) {
                etCommand.setText("hello test1");
                etCommand2.setText("hello test2");
                isHexCmd = false;
            } else if (buttonView.getId() == R.id.rbHex) {
                etCommand.setText("AA55030050002B");
                etCommand2.setText("AA55030050003B");
                isHexCmd = true;
            }
        }
    }

    class CustomSerialOne extends Thread {
        private int position;

        public CustomSerialOne(int position, String name) {
            super(name);
            this.position = position;
        }

        @Override
        public void run() {
            while (true && isRun && !customSerialOne.isInterrupted()) {
                try {
                    Thread.sleep(250);
                } catch (Throwable e) {
                }
                try {
                    int readSize = -1;
                    readSize = serialOne.getInputStream().available();
                    Log.d(TAG, "run: readSize=" + readSize);
                    if (readSize > 0) {
                        byte[] bytes = new byte[readSize];
                        serialOne.read(bytes, bytes.length);
                        Message message = handler.obtainMessage();
                        if (isHexCmd) {
                            message.obj = DataConvertUtils.encodeHexString(bytes);
                        } else {
                            message.obj = new String(bytes, StandardCharsets.UTF_8);
                        }
                        message.arg1 = position;
                        handler.sendMessage(message);
                        Log.d(TAG, "run: received end");
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "run: ", e);
                }
            }
        }
    }

    class CustomSerialTwo extends Thread {
        private int position;

        public CustomSerialTwo(int position, String name) {
            super(name);
            this.position = position;
        }

        @Override
        public void run() {
            while (true && isRun2 && !customSerialTwo.isInterrupted()) {
                try {
                    Thread.sleep(250);
                } catch (Throwable e) {
                }
                try {
                    int readSize = -1;
                    readSize = serialTwo.getInputStream().available();
                    if (readSize > 0) {
                        byte[] bytes = new byte[readSize];
                        serialTwo.read(bytes, bytes.length);
                        Message message = handler.obtainMessage();
                        if (isHexCmd) {
                            message.obj = DataConvertUtils.encodeHexString(bytes);
                        } else {
                            message.obj = new String(bytes, StandardCharsets.UTF_8);
                        }
                        message.arg1 = position;
                        handler.sendMessage(message);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "run: ", e);
                }
            }
        }
    }

    @OnClick({R.id.btn_init, R.id.btn_clear, R.id.btn_test_send, R.id.btn_init2, R.id.btn_clear2, R.id.btn_test_send2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_init:
                if (isRun) {
                    closeSerial(0);
                } else {
                    initOpenSerial(btnInit, spCom, spBurate, 0);
                }
                break;
            case R.id.btn_init2:
                if (isRun2) {
                    closeSerial(1);
                } else {
                    initOpenSerial(btnInit2, spCom2, spBurate2, 1);
                }
                break;
            case R.id.btn_test_send://发送命令
                if (!isRun) {
                    ToastUtils.error("请开启串口");
                    return;
                }
                String autoMs = etAuto.getText().toString();
                if (TextUtils.isEmpty(autoMs)) {
                    if (cbMs.isChecked()) {
                        ToastUtils.error("请填写正确的毫秒数！");
                        return;
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isRun) {
                            String hexComm = etCommand.getText().toString().trim();
                            if (hexComm.length() % 2 == 1) {
                                hexComm = "0" + hexComm;
                            }
                            KLog.d(TAG, "onViewClicked:>hexComm=" + hexComm);
                            serialOne.write(isHexCmd ? DataConvertUtils.decodeHexString(hexComm) : hexComm.getBytes());
                            if (!TextUtils.isEmpty(autoMs)) {
                                if (cbMs.isChecked()) {
                                    handler.postDelayed(this, Integer.parseInt(autoMs));
                                }
                            }
                        }
                    }
                });

                break;
            case R.id.btn_test_send2://发送命令
                if (!isRun2) {
                    ToastUtils.error("请开启串口");
                    return;
                }
                String autoMs2 = etAuto2.getText().toString();
                if (TextUtils.isEmpty(autoMs2)) {
                    if (cbMs2.isChecked()) {
                        ToastUtils.error("请填写正确的毫秒数！");
                        return;
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isRun2) {
                            String hexComm2 = etCommand2.getText().toString().trim();
                            if (hexComm2.length() % 2 == 1) {
                                hexComm2 = "0" + hexComm2;
                            }
                            KLog.d(TAG, "onViewClicked:>hexComm2=" + hexComm2);
                            serialTwo.write(isHexCmd ? DataConvertUtils.decodeHexString(hexComm2) : hexComm2.getBytes());
                            if (!TextUtils.isEmpty(autoMs2)) {
                                if (cbMs2.isChecked()) {
                                    handler.postDelayed(this, Integer.parseInt(autoMs2));
                                }
                            }
                        }
                    }
                });
                break;
            case R.id.btn_clear://清除结果
                tvResult.setText("");
                break;
            case R.id.btn_clear2://清除结果
                tvResult2.setText("");
                break;
        }
    }


    public String HexstrAddZero(String str) {
        String strByeZero = "";
        if (str.length() == 2) {
            strByeZero = str;
        } else if (str.length() == 1) {
            strByeZero = "0" + str;
        } else if (str.length() == 0) {
            strByeZero = "00";
        }
        return strByeZero;
    }

    /**
     * 关闭串口
     *
     * @param position
     */
    private void closeSerial(int position) {
        switch (position) {
            case 0:
                serialOne.close();
                isRun = false;
                btnInit.setText("开启串口");
                btnInit.setTextColor(Color.BLACK);
                customSerialOne.interrupt();
                break;
            case 1:
                serialTwo.close();
                isRun2 = false;
                btnInit2.setText("开启串口");
                btnInit2.setTextColor(Color.BLACK);
                customSerialTwo.interrupt();
                break;
        }
        ToastUtils.info("串口关闭！" + (position + 1));
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 0) {
                KLog.d(TAG, "msg.obj=" + msg.obj.toString());
                tvResult.append("\n\r" + msg.obj.toString());
            } else {
                KLog.d(TAG, "msg.obj=" + msg.obj.toString());
                tvResult2.append("\n\r" + msg.obj.toString());
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRun = false;
        isRun2 = false;
        if (customSerialOne != null) {
            customSerialOne.interrupt();
        }
        if (customSerialTwo != null) {
            customSerialTwo.interrupt();
        }
        if (serialOne != null) {
            serialOne.close();
            KLog.d(TAG, "onDestroy:>serialOne=");
        }
        if (serialTwo != null) {
            serialTwo.close();
            KLog.d(TAG, "onDestroy:>serialTwo=");
        }
    }
}
