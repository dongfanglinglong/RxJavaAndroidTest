package com.dongfang.rx.exception;

/**
 * Created by dongfang on 2016/3/30.
 */
public class SocketException extends Exception {
    private static final long serialVersionUID = 4947761181603355961L;

    public SocketException(int exceptionId, String s) {
        super("[" + exceptionId + "]" + s);
    }

    public static final int IP_EXCEPTION = 82;
    public static final int PORT_EXCEPTION = 733;
    public static final int SOCKET_CONNECT_EXCEPTION = 225;
    public static final int SOCKET_PUTSTREAM_SHOTDOWN = 533;

    public static final int SOCKET_READER_EXCEPTION = 10;
}
