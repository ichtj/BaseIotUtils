package com.wave_chtj.example.keeplive;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.chtj.framework.entity.CommonValue;
import com.chtj.framework.FKeepLiveTools;
import com.chtj.framework.entity.KeepLiveData;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import org.w3c.dom.Text;

import java.util.List;

public class KeepLiveAty extends BaseActivity implements OnClickListener {
    private static final String TAG = "KeepLiveAty";
    TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keeplive);
        tvResult = findViewById(R.id.tvResult);
        getData();
    }

    public void getData() {
        tvResult.setText("");
        List<KeepLiveData> keepLiveDataList = FKeepLiveTools.getKeepLive();
        if (keepLiveDataList != null && keepLiveDataList.size() > 0) {
            ToastUtils.success("获取成功 数量=" + keepLiveDataList.size());
            for (int i = 0; i < keepLiveDataList.size(); i++) {
                tvResult.append((keepLiveDataList.get(i).getType().equals(FKeepLiveTools.TYPE_ACTIVITY) ? "ACTIVITY =" : "SERVICE =") + keepLiveDataList.get(i).toString() + "\n\r");
            }
        } else {
            ToastUtils.error("获取失败 数量=0");
            tvResult.setText("获取失败 数量=0");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_aty:
                KeepLiveData keepLiveData = new KeepLiveData("com.face.baseiotcloud", FKeepLiveTools.TYPE_ACTIVITY, true);
                CommonValue commonValue = FKeepLiveTools.addActivity(keepLiveData, true);
                KLog.d(TAG, "onClick:>=" + commonValue.toString());
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("执行成功！");
                    tvResult.setText("执行成功");
                } else {
                    ToastUtils.error("执行失败！");
                    tvResult.setText("执行失败 errMeg=" + commonValue.getRemarks());
                }
                break;
            case R.id.btn_add_service:
                KeepLiveData keepLiveData1 = new KeepLiveData("com.face.baseiotcloud", FKeepLiveTools.TYPE_SERVICE, "com.face.baseiotcloud.service.OtherService", true);
                CommonValue commonValue1 = FKeepLiveTools.addService(keepLiveData1, true);
                KLog.d(TAG, "onClick:>=" + commonValue1.toString());
                if (commonValue1 == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("执行成功！");
                    tvResult.setText("执行成功");
                } else {
                    ToastUtils.error("执行失败！");
                    tvResult.setText("执行失败 errMeg=" + commonValue1.getRemarks());
                }
                break;
            case R.id.btn_getall:
                getData();
                break;
            case R.id.btn_cleanall:
                CommonValue commonValue2 = FKeepLiveTools.clearKeepLive();
                if (commonValue2 == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("清除成功！");
                    tvResult.setText("清除成功");
                } else {
                    ToastUtils.error("清除失败！");
                    tvResult.setText("清除失败 errMeg=" + commonValue2.getRemarks());
                }
                break;
        }
    }
}
