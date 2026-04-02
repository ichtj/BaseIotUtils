package com.ichtj.basetools.bgservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chtj.socket.BaseTcpSocket;
import com.chtj.socket.ISocketListener;

import java.util.Arrays;

public class MyBgService extends Service {
    private static final String TAG = "MyBgService";
    int count = 0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
        BaseTcpSocket baseTcpSocket = new BaseTcpSocket("192.168.1.150", 8234, 5000);
        baseTcpSocket.connect(this);
        baseTcpSocket.setSocketListener(new ISocketListener() {
            @Override
            public void recv(byte[] data, int offset, int size) {
                Log.d(TAG, "recv: "+ Arrays.toString(data)+" size:"+size+" offset:"+offset);
            }

            @Override
            public void writeSuccess(byte[] data) {
                Log.d(TAG, "writeSuccess: "+ Arrays.toString(data));
            }

            @Override
            public void connSuccess() {
                Log.d(TAG, "connSuccess: ");
            }

            @Override
            public void connFaild(Throwable t) {
                Log.d(TAG, "connFaild: "+t.getMessage());
                baseTcpSocket.connect(MyBgService.this);
            }

            @Override
            public void connClose() {
                Log.d(TAG, "connClose: ");
            }
        });
        //每隔5秒定时发送数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(5000);
                        baseTcpSocket.send(("hello_"+count).getBytes());
                        Log.d(TAG, "run: send hello_"+count);
                        count++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
