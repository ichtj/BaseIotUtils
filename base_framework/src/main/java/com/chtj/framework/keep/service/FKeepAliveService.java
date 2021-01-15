/*
 * Original Copyright 2015 Mars Kwok
 * Modified work Copyright (c) 2020, weishu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chtj.framework.keep.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.chtj.framework.FCommonTools;
import com.chtj.framework.FKeepAliveTools;
import com.chtj.framework.IKeepAliveListener;
import com.chtj.framework.IKeepAliveService;
import com.chtj.framework.entity.CommonValue;
import com.chtj.framework.entity.KeepAliveData;
import com.chtj.framework.receiver.NetworkReceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * 要使用跨进程的服务 必须设置 Service
 * android:exported="true"
 * android:enabled="true"
 * 使外部可以访问
 */
public class FKeepAliveService extends Service {
    private static final String TAG = "FKeepAliveService";
    public static Disposable sDisposable;
    private NetworkReceiver networkReceiver = null;
    public static boolean isKeepAliveStatus = true;

    /**
     * 多进程之间添加保活的Activity或者Service
     */
    IKeepAliveService.Stub iKeepAliveService = new IKeepAliveService.Stub() {
        @Override
        public boolean addKeepLiveInfo(KeepAliveData info, IKeepAliveListener listener) throws RemoteException {
            Log.d(TAG,"addKeepLiveInfo:>="+info.toString());
            if (info.getType()==FKeepAliveTools.TYPE_ACTIVITY) {
                CommonValue commonValue = FKeepAliveTools.addActivity(info);
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    listener.onSuccess();
                    return true;
                } else {
                    listener.onError(commonValue.getRemarks());
                    return false;
                }
            } else {
                CommonValue commonValue = FKeepAliveTools.addService(info);
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    listener.onSuccess();
                    return true;
                } else {
                    listener.onError(commonValue.getRemarks());
                    return false;
                }
            }
        }

        @Override
        public List<KeepAliveData> getKeepLiveInfo() throws RemoteException {
            return FKeepAliveTools.getKeepLive();
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        /**开启广播监听网络**/
        networkReceiver = new NetworkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkReceiver.ANDROID_NET_CHANGE_ACTION);
        registerReceiver(networkReceiver, intentFilter);
        /**是否开启了保活服务**/
        sDisposable = Observable
                .interval(5, 15, TimeUnit.SECONDS)
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
                        //Log.d(TAG, "accept: ");
                        if (!isKeepAliveStatus && sDisposable != null && !sDisposable.isDisposed()) {
                            sDisposable.dispose();
                        }
                        Gson gson = new Gson();
                        String readJson = FCommonTools.readFileData(FCommonTools.SAVE_KEEPLIVE_PATH + FCommonTools.SAVE_KEEPLIVE_FILE_NAME);
                        //Log.d(TAG, "accept:>readJson=" + readJson);
                        List<KeepAliveData> keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
                        }.getType());
                        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
                            for (int i = 0; i < keepAliveDataList.size(); i++) {
                                //Log.d(TAG, "accept: readData=" + keepAliveDataList.get(i).toString());
                                if (keepAliveDataList.get(i).getType()==FKeepAliveTools.TYPE_ACTIVITY) {
                                    FCommonTools.openApk(FKeepAliveService.this, keepAliveDataList.get(i).getPackageName());
                                } else if (keepAliveDataList.get(i).getType()==FKeepAliveTools.TYPE_SERVICE) {
                                    if (keepAliveDataList.get(i).getServiceName() != null && !keepAliveDataList.get(i).getServiceName().equals("")) {
                                        FCommonTools.openService(FKeepAliveService.this, keepAliveDataList.get(i).getPackageName(), keepAliveDataList.get(i).getServiceName());
                                    } else {
                                        Log.d(TAG, "accept: service open err");
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "accept: list == null");
                        }
                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iKeepAliveService;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
        if (sDisposable != null && !sDisposable.isDisposed()) {
            sDisposable.dispose();
        }
    }
}
