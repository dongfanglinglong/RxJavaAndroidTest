package com.dongfang.rx.socket;

/**
 * Created by dongfang on 2016/4/8.
 */
public class SocketUtils {


    /** SOCKET 心跳数据 */
    private static final String HEART_ACTION = "LANG_H";

    public static String getHeartRequest() {
        StringBuffer sb = new StringBuffer("GET /");
        sb.append(HEART_ACTION);
        sb.append("?token=88888888");
        sb.append("&nonce=10292837465");
        sb.append("&c=87654");
        sb.append("&sig=83337654");
        sb.append("&cofig=10000");
        sb.append(" HTTP/1.1");
        return sb.toString();
    }
}