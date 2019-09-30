package com.chtj.base_iotutils.serialport.helper;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.chtj.base_iotutils.DataConversion;
import com.chtj.base_iotutils.entity.ComEntity;
import com.chtj.base_iotutils.serialport.SerialPort;


/**
 * Create on 2019/9/5
 * author chtj
 */
public class SerialPortHelper {
    private static final String TAG = "SerialPortHelper";
    private Context context;//上下文
    private SerialPort port = null;//串口控制
    private OnComListener onComListener;//数据回调
    private ComEntity comEntity;
    private static boolean isOpen = false;//串口是否打开
    //定时检查串口是否正常
    private Handler handler = new Handler();
    //是否正在操作，一般存在两个状态
    //是否正在执行心跳包检测
    private boolean isHeartBeatOperation = false;
    //是否正在读写
    private boolean isWriteReadOperation = false;

    /**
     * 注册串口相关的数据监听
     *
     * @param onComListener
     */
    public void setOnComListener(OnComListener onComListener) {
        this.onComListener = onComListener;
    }

    //初始化时默认开启读取线程
    public SerialPortHelper(Context context, ComEntity comEntity) {
        this.comEntity = comEntity;
        this.context = context;
        //开启心跳包检测
        openHeartBeatCheck();
    }

    //心跳检测
    //主要用于串口是否收发正常
    //如果要使用该方法 需要设置 SerialPortEntity中 heartBeatComm和heartBeatFlag的值
    public void openHeartBeatCheck() {
        if (comEntity != null && comEntity.getHeartBeatEntity().getHeartBeatComm() != null && comEntity.getHeartBeatEntity().getHeartBeatFlag() != 0) {
            handler.postDelayed(heartBeatRunnable, 5000);
        }
    }

    Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (port != null) {
                try {
                    if (!isWriteReadOperation) {//判断是否在执行写读操作
                        writeData(comEntity.getHeartBeatEntity().getHeartBeatComm());
                        //改变为正在执行
                        isHeartBeatOperation = true;
                        boolean isNormal = false;
                        while (true) {
                            //查询可以读取到的字节数量
                            //读取超时的检查 设置为3秒
                            //3秒内无响应 则退出
                            readSize = port.getInputStream().available();
                            //Log.e(TAG, "readSize=" + readSize );
                            if (readSize <= 0) {
                                //当前未检查到数据
                                waitTime += 200;
                                Thread.sleep(200);
                                isNormal = false;
                                if (waitTime >= comEntity.getTimeOut()) {
                                    waitTime = 0;
                                    bytes = null;
                                    break;
                                }
                            } else {
                                //没有超时 获取到了数据
                                byte[] temporaryComm = new byte[readSize];
                                port.getInputStream().read(temporaryComm);
                                Log.e(TAG, "心跳包返回数据：" + DataConversion.encodeHexString(temporaryComm));
                                isNormal = true;
                                break;
                            }
                        }
                        //Log.e(TAG, "串口能正常收发");
                        if (onComListener != null) {
                            onComListener.comStatus(isNormal);
                        }
                        isHeartBeatOperation = false;
                    } else {
                        Log.e(TAG, "当前正在进行写读操作，所以暂时不检查串口是否正常");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "heartBeatCheck: errMeg:" + e.getMessage());
                }
            }
            handler.postDelayed(this, comEntity.getHeartBeatEntity().getDelayMillis());
        }
    };


    /**
     * 打开串口
     */
    public void openSerialPort() {
        //串口状态为关闭时 才能去执行开启
        if (!isOpen) {
            try {
                port = new SerialPort(new File(comEntity.getCom()), comEntity.getBaudrate(),0);
                Log.e(TAG, "串口打开成功 com=" + comEntity.getCom() + ",baudrate=" + comEntity.getBaudrate());
                isOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                isOpen = false;
            } catch (SecurityException e) {
                e.printStackTrace();
                isOpen = false;
            } catch (Exception e) {
                e.printStackTrace();
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
     *
     * @param comm 单个命令
     * @param flag 传进来的flag 读取|写入|超时|写入完成等回调的时候可以标识为当前
     */
    public void setWriteRead(byte[] comm, int flag) {
        List<byte[]> commList = new ArrayList<>();
        commList.add(comm);
        this.setWriteRead(commList, flag);
    }

    /**
     * 添加相关命令
     *
     * @param commList 这次需要执行的命令集合
     * @param flag     传进来的flag 读取|写入|超时|写入完成等回调的时候可以标识为当前
     */
    public synchronized void setWriteRead(List<byte[]> commList, int flag) {
        while (isHeartBeatOperation) {
            //如果正在操作心跳包检测
            //则暂时等待
        }
        if (port == null) {
            if (onComListener != null) {
                onComListener.comStatus(false);
                return;
            }
        }
        isWriteReadOperation = true;//操作中
        if (commList != null && commList.size() > 0) {
            //Iterator 方便删除数据 而不影响下标
            Iterator<byte[]> it = commList.iterator();
            //用于标识3次机会是否用完 并且是否成功写入和读取
            //只有命令发送后在3次机会中成功至少一次的才能继续向下执行
            boolean isSuccessful = true;
            while (it.hasNext()) {
                // 注意：!comEntity.getFlagFilterArray().contains(flag)
                //如果写入的命令存在一条以上时 执行完成一条后
                //如果添加进来的flag不管上一条是否执行失败 继续向下执行
                //否则只执行第一条就退出了
                if (isSuccessful == false && !comEntity.getFlagFilterArray().contains(flag)) {
                    break;
                }
                byte[] nowData = it.next();
                int count = comEntity.getRetriesCount();//数据写入之后的重试次数
                while (--count >= 0) {
                    //每次减去1
                    //然后去检查是否成功获取数据
                    //否则循环n次 继续
                    //还是失败的话则升级失败
                    try {
                        Thread.sleep(250);
                        writeData(nowData);
                        Log.e(TAG, "写入命令>>>：" + DataConversion.encodeHexString(nowData));
                        onComListener.writeCommand(nowData, flag);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "errMeg=" + e.getMessage());
                    } finally {
                        try {
                            if (readInputStreamData(flag)) {
                                it.remove();
                                isSuccessful = true;
                                break;
                            } else {
                                isSuccessful = false;
                                Log.e(TAG, "读取异常,继续重发,剩余重发送次数：" + count);
                                //如果剩余次数小于等于0
                                if (count <= 0) {
                                    if (comEntity.getFlagFilterArray().contains(flag)) {
                                        it.remove();
                                    } else {
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, ">>" + e.getMessage());
                        } finally {
                            Log.e(TAG, "剩余写入的命令数量=" + commList.size());

                            if (commList == null || commList.size() == 0) {
                                onComListener.writeComplet(flag);
                            }
                        }
                    }
                }
            }
            //这里的判断 是防止可能命令只有一个
            if (!isSuccessful) {
                Log.e(TAG, "升级失败了~~~~");
            }
        }
        //操作结束
        isWriteReadOperation = false;
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
            //查询可以读取到的字节数量
            //读取超时的检查 设置为3秒
            //3秒内无响应 则退出
            readSize = port.getInputStream().available();
            //Log.e(TAG, "readSize=" + readSize );
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


    private int count = 0;//当前数据记录到哪一个位置
    private long datalength = -1;//接收到数据的总长度
    private int waitTime = 0;//线程等待时间
    private int readSize = 0;//目前数据流中的字节数量
    private int readNum = -1;//读取到的一个字节
    private byte[] bytes = null;//当前读取到数据
    private boolean isExistData = false;//是否读取到了数据
    private byte[] dataArrayLength;//记录命令中返回的两个字节长度
    private int dataArrayCount = 0;

    /**
     * 读取数据流中的数据
     * 这里需要进行自定义
     * ①比如我这里存在两个数据头 0xAA和0x55  也就是对应(byte[0]下标count=0)和(byte[1]下标count=1)  那么就是要读两个字节出来判断
     * ②然后就是获取data长度 我这里用了两个字节表示长度 byte[3]和byte[4] 这个要看协议是否一致 不过一般是一个字节
     * ③获取到data长度后好需要加上一些固定长度 比如获取到的data为{0x12,0x13,0x14,0x15}长度为4 那么还需要加上另外数据位才能得到完整的总长度
     * 例如返回的数据：AA55000004A001A3017F
     * ①AA55为数据头 length=2
     * ②00 为地址 length=1
     * ③00 04 为data长度 length=2  这里为什么是04：因为我们的协议把④+⑤的长度算在了一起
     * ④A0 为指令 length=1
     * ⑤01 A3 01 为data内容 length=3
     * ⑥7F 为crc校验值 length=1
     * 注：
     * 所以除了data之外的其他固定长度为6
     * 再加上data的长度为4
     * 所以总长度产生的数据为AA 55 00 00 04 A0 01 A3 01 7F
     */
    private boolean readInputStreamData(int flag) {
        try {
            while (true) {
                boolean isBreak = false;//是否需要中断
                bytes = new byte[256];
                count = 0;
                dataArrayCount = 0;
                dataArrayLength = new byte[comEntity.getDataArrayLeng()];
                while (true) {
                    if (checkInputStreaData(flag)) {
                        if ((readNum = port.getInputStream().read()) != -1) {
                            bytes[count] = (byte) readNum;
                            //检查数据头是否正确
                            if (count <= comEntity.getHeadDataList().size()-1 && !comEntity.getHeadDataList().contains(bytes[count])) {
                                //Log.e(TAG,"count="+count+","+!comEntity.getHeadDataList().contains(bytes[count])+",data="+DataConversion.byteToHex(bytes[count]));
                                break;
                            }
                            if (count >= comEntity.getDataArrayStartIndex() && dataArrayCount <= dataArrayLength.length - 1) {
                                //count>=n长度位置开始的位置
                                //dataArrayCount<=dataArrayLength.length-1 读到指定的长度则停止对其赋值
                                dataArrayLength[dataArrayCount] = bytes[count];
                                if (dataArrayCount == dataArrayLength.length - 1) {
                                    //证明dataArrayLength读取完毕
                                    String dataLenHex = DataConversion.encodeHexString(dataArrayLength);
                                    if (dataLenHex.length() % 2 == 1) {
                                        dataLenHex = "0" + dataLenHex;//高位补0
                                    }
                                    datalength = DataConversion.hexToDec(dataLenHex) + comEntity.getFixedLength();
                                    Log.e(TAG, "数据包长度=" + datalength);
                                }
                                ++dataArrayCount;
                            }
                            //这里是判断指令位是否正确
                            if (count == comEntity.getInstructionStartIndex()) {//查看属于哪一种命令
                                if (comEntity.getInstructionList().contains(bytes[count])) {
                                    Log.e(TAG, "指令正确");
                                } else {
                                    isBreak = true;
                                    break;//跳出循环 开始进行下一轮
                                }
                            }
                            if (datalength == count + 1) {
                                isBreak = true;
                                //Log.e(TAG,"数据接收完成了噢,完整的数据为：count"+count);
                                break;
                            }
                            count++;
                        }
                    } else {
                        isBreak = true;
                        break;
                    }
                }
                if (isBreak) {
                    //这里是由于数据读取完成 或者因为读取过程中超时
                    //所以需要退出
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boolean isSuccessful = false;
            if (onComListener != null) {
                if (bytes != null && bytes.length > 0) {
                    //Log.e(TAG, "数据接收完成了噢,完整的数据为1：" + DataConversion.encodeHexString(bytes));
                    byte[] newbytes = new byte[(int) datalength];
                    System.arraycopy(bytes, 0, newbytes, 0, (int) datalength);
                    onComListener.readCommand(newbytes, flag);
                    isSuccessful = true;//正常情况下到这里都为true
                    //Log.e(TAG, "数据接收完成了噢,完整的数据为2：" + DataConversion.encodeHexString(newbytes));
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
    public void closeSerialPort() {
        if (port != null) {
            port.close();
            port = null;
        }
        //取消心跳检测
        if (heartBeatRunnable != null) {
            Log.e(TAG, "心跳包周期检测已关闭");
            handler.removeCallbacks(heartBeatRunnable);
        }
        //设置为关闭状态
        isOpen = false;
        if (onComListener != null) {
            onComListener.isOpen(false);
        }
    }
}
