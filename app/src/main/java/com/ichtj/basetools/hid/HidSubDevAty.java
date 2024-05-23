package com.ichtj.basetools.hid;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.ObjectUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.TranscodingUtils;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.PACKAGES;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Route(path = PACKAGES.BASE + "hidSubDev")
public class HidSubDevAty extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = HidSubDevAty.class.getSimpleName();
    TextView tvResult;
    EditText etData;
    RadioButton rbHex;
    RadioButton rbAscii;
    String dev = "/dev/hidg0";
    String LOG_PATH = "/sdcard/hid_test.log";
    String pattern="yyyyMMddHHmmss";
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hid_sub_dev);
        rbAscii = findViewById(R.id.rbAscii);
        rbHex = findViewById(R.id.rbHex);
        etData = findViewById(R.id.etData);
        tvResult = findViewById(R.id.tvResult);
        FormatViewUtils.setMovementMethod(tvResult);
    }

    /**
     * 初始化hid通讯
     */
    public void connectClick(View view) {
        HidTools.init(dev, new IHidCallback() {
            @Override
            public void receive(byte[] data) {
                try {
                    FileUtils.writeFileData(LOG_PATH, "read:["+ TimeUtils.getTodayDateHms(TimeUtils.DATE_FORMAT_MERGE) +"]：" + Arrays.toString(data) + "\r\n", false);
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            StringBuffer str = getLogContent(LOG_PATH, 99);
                            handler.sendMessage(handler.obtainMessage(0x00, str));
                        }
                    });
                } catch (Throwable throwable) {
                    FileUtils.writeFileData(LOG_PATH, "HidReadErr：" + throwable.getMessage() + "\r\n", false);
                }
            }

            @Override
            public void connect(boolean connected) {
                FormatViewUtils.formatData(tvResult, "Hid连接：" +connected,pattern);
            }
        });
    }

    /**
     * 注销
     */
    public void disConnectClick(View view) {
        HidTools.stopMonitoring();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (!ObjectUtils.isEmpty(msg.obj)) {
                tvResult.setText(msg.obj.toString());
                Layout layout = tvResult.getLayout();
                if (layout != null) {
                    int scrollAmount = layout.getLineTop(tvResult.getLineCount()) - tvResult.getHeight();
                    tvResult.scrollTo(0, scrollAmount > 0 ? scrollAmount : 0);
                }
            }
        }
    };

    public StringBuffer getLogContent(String logFilePath, int nowProgress) {
        StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logFilePath));
            int linesToSkip = (int) (nowProgress / 100f * getFileLines(logFilePath));
            for (int i = 0; i < linesToSkip; i++) {
                reader.readLine(); // 跳过已读的行
            }
            String line;
            while ((line = reader.readLine()) != null) {
                linesToSkip += 1;
                content.append("[" + linesToSkip + "]：").append(line).append("\r\n");
                if (content.length() > 10000) { // 加载过多内容时暂停加载
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    // 获取文件总行数
    public static int getFileLines(String filePath) {
        BufferedReader reader = null;
        int lines = 0;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }


    public void sendCmds(View view) {
        String dataStr = etData.getText().toString();
        boolean isHex = rbHex.isChecked();
        byte[] data = isHex ? TranscodingUtils.decodeHexString(dataStr) : dataStr.getBytes();
        Log.d(TAG, "sendCmds: " + Arrays.toString(data));
        HidTools.sendCmds(dev, data);
        FormatViewUtils.formatData(tvResult, "HidWrite>>" + dataStr,pattern);
    }

    public void clearClick(View view) {
        tvResult.scrollTo(0, 0);
        tvResult.setText("");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            boolean isHex = buttonView.getId() == rbHex.getId();
            etData.setText(isHex ? TranscodingUtils.asciiToHex(etData.getText().toString()) : TranscodingUtils.asciiToHex(etData.getText().toString()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HidTools.stopMonitoring();
    }
}
