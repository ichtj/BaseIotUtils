package com.ichtj.basetools.reboot;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.SPUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppManager;
import com.ichtj.basetools.util.PACKAGES;

@Route(path = PACKAGES.BASE+"reboot")
public class RebootAty extends BaseActivity {
    EditText etCycle;
    TextView tvRebootCount;
    private RebootCustomService service = null;

    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot);
        etCycle = findViewById(R.id.etCycle);
        tvRebootCount = findViewById(R.id.tvRebootCount);
        int rebootCount= SPUtils.getInt("rebootCount",0);
        tvRebootCount.setText("已重启"+rebootCount+"次");
        Intent intent = new Intent(this, RebootCustomService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        AppManager.finishActivity(StartPageAty.class);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBound = true;
            RebootCustomService.TestBinder myBinder = (RebootCustomService.TestBinder) binder;
            service = myBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.i("DemoLog", "ActivityA onServiceDisconnected");
        }
    };

    public void setCycleClick(View view) {
        if(isBound){
            service.stopCycle();
            String cycle = etCycle.getText().toString().trim();
            SPUtils.putInt("timeCycle",Integer.parseInt(cycle));
            service.startCycle();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}