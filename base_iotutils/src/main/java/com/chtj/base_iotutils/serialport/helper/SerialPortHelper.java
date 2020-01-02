package com.chtj.base_iotutils.serialport.helper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.chtj.base_iotutils.DataConvertUtils;
import com.chtj.base_iotutils.KLog;
import com.chtj.base_iotutils.entity.ComEntity;
import com.chtj.base_iotutils.serialport.SerialPort;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Create on 2019/9/5
 * author chtj
 */
public class SerialPortHelper {
    private static final String TAG = "SerialPortHelper";
    private SerialPort port = null;//串口控制
    private OnComListener onComListener;//数据回调
    private ComEntity comEntity;
    //串口是否打开 只有在心跳包检测一直执行时才能实时获取串口状态是否正常
    private volatile boolean isOpen = false;
    //是否正在执行任务
    private volatile boolean isExecuteTask = false;
    private static Disposable sDisposable;
    private static Disposable sDisposable2;
    private static SerialPortHelper serialPortHelper;

    public static SerialPortHelper getInstance(){
        if (serialPortHelper == null) {
            synchronized (SerialPortHelper.class) {
                if (serialPortHelper == null) {
                    serialPortHelper = new SerialPortHelper();
                }
            }
        }
        return serialPortHelper;
    }

    /**
     * 重新设置 命令写入时返回异常
     * 重试的次数
     * @param retriesCount 需要重新设置的次数
     * @return this
     */
    public SerialPortHelper setRetriesCount(int retriesCount){
        if(this.comEntity!=null){
            this.comEntity.setRetriesCount(retriesCount);
            return serialPortHelper;
        }else{
            throw new NullPointerException("comEntity is null,please set param");
        }
    }

    /**
     * 注册串口相关的数据监听
     *
     * @param onComListener
     */
    public SerialPortHelper setOnComListener(OnComListener onComListener) {
        this.onComListener = onComListener;
        return serialPortHelper;
    }


    //初始化时默认开启读取线程
    public SerialPortHelper setComEntity(ComEntity comEntity) {
        this.comEntity = comEntity;
        //开启心跳包检测
        openHeartBeatCheck();
        return serialPortHelper;
    }

    //心跳检测
    //主要用于串口是否收发正常
    //如果要使用该方法 需要设置 SerialPortEntity中 heartBeatComm和heartBeatFlag的值
    public void openHeartBeatCheck() {
        if (comEntity != null&&comEntity.getHeartBeatEntity()!=null && comEntity.getHeartBeatEntity().getHeartBeatComm() != null && comEntity.getHeartBeatEntity().getHeartBeatFlag() != 0) {
            sDisposable = Observable
                    .interval(1, comEntity.getHeartBeatEntity().getDelayMillis(), TimeUnit.MILLISECONDS)
                    //取消任务时取消定时唤醒
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {
                            KLog.e(TAG, "heartBeatThread break ...");
                        }
                    })
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long count) throws Exception {
                            if (port != null) {
                                //判断是否在执行读写操作
                                if (!isExecuteTask) {
                                    try{
                                        isExecuteTask = true;//标识正在执行任务
                                        //写入命令去执行心跳
                                        writeData(comEntity.getHeartBeatEntity().getHeartBeatComm());
                                        boolean isNormal = false;
                                        while (true) {
                                            //查询可以读取到的字节数量
                                            //读取超时的检查 设置为3秒
                                            //3秒内无响应 则退出
                                            readSize = port.getInputStream().available();
                                            //KLog.e(TAG, "readSize=" + readSize );
                                            if (readSize <= 0) {
                                                //当前未检查到数据
                                                waitTime += 200;
                                                Thread.sleep(200);
                                                isNormal = false;
                                                if (waitTime >= comEntity.getTimeOut()) {
                                                    waitTime = 0;
                                                    bytes = null;
                                                    //KLog.e(TAG, "HeartBeatThread timeout ");
                                                    break;
                                                }
                                            } else {
                                                //没有超时 获取到了数据
                                                byte[] temporaryComm = new byte[readSize];
                                                port.getInputStream().read(temporaryComm);
                                                KLog.e(TAG, "heartBeat result：" + DataConvertUtils.encodeHexString(temporaryComm));
                                                isNormal = true;
                                                break;
                                            }
                                        }
                                        if (onComListener != null) {
                                            onComListener.comStatus(isNormal);
                                        }
                                        isExecuteTask = false;//标识任务执行完成
                                    }catch(InterruptedException e){
                                        e.printStackTrace();
                                        KLog.e(TAG,"errMeg:"+e.getMessage());
                                    }
                                } else {
                                    KLog.e(TAG, "setWriteAfterRead method is runing,not check heartBeat");
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 打开串口
     */
    public synchronized void openSerialPort() {
        //串口状态为关闭时 才能去执行开启
        if (!isOpen) {
            try {
                port = new SerialPort(new File(comEntity.getCom()), comEntity.getBaudrate(), 0);
                KLog.d(TAG, "serialport param com=" + comEntity.getCom() + ",baudrate=" + comEntity.getBaudrate());
                isOpen = true;
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
                KLog.e(TAG, "errMeg:" + e.getMessage());
                isOpen = false;
            } catch (Exception e) {
                e.printStackTrace();
                KLog.e(TAG, "errMeg:" + e.getMessage());
                isOpen = false;
            }
            if (onComListener != null) {
                if (isOpen) {
                    onComListener.isOpen(true);
                } else {
                    onComListener.isOpen(false);
                }
            }
        }
    }

    /**
     * 添加相关命令
     * 如果需要重新设置 读取异常时 重试的次数，可使用方法{@link #setRetriesCount(int)}
     * @param comm 单个命令
     * @param flag 传进来的flag 读取|写入|超时|写入完成等回调的时候可以标识为当前
     */
    public void setWriteAfterRead(byte[] comm, int flag) {
        List<byte[]> commList = new ArrayList<>();
        commList.add(comm);
        this.setWriteAfterRead(commList, flag);
    }

    /**
     * 添加相关命令
     * 如果需要重新设置 读取异常时 重试的次数，可使用方法{@link #setRetriesCount(int)}
     * @param commList 这次需要执行的命令集合
     * @param flag     传进来的flag 读取|写入|超时|写入完成等回调的时候可以标识为当前
     */
    public void setWriteAfterRead(List<byte[]> commList, final int flag) {
        while (isExecuteTask) {
            //现在正在执行心跳包检测
            KLog.e(TAG,"heartBeatThread is Running,please waiting...");
        }
        Observer observer = new Observer<byte[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                sDisposable2 = d;
            }

            @Override
            public void onNext(byte[] nowData) {
                isExecuteTask=true;
                //数据写入失败之后的重试次数 次数至少为1
                int count = comEntity.getRetriesCount();
                while (--count >= 0) {
                    try {
                        //检查是否成功获取数据
                        //否则循环count次 继续
                        Thread.sleep(250);
                        writeData(nowData);
                        onComListener.writeCommand(nowData, flag);
                        if (readInputStreamData(flag)) {
                            break;
                        } else {
                            KLog.e(TAG, "read err,resend。。。,reSendcount：" + count);
                            //如果剩余次数小于等于0
                            if (count <= 0) {
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                KLog.e(TAG, "onError: ");
            }

            @Override
            public void onComplete() {
                KLog.e(TAG, "onComplete: ");
                if (onComListener != null) {
                    onComListener.writeComplet(flag);
                }
                //操作写读结束
                isExecuteTask = false;
            }
        };
        Observable.fromIterable(commList)
                .subscribe(observer);
    }


    /**
     * 检查是否存在数据
     * 如果不存在数据就要做延时读取操作
     * 这里的延时时间为 {waitTime}
     *
     * @return 存在数据true  不存在数据false
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean checkInputStreaData(int flag) throws IOException, InterruptedException {
        while (true) {
            if (!isOpen) {
                break;
            }
            //查询可以读取到的字节数量
            //读取超时的检查 设置为3秒
            //3秒内无响应 则退出
            readSize = port.getInputStream().available();
            //KLog.d(TAG, "readSize=" + readSize );
            if (readSize <= 0) {
                //当前未检查到数据
                waitTime += 200;
                Thread.sleep(200);
                isExistData = false;
                if (waitTime >= comEntity.getTimeOut()) {
                    waitTime = 0;
                    bytes = null;
                    //回调超时处理 通知UI
                    onComListener.isReadTimeOut(flag);
                    break;
                }
            } else {
                isExistData = true;
                break;
            }
        }
        return isExistData;
    }


    private int waitTime = 0;//线程等待时间
    private int readSize = 0;//目前数据流中的字节数量
    private byte[] bytes = null;//当前读取到数据
    private boolean isExistData = false;//是否读取到了数据

    /**
     * 读取数据流中的数据
     */
    private boolean readInputStreamData(int flag) {
        try {
            while (true) {
                if (!isOpen) {
                    break;
                }
                if(checkInputStreaData(flag)){
                    if(readSize>0){
                        bytes = new byte[readSize];
                        port.read(bytes,readSize);
                    }
                    break;
                }else{
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boolean isSuccessful = false;
            if (onComListener != null && isOpen) {
                if (bytes != null && bytes.length > 0) {
                    byte[] newbytes = new byte[ readSize];
                    System.arraycopy(bytes, 0, newbytes, 0, readSize);
                    onComListener.readCommand(newbytes, flag);
                    isSuccessful = true;//正常情况下到这里都为true
                } else {
                    isSuccessful = false;
                    onComListener.readCommand(null, flag);
                }
            }
            return isSuccessful;
        }
    }

    /**
     * 写数据
     *
     * @param command
     */
    private synchronized void writeData(byte[] command) {
        port.write(command);
    }


    /**
     * 关闭串口和线程
     */
    public synchronized void closeSerialPort() {
        isOpen = false;
        //取消心跳检测
        if (sDisposable != null) {
            sDisposable.dispose();
        }
        if (sDisposable2 != null) {
            sDisposable2.dispose();
        }
        KLog.d(TAG, "heartBeat close");
        if (port != null) {
            port.close();
            port = null;
        }
        if (onComListener != null) {
            KLog.d(TAG, "serialport close");
            onComListener.isOpen(false);
        }
    }
}
