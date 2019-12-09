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
         implementation 'com.chtj.base_iotutils:base_iotutils:1.2.3'
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

- 进制转换类 | HexUtils

- 设备相关 | DeviceUtils

- 键盘相关 | KeyBoardUtils

- 网络判断 | NetWorkUtils

- adb命令工具类 | ShellUtils

- ShareProfrence工具类 | SPUtils

- Toast工具类 | ToastUtil

- 后台服务类 保活 | AbsWorkService

- App相关信息工具类 | AppUtils

- 屏幕适配相关 | AdaptScreenUtils

- 串口工具 | SerialPort | SerialPortFinder

- 日志管理 | KLog

- 文件操作 | FileUtil

- 事件管理 | RxBus

- 文件下载 | DownLoadManager

# 屏幕适配

### 1080*1920 px 效果

![image](/pic/big_screen.png)

### 480*800 px 效果

![image](/pic/small_screen.png)

-可在app Model中找到使用示例

# 文件操作 覆盖等

![image](/pic/file_write_read.png)

# 文件下载
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
#串口使用
## 1.使用串口封装类
```java
        //初始化串口工具类
        SerialPortHelper serialPortHelper=new SerialPortHelper(SerialPortNormalAty.this, comEntity);
        //一：获取该android设备下的所有串口地址
        //二：波特率请根据实际需要添加，位置在R.array.burate文件下
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        //三：配置参数
        //参数设置
        List<Integer> flagFilterList = new ArrayList<>();
        flagFilterList.add(FlagManager.FLAG_CHECK_UPDATE);
        //数据头(包头) 主要用于判断读取的命令是否符合协议
        List<Byte> headDataList = new ArrayList<>();
        headDataList.add((byte) 0xAA);
        headDataList.add((byte) 0x55);
        //指令标识 主要用于判断读取的命令是否符合协议
        List<Byte> instructionList = new ArrayList<>();
        instructionList.add((byte) -96);//A3 自检
        instructionList.add((byte) -95);//A2 数据写入
        instructionList.add((byte) -94);//A1 加入升级
        instructionList.add((byte) -93);//A0 检查升级
        
        //①未开启心跳包
        //ComEntity comEntity=new ComEntity(com,baudrate,6000,3,headDataList,3,2,6,5,instructionList,flagFilterList);
        //②心跳包参数设置 默认用某一条命令周期性的去获取设备返回的消息 
        //主要判断是否连接正常
        HeartBeatEntity heartBeatEntity = new HeartBeatEntity(new byte[]{(byte) 0xAA, 0x55, 00, 0, 0x01, (byte) 0xA0, (byte) 0xBF}, FlagManager.FLAG_HEARTBEAT, 15 * 1000);
        ComEntity comEntity = new ComEntity(
                com//串口地址
                , baudrate//波特率
                , 6000//超时时间
                , 3//重试次数
                , headDataList//数据头 用于去校验是否正确
                , 3//data长度开始的位置 从0开始
                , 2//data长度
                , 6//其他位的固定长度
                , 5//指令开始的位置 从0开始
                , instructionList//指令集合
                , heartBeatEntity//心跳检测参数
                , flagFilterList//写命令时如果当前flag的命令大于两条 添加进来的不会因为第一条命令执行失败，而不向下执行
         );
         //初始化数据
         serialPortHelper = new SerialPortHelper(SerialPortNormalAty.this, comEntity);
         //注册监听
         serialPortHelper.setOnComListener(new OnComListener() {
             @Override
             public void writeCommand(byte[] comm, int flag) {
             }
             @Override
             public void readCommand(byte[] comm, int flag) {
             }
             @Override
             public void writeComplet(int flag) {
             }
             @Override
             public void isReadTimeOut(int flag) {
             }
             @Override
             public void isOpen(boolean isOpen) {
             }
             @Override
             public void comStatus(boolean isNormal) {
             }
        });
        //4：开启串口
        serialPortHelper.openSerialPort();
        //5:关闭串口
        //if (serialPortHelper != null) {
        //    serialPortHelper.closeSerialPort();
        //}
        
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
# HelloDaemon 后台保活
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

<a href="https://996.icu"><img src="https://img.shields.io/badge/link-996.icu-red.svg"></a>

### Android 服务保活/常驻 (Android Service Daemon)

#### 建议只在App的核心功能需要保活/常驻时使用。

#### 本示例中使用的保活方法部分来源于下面的博客和库。启动前台服务而不显示通知来自于D-clock的AndroidDaemonService，对其他的一些非native层保活方法进行了实现。

[Android 进程常驻（2）----细数利用android系统机制的保活手段](http://blog.csdn.net/marswin89/article/details/50890708)

[D-clock / AndroidDaemonService](https://github.com/D-clock/AndroidDaemonService)

## 实现了上面 2 个链接中的大多数保活思路 :

#### 1. 将Service设置为前台服务而不显示通知

> D-clock :
>
思路一：API < 18，启动前台Service时直接传入new Notification()；
>
思路二：API >= 18，同时启动两个id相同的前台Service，然后再将后启动的Service做stop处理；

//启动前台服务而不显示通知的漏洞已在 API Level 25 修复，大快人心！

前台服务相对于后台服务的优势，除了优先级的提升以外，还有一点：

在最近任务列表中划掉卡片时，前台服务不会停止；

(更新：经过测试，发现只是对于AOSP/CM/国际上对Framework层改动较小的Android系统是成立的；EMUI/MIUI等未加入白名单的情况下，划掉卡片，前台服务也会停止；加入白名单后划掉卡片的行为与国际厂商的系统相似。)

而后台服务会停止，并在稍后重新启动（onStartCommand 返回 START_STICKY 时）。

前台服务和后台服务被划掉卡片时，回调的都是 onTaskRemoved 方法。

onDestroy 方法只在 设置 -> 开发者选项 -> 正在运行的服务 里停止服务时才会回调。

#### 2. 在 Service 的 onStartCommand 方法里返回 START_STICKY

#### 3. 覆盖 Service 的 onDestroy/onTaskRemoved 方法, 保存数据到磁盘, 然后重新拉起服务

#### 4. 监听 8 种系统广播 :

CONNECTIVITY\_CHANGE, USER\_PRESENT, ACTION\_POWER\_CONNECTED, ACTION\_POWER\_DISCONNECTED, BOOT\_COMPLETED, PACKAGE\_ADDED, PACKAGE\_REMOVED.

在网络连接改变, 用户屏幕解锁, 电源连接 / 断开, 系统启动完成, 安装 / 卸载软件包时拉起 Service.

Service 内部做了判断，若 Service 已在运行，不会重复启动.

#### 5. 开启守护服务 : 定时检查服务是否在运行，如果不在运行就拉起来

#### 6. 守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用

详见上面的 2 个链接。

## 增加实现 :

#### \+ 守护服务 : Android 5.0 及以上版本使用 JobScheduler，效果比 AlarmManager 好

使用 JobScheduler, Android 系统能自动拉起被 Force Stop 的 Package，而 AlarmManager 无法拉起.

Android 4.4 及以下版本使用 AlarmManager.

#### \+ 使用定时 Observable : 避免 Android 定制系统 JobScheduler / AlarmManager 唤醒间隔不稳定的情况

#### \+ 增加停止服务并取消定时唤醒的快捷方法

#### \+ 增加在不需要服务运行时取消 Job / Alarm / Subscription 的快捷方法 (广播 Action)

#### \+ 增强对国产机型的适配 : 防止华为机型按返回键回到桌面再锁屏后几秒钟进程被杀

测试机型 : 华为 荣耀6 Plus (EMUI 4.0 Android 6.0), 应用未加入白名单.

>
观察到 :
>
在未加入白名单的情况下，按Back键回到桌面再锁屏后几秒钟即会杀掉进程；
>
但是按Home键返回桌面的话，即使锁屏，也不会杀掉进程。

(更新：经过测试，在EMUI系统上，『即使锁屏，也不会杀掉进程』只对App的卡片还在多任务屏幕的第一屏时有效，一旦被挤到第二页及以后，锁屏后几秒钟即会杀掉进程；加入白名单后，回到桌面再锁屏后不会杀进程。)

因此，重写了onBackPressed方法，使其只是返回到桌面，而不是将当前Activity finish/destroy掉。

测试机型 : 红米1S 4G (MIUI 8 Android 4.4.2), 应用未加入白名单.

>
观察到 :
>
在未加入白名单的情况下，回到桌面再锁屏后不会杀进程；
>
但划掉卡片，进程死亡并不再启动；加入白名单后，划掉卡片，服务不会停止，与CM的行为相似。

可以看出，若不想使用Native保活，引导用户加入白名单可能是比较可行的方法。

#### \+ 用 Intent 跳转

- Android Doze 模式
- 华为 自启管理
- 华为 锁屏清理
- 小米 自启动管理
- 小米 神隐模式
- 三星 5.0/5.1 自启动应用程序管理
- 三星 6.0+ 未监视的应用程序管理
- 魅族 自启动管理
- 魅族 待机耗电管理
- Oppo 自启动管理
- Vivo 后台高耗电
- 金立 应用自启
- 金立 绿色后台
- 乐视 自启动管理
- 乐视 应用保护
- 酷派 自启动管理
- 联想 后台管理
- 联想 后台耗电优化
- 中兴 自启管理
- 中兴 锁屏加速受保护应用

配合 android.support.v7.AlertDialog 引导用户将 App 加入白名单.

#### \+ 守护服务和BroadcastReceiver运行在:watch子进程中，与主进程分离

#### \+ 工作服务运行在主进程中，免去与服务通信需使用AIDL或其他IPC方式的麻烦

参考了 Poweramp, 启动的前台服务与 UI 运行在同一进程中。

#### \+ 做了防止重复启动Service的处理，可以任意调用startService(Intent i)

若服务还在运行，就什么也不做；若服务不在运行就拉起来。

#### \+ 在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题

开始任务前，先检查磁盘中是否有上次销毁时保存的数据；定期将数据保存到磁盘。


### 1. 继承 AbsWorkService, 实现 6 个抽象方法

```
/**
 * 是否 任务完成, 不再需要服务运行?
 * @return 应当停止服务, true; 应当启动服务, false; 无法判断, null.
 */
Boolean shouldStopService();

/**
 * 任务是否正在运行?
 * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, null.
 */
Boolean isWorkRunning();

void startWork();

void stopWork();

//Service.onBind(Intent intent)
@Nullable IBinder onBind(Intent intent, Void unused);

//服务被杀时调用, 可以在这里面保存数据.
void onServiceKilled();
```

### 2. API 说明

#### 启动 Service:

```
Context.startService(new Intent(Context c, Class<? extends AbsWorkService> serviceClass))
```

#### 停止 Service:

在 ? extends AbsWorkService 中, 添加 `stopService()` 方法:

1.操作自己维护的 flag, 使 `shouldStopService()` 返回 `true`;

2.调用自己的方法或第三方 SDK 提供的 API, 停止任务;

3.调用 ```AbsWorkService.cancelJobAlarmSub()``` 取消 Job / Alarm / Subscription.

需要停止服务时, 调用 ? extends AbsWorkService 上的 `stopService()` 即可.

#### 处理白名单:

以下 API 全部位于 IntentWrapper 中:

```
List<IntentWrapper> getIntentWrapperList();

//弹出 android.support.v7.AlertDialog, 引导用户将 App 加入白名单.
void whiteListMatters(Activity a, String reason);

//防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀.
//重写 MainActivity.onBackPressed(), 只保留对以下 API 的调用.
void onBackPressed(Activity a);
```

#### 为节省用户的电量, 当不再需要服务运行时, 可以调用 ```AbsWorkService.cancelJobAlarmSub()``` 取消定时唤醒的 Job / Alarm / Subscription, 并调用 `stopService()` 停止服务.

详见代码及注释。

