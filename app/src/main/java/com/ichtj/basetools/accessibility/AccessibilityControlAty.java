package com.ichtj.basetools.accessibility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.DisplayUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.util.PACKAGES;

@Route(path = PACKAGES.BASE + "accessibility")
public class AccessibilityControlAty extends Activity {
    private static final String TAG = AccessibilityControlAty.class.getSimpleName();
    private static final int REQUEST_CODE_CAPTURE = 1001;
    private ScreenRecorderUtil screenRecorderUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_service);
        screenRecorderUtil = new ScreenRecorderUtil(this);
        screenRecorderUtil.requestCapturePermission(REQUEST_CODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CAPTURE && resultCode == RESULT_OK && data != null) {
            screenRecorderUtil.startRecordingFromService(data, resultCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startClick(View view){
        sendKeepAliveBroadcast(this,"com.android.settings",true);
        boolean isSucc=DisplayUtils.screenshot("/sdcard/"+ TimeUtils.getTodayDateHms("yyyyMMddHHmmss")+".png");
        Log.d(TAG, "startClick: isSucc>>"+isSucc);
    }

    public void stopClick(View view){
        sendKeepAliveBroadcast(this,"com.android.settings",false);
    }

    public static void sendKeepAliveBroadcast(Context context, String packageName, boolean enable) {
        Intent intent = new Intent("com.android.control.keepalive");
        intent.putExtra("packageName", packageName);
        intent.putExtra("enable", enable);//enable true开启 false关闭
        context.sendBroadcast(intent);
    }
}

