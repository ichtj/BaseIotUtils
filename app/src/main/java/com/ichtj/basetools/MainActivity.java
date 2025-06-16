package com.ichtj.basetools;

import android.content.Intent;;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.chtj.base_framework.FStorageTools;
import com.chtj.base_framework.entity.CommonValue;
import com.chtj.base_framework.entity.IpConfigInfo;
import com.chtj.base_framework.entity.Space;
import com.chtj.base_framework.network.FEthTools;
import com.chtj.base_framework.network.FLteTools;
import com.face_chtj.base_iotutils.AppsUtils;
import com.face_chtj.base_iotutils.AudioUtils;
import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.DeviceUtils;
import com.face_chtj.base_iotutils.DisplayUtils;
import com.face_chtj.base_iotutils.FileDialogSelectUtils;
import com.face_chtj.base_iotutils.GlobalDialogUtils;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.NotifyUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.TPoolSingleUtils;
import com.face_chtj.base_iotutils.TPoolUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.face_chtj.base_iotutils.UriPathUtils;
import com.face_chtj.base_iotutils.callback.IDismissListener;
import com.face_chtj.base_iotutils.code.CodeUtils;
import com.face_chtj.base_iotutils.view.OnPopupItemClickListener;
import com.ichtj.basetools.allapp.AllAppAty;
import com.ichtj.basetools.audio.AudioAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.bluetooth.BlueToothAty;
import com.ichtj.basetools.callback.IUsbHubListener;
import com.ichtj.basetools.crash.CrashTools;
import com.ichtj.basetools.crash.MyService;
import com.ichtj.basetools.dialog.DialogAty;
import com.ichtj.basetools.download.FileDownLoadAty;
import com.ichtj.basetools.entity.ExcelEntity;
import com.ichtj.basetools.file.FileOperatAty;
import com.ichtj.basetools.greendao.GreenDaoSqliteAty;
import com.ichtj.basetools.hid.HidMainDevAty;
import com.ichtj.basetools.hid.HidSubDevAty;
import com.ichtj.basetools.install.InstallAPkAty;
import com.ichtj.basetools.keeplive.KeepAliveAty;
import com.ichtj.basetools.network.NetChangeAty;
import com.ichtj.basetools.network.NetRecordAty;
import com.ichtj.basetools.nginx.NginxAty;
import com.ichtj.basetools.screen.ScreenActivity;
import com.ichtj.basetools.serialport.SerialPortAty;
import com.ichtj.basetools.sign.ApkSignSearchAty;
import com.ichtj.basetools.socket.SocketAty;
import com.ichtj.basetools.timer.TimerAty;
import com.ichtj.basetools.touch.TouchDetectAty;
import com.face_chtj.base_iotutils.view.PopupWindowTools;
import com.ichtj.basetools.util.BasicTools;
import com.ichtj.basetools.util.CustomButtonGridView;
import com.ichtj.basetools.util.FKey;
import com.ichtj.basetools.util.JXLExcelUtils;
import com.ichtj.basetools.util.OptionTools;
import com.ichtj.basetools.util.PACKAGES;
import com.ichtj.basetools.util.POIExcelUtils;
import com.ichtj.basetools.util.TableFileUtils;
import com.ichtj.basetools.util.UsbHubTools;
import com.ichtj.basetools.video.PlayCacheVideoAty;
import com.ichtj.basetools.video.VideoPlayAty;
import com.ichtj.basetools.webviews.WebViewAty;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(path = PACKAGES.BASE + "basetools")
public class MainActivity extends BaseActivity implements CustomButtonGridView.OnButtonClickListener {
    private static final String TAG = MainActivity.class.getSimpleName ( );
    private CustomButtonGridView customButtonGridView;
    private static final int REQUEST_CODE_INSTALL_UNKNOWN_APPS = 1234;
    private int VIEW_FALG=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main_aty);
        customButtonGridView = findViewById (R.id.customButtonGridView);
        customButtonGridView.setButtonMap (getDisplayBtn ( ));
        customButtonGridView.setNumColumns (2); // 设置每列显示2个按钮
        customButtonGridView.setOnButtonClickListener (this);
    }

    public Map<Integer, String> getDisplayBtn() {
        Map<Integer, String> btnList = new HashMap<> ( );
        Space ramSpace = FStorageTools.getRamSpace (FStorageTools.TYPE_MB);
        Space sdSpace = FStorageTools.getSdcardSpace (FStorageTools.TYPE_MB);
        btnList.put (FKey.KEY_IMEI, "IMEI：" + DeviceUtils.getImeiOrMeid ( ));
        btnList.put (FKey.KEY_ICCID, "ICCID：" + NetUtils.getLteIccid ( ));
        btnList.put (FKey.KEY_SERIAL, getString (R.string.main_serial, OptionTools.getSerialNo ( )));
        btnList.put (FKey.KEY_NET_TYPE, getString (R.string.main_nettype, NetUtils.getNetWorkTypeName ( )));
        btnList.put (FKey.KEY_APK_VERSION, getString (R.string.main_apk_version, AppsUtils.getAppVersionName ( )));
        btnList.put (FKey.KEY_IS_ROOT, getString (R.string.main_rooted, AppsUtils.isRoot ( ) + ""));
        btnList.put (FKey.KEY_LOCAL_IP, getString (R.string.main_localip, NetUtils.getLocalIp ( )));
        btnList.put (FKey.KEY_FW_VERSION, getString (R.string.main_fw_version, DeviceUtils.getFwVersion ( )));
        btnList.put (FKey.KEY_RAM, getString (R.string.main_running_memory, ramSpace.getTotalSize ( ) + "MB/" + ramSpace.getUseSize ( ) + "MB/" + ramSpace.getAvailableSize ( ) + "MB"));
        btnList.put (FKey.KEY_SD_SPACE, getString (R.string.main_sdcard_memory, sdSpace.getTotalSize ( ) + "MB/" + sdSpace.getUseSize ( ) + "MB/" + sdSpace.getAvailableSize ( ) + "MB"));
        btnList.put (FKey.KEY_ETH_MODE, getString (R.string.main_eth_mode, FEthTools.getIpMode (BaseIotUtils.getContext ( ))));
        btnList.put (FKey.KEY_DBM, getString (R.string.main_lte_dbm, FLteTools.getDbm ( )));
        btnList.put (FKey.KEY_SERIAL_PORT, getString (R.string.main_serial_rw));
        btnList.put (FKey.KEY_TIMERD, getString (R.string.main_timer));
        btnList.put (FKey.KEY_SCREEN, getString (R.string.main_screen_about));
        btnList.put (FKey.KEY_FILE_RW, getString (R.string.main_file_rw));
        btnList.put (FKey.KEY_NETWORK, getString (R.string.main_net_listener));
        btnList.put (FKey.KEY_RESET_MONITOR, getString (R.string.main_net_reset_listener));
        btnList.put (FKey.KEY_FILEDOWN, getString (R.string.main_file_down));
        btnList.put (FKey.KEY_TCP_UDP, getString (R.string.main_tcp_udp));
        btnList.put (FKey.KEY_NOTIFY_SHOW, getString (R.string.main_notify_enable));
        btnList.put (FKey.KEY_NOTIFY_CLOSE, getString (R.string.main_notify_disable));
        btnList.put (FKey.KEY_SYS_DIALOG_SHOW, getString (R.string.main_sys_dialog_enable));
        btnList.put (FKey.KEY_SYS_DIALOG_CLOSE, getString (R.string.main_sys_dialog_disable));
        btnList.put (FKey.KEY_DIALOG, getString (R.string.main_dialog_box));
        btnList.put (FKey.KEY_TOAST, getString (R.string.main_normal_toast));
        btnList.put (FKey.KEY_TOAST_BG, getString (R.string.main_bitmap_toast));
        btnList.put (FKey.KEY_ERR_ANR, getString (R.string.main_test_anr));
        btnList.put (FKey.KEY_ERR_OTHER, getString (R.string.main_test_other_ex));
        btnList.put (FKey.KEY_USB_HUB, getString (R.string.main_usb_dev_listener));
        btnList.put (FKey.KEY_USB_HUB_UNREGIST, getString (R.string.main_usb_dev_release));
        btnList.put (FKey.KEY_GREEN_DAO, getString (R.string.main_test_greendao));
        btnList.put (FKey.KEY_JXL_OPEN, getString (R.string.main_jxl_open_excel));
        btnList.put (FKey.KEY_JXL_EXPORT, getString (R.string.main_jxl_export_excel));
        btnList.put (FKey.KEY_POI_OPEN, getString (R.string.main_poi_open_excel));
        btnList.put (FKey.KEY_POI_EXPORT, getString (R.string.main_poi_export_excel));
        btnList.put (FKey.KEY_APP_LIST, getString (R.string.main_app_list));
        btnList.put (FKey.KEY_VIDEO, getString (R.string.main_play_video));
        btnList.put (FKey.KEY_URL_CONVERT, getString (R.string.main_uri_to_path));
        btnList.put (FKey.KEY_ASSETS, getString (R.string.get_assets_file));
        btnList.put (FKey.KEY_AUDIO, getString (R.string.main_play_audio));
        btnList.put (FKey.KEY_IP_SET_STATIC, getString (R.string.main_set_staticip));
        btnList.put (FKey.KEY_IP_SET_DHCP, getString (R.string.main_set_dhcpip));
        btnList.put (FKey.KEY_SCREENSHOT, getString (R.string.main_screenshot));
        btnList.put (FKey.KEY_KEEPALIVE, getString (R.string.main_as_keepalive));
        btnList.put (FKey.KEY_OTA, getString (R.string.main_upgrade_ota));
        btnList.put (FKey.KEY_INSTALL, getString (R.string.main_silent_installation));
        btnList.put (FKey.KEY_BLUETOOTH, getString (R.string.main_test_bluetooth));
        btnList.put (FKey.VIDEO_CACHE, getString (R.string.main_record_video));
        btnList.put (FKey.KEY_CRASH, getString (R.string.main_crash_verification));
        btnList.put (FKey.KEY_NGINX, getString (R.string.main_nginx));
        btnList.put (FKey.KEY_SUB_DEV_HID, getString (R.string.main_sub_hid_dev));
        btnList.put (FKey.KEY_MAIN_DEV_HID, getString (R.string.main_hid_dev));
        btnList.put (FKey.KEY_APK_SIGN, getString (R.string.main_sign));
        btnList.put (FKey.KEY_TOUCH_DETECT, getString (R.string.main_touch_check));
        btnList.put (FKey.KEY_MQTT_TEST, getString (R.string.main_test_mqtt));
        btnList.put (FKey.KEY_WEBVIEW_TEST, getString (R.string.main_test_webview));
        btnList.put (FKey.KEY_POPWINDOW, getString(R.string.main_popwindow_toast));
        btnList.put (FKey.KEY_DROP_POPWINDOW, getString(R.string.main_pull_down_option_box));
        btnList.put (FKey.KEY_QR_CODE, getString(R.string.main_qrcode_create));
        btnList.put (FKey.KEY_ORIENTATION, getString(R.string.main_screen_orientation));
        btnList.put (FKey.KEY_FILE_SELECT, getString(R.string.main_file_dialog_select));
        return btnList;
    }

    @Override
    public void onButtonClick(int position, String buttonText) {
        switch (position) {
            case FKey.KEY_NOTIFY_SHOW:
                //获取系统中是否已经通过 允许通知的权限
                if (NotifyUtils.notifyIsEnable ( )) {
                    NotifyUtils.setNotifyId (111).setEnableCloseButton (true)
                            .setOnNotifyLinstener (new IDismissListener ( ) {
                                @Override
                                public void dismiss(boolean dismiss) {
                                    KLog.d (TAG, "dismiss=" + dismiss);
                                }
                            })
                            .replaceClickIntent (new Intent (this, FileDownLoadAty.class))
                            .setAppName ("BaseIotUtils")
                            .setAppAbout (AppsUtils.getAppVersionName ( ))
                            .setPrompt ("this a prompt")
                            .setProgress ("this a progress")
                            .setTopRight ("xxxx")
                            .setDataTime ("2022-04-18")
                            .setRemarks ("this is a remarks")
                            .exeuNotify ( );
                } else {
                    NotifyUtils.toOpenNotify ( );
                }
                Handler handler = new Handler ( );
                handler.postDelayed (new Runnable ( ) {
                    @Override
                    public void run() {
                        NotifyUtils.setAppName ("");
                        NotifyUtils.setAppAbout ("");
                        NotifyUtils.setRemarks ("");
                        NotifyUtils.setPrompt ("");
                        NotifyUtils.setDataTime ("");
                        NotifyUtils.setTopRight ("");
                        NotifyUtils.setIvStatus (true, R.drawable.failed);
                    }
                }, 5000);
                break;
            case FKey.KEY_NOTIFY_CLOSE:
                NotifyUtils.closeNotify ( );
                break;
            case FKey.KEY_SYS_DIALOG_SHOW:
                GlobalDialogUtils.getInstance ( ).show (getString(R.string.app_name));
                break;
            case FKey.KEY_SYS_DIALOG_CLOSE:
                GlobalDialogUtils.getInstance ( ).dismiss ( );
                break;
            case FKey.KEY_TOAST:
                ToastUtils.showShort (getString(R.string.app_name));
                break;
            case FKey.KEY_TOAST_BG:
                ToastUtils.success (getString(R.string.app_name));
                break;
            case FKey.KEY_ERR_ANR:
                stopService (new Intent (this, MyService.class));
                startService (new Intent (this, MyService.class));
                break;
            case FKey.KEY_ERR_OTHER:
                int i = 1 / 0;
                break;
            case FKey.KEY_USB_HUB:
                ToastUtils.info (getString (R.string.main_start_usb_listener));
                UsbHubTools.getInstance ( ).registerReceiver ( );
                UsbHubTools.getInstance ( ).setIUsbDeviceListener (new IUsbHubListener ( ) {
                    @Override
                    public void deviceInfo(String action, String path, boolean isConn) {
                        ToastUtils.info ("path:" + path + ",isConn=" + isConn);
                    }
                });
                break;
            case FKey.KEY_USB_HUB_UNREGIST:
                ToastUtils.info (getString (R.string.main_close_usb_listener));
                UsbHubTools.getInstance ( ).unRegisterReceiver ( );
                break;
            case FKey.KEY_JXL_OPEN:
                ToastUtils.info (getString(R.string.main_to_search_jxl));
                TPoolUtils.newInstance ( ).addExecuteTask (new Runnable ( ) {
                    @Override
                    public void run() {
                        try {
                            InputStream input = getAssets ( ).open ("table.xls");
                            if (input != null) {
                                TableFileUtils.writeToLocal (Environment.getExternalStorageDirectory ( ) + "/table.xls", input);
                            }
                            //第一种jxl.jar 只能读取xls
                            List<ExcelEntity> readExcelDatas =
                                    JXLExcelUtils.readExcelxlsx (Environment.getExternalStorageDirectory ( ) + "/table.xls");
                            KLog.d (TAG, "readDataSize: " + readExcelDatas.size ( ));
                        } catch (Exception e) {
                            e.printStackTrace ( );
                            KLog.e (TAG, "errMeg:" + e.getMessage ( ));
                        }
                    }
                });
                break;
            case FKey.KEY_JXL_EXPORT:
                //第一种 jxl.jar导出
                JXLExcelUtils.exportExcel ( );
                ToastUtils.success (getString(R.string.main_export_succ));
                break;
            case FKey.KEY_POI_OPEN:
                ToastUtils.info (getString(R.string.main_to_search_jxl));
                TPoolUtils.newInstance ( ).addExecuteTask (new Runnable ( ) {
                    @Override
                    public void run() {
                        try {
                            InputStream input = getAssets ( ).open ("table.xls");
                            if (input != null) {
                                TableFileUtils.writeToLocal (Environment.getExternalStorageDirectory ( ) + "/table.xls", input);
                            }
                            //poi.jar 可以读取xls xlsx 两种
                            List<ExcelEntity> readExcelDatas =
                                    POIExcelUtils.readExcel (Environment.getExternalStorageDirectory ( ) + "/table.xls");
                            KLog.d (TAG, "readDataSize: " + readExcelDatas.size ( ));
                        } catch (Exception e) {
                            e.printStackTrace ( );
                            KLog.e (TAG, "errMeg:" + e.getMessage ( ));
                        }
                    }
                });
                break;
            case FKey.KEY_POI_EXPORT:
                ToastUtils.info (getString(R.string.main_to_search_jxl));
                TPoolUtils.newInstance ( ).addExecuteTask (new Runnable ( ) {
                    @Override
                    public void run() {
                        //poi.jar导出
                        boolean isOK = POIExcelUtils.createExcelFile ( );
                        KLog.d (TAG, "isOK: " + isOK);
                    }
                });
                break;
            case FKey.KEY_URL_CONVERT:
                Intent intent = new Intent (Intent.ACTION_GET_CONTENT);
                intent.setType ("*/*");
                intent.addCategory (Intent.CATEGORY_OPENABLE);
                startActivityForResult (Intent.createChooser (intent, getString(R.string.main_select_file)), FILE_SELECT_CODE);
                break;
            case FKey.KEY_ASSETS:
                try {
                    InputStream input = this.getAssets ( ).open ("table.xls");
                    if (input != null) {
                        ToastUtils.success (getString(R.string.main_found_table_succ));
                    } else {
                        ToastUtils.success (getString(R.string.main_not_found_table_failed));
                    }
                } catch (Exception e) {
                    e.printStackTrace ( );
                    KLog.e (TAG, "errMeg:" + e.getMessage ( ));
                }
                break;
            case FKey.KEY_IP_SET_DHCP:
                CommonValue commonValue2 = FEthTools.setEthDhcp ( );
                if (commonValue2 == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success (getString(R.string.main_set_dhcp_succ));
                } else {
                    ToastUtils.error ("动态IP设置失败！errMeg=" + commonValue2.getRemarks ( ));
                }
                break;
            case FKey.KEY_IP_SET_STATIC:
                CommonValue commonValue = FEthTools.setStaticIp (new IpConfigInfo ("192.168.1.155",
                        "8.8.8.8", "8.8.4.4", "192.168.1.1", "255.255.255.0"));
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    ToastUtils.success (getString(R.string.main_set_static_succ));
                } else {
                    ToastUtils.error ("静态IP设置失败！errMeg=" + commonValue.getRemarks ( ));
                }
                break;
            case FKey.KEY_SCREENSHOT:
                String filePath="/sdcard/"+TimeUtils.getTodayDateHms("yyyyMMddHHmmss")+".png";
                boolean isSucc = DisplayUtils.screenshot(filePath);
                if (isSucc) {
                    ToastUtils.success (getString(R.string.main_screenshot_succ_toast));
                } else {
                    ToastUtils.error (getString(R.string.main_screenshot_failed));
                }
                break;
            case FKey.KEY_CRASH:
                CrashTools.crashtest ( );
                break;
            case FKey.KEY_OTA:
                OptionTools.showOtaUpgrade ( );
                break;
            case FKey.KEY_SERIAL_PORT:
                startAty (SerialPortAty.class);
                break;
            case FKey.KEY_TIMERD:
                startAty (TimerAty.class);
                break;
            case FKey.KEY_SCREEN:
                startAty (ScreenActivity.class);
                break;
            case FKey.KEY_FILE_RW:
                startAty (FileOperatAty.class);
                break;
            case FKey.KEY_NETWORK:
                startAty (NetChangeAty.class);
                break;
            case FKey.KEY_RESET_MONITOR:
                startAty (NetRecordAty.class);
                break;
            case FKey.KEY_FILEDOWN:
                startAty (FileDownLoadAty.class);
                break;
            case FKey.KEY_TCP_UDP:
                startAty (SocketAty.class);
                break;
            case FKey.KEY_GREEN_DAO:
                startAty (GreenDaoSqliteAty.class);
                break;
            case FKey.KEY_APP_LIST:
                startAty (AllAppAty.class);
                break;
            case FKey.KEY_VIDEO:
                startAty (VideoPlayAty.class);
                break;
            case FKey.KEY_AUDIO:
                startAty (AudioAty.class);
                break;
            case FKey.KEY_KEEPALIVE:
                startAty (KeepAliveAty.class);
                break;
            case FKey.KEY_INSTALL:
                startAty (InstallAPkAty.class);
                break;
            case FKey.KEY_BLUETOOTH:
                startAty (BlueToothAty.class);
                break;
            case FKey.VIDEO_CACHE:
                startAty (PlayCacheVideoAty.class);
                break;
            case FKey.KEY_NGINX:
                startAty (NginxAty.class);
                break;
            case FKey.KEY_DIALOG:
                startAty (DialogAty.class);
                break;
            case FKey.KEY_ICCID:

                break;
            case FKey.KEY_IMEI:

                break;
            case FKey.KEY_SUB_DEV_HID:
                startAty (HidSubDevAty.class);
                break;
            case FKey.KEY_MAIN_DEV_HID:
                startAty (HidMainDevAty.class);
                break;
            case FKey.KEY_APK_SIGN:
                startAty (ApkSignSearchAty.class);
                break;
            case FKey.KEY_TOUCH_DETECT:
                startAty (TouchDetectAty.class);
                break;
            case FKey.KEY_WEBVIEW_TEST:
                startAty (WebViewAty.class);
                break;
            case FKey.KEY_POPWINDOW:
                ++VIEW_FALG;
                int gravityValue=Gravity.TOP;
                if (VIEW_FALG==1){
                    gravityValue=Gravity.BOTTOM;
                }else if(VIEW_FALG==2){
                    gravityValue=Gravity.LEFT;
                }else if(VIEW_FALG==3){
                    gravityValue=Gravity.RIGHT;
                }else if(VIEW_FALG>=4){
                    gravityValue=Gravity.TOP;
                    VIEW_FALG=0;
                }
                PopupWindowTools bubblePopupWindow = new PopupWindowTools(MainActivity.this);
                bubblePopupWindow.setBubbleText(getString(R.string.main_pop_remarks));
                bubblePopupWindow.show(customButtonGridView.getSelectButton(), gravityValue);//view的上部展示
                break;
            case FKey.KEY_DROP_POPWINDOW:
                List<String> options = Arrays.asList("选项A", "选项B", "选项C");
                PopupWindowTools.showDropdownPopup(this, customButtonGridView.getSelectButton(), options, new OnPopupItemClickListener() {
                    @Override
                    public void onItemClick(int position, String itemText) {
                        // 这里你可以接收到点击项的 position 和文本
                        Toast.makeText(MainActivity.this, "点击了第 " + position + " 项：" + itemText, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case FKey.KEY_QR_CODE:
                BasicTools.showTwoScaledBitmapsDialog(this,CodeUtils.createBarCode("www.baidu.com",200,100),CodeUtils.createQRCode("www.baidu.com",200, ContextCompat.getColor(this,R.color.black)));
                break;
            case FKey.KEY_ORIENTATION:
                ShellUtils.CommandResult rotationResult= ShellUtils.execCommand("settings get system user_rotation",true);
                Log.d(TAG, "rotationResult: result>>"+rotationResult.result+",succ>>"+rotationResult.successMsg+",err>>"+rotationResult.errorMsg);
                if (rotationResult.result==0&& !TextUtils.isEmpty(rotationResult.successMsg)){
                    int rotationValue=0;
                    try {
                        rotationValue=Integer.parseInt(rotationResult.successMsg);
                    }catch (Throwable throwable){
                    }
                    rotationValue++;
                    if (rotationValue>3){
                        rotationValue=0;
                    }
                    DisplayUtils.changeRotation(rotationValue);
                }
                break;
            case FKey.KEY_FILE_SELECT:
                FileDialogSelectUtils fileDialogSelectUtils =new FileDialogSelectUtils(this, new File("/sdcard/"), new FileDialogSelectUtils.FileSelectCallback() {
                    @Override
                    public void onFileSelected(List<File> selected) {
                        Log.d(TAG, "onFileSelected: "+selected);
                    }
                }).setSizeRatio(0.3f,0.5f);
                fileDialogSelectUtils.show();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        if (data == null) {
            // 用户未选择任何文件，直接返回
            ToastUtils.error (getString(R.string.main_no_files_were_selected));
            return;
        }
        if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData ( ); // 获取用户选择文件的URI
            String filePath = UriPathUtils.getPath (uri);
            KLog.d (TAG, "filePath=" + filePath + ",uri.getPath()=" + uri.getPath ( ));
            ToastUtils.success ("文件地址:" + filePath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ( );
        NotifyUtils.closeNotify ( );
        GlobalDialogUtils.getInstance ( ).dismiss ( );
        UsbHubTools.getInstance ( ).unRegisterReceiver ( );
        AudioUtils.getInstance ( ).stopPlaying ( );
        TPoolSingleUtils.shutdown ( );
        FLteTools.cancel ( );
    }
}
