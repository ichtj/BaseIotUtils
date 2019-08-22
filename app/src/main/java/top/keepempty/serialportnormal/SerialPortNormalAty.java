package top.keepempty.serialportnormal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.serialport.SerialPort;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import top.keepempty.R;
public class SerialPortNormalAty extends AppCompatActivity implements View.OnClickListener {
    SerialPort port = null;
    //串口路径和波特率 自行获取后修改
    String LOCKCOM = "/dev/ttymxc3";//串口路径
    int BAUDRATE = 115200;//波特率
    Thread thread = null;
    TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialport_normal);
        tvResult = findViewById(R.id.tvResult);

        try {
            // port = new SerialPort(new File(LOCKCOM),BAUDRATE,8,1,'E');
            port = new SerialPort(new File(LOCKCOM), BAUDRATE, 0);
            //     port = new SerialPort(new File(LOCKCOM),BAUDRATE,0);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG>>>", "串口访问失败！");
        }
        thread = new Thread(new TestReadSerialPortThread());
        thread.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_test_scan:
                port.write(new byte[]{0x12});
                break;
        }
    }

    class TestReadSerialPortThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(20);
                    int count = port.getInputStream().available();
                    if (count > 0) {
                        byte[] tt = new byte[count];
                        if (port.getInputStream().read(tt, 0, tt.length) != -1) {
                            Log.d("TAG>>>", "port.getInputStream().read=" + Arrays.toString(tt));
                            Message message = handler.obtainMessage();
                            message.obj = Arrays.toString(tt);
                            handler.sendMessage(message);
                        } else {
                            Log.d("TAG>>>", "读取失败");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (thread != null) {
            thread.interrupt();
        }
    }
}
