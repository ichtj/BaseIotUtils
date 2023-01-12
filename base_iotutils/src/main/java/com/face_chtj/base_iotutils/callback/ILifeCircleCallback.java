package com.face_chtj.base_iotutils.callback;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created to :Activity启动生命周期的监听.
 *
 */

public interface ILifeCircleCallback {


    /**
     * 在Activity的onCreate方法之前调用
     * @param activity
     * @param savedInstanceState
     */
    void onActivityCreated(Activity activity, Bundle savedInstanceState);

    /**
     * 在Activity的onDestroyed方法之前调用
     * @param activity
     */
    void onActivityDestroyed(Activity activity);
}
