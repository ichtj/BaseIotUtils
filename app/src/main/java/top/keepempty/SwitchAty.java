package top.keepempty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import top.keepempty.screenadaptation.ScreenActivity;
import top.keepempty.serialportnormal.SerialPortNormalAty;
import top.keepempty.servicekeep.KeepServiceActivity;

/**
 * 功能选择
 */
public class SwitchAty extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG="SwitchAty";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSeialPortNormal://串口测试 普通 无封装
                startActivity(new Intent(SwitchAty.this, SerialPortNormalAty.class));
                break;
            case R.id.btnServiceKeep://后台保活
                startActivity(new Intent(SwitchAty.this, KeepServiceActivity.class));
                break;
            case R.id.btnScreen://屏幕适配相关
                startActivity(new Intent(SwitchAty.this, ScreenActivity.class));
                break;
            case R.id.btnAppInfo://App相关
                break;
        }
    }
}
