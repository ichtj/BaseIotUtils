package com.ichtj.basetools.socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.face_chtj.base_iotutils.KLog;
import com.chtj.socket.BaseTcpSocket;
import com.chtj.socket.BaseUdpSocket;
import com.chtj.socket.ISocketListener;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.PACKAGES;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@Route(path = PACKAGES.BASE+"socket")
public class SocketAty extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "SocketAty";
    private EditText etIp, etPort, etSendContent;
    private Button btnConnect, btnDisConnect, btnSend, btnClear;
    private TextView tvResult;
    private Spinner spOption;

    private int selectOption = TCP_OPTION;
    private static final int TCP_OPTION = 0;
    private static final int UDP_OPTION = 1;

    private BaseTcpSocket baseTcpSocket = null;
    private BaseUdpSocket baseUdpSocket = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FormatViewUtils.formatData(tvResult, msg.obj.toString());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        initView();
        setupListeners();
        setupSpinner();
    }

    private void initView() {
        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etSendContent = findViewById(R.id.etSendContent);
        btnConnect = findViewById(R.id.btnConnect);
        btnDisConnect = findViewById(R.id.btnDisConnect);
        btnSend = findViewById(R.id.btnSend);
        btnClear = findViewById(R.id.btnClear);
        tvResult = findViewById(R.id.tvResult);
        spOption = findViewById(R.id.sp_option);

        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void setupListeners() {
        btnConnect.setOnClickListener(this);
        btnDisConnect.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        spOption.setOnItemSelectedListener(this);
    }

    private void setupSpinner() {
        String[] socketList = getResources().getStringArray(R.array.net_opiton);
        ArrayAdapter<String> socketListAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, socketList);
        spOption.setAdapter(socketListAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                if (selectOption == TCP_OPTION) {
                    startTcpConnect();
                } else if (selectOption == UDP_OPTION) {
                    startUdpConnect();
                }
                break;
            case R.id.btnDisConnect:
                if (selectOption == TCP_OPTION && baseTcpSocket != null) {
                    baseTcpSocket.close();
                } else if (selectOption == UDP_OPTION && baseUdpSocket != null) {
                    baseUdpSocket.close();
                }
                break;
            case R.id.btnSend:
                if (selectOption == TCP_OPTION && baseTcpSocket != null) {
                    baseTcpSocket.send(etSendContent.getText().toString().getBytes());
                } else if (selectOption == UDP_OPTION && baseUdpSocket != null) {
                    baseUdpSocket.send(etSendContent.getText().toString().getBytes());
                }
                break;
            case R.id.btnClear:
                tvResult.setText("");
                tvResult.scrollTo(0, 0);
                break;
        }
    }

    private void startUdpConnect() {
        baseUdpSocket = new BaseUdpSocket(etIp.getText().toString(), Integer.parseInt(etPort.getText().toString()), 0);
        baseUdpSocket.setSocketListener(new ISocketListener() {
            @Override
            public void recv(byte[] data, int offset, int size) {
                Message message = handler.obtainMessage();
                message.obj = "\n\r读到数据:" + Arrays.toString(data);
                handler.sendMessage(message);
            }

            @Override
            public void writeSuccess(byte[] data) {
                Message message = handler.obtainMessage();
                message.obj = "\n\r写入数据:" + Arrays.toString(data);
                handler.sendMessage(message);
            }

            @Override
            public void connSuccess() {
                Message message = handler.obtainMessage();
                message.obj = "\n\r连接成功";
                handler.sendMessage(message);
            }

            @Override
            public void connFaild(Throwable t) {
                KLog.d(TAG, "errMeg: " + t.getMessage());
                Message message = handler.obtainMessage();
                message.obj = "\n\r连接异常";
                handler.sendMessage(message);
            }

            @Override
            public void connClose() {
                KLog.d(TAG, "The connection is disconnect");
                Message message = handler.obtainMessage();
                message.obj = "\n\r关闭连接";
                handler.sendMessage(message);
            }
        });
        baseUdpSocket.connect(this);
    }

    private void startTcpConnect() {
        baseTcpSocket = new BaseTcpSocket(etIp.getText().toString(), Integer.parseInt(etPort.getText().toString()), 5000);
        baseTcpSocket.setSocketListener(new ISocketListener() {
            @Override
            public void recv(byte[] data, int offset, int size) {
                KLog.d(TAG, "recv:>=" + Arrays.toString(data));
                Message message = handler.obtainMessage();
                try {
                    message.obj = "\n\r读到数据:" + new String(data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(message);
            }

            @Override
            public void writeSuccess(byte[] data) {
                Message message = handler.obtainMessage();
                message.obj = "\n\r写入数据:" + Arrays.toString(data);
                handler.sendMessage(message);
            }

            @Override
            public void connSuccess() {
                Message message = handler.obtainMessage();
                message.obj = "\n\r连接成功";
                handler.sendMessage(message);
            }

            @Override
            public void connFaild(Throwable t) {
                Message message = handler.obtainMessage();
                message.obj = "\n\r连接异常";
                handler.sendMessage(message);
            }

            @Override
            public void connClose() {
                KLog.d(TAG, "The connection is disconnect");
                Message message = handler.obtainMessage();
                message.obj = "\n\r关闭连接";
                handler.sendMessage(message);
            }
        });
        baseTcpSocket.connect(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectOption = position;
        Message message = handler.obtainMessage();
        message.obj = "\n\r连接类型:" + (selectOption == 0 ? "TCP" : "UDP");
        handler.sendMessage(message);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}