package com.wave_chtj.example.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.download.DownloadSupport;
import com.face_chtj.base_iotutils.entity.FileCacheData;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

import butterknife.OnClick;

/**
 * Create on 2019/10/10
 * author chtj
 * desc 文件下载
 */
public class DownLoadAty extends BaseActivity {
    private static final String TAG = "DownLoadAty";
    Button btn_start_down;
    ProgressBar pbProgressbar;
    TextView tvResult;
    //文件下载地址
    private String downloadUrl = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/BM54/v0.99/update.zip";
    //替换的文件名称
    private String fileName1 = "update1.zip";


    //文件下载地址
    private String downloadUrl2 = "https://fireware-1257276602.cos.ap-guangzhou.myqcloud.com/test_AIO145/update.zip";
    //替换的文件名称
    private String fileName2 = "update.zip";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        btn_start_down=findViewById(R.id.btn_start_down);
        pbProgressbar=findViewById(R.id.pbProgressbar);
        tvResult=findViewById(R.id.tvResult);
    }

    //文件下载
    public void downloadFile(View view) {
        if (NetUtils.getNetWorkType() == NetUtils.NETWORK_NO) {
            ToastUtils.error("当前无网络连接！");
            return;
        }
        //开启任务下载----------------------这里可执行多个任务 重复执行即可---------
        FileCacheData fileCacheData = new FileCacheData();
        fileCacheData.setUrl(downloadUrl);
        fileCacheData.setFileName(fileName1);
        fileCacheData.setRequestTag(downloadUrl);
        fileCacheData.setFilePath("/sdcard/" + fileName1);
        new DownloadSupport().download(fileCacheData,downloadCallBack);
        //-----------------------------------------------------------
        FileCacheData fileCacheData2 = new FileCacheData();
        fileCacheData2.setUrl(downloadUrl2);
        fileCacheData2.setFileName(fileName2);
        fileCacheData2.setRequestTag(downloadUrl2);
        fileCacheData2.setFilePath("/sdcard/" + fileName2);
        new DownloadSupport().download(fileCacheData2,downloadCallBack);
        //-----------------------------------------------------------

    }
    DownloadSupport.DownloadCallBack downloadCallBack=new DownloadSupport.DownloadCallBack() {
        @Override
        public void download(FileCacheData fileCacheData, int percent, boolean isComplete) {
            if(isComplete){
                KLog.d(TAG,"download:>="+"fileName="+fileCacheData.getFileName()+",isComplete="+isComplete);
            }else{
                KLog.d(TAG,"download:>="+"fileName="+fileCacheData.getFileName()+",percent>>>"+percent);
            }
        }

        @Override
        public void error(Exception e) {
            KLog.d(TAG,"error:>errMeg="+e.getMessage());
        }
    };
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvResult.append(msg.obj.toString());
        }
    };
}
