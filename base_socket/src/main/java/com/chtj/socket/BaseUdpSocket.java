package com.chtj.socket;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * UDP操作工具类
 */
public class BaseUdpSocket implements ISocket {
    private String TAG = getClass().getSimpleName();

    private DatagramSocket mDatagramSocket;
    private final int MAX_UDP_DATAGRAM_LEN = 1024 * 4;
    private int timeout = 15000;
    protected ISocketListener mSocketListener;

    private byte[] sendBuffer = new byte[MAX_UDP_DATAGRAM_LEN];

    protected ExecutorService esClient = Executors.newFixedThreadPool(3);
    private Future ftRecv, ftSend;

    private LinkedBlockingQueue<SocketData> queueBuffer = new LinkedBlockingQueue<>();

    protected String mHost;
    protected int mPort;

    private int packetSize = MAX_UDP_DATAGRAM_LEN;

    /**
     * @param ip
     * @param port
     */
    public BaseUdpSocket( String ip, int port, int timeout) {
        this(ip, port, timeout, null);
    }

    public BaseUdpSocket( String ip,  int port, int timeout,  ISocketListener listener) {
        this.mSocketListener = listener;
        this.mHost = ip;
        this.mPort = port;
        this.timeout=timeout;
    }

    @Override
    public void connect( Context ctx){
        if (ftRecv == null || ftRecv.isDone()) {
            ftRecv = esClient.submit(recvRunnable);
        }
    }

    @Override
    public void send(byte[] data) {
        send(data, 0, data.length);
    }

    @Override
    public void send(byte[] data, int offset, int size) {
//        Log.d(TAG, "send: -->" + size);
        try {
            queueBuffer.put(new SocketData(data, offset, size));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        esClient.submit(new Runnable() {
            @Override
            public void run() {
                if (mDatagramSocket != null) {
                    mDatagramSocket.close();
                }
            }
        });
        if (ftSend != null) {
            ftSend.cancel(true);
        }
        if (ftRecv!=null){
            ftRecv.cancel(true);
        }
    }

    @Override
    public boolean isClosed() {
        return ftRecv == null || ftRecv.isDone();
    }

    @Override
    public void setSocketListener(ISocketListener listener) {
        this.mSocketListener = listener;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    private Runnable recvRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mDatagramSocket = new DatagramSocket(mPort);
                byte[] buffer = new byte[packetSize];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, packetSize);
                mDatagramSocket.setSoTimeout(timeout);

                //连接成功
                if (mSocketListener != null) {
                    mSocketListener.connSuccess();
                }
                startSendRunnable();
                //线程阻塞
                mDatagramSocket.receive(datagramPacket);
                int size = datagramPacket.getLength();
                Log.d(TAG, "receive Process: " + android.os.Process.myTid() + "  length:" + size);
                while (size > 0) {
                    if (mSocketListener != null) {
                        mSocketListener.recv(buffer, 0, size);
                    }
                    mDatagramSocket.receive(datagramPacket);
                    size = datagramPacket.getLength();
                }
            } catch (SocketException e) {
                e.printStackTrace();
                Log.d(TAG,"run:>SocketException e="+e.getMessage());
                if (mSocketListener != null) {
                    mSocketListener.connFaild(e);
                }
            } catch (IOException e) {
                Log.d(TAG,"run:>SocketException e="+e.getMessage());
                e.printStackTrace();
                if (mSocketListener != null) {
                    mSocketListener.connFaild(e);
                }
            }finally {
                close();
            }
            if (mSocketListener != null) {
                mSocketListener.connClose();
            }
            Log.d(TAG, this.toString() + "start: -->client接收线程结束");
        }
    };

    private void startSendRunnable() {
        if (ftSend == null || ftSend.isDone()) {
            String connectLog = String.format("连接的 ip:%s,prot:%d", mHost, mPort);
            Log.d(TAG, "start: " + connectLog);
            ftSend = esClient.submit(sendBufRunnable);
        }
    }

    private Runnable sendBufRunnable = new Runnable() {
        @Override
        public void run() {
            SocketData socketData;
            try {
                DatagramPacket datagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(mHost), mPort);
                while ((socketData = queueBuffer.take()) != null) {
                    datagramPacket.setData(socketData.data, socketData.offset, socketData.size);
                    mDatagramSocket.send(datagramPacket);
                    mSocketListener.writeSuccess(socketData.data);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                if (mSocketListener != null) {
                    mSocketListener.connFaild(e);
                }
            }
            queueBuffer.clear();
        }
    };

}
