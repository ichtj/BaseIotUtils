package com.wave_chtj.example.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chtj.base_framework.FScreentTools;
import com.chtj.base_framework.entity.CommonValue;
import com.chtj.base_framework.entity.InstallStatus;
import com.chtj.base_framework.entity.IpConfigInfo;
import com.chtj.base_framework.network.FEthTools;
import com.chtj.base_framework.upgrade.FUpgradeTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.ISysDialog;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.notify.NotifyUtils;
import com.face_chtj.base_iotutils.notify.OnNotifyLinstener;
import com.face_chtj.base_iotutils.threadpool.TPoolUtils;
import com.wave_chtj.example.FeaturesOptionAty;
import com.wave_chtj.example.R;
import com.wave_chtj.example.allapp.AllAppAty;
import com.wave_chtj.example.crash.MyService;
import com.wave_chtj.example.download.FileDownLoadAty;
import com.wave_chtj.example.entity.ExcelEntity;
import com.wave_chtj.example.entity.IndexBean;
import com.wave_chtj.example.file.FileOperatAty;
import com.wave_chtj.example.greendao.GreenDaoSqliteAty;
import com.wave_chtj.example.keeplive.KeepAliveAty;
import com.wave_chtj.example.network.NetChangeAty;
import com.wave_chtj.example.video.VideoPlayAty;
import com.wave_chtj.example.audio.PlayAudioAty;
import com.wave_chtj.example.screen.ScreenActivity;
import com.wave_chtj.example.serialport.SerialPortAty;
import com.wave_chtj.example.socket.SocketAty;
import com.wave_chtj.example.timer.TimerAty;
import com.wave_chtj.example.util.excel.JXLExcelUtils;
import com.wave_chtj.example.util.excel.POIExcelUtils;
import com.wave_chtj.example.util.keyevent.IUsbHubListener;
import com.wave_chtj.example.util.keyevent.UsbHubTools;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

public class IndexAdapter extends BaseMultiItemQuickAdapter<IndexBean, BaseViewHolder> {
    private static final String TAG = "BaseIotRvAdapter";
    public static final int LAYOUT_NO_BG = 1;
    public static final int LAYOUT_ONE = 2;
    public static final int LAYOUT_TWO = 3;

    private List<IndexBean> itemList;

    public void setList(List<IndexBean> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    //构造方法，传入数据
    public IndexAdapter(List<IndexBean> itemList) {
        super(itemList);
        this.itemList = itemList;
        addItemType(LAYOUT_NO_BG,R.layout.item_nobg);
        addItemType(LAYOUT_ONE,R.layout.item_one);
    }


    @Override
    public int getItemViewType(int position) {
        if (position < 10) {
            return LAYOUT_NO_BG;
        } else {
            return LAYOUT_ONE;
        } /*else if (position <= 15) {
            return LAYOUT_ONE;
        } else {
            return LAYOUT_TWO;
        }*/
    }


    @Override
    protected void convert(BaseViewHolder helper, IndexBean indexBean) {
        switch (helper.getItemViewType()) {
            case IndexAdapter.LAYOUT_NO_BG:
                break;
            case IndexAdapter.LAYOUT_ONE:
                break;
        }
        helper.setText(R.id.tvOneStr, indexBean.getItem()[0]);
    }


}