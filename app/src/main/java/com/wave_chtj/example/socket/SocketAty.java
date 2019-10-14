package com.wave_chtj.example.socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.chtj.base_iotutils.KLog;
import com.chtj.socket.BaseTcpSocket;
import com.chtj.socket.BaseUdpSocket;
import com.chtj.socket.ISocketListener;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Create on 2019/10/12
 * author chtj
 * desc $
 */
public class SocketAty extends BaseActivity {
    public static final String TAG = "SocketAty";
    @BindView(R.id.etIp)
    EditText etIp;
    @BindView(R.id.etPort)
    EditText etPort;
    @BindView(R.id.btnConnect)
    Button btnConnect;
    @BindView(R.id.btnDisConnect)
    Button btnDisConnect;
    @BindView(R.id.etSendContent)
    EditText etSendContent;
    @BindView(R.id.btnSend)
    Button btnSend;
    @BindView(R.id.tvResult)
    TextView tvResult;
    @BindView(R.id.btnClear)
    Button btnClear;
    @BindView(R.id.sp_option)
    Spinner spOption;
    //当前选择的是TCP 或者是 UDP
    int selectOpiton=TCP_OPTION;
    private static final int TCP_OPTION=0;//TCP
    private static final int UDP_OPTION=1;//UDP
    //TCP
    BaseTcpSocket baseTcpSocket;
    //UDP
    BaseUdpSocket baseUdpSocket;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvResult.append(msg.obj.toString());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        ButterKnife.bind(this);
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        spOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectOpiton=position;
                Message message = handler.obtainMessage();
                message.obj = "\n\r连接类型:" +(selectOpiton==0?"TCP":"UDP");
                handler.sendMessage(message);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick({R.id.btnConnect, R.id.btnDisConnect, R.id.btnSend, R.id.btnClear})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnConnect://开启连接
                if(selectOpiton==TCP_OPTION){
                    baseTcpSocket = new BaseTcpSocket(etIp.getText().toString(), Integer.parseInt(etPort.getText().toString()), 5000);
                    baseTcpSocket.setSocketListener(new ISocketListener() {
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
                }else if(selectOpiton==UDP_OPTION){
                    baseUdpSocket=new BaseUdpSocket(etIp.getText().toString(), Integer.parseInt(etPort.getText().toString()),5000);
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

                break;
            case R.id.btnDisConnect://关闭连接
                if(selectOpiton==TCP_OPTION){
                    if (baseTcpSocket != null) {
                        baseTcpSocket.close();
                    }
                }else if(selectOpiton==UDP_OPTION){
                    if (baseUdpSocket != null) {
                        baseUdpSocket.close();
                    }
                }
                break;
            case R.id.btnSend://发送数据
                if(selectOpiton==TCP_OPTION){
                    if (baseTcpSocket != null) {
                        baseTcpSocket.send(etSendContent.getText().toString().getBytes());
                    }
                }else if(selectOpiton==UDP_OPTION){
                    if (baseUdpSocket != null) {
                        baseUdpSocket.send(etSendContent.getText().toString().getBytes());
                    }
                }
                break;
            case R.id.btnClear:
                tvResult.setText("");
                break;
        }
    }
}
