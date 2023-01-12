package com.wave_chtj.example.file;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.FileUtils;
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
    @BindView(R.id.cbCover)
    CheckBox cbCover;
    //文件路径
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.txt";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_wr);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.btn_write, R.id.btn_del, R.id.btn_read})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_write:
                String content = etContent.getText().toString();
                boolean writeResult = FileUtils.writeFileData(filePath, content, cbCover.isChecked());
                String sizeMb = FileUtils.getFileFormatSize(filePath);
                if (writeResult) {
                    tvResult.append("\n\r " + content + " -> 写入成功" + ",大小=" + sizeMb);
                } else {
                    tvResult.setText("\n\r -> 写入失败");
                }
                break;
            case R.id.btn_del://删除文件
                boolean delResult = FileUtils.delFile(filePath);
                if (delResult) {
                    tvResult.append("\n\r 删除成功！");
                } else {
                    tvResult.append("\n\r -> 删除失败！");
                }
                break;
            case R.id.btn_read: //读数据
                String readResult = FileUtils.readFileData(filePath);
                if (readResult != null && !readResult.equals("")) {
                    tvResult.append("\n\r " + readResult);
                } else {
                    tvResult.append("\n\r -> 读取失败");
                }
                break;
        }
    }
}
