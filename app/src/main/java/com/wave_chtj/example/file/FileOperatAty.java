package com.wave_chtj.example.file;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chtj.base_iotutils.FileUtil;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    }


    @OnClick({R.id.btn_write, R.id.btn_del, R.id.btn_read})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_write:
                String content = etContent.getText().toString();
                boolean writeResult = FileUtil.writeFileData(filePath, content, true);
                if (writeResult) {
                    tvResult.append("\n\r " + content + " -> 写入成功");
                } else {
                    tvResult.setText("\n\r -> 写入失败");
                }
                break;
            case R.id.btn_del://删除文件
                boolean delResult = FileUtil.delFile(filePath);
                if (delResult) {
                    tvResult.append("\n\r 删除成功！");
                } else {
                    tvResult.append("\n\r -> 删除失败！");
                }
                break;
            case R.id.btn_read: //读数据
                String readResult = FileUtil.readFileData(filePath);
                if (readResult != null && !readResult.equals("")) {
                    tvResult.append("\n\r " + readResult);
                } else {
                    tvResult.append("\n\r -> 读取失败");
                }
                break;
        }
    }
}
