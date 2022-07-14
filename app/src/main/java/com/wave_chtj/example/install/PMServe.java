package com.wave_chtj.example.install;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PMServe {

    final static String TAG = "PMService";

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

    public void installPackageByJavaReflect(Context context, String pkgName, String path) {
        if (pkgName == null || pkgName.equals("")) {
            Toast.makeText(context, "Input location of install app", Toast.LENGTH_SHORT).show();
        } else {
            try {
                File apkFile = new File(path);
                if (apkFile.exists()) {
                    Uri packageUri = Uri.fromFile(apkFile);
                    IPackageInstallObserver observer = new IPackageInstallObserver.Stub() {
                        @Override
                        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                            Log.d(TAG, "install packageName:" + packageName);
                            Log.d(TAG, "install app result:" + implementationResult(returnCode));
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
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                    ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void deletePackage(final Context context, String packageName) {
        if (packageName == null || packageName.equals("")) {
            Toast.makeText(context, "Input name of delete app", Toast.LENGTH_SHORT).show();
        } else {
            try {
                PackageManager packageManager = context.getPackageManager();
                IPackageDeleteObserver observer = new IPackageDeleteObserver.Stub() {
                    @Override
                    public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                        Log.d(TAG, "delete packageName:" + packageName);
                        Log.d(TAG, "delete app result:" + implementationResult(returnCode));
                        Toast.makeText(context, "delete app " + implementationResult(returnCode),
                                Toast.LENGTH_SHORT).show();
                    }
                };
                int flags = packageManager.PERMISSION_GRANTED;
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                        packageManager.GET_UNINSTALLED_PACKAGES);
                if (packageInfo == null) {
                    Toast.makeText(context, "deleting app isn't exist", Toast.LENGTH_SHORT).show();
                } else {
                    Class appPackageManager = Class.forName("android.app.ApplicationPackageManager");
                    Method method = appPackageManager.getMethod("deletePackage", String.class,
                            IPackageDeleteObserver.class, int.class);
                    method.invoke(packageManager, packageName, observer, flags);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                    ClassNotFoundException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static String implementationResult(int returnCode) {
        if (returnCode == 1) {
            return "success";
        } else {
            return "failed";
        }
    }
}


