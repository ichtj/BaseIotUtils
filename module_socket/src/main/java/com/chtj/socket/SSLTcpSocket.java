package com.chtj.socket;

import android.content.Context;

import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * @description ：单向加密的tcp链接
 * 1、TCP应建立一个心跳机制，不然长时间没有数据的传输会被关闭。
 */
public class SSLTcpSocket extends BaseTcpSocket {

    private static final String CLIENT_AGREEMENT = "TLS";
    private static final String CLIENT_TRUST_MANAGER = "X509";
    private static final String CLIENT_TRUST_KEYSTORE = "BKS";

    public SSLTcpSocket(String ip, int port, int timeout) {
        super(ip, port, timeout);
    }

    public SSLTcpSocket(String ip, int port, int timeout, ISocketListener listener) {
        super(ip, port, timeout, listener);
    }

    public void setSSLConfig(int keyPath, String keyPwd) {
        mKeyPath = keyPath;
        mKeyPwd = keyPwd;
    }

    private int mKeyPath;
    private String mKeyPwd;

    @Override
    protected void initSocket(Context ctx) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
        TrustManagerFactory trustManager = TrustManagerFactory.getInstance(CLIENT_TRUST_MANAGER);
        KeyStore tks = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
        tks.load(ctx.getResources().openRawResource(mKeyPath), mKeyPwd.toCharArray());
        trustManager.init(tks);
        sslContext.init(null, trustManager.getTrustManagers(), null);

        mSocket = sslContext.getSocketFactory().createSocket(mHost, mPort);
        mSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIEZE);
        mSocket.setSoTimeout(timeout);
        //在握手之前会话是无效的
        ((SSLSocket) mSocket).startHandshake();
    }
}
