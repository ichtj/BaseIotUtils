package com.wave_chtj.example;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.network.NetMonitorAty;
import com.wave_chtj.example.reboot.RebootAty;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.test.TestAty;
import com.wave_chtj.example.util.SwitchUtils;

/**
 * Create on 2019/10/16
 * author chtj
 * desc $ 启动页
 */
public class StartPageAty extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);            //设为全屏
        String pkgName = getPackageName();
        if (pkgName.contains(SwitchUtils.FLAG_SERIALPORT_PKG)) {
            startAty(SerialPortAty.class);
        } else if (pkgName.contains(SwitchUtils.FLAG_REBOOT_PKG)) {
            startAty(RebootAty.class);
        } else if (pkgName.contains(SwitchUtils.FLAG_NETMONITOR_PKG)) {
            startAty(NetMonitorAty.class);
        } else if (pkgName.contains(SwitchUtils.FLAG_EXAMPLE_PKG)) {
            startAty(FeaturesOptionAty.class);
        }else if(pkgName.contains("com.zto.ztoexpresscabinet")||pkgName.contains("com.ingenious_eyes.cabinet")){
            startAty(TestAty.class);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
