package com.ichtj.basetools.file;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.FileUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

/**
 * Create on 2019/10/10
 * author chtj
 * 文件读写
 */
public class FileOperatAty extends BaseActivity implements View.OnClickListener {
    EditText etContent;
    Button btnWrite;
    Button btnDel;
    Button btnRead;
    TextView tvResult;
    CheckBox cbCover;
    //文件路径
    private final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.txt";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_wr);
        initView();
    }
    public void initView() {
        etContent = findViewById(R.id.etContent);
        btnWrite = findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(this);
        btnDel = findViewById(R.id.btnDel);
        btnDel.setOnClickListener(this);
        btnRead = findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);
        tvResult = findViewById(R.id.tvResult);
        cbCover = findViewById(R.id.cbCover);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnWrite) {
            String content = etContent.getText().toString();
            boolean writeResult = FileUtils.writeFileData(filePath, content, cbCover.isChecked());
            String sizeMb = FileUtils.getFileFormatSize(filePath);
            if (writeResult) {
                tvResult.append("\n\r " + content + " -> 写入成功" + ",大小=" + sizeMb);
            } else {
                tvResult.setText("\n\r -> 写入失败");
            }
        } else if (id == R.id.btnDel) {//删除文件
            boolean delResult = FileUtils.delFile(filePath);
            if (delResult) {
                tvResult.append("\n\r 删除成功！");
            } else {
                tvResult.append("\n\r -> 删除失败！");
            }
        } else if (id == R.id.btnRead) { //读数据
            String readResult = FileUtils.readFileData(filePath);
            if (readResult != null && !readResult.equals("")) {
                tvResult.append("\n\r " + readResult);
            } else {
                tvResult.append("\n\r -> 读取失败");
            }
        }
    }
}
