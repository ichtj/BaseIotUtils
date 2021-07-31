package com.face.keepsample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chtj.keepalive.FKeepAliveTools;
import com.chtj.keepalive.entity.CommonValue;
import com.chtj.keepalive.entity.KeepAliveData;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.PermissionsUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.app.ScreenInfoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 如何其他app跨进程向base_keepalive中添加响应得保活呢？
 * 参考app Module中路径com.wave_chtj.example.keeplive.KeepLiveAty中的使用
 * 注意复制相应的aidl和实体类
 */
public class IndexActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "KeepAliveActivity";
    public static final int REQUEST_APP_CODE = 1100;//选择APP
    public static final int REQUEST_SERVICE_CODE = 1101;//选择Service
    private EditText etpkg;
    private EditText etpkg2;
    private EditText etServiceName;
    private TextView tvTime;
    private Button btn_to_select;
    private Button btn_select_service;
    private RecyclerView rvSaveList;
    private IndexAdapter indexAdapter;

    private CountDownTimer mTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        etpkg = findViewById(R.id.etpkg);
        tvTime = findViewById(R.id.tvTime);
        rvSaveList = findViewById(R.id.rvSaveList);
        etpkg2 = findViewById(R.id.etpkg2);
        btn_to_select = findViewById(R.id.btn_select_app);
        btn_select_service = findViewById(R.id.btn_select_service);
        etServiceName = findViewById(R.id.etServiceName);
        PermissionsUtils.with(this).addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).addPermission(Manifest.permission.READ_EXTERNAL_STORAGE).initPermission();

        indexAdapter = new IndexAdapter(new ArrayList<>(), this);
        rvSaveList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        rvSaveList.setAdapter(indexAdapter);
        //启动一个后台服务添加默认的Service
        startService(new Intent(this, KSampleService.class));
        //开始倒计时
        startCountDown(60);
    }


    public void getData() {
        List<KeepAliveData> keepAliveDataList = FKeepAliveTools.getKeepLive();
        List<SimpleKeepAlive> simpleKeepAlives = new ArrayList<>();
        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
            for (int i = 0; i < keepAliveDataList.size(); i++) {
                String pkgName = keepAliveDataList.get(i).getPackageName();
                simpleKeepAlives.add(new SimpleKeepAlive(pkgName
                        , keepAliveDataList.get(i).getType()
                        , keepAliveDataList.get(i).getServiceName()
                        , keepAliveDataList.get(i).getIsEnable()
                        , KSampleTools.getAppIconByPkg(pkgName)
                        , KSampleTools.getAppNameByPkg(pkgName)));
            }
        }
        indexAdapter.setList(simpleKeepAlives);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    /**
     * 开始倒计时
     *
     * @param millisInFuture
     */
    public void startCountDown(int millisInFuture) {
        if (mTimer == null) {
            mTimer = new CountDownTimer(millisInFuture * 1000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    int remainTime = (int) (millisUntilFinished / 1000L);
                    tvTime.setText("" + remainTime);
                }

                @Override
                public void onFinish() {
                    tvTime.setText("finish");
                    AppManager.getAppManager().finishAllActivity();
                }
            };
            mTimer.start();
        }
    }

    /**
     * 关闭倒计时
     */
    public void stopCountDown() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
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
                    ToastUtils.success("添加成功！");
                } else {
                    ToastUtils.error("添加失败！err:" + commonValue.getRemarks());
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
                    ToastUtils.success("添加成功！");
                } else {
                    ToastUtils.error("添加失败！err:" + commonValue1.getRemarks());
                }
                break;
            case R.id.btn_getall://获取数据并刷新
                getData();
                break;
            case R.id.btn_cleanall:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("确认删除全部的保活内容吗?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonValue commonValue2 = FKeepAliveTools.clearKeepLive();
                        if (commonValue2 == CommonValue.EXEU_COMPLETE) {
                            ToastUtils.success("清除成功！");
                        } else {
                            ToastUtils.error("清除失败！err:" + commonValue2.getRemarks());
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

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
                ToastUtils.success("添加成功！");
            } else {
                ToastUtils.error("添加失败！err:" + commonValue1.getRemarks());
            }
            getData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountDown();
    }
}