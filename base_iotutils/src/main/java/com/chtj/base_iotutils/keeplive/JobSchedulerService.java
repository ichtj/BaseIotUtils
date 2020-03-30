package com.chtj.base_iotutils.keeplive;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import com.chtj.base_iotutils.KLog;

/**
 * Android 5.0+ 使用的 JobScheduler.
 * 运行在 :watch 子进程中.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {
    public static final String TAG = "JobSchedulerService";

    @Override
    public boolean onStartJob(JobParameters params) {
        KLog.d(TAG, "BaseIotUtils.sInitialized=" + BaseIotUtils.sInitialized);
        if (!BaseIotUtils.sInitialized) return false;
        BaseIotUtils.startServiceMayBind(BaseIotUtils.sServiceClass);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
