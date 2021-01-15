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

package com.chtj.framework.keep;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.util.Log;

import com.chtj.framework.keep.impl.IDaemonStrategy;
import com.chtj.framework.keep.receiver.Receiver1;
import com.chtj.framework.keep.receiver.Receiver2;
import com.chtj.framework.keep.service.FKeepAliveService;
import com.chtj.framework.keep.service.GuardService;
import com.chtj.framework.keep.service.GuardService2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FBaseDaemon {

    private static final String TAG = "FBaseDaemon";

    private DaemonConfigurations mConfigurations;

    private FBaseDaemon(DaemonConfigurations configurations) {
        this.mConfigurations = configurations;
    }

    public static void init(Context base) {
        DaemonConfigurations configurations;
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 24) {
            //android 7.0以上
            configurations = new DaemonConfigurations(
                    new DaemonConfigurations.LeoricConfig(
                            base.getPackageName() + ":resident",
                            FKeepAliveService.class.getCanonicalName()),
                    new DaemonConfigurations.LeoricConfig(
                            "android.media",
                            GuardService.class.getCanonicalName()));
        } else {
            //android 7.0 以下
            configurations = new DaemonConfigurations(
                    new DaemonConfigurations.LeoricConfig(
                            base.getPackageName() + ":resident",
                            FKeepAliveService.class.getCanonicalName(),
                            Receiver1.class.getCanonicalName()),
                    new DaemonConfigurations.LeoricConfig(
                            base.getPackageName() + ":resident2",
                            GuardService2.class.getCanonicalName(),
                            Receiver2.class.getCanonicalName()));
        }
        Log.d(TAG, "init: configurations="+configurations.toString());
        Reflection.unseal(base);
        FBaseDaemon client = new FBaseDaemon(configurations);
        client.initDaemon(base);
    }


    private final String DAEMON_PERMITTING_SP_FILENAME = "d_permit";
    private final String DAEMON_PERMITTING_SP_KEY = "permitted";


    private BufferedReader mBufferedReader;

    private void initDaemon(Context base) {
        if (!isDaemonPermitting(base) || mConfigurations == null) {
            return;
        }
        String processName = getProcessName();
        String packageName = base.getPackageName();
        if (processName.startsWith(mConfigurations.PERSISTENT_CONFIG.processName)) {
            IDaemonStrategy.Fetcher.fetchStrategy().onPersistentCreate(base, mConfigurations);
        } else if (processName.startsWith(mConfigurations.DAEMON_ASSISTANT_CONFIG.processName)) {
            IDaemonStrategy.Fetcher.fetchStrategy().onDaemonAssistantCreate(base, mConfigurations);
        } else if (processName.startsWith(packageName)) {
            IDaemonStrategy.Fetcher.fetchStrategy().onInit(base);
        }
        releaseIO();
    }


    private String getProcessName() {
        try {
            int sdk = Build.VERSION.SDK_INT;
            File file = null;
            if (sdk >= 24) {
                //android 7.0以上
                file = new File("/proc/self/cmdline");
            } else {
                //android 7.0以下
                file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            }
            mBufferedReader = new BufferedReader(new FileReader(file));
            return mBufferedReader.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void releaseIO() {
        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBufferedReader = null;
        }
    }

    private boolean isDaemonPermitting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(DAEMON_PERMITTING_SP_KEY, true);
    }

    protected boolean setDaemonPermiiting(Context context, boolean isPermitting) {
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(DAEMON_PERMITTING_SP_KEY, isPermitting);
        return editor.commit();
    }

}
