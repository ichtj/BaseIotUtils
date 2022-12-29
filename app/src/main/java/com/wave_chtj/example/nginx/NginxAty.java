package com.wave_chtj.example.nginx;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.convert.TimeUtils;
import com.face_chtj.base_iotutils.display.DeviceUtils;
import com.face_chtj.base_iotutils.network.NetUtils;
import com.jrummyapps.android.shell.CommandResult;
import com.jrummyapps.android.shell.Shell;
import com.wave_chtj.example.R;
import com.wave_chtj.example.base.BaseActivity;

public class NginxAty extends BaseActivity {
    TextView tvResult;
    TextView tvNowIp;
    EditText etNginxCmd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nginx);
        tvNowIp = findViewById(R.id.tvNowIp);
        etNginxCmd = findViewById(R.id.etNginxCmd);
        tvResult = findViewById(R.id.tvResult);
        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        tvNowIp.setText("本机IP："+DeviceUtils.getLocalIp());
    }

    public void startOnClick(View view) {
        CommandResult result = Shell.SU.run("/data/data/xiaoqidun.anmpp/files/root/android.nginx/sbin/nginx -p /data/data/xiaoqidun.anmpp/files/root/android.nginx/ -c /data/data/xiaoqidun.anmpp/files/root/android.nginx/conf/nginx.conf");
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("---------------start >> nginx ----------------\n");
        stringBuilder.append("exitCode >> " + result.exitCode + "\n");
        stringBuilder.append("stdout >> " + result.getStdout() + "\n");
        stringBuilder.append("stderr >> " + result.getStderr() + "\n");
        stringBuilder.append("-----------------------end-----------------------\n\r");
        showData(stringBuilder.toString().replace("\n","<br />"));
    }

    public void stopOnClick(View view) {
        CommandResult result = Shell.SU.run("/data/data/xiaoqidun.anmpp/files/root/android.nginx/sbin/nginx -p /data/data/xiaoqidun.anmpp/files/root/android.nginx/ -s quit");
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("---------------start >> nginx -p  -s quit----------------\n");
        stringBuilder.append("exitCode >> " + result.exitCode + "\n");
        stringBuilder.append("stdout >> " + result.getStdout() + "\n");
        stringBuilder.append("stderr >> " + result.getStderr() + "\n");
        stringBuilder.append("-----------------------end-----------------------\n\r");
        showData(stringBuilder.toString().replace("\n","<br />"));
    }

    public void versionOnClick(View view) {
        CommandResult result = Shell.SU.run("/data/data/xiaoqidun.anmpp/files/root/android.nginx/sbin/nginx -p /data/data/xiaoqidun.anmpp/files/root/android.nginx/ -V");
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("---------------start >> nginx -V----------------\n");
        stringBuilder.append("exitCode >> " + result.exitCode + "\n");
        stringBuilder.append("stdout >> " + result.getStdout() + "\n");
        stringBuilder.append("stderr >> " + result.getStderr() + "\n");
        stringBuilder.append("-----------------------end-----------------------\n\r");
        showData(stringBuilder.toString().replace("\n","<br />"));
    }

    public void helpOnClick(View view) {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("/data/data/xiaoqidun.anmpp/files/root/android.nginx/sbin/nginx -p /data/data/xiaoqidun.anmpp/files/root/android.nginx/ -h", true);
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("---------------start >> nginx -h----------------\n");
        stringBuilder.append("result >> " + commandResult.result + "\n");
        stringBuilder.append("succMsg >> " + commandResult.successMsg + "\n");
        stringBuilder.append("errMsg >> " + commandResult.errorMsg + "\n");
        stringBuilder.append("-----------------------end-----------------------\n\r");
        showData(stringBuilder.toString().replace("\n","<br />"));
    }

    public void exeuOnClick(View view) {
        String cmd="/data/root/android.nginx/sbin/"+etNginxCmd.getText().toString();
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(cmd, true);
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("---------------"+cmd+"----------------\n");
        stringBuilder.append("result >> " + commandResult.result + "\n");
        stringBuilder.append("succMsg >> " + commandResult.successMsg + "\n");
        stringBuilder.append("errMsg >> " + commandResult.errorMsg + "\n");
        stringBuilder.append("-----------------------end-----------------------\n\r");
        showData(stringBuilder.toString().replace("\n","<br />"));
    }

    /**
     * 显示数据到UI
     *
     * @param htmlStr
     */
    public void showData(String htmlStr) {
        tvResult.append(Html.fromHtml(TimeUtils.getTodayDateHms("yy-MM-dd HH:mm:ss") + "：" + htmlStr));
        tvResult.append("\n");
        //刷新最新行显示
        int offset = tvResult.getLineCount() * tvResult.getLineHeight();
        int tvHeight = tvResult.getHeight();
        if (offset > 6000) {
            tvResult.setText("");
            tvResult.scrollTo(0, 0);
        } else {
            if (offset > tvHeight) {
                //Log.d(TAG, "showData: offset >> " + offset + " ,tvHeight >> " + tvHeight);
                tvResult.scrollTo(0, offset - tvHeight);
            }
        }
    }
}
