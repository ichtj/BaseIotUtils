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

import com.face_chtj.base_iotutils.DataConvertUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.serialport.SerialPort;
import com.face_chtj.base_iotutils.serialport.SerialPortFinder;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.customizeview.TopTitleView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SerialPortAty extends BaseActivity {
    private static final String TAG = "SerialPortAty";
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
    @BindView(R.id.ttView)
    TopTitleView ttView;
    private SerialPort port = null;//串口控制
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
                try {
                    String com = spCom.getSelectedItem().toString();
                    int baudrate = Integer.parseInt(spBurate.getSelectedItem().toString());
                    port = new SerialPort(new File(com), baudrate, 0);
                    KLog.d(TAG, "serialport param com=" + com + ",baudrate=" + baudrate);
                    ToastUtils.success("开启串口成功！");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "errMeg:" + e.getMessage());
                    ToastUtils.error("开启串口失败,请查看日志！");
                }
                break;
            case R.id.btn_test_send://发送命令
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            //这里只是一个示例
                            //这里时多个命令发送
                            //单个命令发送
                            String hexComm = etCommand.getText().toString().trim();
                            byte[] comm = DataConvertUtils.decodeHexString(hexComm);
                            port.write(comm);
                            //等待400毫秒
                            Thread.sleep(400);
                            int readSize = port.getInputStream().available();
                            if (readSize > 0) {
                                byte[] bytes = new byte[readSize];
                                port.read(bytes, bytes.length);
                                Message message = handler.obtainMessage();
                                message.obj = DataConvertUtils.encodeHexString(bytes);
                                handler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                }.start();

                break;
            case R.id.btn_close:
                if(port!=null){
                    port.close();
                }
                ToastUtils.info("串口关闭！");
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
            KLog.d(TAG, "msg.obj=" + msg.obj.toString());
            tvResult.append("\n\r" + msg.obj.toString());
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (port != null) {
            port.close();
        }
    }

}
