package com.chtj.framework;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class FBaseService extends Service {
    private static final String TAG = "BaseService";
    public static Disposable sDisposable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sDisposable = Observable
                .interval(0, 15, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long count) throws Exception {

                    }
                });


    }

    /**
     * 判断该包名的应用是否存在
     * @param packageName
     * @return
     */
    private static boolean isAvilible(String packageName) {
        PackageManager packageManager = FBaseTools.getContext().getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 启动第三方apk
     * <p>
     * 如果已经启动apk，则直接将apk从后台调到前台运行（类似home键之后再点击apk图标启动），如果未启动apk，则重新启动
     */
    public void launchAPK(String packName) {
        if (isAvilible(packName)) {
            Intent intent = getAppOpenIntentByPackageName(packName);
            FBaseTools.getContext().startActivity(intent);
            Log.d(TAG, "launch this apk...  packagename=" + packName);
        } else {
            Log.d(TAG, packName + " not find this packageName");
        }
    }


    /**
     * 启用其他应用中的Service
     *
     * @param packName       包名
     * @param serviceService
     */
    public void launchService(String packName, String serviceService) {
        try {
            Log.d(TAG, "launch this service...  packagename=" + packName);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packName, serviceService));
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    /**
     * 获取该包名中的主界面
     * @param packageName
     * @return
     */
    private static Intent getAppOpenIntentByPackageName(String packageName) {
        String mainAct = null;
        PackageManager pkgMag = FBaseTools.getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
