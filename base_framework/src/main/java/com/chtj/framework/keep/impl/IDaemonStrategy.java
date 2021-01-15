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
package com.chtj.framework.keep.impl;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.chtj.framework.keep.DaemonConfigurations;
import com.chtj.framework.keep.strategy.DaemonStrategy21;
import com.chtj.framework.keep.strategy.DaemonStrategy22;
import com.chtj.framework.keep.strategy.DaemonStrategy23;
import com.chtj.framework.keep.strategy.DaemonStrategy24;
import com.chtj.framework.keep.strategy.DaemonStrategyUnder21;
import com.chtj.framework.keep.strategy.DaemonStrategyXiaomi;

public interface IDaemonStrategy {
    /**
     * Initialization some files or other when 1st time
     */
    boolean onInit(Context context);

    /**
     * when Persistent processName create
     */
    void onPersistentCreate(Context context, DaemonConfigurations configs);

    /**
     * when DaemonAssistant processName create
     */
    void onDaemonAssistantCreate(Context context, DaemonConfigurations configs);

    /**
     * when watches the processName dead which it watched
     */
    void onDaemonDead();


    class Fetcher {
    	private static final String TAG = "IDaemonStrategy_Fetcher";

        private static volatile IDaemonStrategy mDaemonStrategy;

        /**
         * fetch the strategy for this device
         *
         * @return the daemon strategy for this device
         */
        public static IDaemonStrategy fetchStrategy() {
            if (mDaemonStrategy != null) {
                return mDaemonStrategy;
            }
            int sdk = Build.VERSION.SDK_INT;
            if (sdk >= 24) {
                mDaemonStrategy = new DaemonStrategy24();
            } else {
                switch (sdk) {
                    case 23:
                        mDaemonStrategy = new DaemonStrategy23();
                        break;

                    case 22:
                        mDaemonStrategy = new DaemonStrategy22();
                        break;

                    case 21:
                        if ("MX4 Pro".equalsIgnoreCase(Build.MODEL)) {
                            mDaemonStrategy = new DaemonStrategyUnder21();
                        } else {
                            mDaemonStrategy = new DaemonStrategy21();
                        }
                        break;

                    default:
                        if (Build.MODEL != null && Build.MODEL.toLowerCase().startsWith("mi")) {
                            mDaemonStrategy = new DaemonStrategyXiaomi();
                        } else if (Build.MODEL != null && Build.MODEL.toLowerCase().startsWith("a31")) {
                            mDaemonStrategy = new DaemonStrategy21();
                        } else {
                            mDaemonStrategy = new DaemonStrategyUnder21();
                        }
                        break;
                }
            }
            return mDaemonStrategy;
        }
    }
}
