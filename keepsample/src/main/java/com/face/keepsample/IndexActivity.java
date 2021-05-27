package com.face.keepsample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chtj.keepalive.FKeepAliveTools;
import com.chtj.keepalive.entity.CommonValue;
import com.chtj.keepalive.entity.KeepAliveData;
import com.face_chtj.base_iotutils.ToastUtils;
import java.util.List;
/**
 * 如何其他app跨进程向base_keepalive中添加响应得保活呢？
 * 参考app Module中路径com.wave_chtj.example.keeplive.KeepLiveAty中的使用
 * 注意复制相应的aidl和实体类
 */
public class IndexActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "KeepAliveActivity";
    public static final int REQUEST_APP_CODE = 1100;
    public static final int REQUEST_SERVICE_CODE = 1101;
    TextView tvResult;
    EditText etpkg;
    EditText etpkg2;
    EditText etServiceName;
    Button btn_to_select;
    Button btn_select_service;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        tvResult = findViewById(R.id.tvResult);
        etpkg = findViewById(R.id.etpkg);
        etpkg2 = findViewById(R.id.etpkg2);
        btn_to_select = findViewById(R.id.btn_select_app);
        btn_select_service = findViewById(R.id.btn_select_service);
        etServiceName = findViewById(R.id.etServiceName);
        getData();
        //启动一个后台服务添加默认的Service
        startService(new Intent(this, KSampleService.class));
    }


    public void getData() {
        tvResult.setText("");
        List<KeepAliveData> keepAliveDataList = FKeepAliveTools.getKeepLive();
        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
            //Log.d(TAG, "getData:获取成功 数量=" + keepAliveDataList.size());
            for (int i = 0; i < keepAliveDataList.size(); i++) {
                tvResult.append((keepAliveDataList.get(i).getType() == FKeepAliveTools.TYPE_ACTIVITY ? "ACTIVITY =" : "SERVICE =") + keepAliveDataList.get(i).toString() + "\n\r");
            }
        } else {
            tvResult.setText("获取失败 数量=0");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_aty:
                String pkg = etpkg.getText().toString().trim();
                if (pkg == null || pkg.equals("")) {
                   ToastUtils.error("请填写包名！");
                    return;
                }
                KeepAliveData keepAliveData = new KeepAliveData(pkg, FKeepAliveTools.TYPE_ACTIVITY, true);
                CommonValue commonValue = FKeepAliveTools.addActivity(keepAliveData);
                Log.d(TAG, "onClick:>=" + commonValue.toString());
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    tvResult.setText("执行成功");
                } else {
                    tvResult.setText("执行失败 errMeg=" + commonValue.getRemarks());
                }
                break;
            case R.id.btn_add_service:
                String pkg2 = etpkg2.getText().toString().trim();
                String serviceName = etServiceName.getText().toString().trim();
                if (pkg2 == null || pkg2.equals("")) {
                    ToastUtils.error("请填写包名！");
                    return;
                }
                if (serviceName == null || serviceName.equals("")) {
                    ToastUtils.error("请填服务名！");
                    return;
                }
                KeepAliveData keepAliveData1 = new KeepAliveData(pkg2, FKeepAliveTools.TYPE_SERVICE, serviceName, true);
                CommonValue commonValue1 = FKeepAliveTools.addService(keepAliveData1);
                Log.d(TAG, "onClick:>=" + commonValue1.toString());
                if (commonValue1 == CommonValue.EXEU_COMPLETE) {
                    tvResult.setText("执行成功");
                } else {
                    tvResult.setText("执行失败 errMeg=" + commonValue1.getRemarks());
                }
                break;
            case R.id.btn_getall://获取数据并刷新
                getData();
                break;
            case R.id.btn_cleanall:
                CommonValue commonValue2 = FKeepAliveTools.clearKeepLive();
                if (commonValue2 == CommonValue.EXEU_COMPLETE) {
                    tvResult.setText("清除成功");
                } else {
                    tvResult.setText("清除失败 errMeg=" + commonValue2.getRemarks());
                }
                break;
            case R.id.btn_select_app://选择APP
                startActivityForResult(new Intent(this, SelectAppActivity.class), REQUEST_APP_CODE);
                break;
            case R.id.btn_select_service://选择APP
                startActivityForResult(new Intent(this, SelectServiceActivity.class), REQUEST_SERVICE_CODE);
                break;
            case R.id.ivClose://退出APP
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_APP_CODE && resultCode == RESULT_OK) {
            String packageName = data.getStringExtra("packageName");
            KeepAliveData keepAliveData = new KeepAliveData(packageName, FKeepAliveTools.TYPE_ACTIVITY, true);
            CommonValue commonValue = FKeepAliveTools.addActivity(keepAliveData);
            Log.d(TAG, "onClick:>=" + commonValue.toString());
            if (commonValue == CommonValue.EXEU_COMPLETE) {
                ToastUtils.success("已修改成功！");
            } else {
                ToastUtils.error("修改失败！");
            }
            getData();
        } else if (requestCode == REQUEST_SERVICE_CODE && resultCode == RESULT_OK) {
            String packageName = data.getStringExtra("packageName");
            String service = data.getStringExtra("service");
            KeepAliveData keepAliveData1 = new KeepAliveData(packageName, FKeepAliveTools.TYPE_SERVICE, service, true);
            CommonValue commonValue1 = FKeepAliveTools.addService(keepAliveData1);
            Log.d(TAG, "onClick:>=" + commonValue1.toString());
            if (commonValue1 == CommonValue.EXEU_COMPLETE) {
                tvResult.setText("执行成功");
            } else {
                tvResult.setText("执行失败 errMeg=" + commonValue1.getRemarks());
            }
            getData();
        }
    }
}