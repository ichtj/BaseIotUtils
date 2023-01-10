package com.face_chtj.base_iotutils.display;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.R;

/**
 * Create on 2020/3/11
 * author chtj
 * desc 应用上层弹窗
 *
 * 调用方式 BSysDialog.getInstance().show("hello world");
 * 更改文本时再次调用 BSysDialog.getInstance().show("hello world"); 即可
 *
 * {@link #dismiss()} 关闭
 * {@link #show(String)} 显示的文字
 *
 */
public class SystemAlertDialog {
    private static final String TAG = "ISysDialog";
    private WindowManager wm;
    private TextView tvRemarks;
    private View scanView;
    private static SystemAlertDialog mSystemAlertDialog;
    private boolean isInitView = false;

    public static SystemAlertDialog getInstance() {
        if (mSystemAlertDialog == null || !mSystemAlertDialog.isInitView) {
            synchronized (SystemAlertDialog.class) {
                if (mSystemAlertDialog == null || !mSystemAlertDialog.isInitView) {
                    mSystemAlertDialog = new SystemAlertDialog();
                    int nowSdkVersion = Build.VERSION.SDK_INT;
                    KLog.d(TAG, "nowSdkVersion=" + nowSdkVersion);
                    if (nowSdkVersion >= 23) {
                        //判断悬浮窗是否已经通过权限
                        if (!Settings.canDrawOverlays(BaseIotUtils.getContext())) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            BaseIotUtils.getContext().startActivity(intent);
                            //跳转应用设置
                        } else {
                            if(nowSdkVersion>=23&&nowSdkVersion<26){
                                //绘ui代码, 这里说明6.0~7.0系统已经有权限了
                                initAddView(1);
                            }else{
                                initAddView(2);
                            }
                        }
                    } else {
                        //绘ui代码,这里android6.0以下的系统直接绘出即可
                        initAddView(1);
                    }
                }
            }
        }
        return mSystemAlertDialog;
    }


    private static void initAddView(int apiType) {
        mSystemAlertDialog.wm = (WindowManager) BaseIotUtils.getContext().getSystemService("window");
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        mSystemAlertDialog.scanView = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.activity_progress, null);
        mSystemAlertDialog.tvRemarks = mSystemAlertDialog.scanView.findViewById(R.id.tvRemarks);
        if (apiType == 1) {
            //6.0~7.0系统以及6.0以下系统使用同一模式
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else{
            //8.0
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        wmParams.format = 1;
        wmParams.flags = 40;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mSystemAlertDialog.wm.addView(mSystemAlertDialog.scanView, wmParams);
        mSystemAlertDialog.isInitView = true;
    }

    // 显示系统级提示框（自定义布局）
    public void show(String content) {
        if (tvRemarks != null) {
            tvRemarks.setText("" + content);
        }
    }

    public void dismiss() {
        if (mSystemAlertDialog.wm != null) {
            mSystemAlertDialog.wm.removeView(mSystemAlertDialog.scanView);
            mSystemAlertDialog.wm = null;
            mSystemAlertDialog = null;
        }
    }
}
