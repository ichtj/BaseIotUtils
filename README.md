# 接入方式

## app 界面截图

### 下面有使用方式,或下载后手动导入使用，其中的base_iotuitls工具类可以普遍适用，而其他工具 module 可能需要系统支持或者 root 情况下适用

![image](/pic/apppic.png)

## Android 项目根目录下文件 build.gradle 中添加

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

## 以下存在三个library按需选择,在App的build.gradle文件中添加

### ① base_iotutils 物联基础工具类 [![](https://jitpack.io/v/wave-chtj/BaseIotUtils.svg)](https://jitpack.io/#wave-chtj/BaseIotUtils)

```groovy
dependencies {
         //多个物联基础工具类,推荐使用import module导入的方式
         implementation 'com.github.wave-chtj:BaseIotUtils:1.0.6'
}
```

### ② base_socket socket tcp/udp 通信

```groovy
dependencies {
         //Socket TCP|UDP,推荐使用import module导入的方式
         implementation 'com.chtj.base_socket:base_socket:1.0.2'
}
```

### ③ base_framework 系统api调用 [![](https://jitpack.io/v/wave-chtj/BaseFramework.svg)](https://jitpack.io/#wave-chtj/BaseFramework)

```groovy
dependencies {
         //framework API调用,推荐使用import module导入的方式
		implementation 'com.github.wave-chtj:BaseFramework:1.0.1'
}
```

### ④ base_keepalive 服务保活处理 [![](https://jitpack.io/v/wave-chtj/BaseKeepAlive.svg)](https://jitpack.io/#wave-chtj/BaseKeepAlive)

```groovy
dependencies {
         //android 保活,推荐使用import module导入的方式
	     implementation 'com.github.wave-chtj:BaseKeepAlive:1.0.1'
}
```

### 自定义 Application

```java
//每个Module library功能描述可在页面下方查看
//别忘了在 Manifest 中通过使用这个自定义的 Application,这里有各个library的初始化
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    //① base_iotutils 物联基础工具类
    BaseIotUtils
      .instance()
      .setBaseScreenParam(1080, 1920, true)
      .setCreenType(SCREEN_TYPE.WIDTH)
      .create(getApplication()); //按照宽度适配

    //② base_socket socket tcp/udp 通信 无需在这里初始化,直接使用即可

    //③ base_framework 系统 api 调用
    FBaseTools.instance().create(getApplication());
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
    //④ base_keepalive 服务保活处理
    FBaseDaemon.init(base);
  }
}

```

## ① base_iotutils 物联基础工具类说明

| 编号 | 工具类                         | 工具名称          | 实现功能                      |
| ---- | ------------------------------ | ----------------- | ----------------------------- |
| 1    | ZipUtils                       | 压缩相关          | 压缩解压,批量等               |
| 2    | UriPathUtils                   | Uri 转真实路径    | android7.0uri 转换            |
| 3    | ToastUtils                     | 系统的 Toast 封装 | 成功,失败，警告等提示         |
| 4    | TimeUtils                      | 时间工具类        | Date 时间日期转换             |
| 5    | ISysDialog                     | 应用上层弹窗      | SYSTEM_ALERT_WINDOW           |
| 6    | SPUtils                        | 存储工具类        | SharedPreferences 读写        |
| 7    | ShellUtils                     | adb 相关工具类    | adb 命令执行                  |
| 8    | ServiceUtils                   | Service 管理工具  | 启动,停止,判断存活等          |
| 9    | PermissionsUtils               | 权限申请工具类    | 多权限申请                    |
| 10   | KLog                           | 日志打印          | 日志标记打印                  |
| 11   | KeyBoardUtils                  | 软键盘管理        | 打开,关闭                     |
| 12   | FileUtils                      | 文件工具类        | 写入/读取文件相关信息         |
| 13   | DeviceUtils                    | 设备的相关信息    | 设备出厂自带参数              |
| 14   | DataConvertUtils               | 进制转换工具类    | 10/16 进制,字节数组等相互转换 |
| 15   | ScheduledTPoolUtils/TPoolUtils | 线程池管理        | 线程重用                      |
| 16   | SerialPort/SerialPortFinder    | 串口相关工具类    | 打开,通讯,关闭                |
| 17   | AdaptScreenUtils               | 屏幕适配          | pt 单位适配                   |
| 18   | NotifyUtils                    | notification      | 自定义 notification,动态调参  |
| 19   | NetUtils                       | 网络工具类        | 网络类型/状态等获取           |
| 20   | NetListenerUtils               | 网络变化广播      | 网络变化回调                  |
| 22   | DownloadSupport                | 多任务下载管理    | 下载,暂停,状态回调            |
| 23   | PlayUtils                      | 音频管理          | 播放继续暂停                  |
| 24   | AppsUtils                      | pp 的相关         | 查询应用以及 app 的信息       |
| 25   | ScreenInfoUtils                | 屏幕相关          | 屏幕信息获取(高宽像素等)      |
| 26   | StatusBarUtil                  | 沉浸式状态栏      | 状态栏变色                    |

## base_iotutils 工具调用方式,及图片展示

#### FileUtils 文件操作 读写,删除,文件大小等

```java
        //@filePath 文件路径 例如/sdcard/config.txt
        //@content 写入内容
        //@isCover 是否覆盖这个文件里的内容
        boolean writeResult = FileUtils.writeFileData(filePath, content, true);
        //读取filePath文件中的内容
        String readResult = FileUtils.readFileData(filePath);
        //更多文件操作方法请查询FileUtils中的内容
```

#### KeyBoardUtils 软键盘管理

```java
       //打卡软键盘
       KeyBoardUtils.openKeybord(editeTextView);

       //关闭软键盘
       KeyBoardUtils.closeKeybord(editeTextView);
```

#### NetUtils 网络工具类

```java
        //得到网络类型 NETWORK_NO,NETWORK_WI,NETWORK_2G,NETWORK_3G,NETWORK_4G,NETWORK_UN,NETWORK_ETH
        NetUtils.getNetWorkType();
        //得到网络类型字符串
        NetUtils.getNetWorkTypeName();
        //判断网络连接是否可用
        NetUtils.isNetworkAvailable();
        //判断网络是否可用
        NetUtils.isAvailable();
        //判断网络是否连接
        NetUtils.isConnected();
        //判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）不要在主线程使用，会阻塞线程
        NetUtils.ping();  NetUtils.ping(int count,int time); NetUtils.ping(String ip);
        //判断WIFI是否打开
        NetUtils.isWifiEnabled();
        //判断网络连接方式是否为WIFI
        NetUtils.isWifi();
        //判断网络连接方式是否为ETH
        NetUtils.isEth();
        //判断wifi是否连接状态
        NetUtils.isWifiConnected();
        //判断是否为3G网络
        NetUtils.is3rd();
        //判断网络是否是4G
        NetUtils.is4G();
        //GPS是否打开
        NetUtils.isGpsEnabled();
        //打开网络设置界面
        NetUtils.openWirelessSettings();
        //获取活动网络信息
        NetUtils.getActiveNetworkInfo();
        //获取移动网络运营商名称 如中国联通、中国移动、中国电信
        NetUtils.getNetworkOperatorName();
        //获取移动终端类型 0 手机制式未知 1 手机制式为GSM，移动和联通 2 手机制式为CDMA，电信 3
        NetUtils.getPhoneType();
```

#### DownloadSupport 多任务下载 任务各自独立

![image](/pic/download.png)

```java
        //①初始化下载工具类
        DownloadSupport downloadSupport=new DownloadSupport();

        //---------------------------任务1--------------------------------
        FileCacheData fileCacheData = new FileCacheData();
        fileCacheData.setUrl(downloadUrl);
        fileCacheData.setFileName(fileName1);
        fileCacheData.setRequestTag(downloadUrl);
        fileCacheData.setFilePath("/sdcard/" + fileName1);

        //---------------------------任务2--------------------------------
        FileCacheData fileCacheData2 = new FileCacheData();
        fileCacheData2.setUrl(downloadUrl2);
        fileCacheData2.setFileName(fileName2);
        fileCacheData2.setRequestTag(downloadUrl2);
        fileCacheData2.setFilePath("/sdcard/" + fileName2);



        //开启任务下载 下载文件信息1
        downloadSupport.addStartTask(fileCacheData, downloadCallBack);
         //开启任务下载 下载文件信息2
        downloadSupport.addStartTask(fileCacheData2, downloadCallBack);
        //-----------------------------------------------------------

        //下载进度
        //多个任务使用同一个DownloadCallBack 可根据设置的requestTag来区分属于哪个下载进度 fileCacheData.getRequestTag()
        DownloadSupport.DownloadCallBack downloadCallBack = new DownloadSupport.DownloadCallBack() {
            @Override
            public void download(FileCacheData fileCacheData, int percent, boolean isComplete) {
                Message message1 = handler.obtainMessage();
                message1.obj = fileCacheData;
                message1.arg1 = percent;
                handler.sendMessage(message1);
            }

            @Override
            public void error(Exception e) {
                KLog.d(TAG, "error:>errMeg=" + e.getMessage());
            }

            @Override
            public void downloadStatus(String requestTag, DownloadStatus downloadStatus) {
                KLog.d(TAG, "downloadStatus:>requestTag =" + requestTag + ",status=" + downloadStatus.name());
            }
        };

        //暂停所有任务
        downloadSupport.pause();
        //暂停单个任务
        downloadSupport.pause(fileCacheData2.getRequestTag());

        //全部关闭
        downloadSupport.cancel();

```

#### NotificationUtils 通知栏通知工具类使用

```java
     //获取系统中是否已经通过 允许通知的权限
     if (NotifyUtils.notifyIsEnable()) {
         NotifyUtils.getInstance("xxid")
                 .setEnableCloseButton(false)//设置是否显示关闭按钮
                 .setOnNotifyLinstener(new OnNotifyLinstener() {
                     @Override
                     public void enableStatus(boolean isEnable) {
                         KLog.e(TAG, "isEnable=" + isEnable);
                     }
                 })
                 .setNotifyParam(R.drawable.ic_launcher, R.drawable.app_img
                         , "BaseIotUtils"
                         , "工具类"
                         , "文件压缩，文件下载，日志管理，时间管理，网络判断。。。"
                         , "this is a library ..."
                         , "2020-3-18"
                         , false
                         , true)
                 .exeuNotify();
     } else {
         //去开启通知
         NotifyUtils.toOpenNotify();
     }
     //更改部分内容
     NotifyUtils.getInstance("xxid").setAppName("");
     NotifyUtils.getInstance("xxid").setAppAbout("");
     NotifyUtils.getInstance("xxid").setRemarks("");
     NotifyUtils.getInstance("xxid").setPrompt("");
     NotifyUtils.getInstance("xxid").setDataTime("");
     //关闭此notification
     NotifyUtils.closeNotify();
```

#### PlayUtils 音频播放

```java
      //开始播放
      PlayUtils.getInstance().
         setPlayStateChangeListener(new PlayUtils.PlayStateChangeListener() {

             @Override
             public void onPlayStateChange(PlayUtils.PLAY_STATUS play_status) {
                 //获取当前状态PLAY, RESUME, PAUSE, STOP, NONE
                 KLog.d(TAG," play_status= "+play_status.name());
             }

             @Override
             public void getProgress(int sumProgress, int nowProgress) {
                 //sumProgress 总时长  nowProgress 当前时长
                 KLog.d(TAG, " sumProgress= " + sumProgress + ",nowProgress= " + nowProgress);
             }

         }).
         startPlaying("/sdcard/ding.wav");//文件地址

      //暂停播放
      PlayUtils.getInstance().pausePlay();

      //继续播放
      PlayUtils.getInstance().resumePlay();

      //停止播放
      PlayUtils.getInstance().stopPlaying();
```

#### NetListenerUtils 网络监听者

```java
     //注册广播
     NetListenerUtils.getInstance().registerReceiver();
     //设置监听 NetTypeInfo (NETWORK_2G,NETWORK_3G,NETWORK_4G,NETWORK_WIFI,NETWORK_ETH,NETWORK_NO,NETWORK_UNKNOWN)
     NetListenerUtils.getInstance().setOnNetChangeLinstener(new OnNetChangeLinstener() {
         @Override
         public void changed(NetTypeInfo type, boolean isNormal) {
             //isNormal 网络经过ping后 true为网络正常 false为网络异常
             KLog.e(TAG, "network type=" + type.name() + ",isNormal=" + isNormal);
             tvType.setText("" + type.name());
             tvStatus.setText("" + isNormal);
         }
     });
     .......
     //注销广播
     NetListenerUtils.getInstance().unRegisterReceiver();
```

#### SerialPort|SerialPortFinder 串口封装类

```java
        //获得串口地址
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        //根据串口地址和波特率开启串口
        SerialPort port =  null;
        int baudrate=9600;//波特率 请自行选择所需波特率
        try{
            //entryValues[xxx] 中保存了一些串口地址 请自行选择
            port=new SerialPort(new File(entryValues[xxx]), baudrate,0);
            Log.e(TAG,"开启成功");
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"errMeg:"+e.getMessage());
        }

        //写命令
        port.write(command);
        //读命令
        //可根据SerialPortHelper中的readInputStreamData(int flag)方法读取数据，这里是一个一个字节读取
        //亦或用port中的read方法一次读取，如果数据量大可能存在粘包
        port.read(byte[] buff,int lenght);
        //关闭串口
        port.close();

```

#### ShellUtils adb 操作工具类

使用方式

```java
        //单条命令执行
        ShellUtils.CommandResult commResult=ShellUtils.execCommand("reboot",true);
        //多条命令执行
        //ShellUtils.CommandResult commResult2=ShellUtils.execCommand(new String[]{"comm1","comm2","comm3","commN..."},true);
        if(commResult.result==0){
            Log.e(TAG, "commResult2 exeu successful");
        }else{
            Log.e(TAG, "commResult exeu faild errMeg="+commResult.errorMsg);
        }
```

#### AppsUtils 工具类

使用方式

```java
        AppsUtils.getSystemAppList();//查询手机内系统应用
        AppsUtils.getDeskTopAppList();//查询桌面所有应用
        AppsUtils.getNormalAppList();//查询手机内非系统应用
        AppsUtils.getMainIntent();//获取某个应用的主界面
        AppsUtils.isAppRunning();//根据包名获取APP是否正在运行
        AppsUtils.startApp();//根据包名获取启动该app的主界面
        AppsUtils.getAppName();//获取当前应用名称
        AppsUtils.getPidByPackageName();//根据包名获取进程PID
        AppsUtils.getAppVersionCode();//获取APP-VersionCode
        AppsUtils.getAppVersionName();//获取APP-VersionName
        AppsUtils.isAppForeground();//判断 App 是否处于前台
```

#### PermissionsUtils 权限申请工具类

使用方式

```java
        PermissionsUtils.with(mContext).
            addPermission(Manifest.permission.ACCESS_FINE_LOCATION).
            addPermission(Manifest.permission.ACCESS_COARSE_LOCATION).
            addPermission(Manifest.permission......).
            initPermission();
```

## ② base_socket socket tcp/udp 通信说明

| 编号 | 工具类        | 备注           | 实现功能     |
| ---- | ------------- | -------------- | ------------ |
| 1    | BaseTcpSocket | TCP 通讯工具类 | 发送接收回调 |
| 2    | BaseUdpSocket | UDP 通讯工具类 | 发送接收回调 |

#### base_socket 工具调用方式

```java
    //BaseUdpSocket | BaseTcpSocket tcp|udp 使用方式类似
    BaseTcpSocket baseTcpSocket = new BaseTcpSocket("192.168.1.100",8080, 5000);
    //监听回调
    baseTcpSocket.setSocketListener(new ISocketListener() {
             @Override
             public void recv(byte[] data, int offset, int size) {
                 KLog.d(TAG, "read content successful");
             }

             @Override
             public void writeSuccess(byte[] data) {
                 KLog.d(TAG, "write content successful");
             }

             @Override
             public void connSuccess() {
                 KLog.d(TAG, "The connection is successful");
             }

             @Override
             public void connFaild(Throwable t) {
                 KLog.d(TAG, "The connection is connFaild");
             }

             @Override
             public void connClose() {
                 KLog.d(TAG, "The connection is disconnect");
             }
    });
    //开启连接
    baseTcpSocket.connect(this);

    //--------------------------------------------------
    //发送数据
    baseTcpSocket.send("hello world!".getBytes());
    //关闭连接
    baseTcpSocket.close();
```

## ③ base_framework 系统 api 调用说明

| 编号 | 模块     | 功能                                     |
| ---- | -------- | ---------------------------------------- |
| 1    | 网络     | 以太网控制 , WIFI 控制，应用网络管理     |
| 2    | 存储空间 | sdcard 容量,TF 卡容量, ram 容量,rom 容量 |
| 3    | 升级管理 | apk 安装/卸载,固件升级                   |

| 编号 | 工具类         | 工具名称       | 实现功能                       |
| ---- | -------------- | -------------- | ------------------------------ |
| 1    | FScreentTools  | 屏幕信息工具类 | 截屏                           |
| 2    | FStorageTools  | 存储空间管理   | TF\SD\RAM\ROM 空间获取         |
| 3    | FUpgradeTools  | 升级管理       | 固件\apk 升级                  |
| 4    | FEthTools      | 以太网管理     | 开启关闭，STATIC\DHCP 模式设置 |
| 5    | FNetworkTools  | 网络工具类     | dns,流量获取                   |
| 6    | FWifiTools     | WIFI 管理      | 开启关闭                       |
| 7    | FUsbHubTools   | USB 管理       | 接入设备获取                   |
| 8    | FIPTablesTools | 应用网络管理   | 应用网络开启关闭               |

## ④ base_keepalive 服务保活处理
![image](/pic/keepalive.png)

| 编号 | 模块     | 功能              |
| ---- | -------- | ----------------- |
| 1    | ACTIVITY | activity 界面拉起 |
| 2    | SERVICE  | service 服务拉起  |

#### FKeepAliveTools 直接添加 SERVICE/ACTIVITY

使用方式 直接引用 library

```java
    //添加ACTIVITY拉起
    KeepAliveData keepAliveData = new KeepAliveData("com.xxx.xxx", FKeepAliveTools.TYPE_ACTIVITY, true);
    CommonValue commonValue = FKeepAliveTools.addActivity(keepAliveData);
    if (commonValue == CommonValue.EXEU_COMPLETE) {
        return true;
    } else {
        return false;
    }
    //
    KeepAliveData keepAliveData2 = new KeepAliveData("com.xxx.xxx", FKeepAliveTools.TYPE_SERVICE, "Service的具体路劲com.xxx.xxx.xxService", true);
    CommonValue commonValue = FKeepAliveTools.addService(keepAliveData2);
    if (commonValue == CommonValue.EXEU_COMPLETE) {
        return true;
    } else {
        return false;
    }
```

使用方式 作为一个中间组件调用 其他 app 可以跨进程添加或者清理

```java
    //将base_keepalive中的aidl中的文件复制到自己的项目中
    //将base_keepalive/src/com/chtj/keepalive/entity中的实体类一同复制到自己项目中，并保持包名一致
    //此时这个项目已经作为一个中间件，用于处理所有APP对其的添加，删除操作
    //为什么这样做？有时并不想每个项目都引用这个base_keepalive进行保活，而是在已经存在的library中去直接调用，避免重复造轮子

    public class KeepLiveAty extends BaseActivity implements OnClickListener, IKeepAliveListener {
        private static final String TAG = "KeepLiveAty";
        TextView tvResult;
        /**
         * 保活的类型为Activity
         */
        public static final int TYPE_ACTIVITY = 0;
        /**
         * 保活的类型为服务
         */
        public static final int TYPE_SERVICE = 1;

        IKeepAliveService iKeepAliveService;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            ....
            Intent intent = new Intent();
            intent.setAction("com.chtj.keepalive.IKeepAliveService");
            intent.setPackage("com.face.keepsample");
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            getData();
        }

        public void getData() {
            try {
                List<KeepAliveData> keepAliveDataList = iKeepAliveService.getKeepLiveInfo();
                if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
                    ToastUtils.success("获取成功 数量=" + keepAliveDataList.size());
                } else {
                    ToastUtils.error("获取失败 数量=0");
                }
            } catch (Exception e) {
                e.printStackTrace();
                KLog.e(TAG, "errMeg:" + e.getMessage());
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add_aty:
                    try {
                        KeepAliveData keepAliveData = new KeepAliveData("com.wave_chtj.example", TYPE_ACTIVITY, true);
                        boolean isAddAty = iKeepAliveService.addKeepLiveInfo(keepAliveData, this);
                        KLog.d(TAG, "onClick:>btn_add_aty=" + isAddAty);
                        if (isAddAty) {
                            ToastUtils.success("执行成功！");
                        } else {
                            ToastUtils.error("执行失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        KLog.e(TAG, "errMeg:" + e.getMessage());
                    }
                    break;
                case R.id.btn_add_service:
                    try {
                        KeepAliveData keepAliveInfo = new KeepAliveData("com.face.baseiotcloud", TYPE_SERVICE, "com.face.baseiotcloud.service.OtherService", true);
                        boolean isaddInfo = iKeepAliveService.addKeepLiveInfo(keepAliveInfo, this);
                        KLog.d(TAG, "onClick:>btn_add_service=" + isaddInfo);
                        if (isaddInfo) {
                            ToastUtils.success("执行成功！");
                        } else {
                            ToastUtils.error("执行失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        KLog.e(TAG, "errMeg:" + e.getMessage());
                    }
                    break;
                case R.id.btn_getall:
                    getData();
                    break;
                case R.id.btn_cleanall:
                    try {
                        boolean isClearAll = iKeepAliveService.clearAllKeepAliveInfo();
                        if (isClearAll) {
                            ToastUtils.success("清除成功！");
                        } else {
                            ToastUtils.error("清除失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        KLog.e(TAG, "errMeg:" + e.getMessage());
                    }
                    break;
            }
        }

        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iKeepAliveService = IKeepAliveService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iKeepAliveService = null;
            }
        };


        IKeepAliveListener.Stub iKeepAliveListener = new IKeepAliveListener.Stub() {
            @Override
            public void onError(String errMeg) throws RemoteException {
                Log.d(TAG, "onError: " + errMeg);
            }

            @Override
            public void onSuccess() throws RemoteException {
                Log.d(TAG, "onSuccess: ");
            }
        };

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (iKeepAliveService != null) {
                unbindService(conn);
            }
        }

        @Override
        public void onError(String errMeg) throws RemoteException {
            Log.d(TAG, "onError: ");
        }

        @Override
        public void onSuccess() throws RemoteException {
            Log.d(TAG, "onSuccess: ");
        }

        @Override
        public IBinder asBinder() {
            return iKeepAliveListener;
        }
    }
```

## Version Code

#### v1.1.0

```
> 本机信息展示(网络,设备空间等)
> 下载工具类 DownloadSupport参数传递更方便,可自行定义每个下载任务的信息
> 系统API调用,例如网络相关,空间相关等
```

#### v1.0.9

```
> 跨进程保活 Service/Activity
> 优化部分工具类使用
```

#### v1.0.8

```
> 新增了 PlayUitls 音频播放器(状态管理)
> DwonloadSupport(全新的多任务下载管理工具类)
> 视频播放管理收集
> 删除了服务保活工具，删除了原始的下载工具类
> 添加了对该系统内的应用的管理，查看 PackagesUtils
> 优化该 app 界面,使用操作等
> 定时器功能添加 倒计时，计时器等
> 收集 greenDAO(收集整合，方便后期使用),封装(Sqlite)操作更加方便
```

#### v1.0.3

```
> 优化各个工具类
> 新增部分工具类
> 添加启动应用时的优化处理
```

#### v1.0.2

```
> 项目优化
> 基本工具类的收集整合
```
