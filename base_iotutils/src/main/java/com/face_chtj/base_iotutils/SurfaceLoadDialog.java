package com.face_chtj.base_iotutils;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Create on 2020/3/11
 * author chtj
 * desc 应用上层弹窗
 *
 * 调用方式 SurfaceLoadDialog.getInstance().show("hello world");
 * 更改文本时再次调用 SurfaceLoadDialog.getInstance().show("hello world"); 即可
 *
 * {@link #dismiss()} 关闭
 * {@link #show(String)} 显示的文字
 *
 */
public class SurfaceLoadDialog {
    private static final String TAG = "SurfaceLoadDialog";
    private WindowManager wm;
    private TextView tvRemarks;
    private View scanView;
    private static SurfaceLoadDialog mSurfaceLoadDialog;
    private boolean isInitView = false;

    public static SurfaceLoadDialog getInstance() {
        if (mSurfaceLoadDialog == null || !mSurfaceLoadDialog.isInitView) {
            synchronized (SurfaceLoadDialog.class) {
                if (mSurfaceLoadDialog == null || !mSurfaceLoadDialog.isInitView) {
                    mSurfaceLoadDialog = new SurfaceLoadDialog();
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
        return mSurfaceLoadDialog;
    }


    private static void initAddView(int apiType) {
        mSurfaceLoadDialog.wm = (WindowManager) BaseIotUtils.getContext().getSystemService("window");
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        mSurfaceLoadDialog.scanView = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.activity_progress, null);
        mSurfaceLoadDialog.tvRemarks = mSurfaceLoadDialog.scanView.findViewById(R.id.tvRemarks);
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
        mSurfaceLoadDialog.wm.addView(mSurfaceLoadDialog.scanView, wmParams);
        mSurfaceLoadDialog.isInitView = true;
    }

    // 显示系统级提示框（自定义布局）
    public void show(String content) {
        if (tvRemarks != null) {
            tvRemarks.setText("" + content);
        }
    }

    public void dismiss() {
        if (mSurfaceLoadDialog.wm != null) {
            mSurfaceLoadDialog.wm.removeView(mSurfaceLoadDialog.scanView);
            mSurfaceLoadDialog.wm = null;
            mSurfaceLoadDialog = null;
        }
    }
}
