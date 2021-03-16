package com.wave_chtj.example.keeplive;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chtj.keepalive.IKeepAliveListener;
import com.chtj.keepalive.IKeepAliveService;
import com.chtj.keepalive.entity.KeepAliveData;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import java.util.List;

/**
 * 这里使用了跨进程向keepsample Module中添加了com.face.keepsample的保活
 */
public class KeepLiveAty extends BaseActivity implements OnClickListener, IKeepAliveListener {
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

    IKeepAliveService iKeepAliveService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keeplive);
        tvResult = findViewById(R.id.tvResult);
        Intent intent = new Intent();
        intent.setAction("com.chtj.keepalive.IKeepAliveService");
        intent.setPackage("com.face.keepsample");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        getData();
    }

    public void getData() {
        tvResult.setText("");
        try {
            List<KeepAliveData> keepAliveDataList = iKeepAliveService.getKeepLiveInfo();
            if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
                ToastUtils.success("获取成功 数量=" + keepAliveDataList.size());
                for (int i = 0; i < keepAliveDataList.size(); i++) {
                    tvResult.append((keepAliveDataList.get(i).getType() == TYPE_ACTIVITY ? "ACTIVITY =" : "SERVICE =") + keepAliveDataList.get(i).toString() + "\n\r");
                }
            } else {
                ToastUtils.error("获取失败 数量=0");
                tvResult.setText("获取失败 数量=0");
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLog.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_aty:
                try {
                    KeepAliveData keepAliveData = new KeepAliveData("com.wave_chtj.example", TYPE_ACTIVITY, true);
                    boolean isAddAty = iKeepAliveService.addKeepLiveInfo(keepAliveData, this);
                    KLog.d(TAG, "onClick:>btn_add_aty=" + isAddAty);
                    if (isAddAty) {
                        ToastUtils.success("执行成功！");
                        tvResult.setText("执行成功");
                    } else {
                        ToastUtils.error("执行失败！");
                        tvResult.setText("执行失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
                break;
            case R.id.btn_add_service:
                try {
                    KeepAliveData keepAliveInfo = new KeepAliveData("com.face.baseiotcloud", TYPE_SERVICE, "com.face.baseiotcloud.service.OtherService", true);
                    boolean isaddInfo = iKeepAliveService.addKeepLiveInfo(keepAliveInfo, this);
                    KLog.d(TAG, "onClick:>btn_add_service=" + isaddInfo);
                    if (isaddInfo) {
                        ToastUtils.success("执行成功！");
                        tvResult.setText("执行成功");
                    } else {
                        ToastUtils.error("执行失败！");
                        tvResult.setText("执行失败 ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
                break;
            case R.id.btn_getall:
                getData();
                break;
            case R.id.btn_cleanall:
                try {
                    boolean isClearAll = iKeepAliveService.clearAllKeepAliveInfo();
                    if (isClearAll) {
                        ToastUtils.success("清除成功！");
                        tvResult.setText("清除成功");
                    } else {
                        ToastUtils.error("清除失败！");
                        tvResult.setText("清除失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
                break;
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iKeepAliveService = IKeepAliveService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iKeepAliveService = null;
        }
    };


    IKeepAliveListener.Stub iKeepAliveListener = new IKeepAliveListener.Stub() {
        @Override
        public void onError(String errMeg) throws RemoteException {
            Log.d(TAG, "onError: " + errMeg);
        }

        @Override
        public void onSuccess() throws RemoteException {
            Log.d(TAG, "onSuccess: ");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iKeepAliveService != null) {
            unbindService(conn);
        }
    }

    @Override
    public void onError(String errMeg) throws RemoteException {
        Log.d(TAG, "onError: ");
    }

    @Override
    public void onSuccess() throws RemoteException {
        Log.d(TAG, "onSuccess: ");
    }

    @Override
    public IBinder asBinder() {
        return iKeepAliveListener;
    }
}
