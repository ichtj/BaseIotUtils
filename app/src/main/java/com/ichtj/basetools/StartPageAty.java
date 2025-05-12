package com.ichtj.basetools;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.PACKAGES;

import java.util.List;

public class StartPageAty extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().build(PACKAGES.BASE+BuildConfig.APP_CHOOSE).navigation();
        getData();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void getData(){
        Intent intent  = getHomeIntent();
// 获取 PackageManager 对象
        PackageManager packageManager = getPackageManager();

        // 使用 queryIntentActivities 查找能够处理此 Intent 的所有活动
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        // 打印出所有能够处理主屏幕 Intent 的应用信息
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            String activityName = resolveInfo.activityInfo.name;

            // 打印包名和活动名称
            Log.d("MainActivityF", "Package Name: " + packageName);
            Log.d("MainActivityF", "Activity Name: " + activityName);
        }
    }

    private Intent getHomeIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }
}
