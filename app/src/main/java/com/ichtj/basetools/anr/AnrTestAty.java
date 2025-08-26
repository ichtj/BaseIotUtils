package com.ichtj.basetools.anr;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.crash.MyCrashService1;
import com.ichtj.basetools.crash.MyService;

public class AnrTestAty extends BaseActivity {
    private static final String TAG = AnrTestAty.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anr);
        MyReceiver receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter("com.demo.TEST_ANR");
        registerReceiver(receiver, filter);
    }

    public void inputDispatchTimeoutClick(View view){
        try {
            // 主线程阻塞，点按钮时会触发
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void broadcastReceiverClick(View view){
        Intent intent = new Intent("com.demo.TEST_ANR");
        sendBroadcast(intent);
    }


    public void serviceTimeoutClick(View view){
        startService(new Intent(this, MyService.class));
    }


    public void contentProviderTimeoutClick(View view){
        getContentResolver().query(Uri.parse("content://com.example.demo.provider/test"),
                null, null, null, null);
    }

    public void whileClick(View view ){
        while (true) { }  // 死循环
    }



    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            try {
                Thread.sleep(15000); // 模拟耗时 > 10s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyProvider extends ContentProvider {
        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {
            try {
                Thread.sleep(20000); // 超过 20s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            return null;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
            return null;
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }
    }



}