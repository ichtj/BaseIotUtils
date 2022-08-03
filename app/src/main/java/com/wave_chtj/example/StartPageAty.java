package com.wave_chtj.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.KLog;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.reboot.RebootAty;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.temget.TempGetAty;

/**
 * Create on 2019/10/16
 * author chtj
 * desc $ 启动页
 */
public class StartPageAty extends BaseActivity {
    private static final String TAG = StartPageAty.class.getSimpleName()+"Test";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);              //去掉TITLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);            //设为全屏
        String pkgName=getPackageName();
        KLog.d(TAG,"onCreate:>pkgName="+pkgName);
        if(pkgName.contains("serialport")){
            startActivity(new Intent(this, SerialPortAty.class));
        }else if(pkgName.contains("reboot")){
            startActivity(new Intent(this, RebootAty.class));
        }else{
            startActivity(new Intent(this, FeaturesOptionAty.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
