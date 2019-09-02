package top.keepempty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.serialport.SerialPort;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import top.keepempty.screenadaptation.ScreenActivity;
import top.keepempty.serialport.SerialPortActivity;
import top.keepempty.serialportnormal.SerialPortNormalAty;
import top.keepempty.servicekeep.KeepServiceActivity;

/**
 * 功能选择
 */
public class SwitchAty extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        Log.e("TAG>>>","Test");
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSeialPort://串口相关 封装了常用的方法及实现
                startActivity(new Intent(SwitchAty.this, SerialPortActivity.class));
                break;
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
