package com.wave_chtj.example.file;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chtj.base_iotutils.FileUtil;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

/**
 * Create on 2019/10/10
 * author chtj
 * 文件读写
 */
public class FileOperatAty extends BaseActivity {
    EditText et_content;
    TextView tv_result;
    //文件路径
    private String filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.txt";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_wirite_read);
        et_content = findViewById(R.id.et_content);
        tv_result = findViewById(R.id.tv_result);
    }

    //写数据
    public void writeData(View view) {
        String content = et_content.getText().toString();
        boolean writeResult = FileUtil.writeFileData(filePath, content, true);
        if (writeResult) {
            tv_result.append("\n\r "+content+" -> 写入成功");
        }else{
            tv_result.setText("\n\r -> 写入失败");
        }
    }

    //读数据
    public void readData(View view) {
        String readResult=FileUtil.readFileData(filePath);
        if(readResult!=null&&!readResult.equals("")){
            tv_result.append("\n\r "+readResult);
        }else{
            tv_result.append("\n\r -> 读取失败");
        }
    }
    //删除文件
    public void delData(View view) {
        boolean delResult=FileUtil.delFile(filePath);
        if(delResult){
            tv_result.append("\n\r 删除成功！");
        }else{
            tv_result.append("\n\r -> 删除失败！");
        }
    }
}
