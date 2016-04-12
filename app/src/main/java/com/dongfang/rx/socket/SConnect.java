package com.dongfang.rx.socket;

import android.util.Patterns;

import com.dongfang.rx.exception.SocketException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 用于socket建链用
 * <p/>
 * <p/>
 * Created by dongfang on 16/4/8.
 */
public class SConnect {

    /** socket建链超时时间 */
    private final static int TIME_OUT = 2 * 1000; // 15*1000
    private volatile Socket mSocket;

    private String mIP;
    private int mPort;

    public SConnect(String ip, int port) throws SocketException {
        if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
            throw new SocketException(SocketException.IP_EXCEPTION, "IP error!");
        } else if (1023 > port || port > 65535) {
            throw new SocketException(SocketException.PORT_EXCEPTION, "Prot error!");
        }

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

    /**
     * Socket 链接，如果之前已经有链接，会先断开
     *
     * @throws SocketException
     */
    public synchronized void connect() throws SocketException {
        if (mSocket == null || mSocket.isClosed()) {
            close();
            try {
                mSocket = new Socket();
                mSocket.connect(new InetSocketAddress(mIP, mPort), TIME_OUT);
            } catch (IOException e) {
                close();
                throw new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION,
                        "Socket connect error [" + mIP + ":" + mPort + "]!");
            }
        }
    }

    /** 断链 */
    public synchronized void disConnect() {
        close();
    }

    /**
     * 断链
     */
    private void close() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                mSocket = null;
            }
        }
    }

}
