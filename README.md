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

#### base_socket
```groovy
dependencies {
         //socket通信 tcp/udp工具类 使用方式请参考app module中的代码
         implementation 'com.chtj.base_socket:base_socket:1.0.2'
}
```

##  base_socket Module使用说明
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
```

#### base_iotutils
```groovy
dependencies {
         //以宽高进行屏幕适配,shell,网络判断等多种工具类以及后台存活串口封装等
         implementation 'com.chtj.base_iotutils:base_iotutils:1.2.4'
}
```

##  base_iotutils Module 说明

### 自定义Application
```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //1.1.6 之后与之前有较大改动 
        //需要在 Application 的 onCreate() 中调用一次 BaseIotTools.instance()....
        //1080,1920是为了适配而去设置相关的值
        //设置宽度|高度布局尺寸 layout 布局文件以pt为单位 setBaseScreenParam(1080,1920,true)
        BaseIotUtils.instance().
              setBaseScreenParam(1080,1920,true).
              setCreenType(SCREEN_TYPE.WIDTH).//按照宽度适配
              create(getApplication());
        //别忘了在 Manifest 中通过 android:name 使用这个自定义的 Application.
    }
}
```

### base_iotutils 常用工具类

- 进制转换类 | DataConvertUtils

- 设备相关 | DeviceUtils

- 键盘相关 | KeyBoardUtils

- 网络判断 | NetWorkUtils

- adb命令工具类 | ShellUtils

- ShareProfrence工具类 | SPUtils

- Toast工具类 | ToastUtil

- 后台服务类 保活 | AbsWorkService

- PackagesName相关信息工具类 | PackagesUtils

- 屏幕适配相关 | AdaptScreenUtils

- 串口工具 | SerialPort | SerialPortFinder

- 日志管理 | KLog

- 文件操作 | FileTxtUtil

- 事件管理 | RxBus

- 文件下载 | DownLoadManager

- Notification通知 | NotificationUtils


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
     //初始化并显示
     NotificationUtils.getInstance()
         .setINotificationLinstener(new INotificationLinstener() {
             @Override
             public void enableStatus(boolean isEnable) {
                 KLog.e(TAG,"isEnable="+isEnable);
             }
         })
         .setNotifyId(10)
         .setNotificationParm("BaseIotUtils"
                 ,"a baseiotutils:serialPort,Rxbus,DownloadManager....!"
                 ,"oh my god!"
                 ,false
                 ,true)
         .exeuNotify();
     //更改相关信息
     NotificationUtils.getInstance().setAppName("ssss");
     NotificationUtils.getInstance().setRemarks("AAAAA");
     NotificationUtils.getInstance().setPrompt("gggggg");
     //关闭通知
     NotificationUtils.getInstance().closeNotify();
 ```
 
#串口使用
## 1.使用串口封装类
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

## 2.不使用串口封装类
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
        //初始化后台保活Service
        BaseIotUtils.initSerice(TraceServiceImpl.class, BaseIotUtils.DEFAULT_WAKE_UP_INTERVAL);

        //开启service
        TraceServiceImpl.sShouldStopService = false;
        BaseIotUtils.startServiceMayBind(TraceServiceImpl.class);
        
        //关闭service
        TraceServiceImpl.stopService();
        
```
