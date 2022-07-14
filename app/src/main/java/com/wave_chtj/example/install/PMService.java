package com.wave_chtj.example.install;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class PMService extends Service {

    private PMServe pmServe = new PMServe();

    public PMService() {
    }

    private PMBinder pmBinder = new PMBinder();

    public class PMBinder extends Binder {

        public void installApp(Context context, String pkgName, String path) {
            pmServe.installPackageByJavaReflect(context, pkgName, path);
        }

        public void deleteApp(Context context, String pkgName) {
            pmServe.deletePackage(context, pkgName);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return pmBinder;
    }
}


