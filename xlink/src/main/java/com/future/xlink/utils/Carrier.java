package com.future.xlink.utils;

import android.util.SparseIntArray;

import io.reactivex.annotations.NonNull;

/**
 * 总线消息载体
 */
public class Carrier {
    public static final int TYPE_SERIAL_MCU_RX = 1000;
    public static final int TYPE_SERIAL_MCU_TX = 1001;
    public static final int TYPE_SERIAL_MCU_TIME_OUT = 1002;
    public  static final int TYPE_MODE_INIT_TX=1003;


    /**
     * 远程控制指令
     */
    public static final int TYPE_REMOTE_RX = 2000;
    public static final int TYPE_REMOTE_TX = 2001;

    public static final int TYPE_REMOTE_TIME_OUT = 2002;
    public  static final  int TYPE_MODE_INIT_RX=2003;   //获取初始化参数
    public  static final  int TYPE_MODE_CONNECT=2005;  //创建connect连接
    public  static final  int TYPE_MODE_CONNECTED=2004; //创建连接 变动的状态

    public  static final  int TYPE_MODE_DISCONNECT=2006; //取消连接
    public  static  final int TYPE_MODE_RECONNECT_COMPLETE=2007; //重连连接完成
    public  static  final int TYPE_MODE_CONNECT_LOST=2008; //连接异常
    public  static  final  int TYPE_REMOTE_TX_SERVICE=2009; //属性服务上报
    public static final int TYPE_REMOTE_TX_EVENT=20010; //事件上报
    /**
     * 远程升级指令
     */
    public static final int TYPE_REMOTE_MCU_UPDATE_RX = 3000;
    public static final int TYPE_REMOTE_MCU_UPDATE_TX = 3001;
    public static final int TYPE_REMOTE_MCU_UPDATE_TIME_OUT = 3002;
    public  static final int TYPE_MODE_INIT_TIMEOUT=3003;


    public static final int TYPE_TCP_SEND = 5000;


    /**
     * 信号强度
     */
    public static final int TYPE_SIGNAL_STRENGTH = 9000;
    /**
     * 升级
     */
    public static final int TYPE_PAGE_UPDATE = 9001;

    public int type;
    public  String topic;
    public Object obj;

    public static final int MODE_MCU = 100;
    public static final int MODE_REMOTE = 200;
    public static final int MODE_REMOTE_UPDATE = 300;
    public  static final  int MODE_INIT=400;


    /**
     * 发送映射
     */
    private static SparseIntArray txSia;
    /**
     * 接收映射
     */
    private static SparseIntArray rxSia;
    /**
     * 超时映射
     */
    private static SparseIntArray toSia;

    static {
        txSia = new SparseIntArray();
        txSia.put(MODE_MCU, TYPE_SERIAL_MCU_TX);
        txSia.put(MODE_REMOTE, TYPE_REMOTE_TX);
        txSia.put(MODE_REMOTE_UPDATE, TYPE_REMOTE_MCU_UPDATE_TX);
        txSia.put(MODE_INIT,TYPE_MODE_INIT_TX);

        rxSia = new SparseIntArray();
        rxSia.put(TYPE_SERIAL_MCU_TX, TYPE_SERIAL_MCU_RX);
        rxSia.put(TYPE_REMOTE_TX, TYPE_REMOTE_RX);
        rxSia.put(TYPE_REMOTE_MCU_UPDATE_TX, TYPE_REMOTE_MCU_UPDATE_RX);
        rxSia.put(TYPE_MODE_INIT_TX,TYPE_MODE_INIT_RX);

        toSia = new SparseIntArray();
        toSia.put(TYPE_SERIAL_MCU_TX, TYPE_SERIAL_MCU_TIME_OUT);
        toSia.put(TYPE_REMOTE_TX, TYPE_REMOTE_TIME_OUT);
        toSia.put(TYPE_REMOTE_MCU_UPDATE_TX, TYPE_REMOTE_MCU_UPDATE_TIME_OUT);
        toSia.put(TYPE_MODE_INIT_TX,TYPE_MODE_INIT_TIMEOUT);

    }

    public Carrier(int type, @NonNull Object obj) {
        this.type = type;
        this.obj = obj;
    }
    public Carrier(int type,@NonNull String topic, @NonNull Object obj) {
        this.type = type;
        this.topic=topic;
        this.obj = obj;
    }

    public Carrier(int type) {
        this.type = type;
    }

    /**
     * Netty发送消息
     */
    public static Carrier tcpTx(@NonNull String json) {
        return new Carrier(TYPE_TCP_SEND, json);
    }

    /**
     * 接收载体
     */
    public static Carrier rx(int eventType, @NonNull Object protocol) {
        int index = rxSia.indexOfKey(eventType);
        int type = rxSia.valueAt(index);
        return new Carrier(type, protocol);
    }

    /**
     * 单片机发送
     */
    public static Carrier tx(int mode, @NonNull Object data) {
        int index = txSia.indexOfKey(mode);
        int type = txSia.valueAt(index);
        return new Carrier(type, data);
    }

    /**
     * 超时
     */
    public static Carrier timeOut(int eventType, String msg) {
        int index = toSia.indexOfKey(eventType);
        int type = toSia.valueAt(index);
        return new Carrier(type, msg);
    }

    public static boolean isMcuTimeOut(int type) {
        return type == TYPE_SERIAL_MCU_TIME_OUT;
    }

    public static boolean isRemoteTimeOut(int type) {
        return type == TYPE_REMOTE_TIME_OUT;
    }

    public static boolean isUpdateTimeOut(int type) {
        return type == TYPE_REMOTE_MCU_UPDATE_TIME_OUT;
    }
}