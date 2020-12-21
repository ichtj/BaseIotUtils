package com.chtj.framework.task;

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

import com.chtj.framework.FBaseTools;
import com.chtj.framework.FCommonTools;
import com.chtj.framework.FKeepLiveTools;
import com.chtj.framework.entity.KeepLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * 处理任务，广播等基础事件
 */
public class FBaseService extends Service {
    private static final String TAG = "BaseService";
    public static boolean isStopTask=false;
    public static Disposable sDisposable;
    private NetworkReceiver networkReceiver = null;

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
        /**开启广播监听网络**/
        if (FBaseTools.instance().getOpenNetWorkRecord()) {
            networkReceiver = new NetworkReceiver();
            networkReceiver.registerReceiver();
        }

        sDisposable = Observable
                .interval(0, 15, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "run: close task");
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long count) throws Exception {
                        Log.d(TAG, "accept: ");
                        if (isStopTask) {
                            if (sDisposable != null && !sDisposable.isDisposed()) {
                                sDisposable.dispose();
                            }
                        }
                        Gson gson=new Gson();
                        String readJson = FCommonTools.readFileData(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME);
                        Log.d(TAG,"accept:>readJson="+readJson);
                        List<KeepLiveData> keepLiveDataList = gson.fromJson(readJson, new TypeToken<List<KeepLiveData>>() {
                        }.getType());
                        if (keepLiveDataList != null && keepLiveDataList.size() > 0) {
                            for (int i = 0; i < keepLiveDataList.size(); i++) {
                                Log.d(TAG, "accept: readData="+ keepLiveDataList.get(i).toString());
                                if (keepLiveDataList.get(i).getType().equals(FKeepLiveTools.TYPE_ACTIVITY)) {
                                    openApk(keepLiveDataList.get(i).getPackageName());
                                } else if (keepLiveDataList.get(i).getType().equals(FKeepLiveTools.TYPE_SERVICE)) {
                                    if(keepLiveDataList.get(i).getServiceName()!=null&&!keepLiveDataList.get(i).getServiceName().equals("")){
                                        openService(keepLiveDataList.get(i).getPackageName(), keepLiveDataList.get(i).getServiceName());
                                    }else{
                                        Log.d(TAG, "accept: service open err");
                                    }
                                }
                            }
                        }else{
                            Log.d(TAG, "accept: list == null");
                        }
                    }
                });
    }

    /**
     * 判断该包名的应用是否存在
     *
     * @param packageName
     * @return
     */
    private static boolean existPackageName(String packageName) {
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
    public void openApk(String packName) {
        if (existPackageName(packName)) {
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
     * @param servicePackageName service包名路径
     */
    public void openService(String packName, String servicePackageName) {
        try {
            Log.d(TAG, "launch this service...  packagename=" + packName);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packName, servicePackageName));
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    /**
     * 获取该包名中的主界面
     *
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
        Log.d(TAG, "onDestroy: ");
        isStopTask = true;
        networkReceiver.unRegisterReceiver();
        if (sDisposable != null && !sDisposable.isDisposed()) {
            sDisposable.dispose();
        }
    }
}
