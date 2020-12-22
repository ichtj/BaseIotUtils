# 接入方式

### app 界面截图 这里显示不全,部分功能可能未在图中显示

![image](/pic/apppic.png)

### Step 1. Add the JitPack repository to your build file

Add it in your root **build.gradle** at the end of repositories:

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

### Step 2. Add the dependency

#### 以下存在三个 library 请按需选择

#### base_iotutils

```groovy
dependencies {
         //以宽高进行屏幕适配,shellUtils,网络判断等多种工具类以及串口封装等
         implementation 'com.face_chtj.base_iotutils:base_iotutils:1.8.2'
}
```

| 编号 | 工具类                         | 备注                                          |
| ---- | ------------------------------ | --------------------------------------------- |
| 1    | ZipUtils                       | 压缩相关工具类                                |
| 2    | UriPathUtils                   | Uri 路径转真实路径 针对于 android7.0 以上特性 |
| 3    | ToastUtils                     | 系统的 Toast 封装(各场景)                     |
| 4    | TimeUtils                      | 时间工具类                                    |
| 5    | SurfaceLoadDialog              | 应用上层弹窗                                  |
| 6    | SPUtils                        | 存储工具类                                    |
| 7    | ShellUtils                     | adb Shell 相关工具类                          |
| 8    | ServiceUtils                   | Service 管理工具                              |
| 9    | PermissionsUtils               | 权限申请工具类                                |
| 10   | KLog                           | 日志打印                                      |
| 11   | KeyBoardUtils                  | 打开关闭软键盘                                |
| 12   | FileUtils                      | 文件工具类                                    |
| 13   | DeviceUtils                    | 得到设备的相关信息                            |
| 14   | DataConvertUtils               | 进制转换工具类                                |
| 15   | ScheduledTPoolUtils/TPoolUtils | 线程池管理                                    |
| 16   | SerialPort/SerialPortFinder    | 串口相关工具类                                |
| 17   | AdaptScreenUtils               | 屏幕适配                                      |
| 18   | NotifyUtils                    | notification 工具类                           |
| 19   | NetUtils                       | 网络工具类                                    |
| 20   | NetListenerUtils               | 网络变化广播                                  |
| 21   | NetUtils                       | 网络工具类                                    |
| 22   | DownloadSupport                | 多任务下载管理工具类(断点)                    |
| 23   | PlayUtils                      | 音频播放(播放继续暂停)                        |
| 24   | AppsUtils                      | 系统中 app 的相关工具类                       |
| 25   | ScreenInfoUtils                | 屏幕相关工具类                                |
| 26   | StatusBarUtil                  | 沉浸式状态栏                                  |

#### base_socket

```groovy
dependencies {
         //socket通信 tcp/udp工具类 使用方式请参考app module中的代码
         implementation 'com.chtj.base_socket:base_socket:1.0.2'
}
```

| 编号 | 工具类        | 备注           |
| ---- | ------------- | -------------- |
| 1    | BaseTcpSocket | TCP 通讯工具类 |
| 2    | BaseUdpSocket | UDP 通讯工具类 |

#### framework_utils 实现功能(后继陆更|目前未上传)

| 编号 | 模块     | 功能                                          |
| ---- | -------- | --------------------------------------------- |
| 1    | 网络     | 静态/动态 IP,开启/关闭以太网 , 开启/关闭 WIFI |
| 2    | 存储空间 | sdcard 容量,TF 卡容量, ram 容量,rom 容量      |
| 3    | 升级管理 | apk 安装/卸载,固件升级                        |
| 5    | 保活管理 | ACTIVITY/SERVICE 添加,启动 ACTIVITY/SERVICE   |
| 6    | 日志     | 异常网络日志记录                              |

## base_iotutils Module 说明

### 自定义 Application

```java
//注意：别忘了在 Manifest 中通过 android:name 使用这个自定义的 Application.

public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    //如果(不)需要开启适配  请用此方式
    BaseIotUtils.instance().create(getApplication());

    //如果需要开启适配 请用此方式
    //1080(宽),1920(高)是为了适配而去设置相关的值
    //设置宽度|高度布局尺寸 layout 布局文件以pt为单位 setBaseScreenParam(1080,1920,true)
    BaseIotUtils
      .instance()
      .setBaseScreenParam(1080, 1920, true)
      .setCreenType(SCREEN_TYPE.WIDTH)
      .create(getApplication()); //按照宽度适配
  }
}

```

## base_socket Module 使用说明

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

## 屏幕适配

### 创建 pt 模拟器设备

![image](/pic/create_step.png)

![image](/pic/unit_pt.png)

### 1080\*1920 px 效果

![image](/pic/big_screen.png)

-可在 app Model 中找到使用示例

## base_iotutils module 工具类 请在下方查看部分工具类使用详情

- app 列表/基础信息 | AppsUtils 获取桌面应用/系统应用/app 基础信息等

- 屏幕相关属性 | ScreenInfoUtils

- 进制转换类 | DataConvertUtils

- 设备相关 | DeviceUtils

- 音频播放 | PlayUtils

- 键盘相关 | KeyBoardUtils

- 网络判断 | NetUtils

- adb 命令工具类 | ShellUtils 可在这里操作 adb 的命令

- App 相关信息工具类 | AppsUtils

- 屏幕适配相关 | AdaptScreenUtils

- 串口工具 | SerialPort | SerialPortFinder

- 日志管理(使用时开启日志) | KLog

- 文件操作 | FileUtils

- 事件管理 | RxBus

- 断点下载 | DownloadSupport 多文件下载 状态监听 暂停等

- Notification 通知 | NotifyUtils

- 权限管理 | PermissionsUtils

- ShareProfrence 工具类 | SPUtils

- 字符串判断 | StringUtils

- Service 状态获取(是否正在运行) | ServiceUtils

- 时间工具类(返回各种时间格式) | TimeUtils

- 应用上方对话框(全局对话框) | SurfaceLoadDialog

- 网络侦听者 | NetListenerUtils 网络是否正常，类型，连接状态等

- 时间格式获取 | TimeUtils

- 吐司工具类 | ToastUtils

- 文件地址转换真实路径 | UriPathUtils 主要针对 android7.0 之后的

- 压缩相关 | ZipUtils

- 线程池相关 | ScheduledTPoolUtils>TPoolUtils

# FileUtils 文件操作 读写,删除,文件大小等

```java
        //param1 文件路径 例如/sdcard/config.txt
        //param2 写入内容
        //param3 是否覆盖这个文件里的内容
        boolean writeResult = FileUtils.writeFileData(filePath, content, true);
        //读取filePath文件中的内容
        String readResult = FileUtils.readFileData(filePath);
        //更多文件操作方法请查询FileUtils中的内容
```

# KeyBoardUtils 软键盘管理

```java
       //打卡软键盘
       KeyBoardUtils.openKeybord(editeTextView);

       //关闭软键盘
       KeyBoardUtils.closeKeybord(editeTextView);
```

# NetUtils 网络工具类

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

# DownloadSupport 多任务下载 任务各自独立

![image](/pic/download.png)

```java
        //初始化下载工具类
        DownloadSupport downloadSupport=new DownloadSupport();

        //---------------------------任务--------------------------------
        FileCacheData fileCacheData = new FileCacheData();
        fileCacheData.setUrl(downloadUrl);
        fileCacheData.setFileName(fileName1);
        fileCacheData.setRequestTag(downloadUrl);
        fileCacheData.setFilePath("/sdcard/" + fileName1);
        //开启任务下载 下载文件信息1
        downloadSupport.addDownloadTask(fileCacheData);

        FileCacheData fileCacheData2 = new FileCacheData();
        fileCacheData2.setUrl(downloadUrl2);
        fileCacheData2.setFileName(fileName2);
        fileCacheData2.setRequestTag(downloadUrl2);
        fileCacheData2.setFilePath("/sdcard/" + fileName2);
         //开启任务下载 下载文件信息2
        downloadSupport.addStartTask(fileCacheData, downloadCallBack);
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

# NotificationUtils 通知使用

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

# PlayUtils 音频播放

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

# NetListenerUtils 网络监听者

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

## SerialPort|SerialPortFinder 串口封装类

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

## ShellUtils adb 操作工具类

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

## AppsUtils 工具类

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

## PermissionsUtils 权限申请工具类

使用方式

```java
        PermissionsUtils.with(mContext).
            addPermission(Manifest.permission.ACCESS_FINE_LOCATION).
            addPermission(Manifest.permission.ACCESS_COARSE_LOCATION).
            addPermission(Manifest.permission......).
            initPermission();
```

## Version Code

### v1.0.8

> 新增了 PlayUitls 音频播放器(状态管理)
> DwonloadSupport(全新的多任务下载管理工具类)
> 视频播放管理收集
> 删除了服务保活工具，删除了原始的下载工具类
> 添加了对该系统内的应用的管理，查看 PackagesUtils
> 优化该 app 界面,使用操作等
> 定时器功能添加 倒计时，计时器等
> 收集 greenDAO(收集整合，方便后期使用),封装(Sqlite)操作更加方便

### v1.0.3

> 优化各个工具类
> 新增部分工具类
> 添加启动应用时的优化处理

### v1.0.2

> 项目优化
> 基本工具类的手机整合
