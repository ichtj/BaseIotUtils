package com.future.xlink.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.xlink.XLink;
import com.future.xlink.api.SubscriberSingleton;
import com.future.xlink.bean.Constants;
import com.future.xlink.bean.InitParams;
import com.future.xlink.bean.McuProtocal;
import com.future.xlink.bean.Protocal;
import com.future.xlink.bean.Register;
import com.future.xlink.bean.common.ConnectLostType;
import com.future.xlink.bean.common.ConnectType;
import com.future.xlink.bean.common.InitState;
import com.future.xlink.bean.common.MsgType;
import com.future.xlink.bean.common.RespType;
import com.future.xlink.bean.mqtt.Request;
import com.future.xlink.bean.mqtt.RespStatus;
import com.future.xlink.bean.mqtt.Response;
import com.future.xlink.listener.MessageListener;
import com.future.xlink.logs.Log4J;
import com.future.xlink.utils.Carrier;
import com.future.xlink.utils.GlobalConfig;
import com.future.xlink.utils.JckJsonHelper;
import com.future.xlink.utils.ObserverUtils;
import com.future.xlink.utils.PingUtils;
import com.future.xlink.utils.PropertiesUtil;
import com.future.xlink.utils.ThreadPool;
import com.future.xlink.utils.XBus;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.future.xlink.bean.common.ConnectType.CONNECT_NO_NETWORK;
import static com.future.xlink.bean.common.ConnectType.CONNECT_RESPONSE_TIMEOUT;


/**
 * mqtt消息推送服务
 */

public class RxMqttService extends Service {
    private static final String RESP = "-resp"; //消息回应后缀
    public static final String INIT_PARAM = "initparams";
    private static final Class TAG = RxMqttService.class;

    private final Object lock = new Object();
    private boolean threadTerminated = false; //线程控制器

    InitParams params = null;
    private MqttManager mqttManager;
    private ConcurrentHashMap<String, McuProtocal> map = new ConcurrentHashMap<String, McuProtocal>(); //消息存储
    private String ssid = null;

    @Override
    public void onCreate() {
        super.onCreate();
        XBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log4J.info(TAG, "onStartCommand", "start service");
        if (intent != null) {
            params = (InitParams) intent.getSerializableExtra(INIT_PARAM);
            InitState initState = null; //获取参数状态
            try {
                //开启线程执行消息
                looperQueen();
                if (TextUtils.isEmpty(params.key) || TextUtils.isEmpty(params.secret)) {
                    //key不能为空
                    initState = InitState.INIT_PARAMS_LOST;
                } else {
                    Register register = PropertiesUtil.getProperties(this);
                    if (register.isNull()) {
                        ObserverUtils.getAgentList(RxMqttService.this, params);
                    } else {
                        //这里通知获取参数成功
                        initState = InitState.INIT_SUCCESS;
                    }
                }


            } catch (Exception e) {
                //初始化异常1
                e.printStackTrace();
                initState = InitState.INIT_CONN_SERVICE_ERR;
            } finally {
                if (initState != null) {
                    XBus.post(new Carrier(Carrier.TYPE_MODE_INIT_RX, initState));
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 创建连接
     */
    private void createConect(Register register) {
        mqttManager = MqttManager.getInstance();
        if (!mqttManager.isConnectIsNormal(RxMqttService.this)) {
            //创建连接时网络不可用
//            XBus.post(new Carrier(Carrier.TYPE_MODE_INIT_RX,InitState.CONNECT_NO_NETWORK));
            connnectComplete(CONNECT_NO_NETWORK);
            return;
        }
        mqttManager.creatConnect(RxMqttService.this, params, register);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            map.clear();
            Log4J.info(TAG, "onDestroy", "stop service");
            stopCheckReconnect();
            XBus.unregister(this);
            threadTerminated = true;
            mqttManager.release();
            mqttManager = null;
        } catch (Exception e) {
            e.printStackTrace();
            //停止服务异常2
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(Carrier msg) throws MqttException, IOException {
        if (msg.type == Carrier.TYPE_MODE_INIT_RX) {
            Log.d(TAG.getSimpleName(), "onEvent: TYPE_MODE_INIT_RX");
            //获取初始化参数状态
            InitState initState = (InitState) msg.obj;
            if (XLink.getInstance().getListener() != null)
                XLink.getInstance().getListener().initState(initState);

        } else if (msg.type == Carrier.TYPE_MODE_CONNECT) {
            //创建连接
            Log.d(TAG.getSimpleName(), "onEvent: TYPE_MODE_CONNECT");
            Register register = PropertiesUtil.getProperties(RxMqttService.this);
            map.clear();
            if (mqttManager != null) {
                if (!mqttManager.isConnectIsNormal(RxMqttService.this)) {
                    connnectComplete(CONNECT_NO_NETWORK);
                    return;
                }
                //如果先前的连接还在建立，先断开之前的连接，再重新创建
                if (mqttManager.isConnect()) {
                    mqttManager.release();
                }
                boolean isNetConn = mqttManager.doConntect(RxMqttService.this, params, register);
                Log.d(TAG.getSimpleName(), "onEvent: isNetConn=" + isNetConn);
                if (!isNetConn) {
                    ConnectType connectType = CONNECT_RESPONSE_TIMEOUT;
                    if (XLink.getInstance().getListener() != null) {
                        Log.d(TAG.getSimpleName(), "onEvent: CONNECT_RESPONSE_TIMEOUT");
                        XLink.getInstance().getListener().connectState(connectType);
                    }
                }
            } else {
                Log.d(TAG.getSimpleName(), "onEvent: createConect(register);");
                createConect(register);
            }
        } else if (msg.type == Carrier.TYPE_MODE_CONNECTED) {
            //连接成功
            Log.d(TAG.getSimpleName(), "onEvent: TYPE_MODE_CONNECTED");
            ConnectType type = (ConnectType) msg.obj;
            connnectComplete(type);
            if (type == ConnectType.CONNECT_SUCCESS)
                subscrible();
        } else if (msg.type == Carrier.TYPE_MODE_RECONNECT_COMPLETE) {
            //重连成功
            Log.d(TAG.getSimpleName(), "onEvent: TYPE_MODE_RECONNECT_COMPLETE");
            stopCheckReconnect();//停止网络状态检测
            //为true表示重连成功 为false代表第一次连接
            boolean reconnect = (Boolean) msg.obj;
            if (reconnect) {
                connnectComplete(ConnectType.RECONNECT_SUCCESS);
                subscrible();
            }
        } else if (msg.type == Carrier.TYPE_MODE_DISCONNECT) {
            //连接断开 注销此连接
            Log.d(TAG.getSimpleName(), "onEvent: TYPE_MODE_DISCONNECT");
            stopCheckReconnect();//如果有网络状态检测，停止
            if (mqttManager != null) {
                mqttManager.release();
                map.clear();
            }
        } else if (msg.type == Carrier.TYPE_MODE_CONNECT_LOST) {
            //连接丢失
            if (params.automaticReconnect)
                checkReconnect();//开始重连状态检测
            MessageListener listener = XLink.getInstance().getListener();
            if (listener != null) {
                //代理转发到
                listener.connectionLost(ConnectLostType.LOST_TYPE_0, (Throwable) msg.obj);
            }
        } else if (msg.type == Carrier.TYPE_REMOTE_RX) {
            //代理服务器下发消息
            MqttMessage mqttMessage = (MqttMessage) msg.obj;
            String temp = mqttMessage.toString();
            ObjectMapper mapper = JckJsonHelper.get();

            Request request = mapper.readValue(temp, Request.class);
            parseData(msg.type, request);
        } else if (msg.type == Carrier.TYPE_REMOTE_TX_EVENT || msg.type == Carrier.TYPE_REMOTE_TX_SERVICE || msg.type == Carrier.TYPE_REMOTE_TX) {
            //事件，服务属性上报
            Protocal protocal = (Protocal) msg.obj;
            parseData(msg.type, protocal);
        }
    }

    private void connnectComplete(ConnectType type) {
        MessageListener listener = XLink.getInstance().getListener();
        if (listener != null) {
            listener.connectState(type);
        }
    }

    /**
     * 订阅消息
     */
    private void subscrible() {
        //添加#进行匹配
        mqttManager.subscribe("dev/" + params.sn + "/#", 2);
    }


    /**
     * 解析客户端上报的消息，添加到消息map集合中
     **/
    private synchronized void parseData(int type, Protocal protocal) {
        //消息iid为上传判断
        if (protocal == null || TextUtils.isEmpty(protocal.iid)) {
            //抛出异常消息id为空，空指针异常3
            protocal.rx = JckJsonHelper.toJson(new RespStatus(RespType.RESP_IID_LOST.getTye(), RespType.RESP_IID_LOST.getValue()));
            sendTxMsg(protocal);
            return;
        }
        //iid消息重复
        if (TextUtils.isEmpty(protocal.rx) && map.containsKey(protocal.iid)) {
            //抛出异常，消息iid重复发送4
            protocal.rx = JckJsonHelper.toJson(new RespStatus(RespType.RESP_IID_REPEAT.getTye(), RespType.RESP_IID_REPEAT.getValue()));
            sendTxMsg(protocal);
            return;
        }

        McuProtocal mcuprotocal = null;
        if (map.containsKey(protocal.iid)) {
            mcuprotocal = map.get(protocal.iid);
            mcuprotocal.status = mcuprotocal.status + 1;
        } else {
            mcuprotocal = new McuProtocal();
            mcuprotocal.iid = protocal.iid;
            mcuprotocal.time = System.currentTimeMillis();
            mcuprotocal.act = "cmd";//另外新加的参数 回复某种情况下由于未及时回复 而需要回复的情况
            mcuprotocal.ack = "svr/" + Build.SERIAL;//另外新加的参数 回复某种情况下由于未及时回复 而需要回复的情况
        }
        if (type == Carrier.TYPE_REMOTE_TX_SERVICE) {
            mcuprotocal.act = "upload";
            mcuprotocal.ack = MsgType.MSG_PRO.getTye() + "/" + getSsid();
        } else if (type == Carrier.TYPE_REMOTE_TX_EVENT) {
            mcuprotocal.act = "event";
            mcuprotocal.ack = MsgType.MSG_EVENT.getTye() + "/" + getSsid();
        } else if (type == Carrier.TYPE_REMOTE_TX) {
            if (!mcuprotocal.act.contains(RESP)) {
                mcuprotocal.act = mcuprotocal.act + RESP;
            } else if (mcuprotocal.act.contains(RESP)) {
                mcuprotocal.status = mcuprotocal.status + 1;
            }
        }
        mcuprotocal.type = type;
        mcuprotocal.tx = protocal.tx;
        map.put(mcuprotocal.iid, mcuprotocal);
    }

    /**
     * 解析代理服务器下发的消息，添加到消息map集合中
     **/
    private synchronized void parseData(int type, Request request) {
        McuProtocal protocal = null;
        if (map.containsKey(request.iid)) {
            protocal = map.get(request.iid);
            //如果接收到代理服务端下发的重复数据，还没有处理，需要过滤掉
            if (protocal.tx == null) {
                Log4J.info(TAG, "parseData", "重复数据下发-->" + request.iid);
                return;
            }
            protocal.status = protocal.status + 1;
        } else {
            protocal = new McuProtocal();
            protocal.ack = request.ack;
            protocal.iid = request.iid;
            protocal.act = request.act;
            protocal.time = System.currentTimeMillis();
        }
        protocal.type = type;
        String rx = JckJsonHelper.toJson(request.inputs);
        if (!TextUtils.isEmpty(request.act) && request.act.contains(RESP)) {
            protocal.rx = JckJsonHelper.toJson(new RespStatus(RespType.RESP_SUCCESS.getTye(), RespType.RESP_SUCCESS.getValue()));
        } else {
            protocal.rx = rx;
        }

        map.put(request.iid, protocal);

    }

    /**
     * 开启消息接收线程，轮询消息处理
     */
    private void looperQueen() {
        threadTerminated = false;
        ThreadPool.add(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    while (!threadTerminated) {
                        try {
                            executeQueen();
                            lock.wait(50);
                        } catch (InterruptedException e) {
                            //数据处理异常5
                            Log4J.info(getClass(), "读取等待", e);
                        }
                    }
                }
            }
        });
    }

    /**
     * 消息中转处理判断
     */
    private void executeQueen() {
        for (Map.Entry<String, McuProtocal> entry : map.entrySet()) {
            McuProtocal protocal = entry.getValue();
            if (protocal.isOverTime()) {
                //超时未应答
                if (protocal.type == Carrier.TYPE_REMOTE_RX) {
                    if (protocal.tx == null)
                        protocal.tx = JckJsonHelper.toJson(new RespStatus(RespType.RESP_OUTTIME.getTye(), RespType.RESP_OUTTIME.getValue()));
                } else if (protocal.type == Carrier.TYPE_REMOTE_TX || protocal.type == Carrier.TYPE_REMOTE_TX_EVENT || protocal.type == Carrier.TYPE_REMOTE_TX_SERVICE) {
                    //告知消息超时，发送消息到代理服务器

                    if (TextUtils.isEmpty(protocal.rx))
                        protocal.rx = JckJsonHelper.toJson(new RespStatus(RespType.RESP_OUTTIME.getTye(), RespType.RESP_OUTTIME.getValue()));
                }
                //超时两端都需要汇报
                sendTxMsg(protocal);
                sendRxMsg(protocal);
                map.remove(protocal.iid);

            } else {
                //未超时
                if (protocal.status == 0) {
                    //消息发送处理
                    judgeMethod(protocal);
                    protocal.status = protocal.status + 1;
                } else {
                    //消息应答处理
                    if (protocal.tx != null && (!TextUtils.isEmpty(protocal.rx))) {
                        if (judgeMethod(protocal))
                            map.remove(protocal.iid);
                    }
                }
            }
        }
    }

    private boolean judgeMethod(McuProtocal protocal) {
        boolean flag = false;
        //非超时处理
        if (protocal.type == Carrier.TYPE_REMOTE_TX || protocal.type == Carrier.TYPE_REMOTE_TX_EVENT || protocal.type == Carrier.TYPE_REMOTE_TX_SERVICE) {
            flag = sendRxMsg(protocal);
        } else if (protocal.type == Carrier.TYPE_REMOTE_RX) {
            flag = sendTxMsg(protocal);
        }
        return flag;
    }


    /**
     * 获取ssid
     */
    private String getSsid() {
        if (TextUtils.isEmpty(ssid)) {
            PropertiesUtil propertiesUtil = PropertiesUtil.getInstance(RxMqttService.this).init();
            ssid = propertiesUtil.readString(Constants.SSID, "");
        }
        return ssid;
    }

    /**
     * 转发消息到调用端
     */
    private boolean sendTxMsg(Protocal msg) {
        MessageListener listener = XLink.getInstance().getListener();
        if (listener != null) {
            listener.messageArrived(msg);
            return true;
        }
        return false;
    }

    /**
     * 发送消息到服务端
     */
    private boolean sendRxMsg(McuProtocal msg) {
        try {
            if (mqttManager != null && mqttManager.isConnect()) {
                Response response = new Response();
                response.act = msg.act;
                response.iid = msg.iid;

                response.payload = msg.tx;
                boolean isComplete = mqttManager.publish(msg.ack, 2, JckJsonHelper.toJson(response).getBytes());
                Log4J.info(TAG, "sendRxMsg", "test msg-->" + JckJsonHelper.toJson(response));
                //Log.d(TAG.getSimpleName(), "sendRxMsg: isComplete="+isComplete);
                return isComplete;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //7消息发送异常
            Log.d(TAG.getSimpleName(), "sendRxMsg: errMeg=" + e.getMessage());
        }
        return false;
    }

    /**
     * 网络重连状态检测
     */
    public void checkReconnect() {
        int outtime = params.reconnectTime;
        SubscriberSingleton.add(TAG.getName(), Observable.interval(1, 5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            boolean isConnect = mqttManager.isConnect();
            Log.d(TAG.getSimpleName(), "checkReconnect: isConnect=" + isConnect);
            if (!isConnect) {
                int specifiedTime = outtime * 60 / 5;
                MessageListener listener = XLink.getInstance().getListener();
                ConnectLostType type = null;
                if (aLong.intValue() == specifiedTime) {
                    Log.d(TAG.getSimpleName(), "checkReconnect: next ");
                    boolean state = PingUtils.ping("114.114.114.114");
                    if (state) {
                        //百度能ping通，说明网络通讯正常；
                        boolean state2 = PingUtils.ping(GlobalConfig.HTTP_SERVER);
                        if (state2) {
                            //网络和代理连接正常，但服务无法正常连接
                            type = ConnectLostType.LOST_TYPE_1;
                        } else {
                            //网络正常，代理服务异常2
                            type = ConnectLostType.LOST_TYPE_2;
                        }
                    } else {
                        //本地网络异常，3
                        type = ConnectLostType.LOST_TYPE_3;
                    }
                    if (listener != null) {
                        listener.connectionLost(type, new Throwable(type.getValue()));
                    }
                } else if (aLong.intValue() > specifiedTime) {
                    //本地网络异常，3
                    type = ConnectLostType.LOST_TYPE_3;
                    if (listener != null) {
                        listener.connectionLost(type, new Throwable(type.getValue()));
                    }
                }
            }
        }));
    }

    /**
     * 停止网络状态检测
     **/
    private void stopCheckReconnect() {
        Log.d(TAG.getSimpleName(), "stopCheckReconnect: ");
        SubscriberSingleton.clear(TAG.getName());
    }
}
