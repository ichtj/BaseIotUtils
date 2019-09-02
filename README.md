# 接入方式
#### Step 1. Add the JitPack repository to your build file

Add it in your root **build.gradle** at the end of repositories:

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

#### Step 2. Add the dependency

```groovy
dependencies {
         //以下请按需要选择一种 
         implementation 'com.chtj.base_iotutils:base_iotutils:1.1.6'//以宽高进行屏幕适配,shell,网络判断等多种工具类以及后台存活串口封装等
         implementation 'com.chtj.base_serialport:base_serialport:1.0.1'//只包含串口相关的基本类(SerialPort | SerialPortFinder)
         
}
```
```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //1.1.6 之后与之前有较大改动 
        //增加了适配方案 使用如下
        BaseIotTools.instance().
                        setBaseWidth(1080).//设置宽度布局尺寸
                        setBaseHeight(1920).//设置高度布局尺寸
                        setCreenType(SCREEN_TYPE.WIDTH).//按照宽度适配 SCREEN_TYPE param(WIDTH|HEIGHT)
                        setAutoScreenAdaptation(true).//开启自动适配 true 开启  false关闭
                        initSerice(TraceServiceImpl.class, /*DaemonEnv.DEFAULT_WAKE_UP_INTERVAL*/5000).//是否初始化后台保活Service
                                create(this);
    }
}
```
# 屏幕适配

### 1080*1920 px 效果

![image](https://github.com/freyskill/SerialPortHelper/blob/master/pic/big_screen.png)

### 480*800 px 效果

![image](https://github.com/freyskill/SerialPortHelper/blob/master/pic/small_screen.png)

-可在app Model中找到使用示例

##  base_iotutils Module 路径说明
### 1.\base_iotutils\src\main\java\com\chtj\base_iotutils 常用工具类

--- 进制转换类 | HexUtil

--- 键盘相关 | KeyBoardUtils

--- 网络判断 | NetWorkUtils

--- shell命令工具类 | ShellUtils

--- ShareProfrence工具类 | SPUtils

--- Toast工具类 | ToastUtil

--- 后台服务类 保活 | AbsWorkService

--- App相关信息工具类 | AppMegUtils

--- 屏幕适配相关 | AdaptScreenUtils

### 2.\base_iotutils\src\main\java\top\keepempty\sph\library

---串口相关工具类 | SerialPortHelper

---读写数据线程 | SphThreads(ReadThread|WriteThread)

# SerialPortHelper（Android串口通信）

Android串口通讯助手可以用于需要使用串口通信的Android外设，该库有如下特点：

1. 串口通信部分使用C++实现，在笔者接触的部分设备上实测，使用C++实现与Google官方提供的Demo的方式要快；
2. 支持且必须设置串口接收最大数据长度，初始化库时填入该参数，这样设置的原因是考虑在实际使用中，规定的串口通信协议格式一般会固定有最大长度，方便对数据进行处理；
3. 支持命令一发一收，通过对串口的读写线程进行同步控制，命令会先加入到队列然后依次发送和接收，前提需要设置超时时间以及超时处理，参考下面第4、5点；
4. 支持超时设置，设置超时时间后，如果命令在设置的时间内未反馈，则会根据设置的操作进行重发或退出该命令；
5. 支持超时重发（可以N次重发，具体按需设置）与退出，退出会调用接收回调的 **onComplete** 方法。

### 1、DEMO演示

使用该库简单实现的串口调试助手工具，[APK下载](https://github.com/wave-chtj/BaseIotUtils/blob/master/BaseIotUtils_v1.0_2019_9_2.apk)

![image](https://github.com/freyskill/SerialPortHelper/blob/master/SerialPortHelper.png)

### 2、使用说明

初始化需要设置maxSize，也可以设置isReceiveMaxSize该参数默认为false，详细说明如下：

int maxSize;  // 设置串口读取的最大数据长度

boolean isReceiveMaxSize; // 设置是否接收命令按最大长度进行返回，比如串口协议定义的格式长度为16个字节，这样可以设置maxSize为16，然后设置该参数为true，则接收的命令就会返回16个字节的长度。

 **提示：** 设置isReceiveMaxSize为true是为了处理命令返回不完整的情况，例如完整命令长度为16，但是串口读的过程分几次返回。

```java
SerialPortHelper serialPortHelper = new SerialPortHelper(32);
SerialPortHelper serialPortHelper = new SerialPortHelper(32,true);
```

#### 2.1.初始化串口

```java
//方式一：快速接入方式，设置好串口地址，或者地址和波特率即可，数据位、停止位、校验类型分别默认为8、1、N。
SerialPortHelper serialPortHelper = new SerialPortHelper(32);
//serialPortHelper.openDevice("dev/ttyS0");
serialPortHelper.openDevice("dev/ttyS0",11520);
// 数据接收回调
serialPortHelper.setSphResultCallback(new SphResultCallback() {
            @Override
            public void onSendData(SphCmdEntity sendCom) {
                Log.d(TAG, "发送命令：" + sendCom.commandsHex);
            }

            @Override
            public void onReceiveData(SphCmdEntity data) {
                Log.d(TAG, "收到命令：" + data.commandsHex);
            
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "完成");
            }
        });
```

```java
//方式二：通过SerialPortConfig设置相关串口参数

//串口参数
SerialPortConfig serialPortConfig = new SerialPortConfig();
serialPortConfig.mode = 0;            // 是否使用原始模式(Raw Mode)方式来通讯
serialPortConfig.path = path;         // 串口地址
serialPortConfig.baudRate = baudRate; // 波特率
serialPortConfig.dataBits = dataBits; // 数据位 取值 位 7或 8
serialPortConfig.parity   = checkBits;// 检验类型 取值 N ,E, O
serialPortConfig.stopBits = stopBits; // 停止位 取值 1 或者 2

// 初始化串口
serialPortHelper = new SerialPortHelper(16);
// 设置串口参数
serialPortHelper.setConfigInfo(serialPortConfig);
// 开启串口
isOpen = serialPortHelper.openDevice();
if(!isOpen){
    Toast.makeText(this,"串口打开失败！",Toast.LENGTH_LONG).show();
}
// 数据接收回调
serialPortHelper.setSphResultCallback(new SphResultCallback() {
    @Override
    public void onSendData(SphCmdEntity sendCom) {
        Log.d(TAG, "发送命令：" + sendCom.commandsHex);
    }

    @Override
    public void onReceiveData(SphCmdEntity data) {
        Log.d(TAG, "收到命令：" + data.commandsHex);
        
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "完成");
    }
});
```

#### 2.2.数据发送与接收

```java
// 发送数据
serialPortHelper.addCommands(sendHexTxt);   // 发送十六进制字符串
serialPortHelper.addCommands(sendComBytes); // 发送字节数组

// 发送数据实体
SphCmdEntity comEntry = new SphCmdEntity();
comEntry.commands = commands; // 发送命令字节数组
comEntry.flag = flag;         // 备用标识
comEntry.commandsHex = DataConversion.encodeHexString(commands);  // 发送十六进制字符串
comEntry.timeOut = 100;       // 超时时间 ms
comEntry.reWriteCom = false;  // 超时是否重发 默认false
comEntry.reWriteTimes = 5;    // 重发次数 
comEntry.receiveCount = 1;    // 接收数据条数，默认为1
serialPortHelper.addCommands(comEntry);
```

```java
// 数据接收回调
serialPortHelper.setSphResultCallback(new SphResultCallback() {
    @Override
    public void onSendData(SphCmdEntity sendCom) {
        Log.d(TAG, "发送命令：" + sendCom.commandsHex);
    }

    @Override
    public void onReceiveData(SphCmdEntity data) {
        // 对于接受数据的SphCmdEntity，其中需要使用的有 
        // commandsHex 返回的十六进制数据
        // commands    返回的字节数组
        // flag        备用标识，例如标识该命令是相关操作
        Log.d(TAG, "收到命令：" + data.commandsHex);
        
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "完成");
    }
});
```

### 3、关闭串口

```java
serialPortHelper.closeDevice();
```


# HelloDaemon 后台保活

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

别忘了在 Manifest 中注册这个 Service.

### 2. 自定义 Application

在 Application 的 `onCreate()` 中, 调用

```
DaemonEnv.initialize(
  Context app,  //Application Context.
  Class<? extends AbsWorkService> serviceClass, //刚才创建的 Service 对应的 Class 对象.
  @Nullable Integer wakeUpInterval);  //定时唤醒的时间间隔(ms), 默认 6 分钟.

Context.startService(new Intent(Context app, Class<? extends AbsWorkService> serviceClass));
```

别忘了在 Manifest 中通过 android:name 使用这个自定义的 Application.

### 3. API 说明

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

