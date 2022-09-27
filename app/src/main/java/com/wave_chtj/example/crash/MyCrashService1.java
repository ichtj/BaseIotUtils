package com.wave_chtj.example.crash;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.KLog;

import java.io.File;

public class MyCrashService1 extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        KLog.d("onCreate() >>MyCrashService1 ");
        while (true){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    File file=new File("/sdcard/test/");
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    for (int j = 0; j < 1000; j++) {
                        String newPath="/sdcard/test/"+j+"fileInfo.log";
                        FileUtils.writeFileData(newPath,"zhelsldjflksdjflkjsdlfjsd",true);
                    }
                }
            }.start();
        }
    }
}
