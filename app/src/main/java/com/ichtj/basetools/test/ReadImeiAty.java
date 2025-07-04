package com.ichtj.basetools.test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.DeviceUtils;
import com.face_chtj.base_iotutils.FormatViewUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.LoadingDialog;
import com.ichtj.basetools.util.PACKAGES;

@Route(path = PACKAGES.BASE + "readimei")
public class ReadImeiAty extends BaseActivity{
    private static final String TAG = ReadImeiAty.class.getSimpleName ( );
    TextView tvResult;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei);
        tvResult=findViewById(R.id.tvResult);
        FormatViewUtils.setMovementMethod(tvResult);
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true){
                    handler.sendMessage(handler.obtainMessage(0x22, "imei::"+DeviceUtils.getImeiOrMeid()));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }.start();
        int[] frames = new int[] {
                R.drawable.ic_1,
                R.drawable.ic_2,
                R.drawable.ic_3,
                R.drawable.ic_4,
                R.drawable.ic_5,
                R.drawable.ic_6
        };

        LoadingDialog loadingDialog = new LoadingDialog(this);

        // 情况一：PNG 列表
        loadingDialog.setLoadingImages(frames, 100, 100, 1200);
        loadingDialog.showLoading();

        // 情况二：仅一张图旋转
        // loadingDialog.setLoadingImages(new int[]{R.drawable.loading_icon}, 100, 100, 0);
        // loadingDialog.showLoading();

        // 关闭时
        // loadingDialog.hideLoading();
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            FormatViewUtils.formatData(tvResult,msg.obj.toString());
        }
    };

}