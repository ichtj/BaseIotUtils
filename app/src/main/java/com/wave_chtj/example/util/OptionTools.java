package com.wave_chtj.example.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.chtj.base_framework.entity.UpgradeBean;
import com.chtj.base_framework.upgrade.FUpgradeInterface;
import com.chtj.base_framework.upgrade.FUpgradeTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.DialogUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.callback.IDialogCallback;
import com.wave_chtj.example.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * apk安装工具类
 */
public class OptionTools {
    private static final String TAG = OptionTools.class.getSimpleName();
    public static final int INSTALL_FORWARD_LOCK = 0x00000001;
    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
    public static final int INSTALL_ALLOW_TEST = 0x00000004;
    public static final int INSTALL_EXTERNAL = 0x00000008;
    public static final int INSTALL_INTERNAL = 0x00000010;
    public static final int INSTALL_FROM_ADB = 0x00000020;
    public static final int INSTALL_ALL_USERS = 0x00000040;
    public static final int INSTALL_ALLOW_DOWNGRADE = 0x00000080;
    public static final int INSTALL_GRANT_RUNTIME_PERMISSIONS = 0x00000100;
    public static final int INSTALL_FORCE_VOLUME_UUID = 0x00000200;
    public static final int INSTALL_FORCE_PERMISSION_PROMPT = 0x00000400;
    public static final int INSTALL_EPHEMERAL = 0x00000800;
    public static final int INSTALL_DONT_KILL_APP = 0x00001000;
    /**
     * ota升级 确保sdcard目录存在update.zip固件
     */
    public static void showOtaUpgrade() {
        File file = new File("/sdcard/update.zip");
        if (file.exists()) {
            DialogUtils.setDialogCallback(new IDialogCallback() {
                @Override
                public void show() {

                }

                @Override
                public void onPositiveClick(String content) {
                    FUpgradeTools.firmwareUpgrade(new UpgradeBean("/sdcard/update.zip", new FUpgradeInterface() {
                        @Override
                        public void installStatus(int installStatus) {
                            Log.d(TAG, "installStatus: "+installStatus);
                        }

                        @Override
                        public void error(String error) {
                            Log.d(TAG, "error: "+error);
                        }

                        @Override
                        public void warning(String warning) {
                            Log.d(TAG, "warning: "+warning);
                        }
                    }));
                    DialogUtils.dismiss();
                }

                @Override
                public void onNegativeClick() {

                }

                @Override
                public void dismiss() {

                }
            }).show(BaseIotUtils.getContext(), R.drawable.logo_splash,"提示:","进行固件升级吗？点击确认后请等待...");
        } else {
            ToastUtils.error("/sdcard/目录下未找到update.zip文件！");
        }
    }
    /**
     * 普通弹窗安装 签名必须一致
     * @param apkPath apk路径
     * @param context 上下文
     */
    public static void installOrdinary(String apkPath,Context context){
        File file=new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 带提示窗口卸载
     *
     * @param packageName 包名
     */
    public static void uninstallOrdinary(String packageName,Context context) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String getSerialNo(){
        ShellUtils.CommandResult commandResult=ShellUtils.execCommand("getprop ro.serialno",true);
        String serial= Build.VERSION.SDK_INT>=30?(TextUtils.isEmpty(commandResult.successMsg)?Build.SERIAL:commandResult.successMsg):Build.SERIAL;
        return serial;
    }
    /**
     * pm 静默安装
     * @param apkPath apk路径
     * @return 是否成功
     */
    public static boolean pmInstallBySilent(String apkPath) {
        try {
            String [] cmd={"pm", "install","-i ", "com.face.keepsample", "-r", "--user", "0", apkPath };
            File file = new File(apkPath);
            if (apkPath == null || apkPath.length() == 0 || (file = new File(apkPath)) == null
                    || file.length() <= 0 || !file.exists() || !file.isFile()) {
                return false;
            }
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            StringBuilder successMsg = new StringBuilder();
            StringBuilder errorMsg = new StringBuilder();
            Process process = null;
            BufferedReader successResult = null;
            BufferedReader errorResult = null;
            try {
                process = processBuilder.start();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, "installBySilent[0]: ", e);
            } finally {
                try {
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.e(TAG, "installBySilent[1]: ", e);
                }
                if (process != null) {
                    process.destroy();
                }
            }
            Log.d(TAG, "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
            if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            Log.e(TAG, "installBySilent[2]: ", e);
            return false;
        }
    }

    /**
     * 反射安装
     * @param context 上下文
     * @param pkgName 包名
     * @param apkPath apk路径
     * @param iResult 结果回调
     */
    public static void installPackageByJavaReflect(Context context, String pkgName, String apkPath,IResult iResult) {
        if (pkgName == null || pkgName.equals("")) {
            iResult.getResult(false,"packageName is null");
        } else {
            try {
                File apkFile = new File(apkPath);
                if (apkFile.exists()) {
                    Uri packageUri = Uri.fromFile(apkFile);
                    IPackageInstallObserver observer = new IPackageInstallObserver.Stub() {
                        @Override
                        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                            Log.d(TAG, "install packageName:" + packageName);
                            Log.d(TAG, "install app result:" + implementationResult(returnCode));
                            iResult.getResult(returnCode==1,"");
                        }
                    };
                    PackageManager packageManager = context.getPackageManager();
                    int flags = packageManager.PERMISSION_GRANTED;
                    try {
                        PackageInfo packageInfo = packageManager.getPackageInfo(pkgName,
                                packageManager.GET_UNINSTALLED_PACKAGES);
                        flags = INSTALL_REPLACE_EXISTING;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "errMeg:" + e.getMessage());
                        flags = packageManager.PERMISSION_GRANTED;
                    }
                    Class appPackageManager = Class.forName("android.app.ApplicationPackageManager");
                    Method method = appPackageManager.getMethod("installPackage", Uri.class,
                            IPackageInstallObserver.class, int.class, String.class);
                    method.invoke(packageManager, packageUri, observer, flags, pkgName);
                }
            }  catch (Exception e) {
                iResult.getResult(false,e.getMessage());
            }
        }
    }

    /**
     * 回调结果
     */
    public interface IResult{
        /**
         * 回调结果及错误
         * @param isComplete 是否成功
         * @param err 错误日志
         */
        void getResult(boolean isComplete,String err);
    }

    /**
     * 反射卸载
     * @param context 上下文
     * @param pkgName 包名
     * @param iResult 结果回调
     */
    public static void deletePackage(final Context context, String pkgName,IResult iResult) {
        if (pkgName == null || pkgName.equals("")) {
            iResult.getResult(false,"packageName is null");
        } else {
            try {
                PackageManager packageManager = context.getPackageManager();
                IPackageDeleteObserver observer = new IPackageDeleteObserver.Stub() {
                    @Override
                    public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                        Log.d(TAG, "delete packageName:" + packageName);
                        Log.d(TAG, "delete app result:" + implementationResult(returnCode));
                        iResult.getResult(returnCode==1,"");
                    }
                };
                int flags = packageManager.PERMISSION_GRANTED;
                PackageInfo packageInfo = packageManager.getPackageInfo(pkgName,
                        packageManager.GET_UNINSTALLED_PACKAGES);
                if (packageInfo == null) {
                    iResult.getResult(false,"deleting app isn't exist");
                } else {
                    Class appPackageManager = Class.forName("android.app.ApplicationPackageManager");
                    Method method = appPackageManager.getMethod("deletePackage", String.class,
                            IPackageDeleteObserver.class, int.class);
                    method.invoke(packageManager, pkgName, observer, flags);
                }
            }catch (Exception e) {
                iResult.getResult(false,e.getMessage());
            }
        }
    }


    /**
     * 是否成功
     * @param returnCode 1成功 其他失败
     * @return 判断结果
     */
    private static String implementationResult(int returnCode) {
        if (returnCode == 1) {
            return "success";
        } else {
            return "failed";
        }
    }
}
