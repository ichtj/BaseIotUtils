package com.wave_chtj.example;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.wave_chtj.example.screenadaptation.ScreenActivity;
import com.wave_chtj.example.serialportnormal.SerialPortNormalAty;
import com.wave_chtj.example.keepservice.KeepServiceActivity;

/**
 * 功能选择
 */
public class SwitchAty extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "SwitchAty";
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        mContext=SwitchAty.this;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSeialPortNormal://串口测试
                startActivity(new Intent(mContext, SerialPortNormalAty.class));
                break;
            case R.id.btnServiceKeep://后台Service
                startActivity(new Intent(mContext, KeepServiceActivity.class));
                break;
            case R.id.btnScreen://屏幕适配相关
                startActivity(new Intent(mContext, ScreenActivity.class));
                break;
            case R.id.btnAppInfo://App相关
                break;
        }
    }

}
