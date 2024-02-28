package com.wave_chtj.example.hid;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.face_chtj.base_iotutils.TranscodingUtils;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.util.Arrays;

public class HidAty extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = HidAty.class.getSimpleName();
    TextView tvResult;
    EditText etData;
    RadioButton rbHex;
    RadioButton rbAscii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hid);
        rbAscii = findViewById(R.id.rbAscii);
        rbHex = findViewById(R.id.rbHex);
        etData = findViewById(R.id.etData);
        tvResult = findViewById(R.id.tvResult);
        FormatViewUtils.setMovementMethod(tvResult);
        HidTools.startMonitoring(new IHidCallback() {
            @Override
            public void receive(byte[] data) {
                FormatViewUtils.formatData(tvResult, "HidRead>>" + TranscodingUtils.encodeHexString(data));
            }
        });
    }

    /*new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x00}*/
    public void sendCmds(View view) {
        String dataStr = etData.getText().toString();
        boolean isHex = rbHex.isChecked();
        byte[] data = isHex ? TranscodingUtils.decodeHexString(dataStr) : dataStr.getBytes();
        Log.d(TAG, "sendCmds: " + Arrays.toString(data));
        HidTools.sendCmds(data);
        FormatViewUtils.formatData(tvResult, "HidWrite>>" + dataStr);
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
}
