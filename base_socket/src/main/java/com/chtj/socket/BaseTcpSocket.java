package com.chtj.socket;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TCP操作工具类
 */
public class BaseTcpSocket implements ISocket {

    protected String TAG = getClass().getSimpleName();

    protected String mHost;
    protected int mPort;

    protected Socket mSocket;
    protected InputStream mInStream;
    protected OutputStream mOutStream;

    protected ISocketListener mSocketListener;

    /**
     * 包含 1、连接
     * 2、发送
     */
    private ExecutorService esSocket = Executors.newFixedThreadPool(3);
    private LinkedBlockingQueue<SocketData> queueBuffer = new LinkedBlockingQueue<>();
    private Future futSend, ftReceive;

    /**
     * @param ip
     * @param port
     */
    public BaseTcpSocket(String ip, int port, int timeout) {
        this(ip, port, timeout, null);
    }

    public BaseTcpSocket(String ip, int port, int timeout, ISocketListener listener) {
        this.mSocketListener = listener;
        this.timeout = timeout;
        this.mHost = ip;
        this.mPort = port;
    }

    @Override
    public void connect(final Context ctx) {
        ftReceive = esSocket.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    initSocket(ctx);
                    startReceive();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mSocketListener != null) {
                        mSocketListener.connFaild(e);
                        Log.d(TAG, "connect: errMeg="+e.getMessage());
                    }
                }
            }
        });
    }

    protected final int PACKED_BUFFER_SIZE = 3000;
    protected int timeout = 30000;//超时时间
    protected final int RECEIVE_BUFFER_SIEZE = 2 * 1024 * 1024;

    /**
     * 连接成功开始接收数据
     */
    private void startReceive() {
        try {
            mOutStream = mSocket.getOutputStream();
            mInStream = mSocket.getInputStream();
            if (mSocketListener != null) {
                mSocketListener.connSuccess();//发送通知 ，连接成功
            }
            //开始执行发送线程
            startSend();
            // 从Socket当中得到InputStream对象
            byte[] buffer = new byte[PACKED_BUFFER_SIZE];
            int packetSize;
            while ((packetSize = mInStream.read(buffer, 0, PACKED_BUFFER_SIZE)) != -1) {
                if (mSocketListener != null) {
                    mSocketListener.recv(buffer, 0, packetSize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeIoThread();//关闭IO 和多线程
            if (mSocketListener != null) {
                Log.d(TAG, "startReceive: errMeg="+e.getMessage());
                mSocketListener.connFaild(e);
            }
        }
    }

    /**
     * 开始执行发送线程
     */
    private void startSend() {
        if (futSend == null || futSend.isDone()) {
            futSend = esSocket.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        SocketData data;
                        while ((data = queueBuffer.take()) != null) {
//                            Log.d(TAG, "run: -->发送数据 size:" + data.size);
//                            Log.d(TAG, "run: "+ Arrays.toString(data.data));
                            mOutStream.write(data.data, data.offset, data.size);
                            mSocketListener.writeSuccess(data.data);
                        }
                    } catch (InterruptedException e) {

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    queueBuffer.clear();
                }
            });
        }


    }

    @Override
    public void send(byte[] data) {
        send(data, 0, data.length);
    }

    @Override
    public void send(byte[] data, int offset, int size) {
        try {
            queueBuffer.put(new SocketData(data, offset, size));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        closeIoThread();//关闭IO 和多线程
        if (mSocketListener != null) {
            mSocketListener.connClose();
            mSocketListener = null;
        }
    }

    /**
     * 关闭IO 和多线程
     */
    private void closeIoThread() {
        esSocket.submit(new Runnable() {
            @Override
            public void run() {
                //先把流给关闭
                try {
                    if (mInStream != null) {
                        mInStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (mOutStream != null) {
                        mOutStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (mSocket != null) {
                        mSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (futSend != null) {
            futSend.cancel(true);
        }
        if (ftReceive != null) {
            ftReceive.cancel(true);
        }
    }

    @Override
    public boolean isClosed() {
        return mSocket != null && mSocket.isClosed();
    }

    @Override
    public void setSocketListener(ISocketListener listener) {
        this.mSocketListener = listener;
    }

    /**
     * 初始化socket
     *
     * @param ctx 上下文
     * @return is init success
     */
    protected void initSocket(Context ctx) throws Exception {
        mSocket = new Socket();
        //设置IP和端口
        InetSocketAddress isa = new InetSocketAddress(mHost, mPort);
        //设置接收的长度
        mSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIEZE);
        //执行连接
        mSocket.connect(isa, timeout);
    }
}
