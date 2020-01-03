package com.wave_chtj.example.serialport;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.chtj.base_iotutils.DataConvertUtils;
import com.chtj.base_iotutils.entity.ComEntity;
import com.chtj.base_iotutils.entity.HeartBeatEntity;
import com.chtj.base_iotutils.serialport.SerialPortFinder;
import com.chtj.base_iotutils.serialport.helper.OnComListener;
import com.chtj.base_iotutils.serialport.helper.SerialPortHelper;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SerialPortAty extends BaseActivity {
    public static final String TAG = "SerialPortAty";
    @BindView(R.id.sp_com)
    Spinner spCom;
    @BindView(R.id.sp_burate)
    Spinner spBurate;
    @BindView(R.id.btn_init)
    Button btnInit;
    @BindView(R.id.btn_close)
    Button btnClose;
    @BindView(R.id.etCommand)
    EditText etCommand;
    @BindView(R.id.btn_test_send)
    Button btnTestSend;
    @BindView(R.id.btn_clear)
    Button btnClear;
    @BindView(R.id.tvResult)
    TextView tvResult;
    //private SerialPortHelper serialPortHelper = null;//串口控制类
    private List<String> list_serialcom = null;//串口地址
    private String[] arrays_burate;//波特率

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialport_normal);
        ButterKnife.bind(this);
        //初始化控件
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        //获取所有串口地址
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        list_serialcom = Arrays.asList(entryValues);
        //获取所有的波特率 可在R.array.burate 中手动添加需要的波特率
        arrays_burate = getResources().getStringArray(R.array.burate);
        //添加到适配器中显示
        ArrayAdapter arr_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list_serialcom);
        spCom.setAdapter(arr_adapter);
    }

    @OnClick({R.id.btn_init, R.id.btn_close, R.id.btn_clear, R.id.btn_test_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_init:
                //获得当前选择串口和波特率
                String com = spCom.getSelectedItem().toString();
                int baudrate = Integer.parseInt(spBurate.getSelectedItem().toString());
                //①未开启心跳包
                //心跳包参数设置 默认用某一条命令周期性的去获取设备返回的消息
                //主要判断是否连接正常
                HeartBeatEntity heartBeatEntity = new HeartBeatEntity(new byte[]{(byte) 0x12}, FlagManager.FLAG_HEARTBEAT, 15 * 1000);
                ComEntity comEntity = new ComEntity(
                        com//串口地址
                        , baudrate//波特率
                        , 6000//超时时间
                        , 3//重试次数
                        , heartBeatEntity//心跳检测参数
                );
                //初始化数据
                SerialPortHelper.
                        getInstance().
                        setComEntity(comEntity).
                        setOnComListener(new OnComListener() {
                            @Override
                            public void writeCommand(byte[] comm, int flag) {
                                String writeData = "writeCommand>>> comm=" + DataConvertUtils.encodeHexString(comm) + ",flag=" + flag;
                                Log.e(TAG, writeData);
                                Message message = handler.obtainMessage();
                                message.obj = writeData;
                                handler.sendMessage(message);
                            }

                            @Override
                            public void readCommand(byte[] comm, int flag) {
                                String readData = "readCommand>>> comm=" + DataConvertUtils.encodeHexString(comm) + ",flag=" + flag;
                                Log.e(TAG, readData);
                                Message message = handler.obtainMessage();
                                message.obj = readData;
                                handler.sendMessage(message);
                            }

                            @Override
                            public void writeComplet(int flag) {
                                String writeSuccessful = "writeComplet>>> flag=" + flag;
                                Log.e(TAG, writeSuccessful);
                                Message message = handler.obtainMessage();
                                message.obj = writeSuccessful;
                                handler.sendMessage(message);
                            }


                            @Override
                            public void isReadTimeOut(int flag) {
                                String readTimeOut = "isReadTimeOut>>> flag=" + flag;
                                Log.e(TAG, readTimeOut);
                                Message message = handler.obtainMessage();
                                message.obj = readTimeOut;
                                handler.sendMessage(message);
                            }

                            @Override
                            public void isOpen(boolean isOpen) {
                                String comStatus = isOpen ? "isOpen>>>串口打开！" : "isOpen>>>串口关闭";
                                Log.e(TAG, comStatus);
                                Message message = handler.obtainMessage();
                                message.obj = comStatus;
                                handler.sendMessage(message);
                            }

                            @Override
                            public void comStatus(boolean isNormal) {
                                String comStatus = isNormal ? "comStatus>>>串口正常！" : "comStatus>>>串口异常";
                                Log.e(TAG, comStatus);
                                Message message = handler.obtainMessage();
                                message.obj = comStatus;
                                handler.sendMessage(message);
                            }

                        }).
                        openSerialPort();
                break;
            case R.id.btn_test_send://发送命令
                //这里只是一个示例
                //这里时多个命令发送
                //单个命令发送
                String hexComm = etCommand.getText().toString().trim();
                byte[] comm = DataConvertUtils.decodeHexString(hexComm);
                SerialPortHelper.getInstance().setWriteAfterRead(comm, FlagManager.FLAG_CHECK_UPDATE);
                break;
            case R.id.btn_close:
                SerialPortHelper.getInstance().closeSerialPort();
                break;
            case R.id.btn_clear://清除结果
                tvResult.setText("");
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvResult.append("\n\r" + msg.obj.toString());
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SerialPortHelper.getInstance().closeSerialPort();
    }

}
