package com.ichtj.basetools.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.LogView;
import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppManager;
import com.ichtj.basetools.util.PACKAGES;
import java.util.ArrayList;
import java.util.List;

@Route(path = PACKAGES.BASE + "testDemo")
public class TestAty extends BaseActivity {
    private static final String TAG = TestAty.class.getSimpleName();
    int count=0;
    TextView tvCount;
    LogView logView;
    Handler handler=new Handler (  );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        AppManager.finishActivity(StartPageAty.class);
        logView = findViewById(R.id.logView);
        tvCount = findViewById(R.id.tvCount);

        // 设置初始日志
        List<String> defaults = new ArrayList<>();
        defaults.add("系统启动...");
        defaults.add("网络连接中...");
        defaults.add("初始化完成。");
        logView.setInitialLogs(defaults);



        // 修改配置（运行时）
        logView.setTextColor(Color.BLACK);
        logView.setTextSize(16);
        logView.setAutoScroll(true);
        logView.setDebugLogEnabled(false);
        sendKeepAliveBroadcast(this,"com.android.settings",false);
    }


    public void onStartClick(View view){
        handler.postDelayed (runnable,1000);
    }
    Runnable runnable=new Runnable ( ) {
        @Override
        public void run() {
            // 后续追加日志
            logView.appendLog("收到服务器心跳包>>>"+count);
            Log.d (TAG, "run: "+count);
            count++;
            // 实时监控内存状态
            Log.d(TAG, "日志总数: " + logView.getLogCount()+",缓冲区: " + logView.getBufferSize()+",低内存模式: " + logView.isLowMemoryMode());
            tvCount.setText ("count: "+count);
            handler.postDelayed (this,10);
        }
    };

    public void onStopClick(View view){
        handler.removeCallbacks (runnable);
    }


    public static void sendKeepAliveBroadcast(Context context, String packageName, boolean enable) {
        Intent intent = new Intent("com.android.control.keepalive");
        intent.putExtra("packageName", packageName);
        intent.putExtra("enable", enable);//enable true开启 false关闭
        context.sendBroadcast(intent);
    }
}
