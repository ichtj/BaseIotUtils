package com.chtj.base_iotutils;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.chtj.base_iotutils.keeplive.BaseIotUtils;

/**
 * Create on 2020/3/11
 * author chtj
 * desc 应用上层弹窗
 */
public class SystemLoadDialog {
    private WindowManager wm;
    private TextView tvRemarks;
    private View scanView;
    private static SystemLoadDialog mSystemLoadDialog;


    public static SystemLoadDialog getInstance() {
        if (mSystemLoadDialog == null) {
            synchronized (SystemLoadDialog.class) {
                if (mSystemLoadDialog == null) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if(!Settings.canDrawOverlays(BaseIotUtils.getContext())) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            BaseIotUtils.getContext().startActivity(intent);
                        } else {
                            //绘ui代码, 这里说明6.0系统已经有权限了
                            initAddView(1);
                        }
                    } else {
                        //绘ui代码,这里android6.0以下的系统直接绘出即可
                        initAddView(2);
                    }
                }
            }
        }
        return mSystemLoadDialog;
    }

    private static void initAddView(int apiType){
        mSystemLoadDialog = new SystemLoadDialog();
        mSystemLoadDialog.wm = (WindowManager) BaseIotUtils.getContext().getSystemService("window");
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        mSystemLoadDialog.scanView = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.activity_progress, null);
        mSystemLoadDialog.tvRemarks = mSystemLoadDialog.scanView.findViewById(R.id.tvRemarks);
        if(apiType==1){
            //6.0以上
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;;
        }else{
            //6.0以下
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        wmParams.format = 1;
        wmParams.flags = 40;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mSystemLoadDialog.wm.addView(mSystemLoadDialog.scanView, wmParams);
    }

    // 显示系统级提示框（自定义布局）
    public void show(String content) {
        tvRemarks.setText(""+content);
    }

    public void dismiss(){
        if(mSystemLoadDialog.wm!=null){
            mSystemLoadDialog.wm.removeView(mSystemLoadDialog.scanView);
            mSystemLoadDialog.wm=null;
            mSystemLoadDialog =null;
        }
    }
}
