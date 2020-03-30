package com.wave_chtj.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.chtj.base_iotutils.SPUtils;
import com.wave_chtj.example.base.BaseActivity;

/**
 * Create on 2019/10/16
 * author chtj
 * desc $ 启动页
 */
public class StartPageAty extends BaseActivity {
    public static final String TAG="StartPageAty";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);              //去掉TITLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);            //设为全屏
        startActivity(new Intent(StartPageAty.this, FeaturesOptionAty.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        boolean isFrist=SPUtils.getBoolean("isFirst",true);
        if(isFrist){
            //如果是第一次进来
            SPUtils.putBoolean("isFirst",false);
        }else{
            //如果是第二次及以后进来

        }

    }
}
