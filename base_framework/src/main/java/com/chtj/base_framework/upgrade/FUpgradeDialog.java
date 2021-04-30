package com.chtj.base_framework.upgrade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;

import com.chtj.base_framework.FBaseTools;
import com.chtj.base_framework.entity.InstallStatus;

public class FUpgradeDialog {
    private static final String TAG = "FUpgradeDialog";
    private static volatile FUpgradeDialog fUpgradeDialog;
    AlertDialog dialog = null;
    /**
     * 单例模式
     *
     * @return
     */
    public static FUpgradeDialog instance() {
        if (fUpgradeDialog == null) {
            synchronized (FUpgradeDialog.class) {
                if (fUpgradeDialog == null) {
                    fUpgradeDialog = new FUpgradeDialog();
                }
            }
        }
        return fUpgradeDialog;
    }
    /**
     * 显示ota升级弹出框
     */
    public  void showUpdateDialog(String otaPath, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("U盘升级");
        builder.setMessage("检测到固件包,路径为："+otaPath);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Log.d(TAG, "onClick: setSingleChoiceItems selectPathInfo=" + otaPath);
                FUpgradeTools.firmwareUpgrade(otaPath, new FUpgradeTools.UpgradeInterface() {
                    @Override
                    public void operating(InstallStatus installStatus) {
                        Log.d(TAG, "operating:installStatus =" + installStatus);
                    }

                    @Override
                    public void error(String errInfo) {
                        Log.d(TAG, "operating:errInfo =" + errInfo);

                    }
                });
                dialogInterface.dismiss();
            }
        });
        fUpgradeDialog.dialog = builder.create();
        fUpgradeDialog.dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        fUpgradeDialog.dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        fUpgradeDialog.dialog.show();
        fUpgradeDialog.dialog.setCanceledOnTouchOutside(true);// dialog弹出后，点击界面其他部分dialog消失
    }

    public void dismissDialog() {
        if(fUpgradeDialog.dialog!=null){
            fUpgradeDialog.dialog.dismiss();
        }
    }
}
