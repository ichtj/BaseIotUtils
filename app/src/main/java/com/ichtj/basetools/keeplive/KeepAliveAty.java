package com.ichtj.basetools.keeplive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.Nullable;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

/**
 * 这里使用了跨进程向keepsample Module中添加了com.face.keepsample的保活
 */
public class KeepAliveAty extends BaseActivity implements OnClickListener{
    private static final String TAG = "KeepLiveAty";
    TextView tvResult;
    /**
     * 保活的类型为Activity
     */
    public static final int TYPE_ACTIVITY = 0;
    /**
     * 保活的类型为服务
     */
    public static final int TYPE_SERVICE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keeplive);
        tvResult = findViewById(R.id.tvResult);
        Intent intent = new Intent();
        intent.setAction("com.chtj.keepalive.IKeepAliveService");
        intent.setPackage("com.face.keepsample");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_aty:
                break;
            case R.id.btn_add_service:

                break;
            case R.id.btn_getall:
                break;
            case R.id.btn_cleanall:

                break;
        }
    }



}
