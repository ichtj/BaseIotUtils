package com.wave_chtj.example.serialport;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.chtj.base_iotutils.HexUtils;
import com.chtj.base_iotutils.serialport.SerialPortFinder;
import com.wave_chtj.example.R;
import com.chtj.base_iotutils.entity.ComEntity;
import com.chtj.base_iotutils.entity.HeartBeatEntity;
import com.chtj.base_iotutils.serialport.helper.OnComListener;
import com.chtj.base_iotutils.serialport.helper.SerialPortHelper;
import com.wave_chtj.example.base.BaseActivity;

public class SerialPortAty extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "SerialPortAty";
    private TextView tvResult;//返回的结果
    private EditText etCommand;//命令
    private Spinner sp_com, sp_burate;//串口列表，波特率列表
    private SerialPortHelper serialPortHelper = null;//串口控制类
    private List<String> list_serialcom = null;//串口地址
    private String[] arrays_burate;//波特率

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialport_normal);
        //初始化控件
        tvResult = findViewById(R.id.tvResult);
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        etCommand = findViewById(R.id.etCommand);
        sp_com = findViewById(R.id.sp_com);
        sp_burate = findViewById(R.id.sp_burate);
        //获取所有串口地址
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        list_serialcom = Arrays.asList(entryValues);
        //获取所有的波特率 可在R.array.burate 中手动添加需要的波特率
        arrays_burate = getResources().getStringArray(R.array.burate);
        //添加到适配器中显示
        ArrayAdapter arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list_serialcom);
        sp_com.setAdapter(arr_adapter);
        findViewById(R.id.btn_init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获得当前选择串口和波特率
                String com = sp_com.getSelectedItem().toString();
                int baudrate = Integer.parseInt(sp_burate.getSelectedItem().toString());
                //参数设置
                List<Integer> flagFilterList = new ArrayList<>();
                flagFilterList.add(FlagManager.FLAG_CHECK_UPDATE);
                //数据头(包头) 主要用于判断读取的命令是否符合协议
                List<Byte> headDataList = new ArrayList<>();
                headDataList.add((byte) 0xAA);
                headDataList.add((byte) 0x55);
                //指令标识 主要用于判断读取的命令是否符合协议
                List<Byte> instructionList = new ArrayList<>();
                instructionList.add((byte) -96);//A3 自检
                instructionList.add((byte) -95);//A2 数据写入
                instructionList.add((byte) -94);//A1 加入升级
                instructionList.add((byte) -93);//A0 检查升级

                //①未开启心跳包
                //ComEntity comEntity=new ComEntity(com,baudrate,6000,3,headDataList,3,2,6,5,instructionList,flagFilterList);
                //②心跳包参数设置 默认用某一条命令周期性的去获取设备返回的消息
                //主要判断是否连接正常
                HeartBeatEntity heartBeatEntity = new HeartBeatEntity(new byte[]{(byte) 0xAA, 0x55, 00, 0, 0x01, (byte) 0xA0, (byte) 0xBF}, FlagManager.FLAG_HEARTBEAT, 15 * 1000);
                ComEntity comEntity = new ComEntity(
                        com//串口地址
                        , baudrate//波特率
                        , 6000//超时时间
                        , 3//重试次数
                        , headDataList//数据头 用于去校验是否正确
                        , 3//data长度开始的位置 从0开始
                        , 2//data长度
                        , 6//其他位的固定长度
                        , 5//指令开始的位置 从0开始
                        , instructionList//指令集合
                        , heartBeatEntity//心跳检测参数
                        , flagFilterList//写命令时如果当前flag的命令大于两条 添加进来的不会因为第一条命令执行失败，而不向下执行
                );
                //初始化数据
                serialPortHelper = new SerialPortHelper(comEntity);
                //注册监听
                serialPortHelper.setOnComListener(new OnComListener() {
                    @Override
                    public void writeCommand(byte[] comm, int flag) {
                        String writeData = "writeCommand>>> comm=" + HexUtils.encodeHexString(comm) + ",flag=" + flag;
                        Log.e(TAG, writeData);
                        Message message = handler.obtainMessage();
                        message.obj = writeData;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void readCommand(byte[] comm, int flag) {
                        String readData = "readCommand>>> comm=" + HexUtils.encodeHexString(comm) + ",flag=" + flag;
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

                });
                //开启串口
                serialPortHelper.openSerialPort();
            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close://关闭串口
                if (serialPortHelper != null) {
                    serialPortHelper.closeSerialPort();
                }
                break;
            case R.id.btn_test_send://发送命令
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //这里只是一个示例
                        //这里时多个命令发送
                        /*List<byte[]> bytesList = new ArrayList<>();
                        for (int i = 0; i <=15; i++) {
                            byte[] bytes = new byte[]{(byte) 0xAA, 0x55, (byte) i, 0, 0x01, (byte) 0xA0};
                            byte crcNum = CrcUtils.calcCrc8(bytes);//获得校验值
                            //复制到新的数组并把校验值填写到最后一位
                            byte[] newbytes = new byte[bytes.length + 1];
                            System.arraycopy(bytes, 0, newbytes, 0, 6);
                            newbytes[newbytes.length - 1] = crcNum;//填充校验值
                            //Log.e(TAG, "检测升级>>>计算校验值后得command[" + i + "]=" + HexUtils.encodeHexString(newbytes));
                            bytesList.add(newbytes);
                        }
                        if (bytesList.size() > 0) {
                            //传进去的flag 读取|写入|超时|写入完成等回调的时候可以标识为当前
                            serialPortHelper.setWriteAfterRead(bytesList, FlagManager.FLAG_CHECK_UPDATE);
                        } else {
                            Log.e(TAG, "没有任何需要检测升级的 checkUpdate bytesList.size()=" + bytesList.size());
                        }*/
                        //单个命令发送
                        String hexComm = etCommand.getText().toString().trim();
                        byte[] comm = HexUtils.decodeHexString(hexComm);
                        serialPortHelper.setWriteAfterRead(comm, FlagManager.FLAG_CHECK_UPDATE);
                    }
                }).start();
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
        if (serialPortHelper != null) {
            serialPortHelper.closeSerialPort();
        }
    }
}
