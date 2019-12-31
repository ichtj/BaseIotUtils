package com.wave_chtj.example.file;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chtj.base_iotutils.FileTxtUtil;
import com.chtj.base_iotutils.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

/**
 * Create on 2019/10/10
 * author chtj
 * 文件读写
 */
public class FileOperatAty extends BaseActivity {
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.btn_write)
    Button btnWrite;
    @BindView(R.id.btn_del)
    Button btnDel;
    @BindView(R.id.btn_read)
    Button btnRead;
    @BindView(R.id.tv_result)
    TextView tvResult;
    //文件路径
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.txt";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_wirite_read);
        ButterKnife.bind(this);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}).
                subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) { // Always true pre-M
                            // I can control the camera now
                            ToastUtils.showShort("已通过权限");
                        } else {
                            // Oups permission denied
                            ToastUtils.showShort("未通过权限");
                        }
                    }
                });
    }


    @OnClick({R.id.btn_write, R.id.btn_del, R.id.btn_read})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_write:
                String content = etContent.getText().toString();
                boolean writeResult = FileTxtUtil.writeFileData(filePath, content, true);
                if (writeResult) {
                    tvResult.append("\n\r " + content + " -> 写入成功");
                } else {
                    tvResult.setText("\n\r -> 写入失败");
                }
                break;
            case R.id.btn_del://删除文件
                boolean delResult = FileTxtUtil.delFile(filePath);
                if (delResult) {
                    tvResult.append("\n\r 删除成功！");
                } else {
                    tvResult.append("\n\r -> 删除失败！");
                }
                break;
            case R.id.btn_read: //读数据
                String readResult = FileTxtUtil.readFileData(filePath);
                if (readResult != null && !readResult.equals("")) {
                    tvResult.append("\n\r " + readResult);
                } else {
                    tvResult.append("\n\r -> 读取失败");
                }
                break;
        }
    }
}
