# 接入方式
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

#### 以下存在两个library 请按需选择

#### base_iotutils
```groovy
dependencies {
         //以宽高进行屏幕适配,shell,网络判断等多种工具类以及后台存活串口封装等
         implementation 'com.face_chtj.base_iotutils:base_iotutils:1.3.8'
}
```

#### base_socket
```groovy
dependencies {
         //socket通信 tcp/udp工具类 使用方式请参考app module中的代码
         implementation 'com.chtj.base_socket:base_socket:1.0.2'
}
```
##  base_iotutils Module 说明

### 自定义Application
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
        BaseIotUtils.instance().
              setBaseScreenParam(1080,1920,true).
              setCreenType(SCREEN_TYPE.WIDTH).//按照宽度适配
              create(getApplication());
    }
}
```


##  base_socket Module使用说明
<!--
```java
    //BaseUdpSocket | BaseTcpSocket tcp|udp 使用方式类似
    BaseTcpSocket baseTcpSocket = new BaseTcpSocket(192.168.1.100,8080, 5000);
    //监听回调
    baseTcpSocket.setSocketListener(new ISocketListener()...);
    //开启连接
    baseTcpSocket.connect(this);
    //发送数据
     baseTcpSocket.send("hello world!".getBytes());
    //关闭连接
    baseTcpSocket.close();
```-->

### base_iotutils 常用工具类

- 进制转换类 | DataConvertUtils

- 设备相关 | DeviceUtils

- 键盘相关 | KeyBoardUtils

- 网络判断 | NetUtils

- adb命令工具类 | ShellUtils

- ShareProfrence工具类 | SPUtils

- Toast工具类 | ToastUtils

- 后台服务类 保活 | AbsWorkService

- PackagesName相关信息工具类 | PackagesUtils

- 屏幕适配相关 | AdaptScreenUtils

- 串口工具 | SerialPort | SerialPortFinder

- 日志管理(使用时开启日志) | KLog

- 文件操作 | FileUtils

- 事件管理 | RxBus

- 文件下载 | DownLoadManager

- Notification通知 | NotifyUtils

- 权限管理 | PermissionsUtils

- Service状态获取(是否正在运行) | ServiceUtils

- 时间工具类(返回各种时间格式) | TimeUtils

- 应用上方对话框(全局对话框) | SurfaceLoadDialog

- 压缩相关工具类 | ZipUtils

- 字符串判断 | StringUtils

- 网络侦听者 | NetListenerUtils 网络是否正常，类型，连接状态

# 屏幕适配

### 1080*1920 px 效果

![image](/pic/big_screen.png)

### 480*800 px 效果

![image](/pic/small_screen.png)

-可在app Model中找到使用示例

# 文件操作 覆盖等

![image](/pic/file_write_read.png)

# 文件下载 DownLoadManager
```java
       //downloadUrl 文件下载地址
       //destFileDir 存放地址
       //destFileName 文件名称
       DownLoadManager.getInstance().load(downloadUrl, new ProgressCallBack<ResponseBody>(destFileDir, destFileName) {
                   @Override
                   public void onStart() {
                       super.onStart();
                   }
       
                   @Override
                   public void onSuccess(ResponseBody responseBody) {
                       ToastUtils.showShort("文件下载完成！");
                   }
       
                   @Override
                   public void progress(final long progress, final long total) {
                   }
       
                   @Override
                   public void onError(Throwable e) {
                       e.printStackTrace();
                       ToastUtils.showShort("文件下载失败！");
                   }
               });
```

# NotificationUtils 使用
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
 
#串口使用
<!--## 1.使用串口封装类
```java
      HeartBeatEntity heartBeatEntity = new HeartBeatEntity(new byte[]{(byte) 0x12}, FlagManager.FLAG_HEARTBEAT, 15 * 1000);
      //初始化串口工具类
      ComEntity comEntity = new ComEntity(
              com//串口地址
                    , baudrate//波特率
                    , 6000//超时时间
                    , 3//重试次数
                    , null//心跳检测参数
            );
            //初始化数据
            SerialPortHelper.
                    getInstance().
                    setComEntity(comEntity).
                    setOnComListener(new OnComListener() {
                        @Override
                        public void writeCommand(byte[] comm, int flag) {
                            String writeData = "writeCommand>>> comm=" + DataConvertUtils.encodeHexString(comm) + ",flag=" + flag;
                            Log.e(TAG, writeData);
                            Message message = handler.obtainMessage();
                            message.obj = writeData;
                            handler.sendMessage(message);
                        }

                        @Override
                        public void readCommand(byte[] comm, int flag) {
                            String readData = "readCommand>>> comm=" + DataConvertUtils.encodeHexString(comm) + ",flag=" + flag;
                            Log.e(TAG, readData);
                            Message message = handler.obtainMessage();
                            message.obj = readData;
                            handler.sendMessage(message);
                        }

                        @Override
                        public void writeComplet(int flag) {
                            String writeSuccessful = "writeComplet>>> flag=" + flag;
                            Log.e(TAG, writeSuccessful);
                            Message message = handler.obtainMessage();
                            message.obj = writeSuccessful;
                            handler.sendMessage(message);
                        }


                        @Override
                        public void isReadTimeOut(int flag) {
                            String readTimeOut = "isReadTimeOut>>> flag=" + flag;
                            Log.e(TAG, readTimeOut);
                            Message message = handler.obtainMessage();
                            message.obj = readTimeOut;
                            handler.sendMessage(message);
                        }

                        @Override
                        public void isOpen(boolean isOpen) {
                            String comStatus = isOpen ? "isOpen>>>串口打开！" : "isOpen>>>串口关闭";
                            Log.e(TAG, comStatus);
                            Message message = handler.obtainMessage();
                            message.obj = comStatus;
                            handler.sendMessage(message);
                        }

                        @Override
                        public void comStatus(boolean isNormal) {
                            String comStatus = isNormal ? "comStatus>>>串口正常！" : "comStatus>>>串口异常";
                            Log.e(TAG, comStatus);
                            Message message = handler.obtainMessage();
                            message.obj = comStatus;
                            handler.sendMessage(message);
                        }

                    }).
                    openSerialPort();
      //发送数据
      SerialPortHelper.getInstance().setWriteAfterRead(comm, FlagManager.FLAG_CHECK_UPDATE);
      //关闭串口
      SerialPortHelper.getInstance().closeSerialPort();
```
-->

## 1.串口封装类
```java
        //获得串口地址
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        //根据串口地址和波特率开启串口
        SerialPort port =  null;
        try{ 
            port=new SerialPort(new File(entryValues[xxx]), xxx,0);
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
# 后台保活
使用方式
```java

        创建Service继承 AbsWorkService重写方法
        public class TraceServiceImpl extend AbsWorkService{
             //是否 任务完成, 不再需要服务运行?
                public static boolean sShouldStopService;
                public static Disposable sDisposable;

                public static void stopService() {
                    //我们现在不再需要服务运行了, 将标志位置为 true
                    sShouldStopService = true;
                    //取消对任务的订阅
                    if (sDisposable != null) sDisposable.dispose();
                    //取消 Job / Alarm / Subscription
                    cancelJobAlarmSub();
                }

                /**
                 * 是否 任务完成, 不再需要服务运行?
                 * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
                 */
                @Override
                public Boolean shouldStopService(Intent intent, int flags, int startId) {
                    return sShouldStopService;
                }

                @Override
                public void startWork(Intent intent, int flags, int startId) {
                    //在这里操作。。。。
                }

                @Override
                public void stopWork(Intent intent, int flags, int startId) {
                    stopService();
                }

                /**
                 * 任务是否正在运行?
                 * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
                 */
                @Override
                public Boolean isWorkRunning(Intent intent, int flags, int startId) {
                    //若还没有取消订阅, 就说明任务仍在运行.
                    return sDisposable != null && !sDisposable.isDisposed();
                }

                @Override
                public IBinder onBind(Intent intent, Void v) {
                    return null;
                }

                @Override
                public void onServiceKilled(Intent rootIntent) {
                    System.out.println("保存数据到磁盘。");
                }
        }

        //初始化后台保活Service
        BaseIotUtils.initSerice(TraceServiceImpl.class, BaseIotUtils.DEFAULT_WAKE_UP_INTERVAL);

        //开启service
        TraceServiceImpl.sShouldStopService = false;
        BaseIotUtils.startServiceMayBind(TraceServiceImpl.class);
        
        //关闭service
        TraceServiceImpl.stopService();
        
```

# adb操作工具类
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

# PermissionsUtils操作工具类
使用方式
```java
        PermissionsUtils.with(mContext).
            addPermission(Manifest.permission.ACCESS_FINE_LOCATION).
            addPermission(Manifest.permission.ACCESS_COARSE_LOCATION).
            addPermission(Manifest.permission......).
            initPermission();
```

# Version Code
 ### v1.0.3
> 优化各个工具类
> 新增部分工具类
 ### v1.0.2
> 新增crash控制界面
> 修改NotifyUtils支持6.0以上系统显示，并新增获取通知是否允许的状态NotifyUtils.notifyIsEnable();跳转应用设置界面NotifyUtils.toOpenNotify();
