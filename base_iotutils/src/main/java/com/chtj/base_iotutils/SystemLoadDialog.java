package com.chtj.base_iotutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.chtj.base_iotutils.keeplive.BaseIotUtils;

/**
 * Create on 2020/3/11
 * author chtj
 * desc
 */
public class SystemLoadDialog {
    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;
    private TextView tvRemarks;
    private View scanView;
    private static SystemLoadDialog mSystemLoadDialog;


    public static SystemLoadDialog getInstance() {
        if (mSystemLoadDialog == null) {
            synchronized (SystemLoadDialog.class) {
                if (mSystemLoadDialog == null) {
                    mSystemLoadDialog = new SystemLoadDialog();
                    mSystemLoadDialog.wm = (WindowManager) BaseIotUtils.getContext().getSystemService("window");
                    mSystemLoadDialog.wmParams = new WindowManager.LayoutParams();
                    mSystemLoadDialog.scanView = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.activity_progress, null);
                    mSystemLoadDialog.tvRemarks = mSystemLoadDialog.scanView.findViewById(R.id.tvRemarks);
                    mSystemLoadDialog.wmParams.type = 2002;
                    mSystemLoadDialog.wmParams.format = 1;
                    mSystemLoadDialog.wmParams.flags = 40;
                    mSystemLoadDialog.wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    mSystemLoadDialog.wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mSystemLoadDialog.wm.addView(mSystemLoadDialog.scanView, mSystemLoadDialog.wmParams);
                }
            }
        }
        return mSystemLoadDialog;
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
