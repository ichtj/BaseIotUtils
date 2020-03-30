/*
 * Copyright 2018 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.face_chtj.base_iotutils.screen_adapta.activitylifecycle;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

/**
 * 屏幕适配逻辑策略类
 */
public interface AutoAdaptStrategy {

    /**
     * 开始执行屏幕适配逻辑
     *
     * @param target   需要屏幕适配的对象 (可能是 {@link Activity} 或者 {@link Fragment})
     * @param activity 需要拿到当前的 {@link Activity} 才能修改 {@link DisplayMetrics#density}
     */
    void applyAdapt(Object target, Activity activity);
}
