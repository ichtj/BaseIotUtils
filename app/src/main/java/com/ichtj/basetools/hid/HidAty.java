package com.ichtj.basetools.hid;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.TranscodingUtils;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.PACKAGES;

import java.util.Arrays;
import java.util.HashMap;

@Route(path = PACKAGES.BASE + "hidTest")
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
        UsbManager manager= (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDeviceHashMap=manager.getDeviceList();
        Log.d(TAG, "onCreate: usbDeviceHashMap>>"+usbDeviceHashMap);
        tvResult.setText(usbDeviceHashMap.toString());
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
