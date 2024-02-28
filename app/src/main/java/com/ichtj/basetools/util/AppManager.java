package com.ichtj.basetools.util;

import android.app.Activity;

import java.util.Stack;

/**
 * Create on 2019/10/12
 * author chtj
 * desc Activity管理类：用于管理Activity和退出程序
 */
public class AppManager {
    private Stack<Activity> activityStack;

    private static AppManager instance;

    private AppManager() {
    }

    /**
     * 单一实例
     */
    private static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public static void addActivity(Activity activity) {
        if (getAppManager().activityStack == null) {
            getAppManager().activityStack = new Stack();
        }
        getAppManager().activityStack.add(activity);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        if(getAppManager().activityStack!=null&&getAppManager().activityStack.size()>0){
            Activity activity = getAppManager().activityStack.lastElement();
            return activity;
        }else{
            return null;
        }
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        if(getAppManager().activityStack!=null&&getAppManager().activityStack.size()>0){
            Activity activity = getAppManager().activityStack.lastElement();
            finishActivity(activity);
        }
    }

    /**
     * 结束指定的Activity
     */
    public static void finishActivity(Activity activity) {
        if (activity != null) {
            if(getAppManager().activityStack.size()>0){
                getAppManager().activityStack.remove(activity);
            }
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public static void finishActivity(Class cls) {
        if(getAppManager().activityStack!=null&&getAppManager().activityStack.size()>0){
            for (Activity activity : getAppManager().activityStack) {
                if (activity.getClass().equals(cls)) {
                    finishActivity(activity);
                    break;
                }
            }
        }

    }

    /**
     * 结束所有Activity
     */
    public static void finishAllActivity() {
        if(getAppManager().activityStack!=null&&getAppManager().activityStack.size()>0){
            for (int i = 0; i < getAppManager().activityStack.size(); i++) {
                if (null != getAppManager().activityStack.get(i)) {
                    getAppManager().activityStack.get(i).finish();
                }
            }
            getAppManager().activityStack.clear();
        }
    }

    /**
     * 退出应用程序
     */
    public static void AppExit() {
        try {
            finishAllActivity();
            /*ActivityManager activityMgr = (ActivityManager) BaseIotUtils.getContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(BaseIotUtils.getContext().getPackageName());*/
            // 退出进程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
        }
    }
}
