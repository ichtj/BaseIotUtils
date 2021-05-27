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

import androidx.recyclerview.widget.RecyclerView;

import com.chtj.base_framework.FScreentTools;
import com.chtj.base_framework.entity.CommonValue;
import com.chtj.base_framework.entity.InstallStatus;
import com.chtj.base_framework.entity.IpConfigInfo;
import com.chtj.base_framework.network.FEthTools;
import com.chtj.base_framework.upgrade.FUpgradeTools;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.SurfaceLoadDialog;
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

public class BaseIotRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "BaseIotRvAdapter";
    private Context context;
    private static final int LAYOUT_NO_BG = 1;
    private static final int LAYOUT_ONE = 2;
    private static final int LAYOUT_TWO = 3;

    public void setList(LinkedHashMap<Integer, String[]> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }


    private LinkedHashMap<Integer, String[]> itemList;
    private View inflater;

    //构造方法，传入数据
    public BaseIotRvAdapter(Context context, LinkedHashMap<Integer, String[]> itemList) {
        this.context = context;
        this.itemList = itemList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        switch (viewType) {
            case LAYOUT_NO_BG:
                inflater = LayoutInflater.from(context).inflate(R.layout.item_nobg, parent, false);
                NoBgViewHolder noBgViewHolder = new NoBgViewHolder(inflater);
                return noBgViewHolder;
            case LAYOUT_ONE:
                inflater = LayoutInflater.from(context).inflate(R.layout.item_one, parent, false);
                OneViewHolder oneViewHolder = new OneViewHolder(inflater);
                return oneViewHolder;
            case LAYOUT_TWO:
                inflater = LayoutInflater.from(context).inflate(R.layout.item_two, parent, false);
                TwoViewHolder twoViewHolder = new TwoViewHolder(inflater);
                return twoViewHolder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case LAYOUT_NO_BG:
                //KLog.d(TAG,"onBindViewHolder:>LAYOUT_NO_BG="+itemList.get(position)[0]+",position="+position);
                NoBgViewHolder noBgViewHolder = (NoBgViewHolder) holder;
                //将数据和控件绑定
                noBgViewHolder.tvOneStr.setText(itemList.get(position)[0]);
                break;
            case LAYOUT_ONE:
                OneViewHolder oneViewHolder = (OneViewHolder) holder;
                //KLog.d(TAG,"onBindViewHolder:>LAYOUT_ONE="+itemList.get(position)[0]+",position="+position);
                //将数据和控件绑定
                oneViewHolder.tvOneStr.setText(itemList.get(position)[0]);
                oneViewHolder.tvOneStr.setTag(position);
                oneViewHolder.tvOneStr.setOnClickListener(this);
                break;
            case LAYOUT_TWO:
                //TwoViewHolder twoViewHolder = (TwoViewHolder) holder;
                ////将数据和控件绑定
                //twoViewHolder.tv_one.setText(itemList.get(position)[0]);
                //twoViewHolder.tv_two.setText(itemList.get(position)[1]);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 10) {
            return LAYOUT_NO_BG;
        }else{
            return LAYOUT_ONE;
        } /*else if (position <= 15) {
            return LAYOUT_ONE;
        } else {
            return LAYOUT_TWO;
        }*/
    }


    @Override
    public int getItemCount() {
        //返回Item总条数
        return itemList.size();
    }

    @Override
    public void onClick(View v) {
        switch (Integer.valueOf(v.getTag().toString())) {
            case FKey.KEY_SERIAL_PORT:
                context.startActivity(new Intent(context, SerialPortAty.class));
                break;
            case FKey.KEY_TIMERD:
                context.startActivity(new Intent(context, TimerAty.class));
                break;
            case FKey.KEY_SCREEN:
                context.startActivity(new Intent(context, ScreenActivity.class));
                break;
            case FKey.KEY_FILE_RW:
                context.startActivity(new Intent(context, FileOperatAty.class));
                break;
            case FKey.KEY_NETWORK:
                context.startActivity(new Intent(context, NetChangeAty.class));
                break;
            case FKey.KEY_FILEDOWN:
                context.startActivity(new Intent(context, FileDownLoadAty.class));
                break;
            case FKey.KEY_TCP_UDP:
                context.startActivity(new Intent(context, SocketAty.class));
                break;
            case FKey.KEY_NOTIFY_SHOW:
                //获取系统中是否已经通过 允许通知的权限
                if (NotifyUtils.notifyIsEnable()) {
                    NotifyUtils.getInstance("111")
                            .setEnableCloseButton(false)//设置是否显示关闭按钮
                            .setOnNotifyLinstener(new OnNotifyLinstener() {
                                @Override
                                public void enableStatus(boolean isEnable) {
                                    KLog.e(TAG, "isEnable=" + isEnable);
                                }
                            })
                            .setNotifyParam(R.drawable.app_img, R.drawable.app_img
                                    , "BaseIotUtils"
                                    , "工具类"
                                    , "文件压缩，文件下载，日志管理，时间管理，网络判断。。。"
                                    , "this is a library ..."
                                    , "2020-3-18"
                                    , "xxx"
                                    , false
                                    , false)
                            .exeuNotify();
                } else {
                    //去开启通知
                    NotifyUtils.toOpenNotify();
                }
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            NotifyUtils.getInstance("111").setAppName("");
                            NotifyUtils.getInstance("111").setAppAbout("");
                            NotifyUtils.getInstance("111").setRemarks("");
                            NotifyUtils.getInstance("111").setPrompt("");
                            NotifyUtils.getInstance("111").setDataTime("");
                            NotifyUtils.getInstance("111").setTopRight("");
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                });
                break;
            case FKey.KEY_NOTIFY_CLOSE:
                NotifyUtils.closeNotify();
                break;
            case FKey.KEY_SYS_DIALOG_SHOW:
                SurfaceLoadDialog.getInstance().show("hello world");
                break;
            case FKey.KEY_SYS_DIALOG_CLOSE:
                SurfaceLoadDialog.getInstance().dismiss();
                break;
            case FKey.KEY_TOAST:
                ToastUtils.showShort("Hello Worold!");
                break;
            case FKey.KEY_TOAST_BG:
                ToastUtils.success("Hello Worold!");
                break;
            case FKey.KEY_ERR_ANR:
                context.stopService(new Intent(context, MyService.class));
                context.startService(new Intent(context, MyService.class));
                break;
            case FKey.KEY_ERR_OTHER:
                int i = 1 / 0;
                break;
            case FKey.KEY_USB_HUB:
                ToastUtils.info("usb设备监听开始,插入或拔出将提示！");
                UsbHubTools.getInstance().registerReceiver();
                UsbHubTools.getInstance().setIUsbDeviceListener(new IUsbHubListener() {
                    @Override
                    public void deviceInfo(String action, String path, boolean isConn) {
                        ToastUtils.info("path:" + path + ",isConn=" + isConn);
                    }
                });
                break;
            case FKey.KEY_USB_HUB_UNREGIST:
                ToastUtils.info("解除usb设备监听注册");
                UsbHubTools.getInstance().unRegisterReceiver();
                break;
            case FKey.KEY_GREEN_DAO:
                context.startActivity(new Intent(context, GreenDaoSqliteAty.class));
                break;
            case FKey.KEY_JXL_OPEN:
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //第一种jxl.jar 只能读取xls
                            List<ExcelEntity> readExcelDatas = JXLExcelUtils.readExcelxlsx(Environment.getExternalStorageDirectory() + "/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                });
                break;
            case FKey.KEY_JXL_EXPORT:
                //第一种 jxl.jar导出
                JXLExcelUtils.exportExcel();
                ToastUtils.success("export successful!");
                break;
            case FKey.KEY_POI_OPEN:
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //poi.jar 可以读取xls xlsx 两种
                            List<ExcelEntity> readExcelDatas = POIExcelUtils.readExcel(Environment.getExternalStorageDirectory() + "/table.xls");
                            KLog.d(TAG, "readDataSize: " + readExcelDatas.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            KLog.e(TAG, "errMeg:" + e.getMessage());
                        }
                    }
                });
                break;
            case FKey.KEY_POI_EXPORT:
                TPoolUtils.newInstance().addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        //poi.jar导出
                        boolean isOK = POIExcelUtils.createExcelFile();
                        KLog.d(TAG, "isOK: " + isOK);
                    }
                });
                break;
            case FKey.KEY_APP_LIST:
                context.startActivity(new Intent(context, AllAppAty.class));
                break;
            case FKey.KEY_VIDEO:
                context.startActivity(new Intent(context, VideoPlayAty.class));
                break;
            case FKey.KEY_URL_CONVERT:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                ((Activity) context).startActivityForResult(Intent.createChooser(intent, "请选择文件"), FeaturesOptionAty.FILE_SELECT_CODE);
                break;
            case FKey.KEY_ASSETS:
                try {
                    InputStream input = context.getAssets().open("table.xls");
                    if (input != null) {
                        ToastUtils.success("found table.xls");
                    } else {
                        ToastUtils.success("not found table.xls");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
                break;
            case FKey.KEY_AUDIO:
                context.startActivity(new Intent(context, PlayAudioAty.class));
                break;
            case FKey.KEY_IP_SET_DHCP:
                CommonValue commonValue2 = FEthTools.setEthDhcp();
                if (commonValue2 == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("动态IP设置成功！");
                } else {
                    ToastUtils.error("动态IP设置失败！errMeg=" + commonValue2.getRemarks());
                }
                break;
            case FKey.KEY_IP_SET_STATIC:
                CommonValue commonValue = FEthTools.setStaticIp(new IpConfigInfo("192.168.1.155", "8.8.8.8", "8.8.4.4", "192.168.1.1", "255.255.255.0"));
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success("静态IP设置成功！");
                } else {
                    ToastUtils.error("静态IP设置失败！errMeg=" + commonValue.getRemarks());
                }
                break;
            case FKey.KEY_SCREENSHOT:
                String imgPath = FScreentTools.takeScreenshot("/sdcard/");
                if (imgPath != null && !imgPath.equals("")) {
                    ToastUtils.success("截屏成功,位置:/sdcard/目录下");
                } else {
                    ToastUtils.error("截屏失败！");
                }
                break;
            case FKey.KEY_KEEPALIVE:
                context.startActivity(new Intent(context, KeepAliveAty.class));
                break;
            case FKey.KEY_OTA:
                showOtaUpgrade();
                break;
            case FKey.KEY_MORE:
                break;


        }
    }

    //内部类，绑定控件
    class NoBgViewHolder extends RecyclerView.ViewHolder {
        TextView tvOneStr;

        public NoBgViewHolder(View itemView) {
            super(itemView);
            tvOneStr = (TextView) itemView.findViewById(R.id.tvOneStr);
        }
    }

    //内部类，绑定控件
    class OneViewHolder extends RecyclerView.ViewHolder {
        TextView tvOneStr;

        public OneViewHolder(View itemView) {
            super(itemView);
            tvOneStr = (TextView) itemView.findViewById(R.id.tvOneStr);
        }
    }

    //内部类，绑定控件
    class TwoViewHolder extends RecyclerView.ViewHolder {
        TextView tv_one;
        TextView tv_two;

        public TwoViewHolder(View itemView) {
            super(itemView);
            tv_one = (TextView) itemView.findViewById(R.id.btn_one);
            tv_two = (TextView) itemView.findViewById(R.id.btn_two);
        }
    }

    /**
     * ota升级 确保sdcard目录存在update.zip固件
     */
    public static void showOtaUpgrade() {
        File file = new File("/sdcard/update.zip");
        if (file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseIotUtils.getContext());
            builder.setTitle("提示:");
            builder.setMessage("进行固件升级吗？点击确认后请等待...");
            builder.setIcon(R.drawable.logo_splash);
            builder.setCancelable(true);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FUpgradeTools.firmwareUpgrade("/sdcard/update.zip", new FUpgradeTools.UpgradeInterface() {
                        @Override
                        public void operating(InstallStatus installStatus) {
                            Log.d(TAG, "operating: " + installStatus.name());
                        }

                        @Override
                        public void error(String errInfo) {
                            Log.d(TAG, "error: ");
                        }
                    });
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            ToastUtils.error("/sdcard/目录下未找到update.zip文件！");
        }
    }

}