package com.wave_chtj.example;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.launcher.ARouter;
import com.wave_chtj.example.base.BaseActivity;
import com.wave_chtj.example.install.InstallAPkAty;
import com.wave_chtj.example.network.NetMonitorAty;
import com.wave_chtj.example.network.NetRecordAty;
import com.wave_chtj.example.reboot.RebootAty;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.socket.SocketAty;
import com.wave_chtj.example.test.TestAty;
import com.wave_chtj.example.util.PACKAGES;

/**
 * Create on 2019/10/16
 * author chtj
 * desc $ 启动页
 */
public class StartPageAty extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().build(PACKAGES.BASE+BuildConfig.APP_CHOOSE).navigation();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
