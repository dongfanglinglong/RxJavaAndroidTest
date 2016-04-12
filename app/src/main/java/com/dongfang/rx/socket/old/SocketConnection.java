package com.dongfang.rx.socket.old;

import android.os.Build;
import android.util.Patterns;

import com.dongfang.rx.exception.SocketException;
import com.dongfang.rx.utils.ULog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dongfang on 2016/3/30.
 *
 * @deprecated
 */
public class SocketConnection {
    private static final String TAG = "SocketConnection";

    private final static int TIME_OUT = 10 * 1000;
    private volatile Socket mSocket;

    private String mIP;
    private int mPort;
    private InetSocketAddress mSocketAddress;

    public SocketConnection(String ip, int port) {
        mIP = ip;
        mPort = port;
    }


    public Socket getSocket() {
        return mSocket;
    }

    public String getIP() {
        return mIP;
    }

    public int getPort() {
        return mPort;
    }

    public InetSocketAddress getSocketAddress() {
        return mSocketAddress;
    }

    public void setIP(String IP) {
        mIP = IP;
    }

    public void setPort(int port) {
        mPort = port;
    }

    /**
     * @return soocket建联是否成功
     * @throws SocketException
     */
    public synchronized void connect() throws SocketException {
        ULog.d("------------------------------");

        if (!Patterns.IP_ADDRESS.matcher(mIP).matches()) {
            throw new SocketException(SocketException.IP_EXCEPTION, "IP error!");
        } else if (1023 > mPort || mPort > 65535) {
            throw new SocketException(SocketException.PORT_EXCEPTION, "Prot error!");
        }

        // connet
        mSocketAddress = new InetSocketAddress(mIP, mPort);

        close();

        mSocket = new Socket();
        try {
            mSocket.connect(mSocketAddress, TIME_OUT);
            // sendConnectReq();
        } catch (IOException e) {
            e.printStackTrace();
            close();
            throw new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION, "Socket connect error [" + mIP + ":" + mPort + "]!");
        }
    }

    /**
     * 断链
     */
    public synchronized void disConnect() {
        ULog.d("------------------------------");
        close();
    }

    private void close() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mSocket = null;
            }
        }
    }


    /**
     * 发送连接信息或者认证之类的。。。。
     */
    private void sendConnectReq() {
        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("method", "connect");
        paraMap.put("config", "config");
        paraMap.put("osType", "2-android");
        paraMap.put("osVersion", Integer.toString(Build.VERSION.SDK_INT));
        paraMap.put("appVersion", "0.1.1");
        paraMap.put("udid", "dongfang927838934");
        paraMap.put("cityId", "021");
    }

}
