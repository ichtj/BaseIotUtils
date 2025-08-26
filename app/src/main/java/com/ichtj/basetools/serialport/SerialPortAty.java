package com.ichtj.basetools.serialport;

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

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.TranscodingUtils;
import com.face_chtj.base_iotutils.serialport.SerialPort;
import com.face_chtj.base_iotutils.serialport.SerialPortFinder;
import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppManager;
import com.ichtj.basetools.util.PACKAGES;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Route(path = PACKAGES.BASE + "serialport")
public class SerialPortAty extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = "SerialPortAty";
    Spinner spCom, spBurate, spCom2, spBurate2;
    Button btnInit, btnTestSend, btnClear, btnInit2, btnTestSend2, btnClear2;
    EditText etCommand, etCommand2, etAuto, etAuto2;
    TextView tvResult, tvResult2;
    RadioButton rbTxt, rbHex;
    CheckBox cbMs, cbMs2;
    private SerialPort serialOne, serialTwo;
    private List<String> list_serialcom = null;
    private String[] arrays_burate;
    CustomSerialOne customSerialOne;
    CustomSerialTwo customSerialTwo;
    boolean isRun = false, isRun2 = false, isHexCmd = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialport);
        initView();
        setupListeners();
        initializeSerialPortData();
    }

    private void initView() {
        spCom = findViewById(R.id.spCom);
        spBurate = findViewById(R.id.spBurate);
        btnInit = findViewById(R.id.btnInit);
        etCommand = findViewById(R.id.etCommand);
        btnTestSend = findViewById(R.id.btnTestSend);
        btnClear = findViewById(R.id.btnClear);
        tvResult = findViewById(R.id.tvResult);
        spCom2 = findViewById(R.id.spCom2);
        spBurate2 = findViewById(R.id.spBurate2);
        btnInit2 = findViewById(R.id.btnInit2);
        etCommand2 = findViewById(R.id.etCommand2);
        btnTestSend2 = findViewById(R.id.btnTestSend2);
        btnClear2 = findViewById(R.id.btnClear2);
        tvResult2 = findViewById(R.id.tvResult2);
        rbTxt = findViewById(R.id.rbTxt);
        rbHex = findViewById(R.id.rbHex);
        etAuto = findViewById(R.id.etAuto);
        cbMs = findViewById(R.id.cbMs);
        etAuto2 = findViewById(R.id.etAuto2);
        cbMs2 = findViewById(R.id.cbMs2);
    }

    private void setupListeners() {
        btnInit.setOnClickListener(this);
        btnTestSend.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnInit2.setOnClickListener(this);
        btnTestSend2.setOnClickListener(this);
        btnClear2.setOnClickListener(this);
        rbTxt.setOnCheckedChangeListener(this);
        rbHex.setOnCheckedChangeListener(this);
    }

    private void initializeSerialPortData() {
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvResult2.setMovementMethod(ScrollingMovementMethod.getInstance());
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        KLog.d(TAG, "onCreate:>devices=[" + Arrays.toString(entryValues) + "]");
        list_serialcom = Arrays.asList(entryValues);
        arrays_burate = getResources().getStringArray(R.array.burate);
        ArrayAdapter<String> arr_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list_serialcom);
        spCom.setAdapter(arr_adapter);
        spCom2.setAdapter(arr_adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnInit:
                handleSerialPortInit(0, btnInit, spCom, spBurate);
                break;
            case R.id.btnInit2:
                handleSerialPortInit(1, btnInit2, spCom2, spBurate2);
                break;
            case R.id.btnTestSend:
                handleSerialPortSend(0, etCommand, etAuto, cbMs, serialOne);
                break;
            case R.id.btnTestSend2:
                handleSerialPortSend(1, etCommand2, etAuto2, cbMs2, serialTwo);
                break;
            case R.id.btnClear:
                tvResult.setText("");
                break;
            case R.id.btnClear2:
                tvResult2.setText("");
                break;
        }
    }

    private void handleSerialPortInit(int position, Button btnInit, Spinner spCom, Spinner spBurate) {
        if ((position == 0 && isRun) || (position == 1 && isRun2)) {
            closeSerial(position);
        } else {
            initOpenSerial(btnInit, spCom, spBurate, position);
        }
    }

    private void handleSerialPortSend(int position, EditText etCommand, EditText etAuto, CheckBox cbMs, SerialPort serialPort) {
        if ((position == 0 && !isRun) || (position == 1 && !isRun2)) {
            ToastUtils.error("请开启串口");
            return;
        }
        String autoMs = etAuto.getText().toString();
        if (TextUtils.isEmpty(autoMs) && cbMs.isChecked()) {
            ToastUtils.error("请填写正确的毫秒数！");
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if ((position == 0 && isRun) || (position == 1 && isRun2)) {
                    String hexComm = etCommand.getText().toString().trim();
                    if (hexComm.length() % 2 == 1) {
                        hexComm = "0" + hexComm;
                    }
                    KLog.d(TAG, "onViewClicked:>hexComm=" + hexComm);
                    serialPort.write(isHexCmd ? TranscodingUtils.decodeHexString(hexComm) : hexComm.getBytes());
                    if (!TextUtils.isEmpty(autoMs) && cbMs.isChecked()) {
                        handler.postDelayed(this, Integer.parseInt(autoMs));
                    }
                }
            }
        });
    }

    private void initOpenSerial(Button btnInit, Spinner spCom, Spinner spBurate, int position) {
        try {
            String com = spCom.getSelectedItem().toString();
            int baudrate = Integer.parseInt(spBurate.getSelectedItem().toString());
            switch (position) {
                case 0:
                    serialOne = new SerialPort(new File(com), baudrate, 0);
                    isRun = true;
                    customSerialOne = new CustomSerialOne(0, "init1");
                    customSerialOne.start();
                    break;
                case 1:
                    serialTwo = new SerialPort(new File(com), baudrate, 0);
                    isRun2 = true;
                    customSerialTwo = new CustomSerialTwo(1, "init2");
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
                    int readSize = serialOne.getInputStream().available();
                    if (readSize > 0) {
                        byte[] bytes = new byte[readSize];
                        serialOne.read(bytes, bytes.length);
                        Message message = handler.obtainMessage();
                        message.obj = isHexCmd ? TranscodingUtils.encodeHexString(bytes) : new String(bytes, StandardCharsets.UTF_8);
                        message.arg1 = position;
                        handler.sendMessage(message);
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
                    int readSize = serialTwo.getInputStream().available();
                    if (readSize > 0) {
                        byte[] bytes = new byte[readSize];
                        serialTwo.read(bytes, bytes.length);
                        Message message = handler.obtainMessage();
                        message.obj = isHexCmd ? TranscodingUtils.encodeHexString(bytes) : new String(bytes, StandardCharsets.UTF_8);
                        message.arg1 = position;
                        handler.sendMessage(message);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "run: ", e);
                }
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 0) {
                tvResult.append("\n\r" + msg.obj.toString());
            } else {
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
        }
        if (serialTwo != null) {
            serialTwo.close();
        }
    }
}