package com.ichtj.basetools.allapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chtj.base_framework.FIPTablesTools;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.entity.AppEntity;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.util.TrafficStatistics;

import java.util.List;

/**
 * Create on 2020/6/29
 * author chtj
 * desc
 */
public class AllAppAdapter extends RecyclerView.Adapter<AllAppAdapter.MyViewHolder> {
    private List<AppEntity> list;

    public AllAppAdapter(List<AppEntity> list) {
        this.list = list;
    }

    public void setList(List<AppEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(BaseIotUtils.getContext()).inflate(R.layout.item_app, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String pkgName=list.get(position).packageName;
        holder.tvAppName.setText(list.get(position).appName);
        holder.tvPackName.setText(pkgName);
        holder.tvUid.setText("UID:" + list.get(position).uid + "");
        holder.ivAppIcon.setImageDrawable(list.get(position).icon);
        KLog.d(" uid= " + list.get(position).uid+ ",sourceDir= "+list.get(position).sourceDir+",pkg= "+list.get(position).packageName);
        holder.tvAppPath.setText(list.get(position).sourceDir);
        //4.4系统获取流量
        double traffic = TrafficStatistics.getUidFlow(list.get(position).uid);
        double sumTraffic = TrafficStatistics.getDouble(traffic / 1024 / 1024);
        holder.tvTraffic.setText("use:" + sumTraffic + "M");
        holder.tvVersion.setText("v:" + list.get(position).versionName + "." + list.get(position).versionCode);
        //7.1.2系统获取流量
        //long total= FNetworkTools.getEthAppUsageByUid(list.get(position).getUid(),FNetworkTools.getTimesMonthMorning(), FNetworkTools.getNow());
        //String totalPhrase = Formatter.formatFileSize(BaseIotUtils.getContext(), total);
        //((MyViewHolder) holder).tvTraffic.setText("流量消耗:" + totalPhrase);
        final int posiNum = position;
        holder.tvStartApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KLog.d("PackageName: " + list.get(posiNum).packageName);
                try {
                    AppsUtils.openPackage(list.get(posiNum).packageName);
                } catch (Exception e) {
                    ToastUtils.error("打开错误!");
                }
            }
        });
        //复制到剪切板
        holder.tvCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) BaseIotUtils.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                StringBuilder sb = new StringBuilder();
                sb.append("appName：" + list.get(posiNum).appName + "\n");
                sb.append("pkgName：" + list.get(posiNum).packageName + "\n");
                sb.append("appPath：" + list.get(posiNum).sourceDir+ "\n");
                sb.append("version：" + list.get(posiNum).versionCode + "\n");
                sb.append("isSys：" + list.get(posiNum).isSystemApp);
                // 将ClipData内容放到系统剪贴板里。
                ClipData mClipData = ClipData.newPlainText("Label", sb.toString());
                cm.setPrimaryClip(mClipData);
                ToastUtils.success(sb.toString());
            }
        });
        holder.tvToAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent localIntent = new Intent();
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    localIntent.setData(Uri.fromParts("package", list.get(posiNum).packageName, null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    localIntent.setAction(Intent.ACTION_VIEW);
                    localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    localIntent.putExtra("com.android.settings.ApplicationPkgName", list.get(posiNum).packageName);
                }
                BaseIotUtils.getContext().startActivity(localIntent);
            }
        });
        holder.tvUnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list.get(posiNum).isSystemApp){
                    ToastUtils.info("系统系统请使用静默卸载！");
                }else{
                    AppsUtils.uninstall(list.get(posiNum).packageName);
                }
            }
        });
        holder.tvSilence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPath=AppsUtils.getAppPath(pkgName);
                boolean isSys=list.get(posiNum).isSystemApp;
                String appName=list.get(posiNum).appName;
                if(isSys){
                    String []appInstallInfo=appPath.split("/");
                    appName=appInstallInfo[appInstallInfo.length-1].replace(".apk","");
                }
                if(!TextUtils.isEmpty(appName)){
                    KLog.d("onClick() appName >> "+appName);
                    boolean isOk = AppsUtils.uninstallSilent(isSys,false,appName, list.get(posiNum).packageName);
                    if (isOk) {
                        ToastUtils.success("卸载成功,如果是系统应用请重启查看！");
                        list.remove(list.get(posiNum));
                        notifyDataSetChanged();
                    } else {
                        ToastUtils.error("卸载失败");
                    }
                }else{
                    ToastUtils.error("未获取到APP名称,请重试！");
                }
            }
        });
        holder.tvEnableNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isClearResult = FIPTablesTools.allowAppInternet(list.get(posiNum).packageName);
                ToastUtils.info(isClearResult?"启用成功,该应用可正常上网！":"启用失败,请重试！");
            }
        });
        holder.tvDisableNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPutComplete = FIPTablesTools.blockAppInternet(list.get(posiNum).packageName);
                ToastUtils.info(isPutComplete?"禁用成功,该应用无法上网！":"禁用失败,请重试！");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAppName,tvAppPath, tvSilence, tvPackName, tvUid, tvCopy, tvToAppInfo, tvStartApp, tvTraffic, tvUnInstall, tvEnableNet, tvDisableNet, tvVersion;
        public ImageView ivAppIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvPackName = itemView.findViewById(R.id.tvPackName);
            tvCopy = itemView.findViewById(R.id.tvCopy);
            tvToAppInfo = itemView.findViewById(R.id.tvToAppInfo);
            tvUid = itemView.findViewById(R.id.tvUid);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvStartApp = itemView.findViewById(R.id.tvStartApp);
            tvTraffic = itemView.findViewById(R.id.tvTraffic);
            tvUnInstall = itemView.findViewById(R.id.tvUnInstall);
            tvEnableNet = itemView.findViewById(R.id.tvEnableNet);
            tvDisableNet = itemView.findViewById(R.id.tvDisableNet);
            tvVersion = itemView.findViewById(R.id.tvVersion);
            tvSilence = itemView.findViewById(R.id.tvSilence);
            tvAppPath = itemView.findViewById(R.id.tvAppPath);
        }
    }
}