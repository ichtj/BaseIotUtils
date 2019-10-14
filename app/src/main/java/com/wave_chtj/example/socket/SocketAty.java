package com.wave_chtj.example.socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chtj.base_iotutils.KLog;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;
import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

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
    @BindView(R.id.port)
    EditText port;
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
    ConnectionInfo info = null;
    IConnectionManager manager = null;

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

        //Connection parameter Settings (IP, port number), which is also a unique identifier for a connection.
        info = new ConnectionInfo("192.168.1.122", 10246);
        //Call OkSocket open() the channel for this connection, and the physical connections will be connected.
        manager = OkSocket.open(info);
        manager.registerReceiver(new SocketActionAdapter() {
            @Override
            public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
                Message message = handler.obtainMessage();
                message.obj = "\n\r连接成功";
                handler.sendMessage(message);
            }

            @Override
            public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
                super.onSocketConnectionFailed(info, action, e);
                Message message = handler.obtainMessage();
                message.obj = "\n\r连接异常";
                handler.sendMessage(message);
            }

            @Override
            public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
                super.onSocketDisconnection(info, action, e);
                KLog.d(TAG, "The connection is disconnect");
                Message message = handler.obtainMessage();
                message.obj = "\n\r断开连接";
                handler.sendMessage(message);
            }
            //Follow the above rules, this callback can be normal received the data returned from the server,
            //the data in the OriginalData, for byte [] array,
            //the array data already processed byte sequence problem,
            //can be used in the ByteBuffer directly
            @Override
            public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
                super.onSocketReadResponse(info, action, data);
                Message message = handler.obtainMessage();
                message.obj = "\n\r读到数据:"+Arrays.toString(data.getHeadBytes())+Arrays.toString(data.getBodyBytes());
                handler.sendMessage(message);
            }

            @Override
            public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
                super.onSocketWriteResponse(info, action, data);
                Message message = handler.obtainMessage();
                message.obj = "\n\r 写入数据:"+Arrays.toString(data.parse());
                handler.sendMessage(message);
            }

            @Override
            public void onSocketIOThreadShutdown(String action, Exception e) {
                super.onSocketIOThreadShutdown(action, e);
                KLog.e(e.getMessage());
            }

            @Override
            public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
                super.onPulseSend(info, data);

            }
        });
    }

    @OnClick({R.id.btnConnect, R.id.btnDisConnect, R.id.btnSend,R.id.btnClear})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                manager.connect();
                break;
            case R.id.btnDisConnect:
                manager.disconnect();
                break;
            case R.id.btnSend:
                manager.send(new TestSendData(etSendContent.getText().toString()));
                break;
            case R.id.btnClear:
                tvResult.setText("");
                break;
        }
    }
}
