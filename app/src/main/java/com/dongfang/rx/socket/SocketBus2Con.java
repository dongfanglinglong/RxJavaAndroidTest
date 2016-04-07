package com.dongfang.rx.socket;

import android.util.Patterns;

import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by dongfang on 2016/4/7.
 */
public final class SocketBus2Con {

    /**
     * 向socket写数据
     */
    private Observable<PrintStream> mObservableWrite2Soc = null;
    /**
     * 从socket读取数据（或消息）
     */
    private Observable<SocketMsgBean> mObservableRead42Soc = null;


    private Observable<Socket> mObservableConnect;

    /**
     * merge(mObservableWrite2Soc,mObservableRead42Soc)
     */
    private Observable<SocketMsgBean> mObservableSocket = null;
    /**
     * 输出流，发消息给socket
     */
    private PrintStream mPrintStream = null;
    /**
     * 输入流，从socket读取消息（或消息）
     */
    private BufferedReader mBufferedReader;
    /**
     * 心跳的默认subscription
     */
    private Subscription mSubscriptionHeart;
    /**
     * 默认读取socket消息的subscription
     */
    private Subscription mSubscriptionRead42Soc;
    /**
     * sokcet 链接
     */
    private Subscription mSubscriptionConnect;

    /**
     * 获取Socket全局的Observable，可以获取到 {@code SocketMsgBean} 对象数据
     *
     * @return mObservableSocket
     */
    public Observable<SocketMsgBean> getObservableSocket() {
        if (mObservableSocket != null) {
            return mObservableSocket;
        }

        mObservableSocket = getObservableConnect()
                .flatMap(new Func1<Socket, Observable<SocketMsgBean>>() {
                    @Override
                    public Observable<SocketMsgBean> call(Socket socket) {
                        if (null != getObservableWrite2SocHeart() && null != getObservableRead42Soc()) {
                            return Observable.merge(getObservableWrite2SocHeart(), getObservableRead42Soc())
                                    .filter(new Func1<SocketMsgBean, Boolean>() {
                                        @Override
                                        public Boolean call(SocketMsgBean bean) {
                                            return null != bean;
                                        }
                                    })
                                    ;
                        }
                        return Observable.empty();
                    }
                }).share();


        return mObservableSocket;
    }
    // --------------------------------------------------------------------------------------

    /**
     * 获取链接socket的Observable
     */
    public Observable<Socket> getObservableConnect() {

        if (null == mObservableConnect) {
            if (mSubscriptionConnect == null) {
                mSubscriptionConnect.unsubscribe();
            }

            mObservableConnect = Observable
                    .create(new Observable.OnSubscribe<Socket>() {
                        @Override
                        public void call(Subscriber<? super Socket> subscriber) {
                            try {
                                ULog.d("---------------------connect");
                                connect();
                                mPrintStream = new PrintStream(mSocket.getOutputStream());
                                mPrintStream.println("{\"id\":10000}");
                                ULog.d("---------------------new PrintStream and check");
                                mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                                ULog.d("---------------------nwe BufferedReader");
                            } catch (SocketException e) {
                                ULog.e("-------------------------");
                                e.printStackTrace();
                                subscriber.onError(e);
                            } catch (IOException e) {
                                ULog.e("-------------------------");
                                e.printStackTrace();
                                mPrintStream = null;
                                mBufferedReader = null;
                                subscriber.onError(e);
                            }
                            subscriber.onNext(mSocket);
                            subscriber.onCompleted();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .retry(new Func2<Integer, Throwable, Boolean>() {
                        @Override
                        public Boolean call(Integer integer, Throwable throwable) {
                            return null;
                        }
                    })
                    .share();

            mSubscriptionConnect = mObservableConnect
                    .subscribe(new Observer<Socket>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            //TODO: reconnect

                        }

                        @Override
                        public void onNext(Socket socket) {

                        }
                    });
        }

        return mObservableConnect;
    }


    // --------------------------------------------------------------------------------------

    /**
     * 用于读取Socket数据的Observable，使用时需判空
     *
     * @return mObservableRead42Soc
     */
    private Observable<SocketMsgBean> getObservableRead42Soc() {
        if (mObservableRead42Soc == null && mBufferedReader != null) {
            mObservableRead42Soc = Observable.just(mBufferedReader)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<BufferedReader, SocketMsgBean>() {
                        @Override
                        public SocketMsgBean call(BufferedReader bufferedReader) {
                            try {
                                ULog.d("---------------------bufferedReader.readLine");
                                String str = bufferedReader.readLine();
                                ULog.d("---------------------bufferedReader.readLined = " + str);
                                if (null != str && str.length() > 0) {
                                    return new Gson().fromJson(str, SocketMsgBean.class);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    })

                    .doOnNext(new Action1<SocketMsgBean>() { // doOnNext 只适用于debug，看看而已
                        @Override
                        public void call(SocketMsgBean socketMegBean) {
                            if (null == socketMegBean)
                                ULog.e("null == socketMegBean");
                            else
                                ULog.d(socketMegBean.toString());
                        }
                    })
                    .repeat()
                    .share();
            mSubscriptionRead42Soc = mObservableRead42Soc.subscribe();
        }
        return mObservableRead42Soc;
    }


    // --------------------------------------------------------------------------------------

    /**
     * 获取心跳专用Observable
     */
    private Observable getObservableWrite2SocHeart() {
        if (null == getObservaleWrite2Soc())
            return null;

        Observable observable = getObservaleWrite2Soc()
                .map(new Func1<PrintStream, SocketMsgBean>() {
                    @Override
                    public SocketMsgBean call(PrintStream out) {
                        ULog.d("---------------------heart");
                        out.println(getHeartRequest());
                        return null;
                    }
                })
                .delay(5, TimeUnit.SECONDS)
                .repeat();

        mSubscriptionHeart = observable.subscribe();
        return observable;
    }

    /**
     * 使用时需判空
     */
    private Observable<PrintStream> getObservaleWrite2Soc() {
        if (mObservableWrite2Soc == null || mPrintStream != null) {
            mObservableWrite2Soc = Observable.just(mPrintStream)
                    .subscribeOn(Schedulers.io())
                    .share();
        }
        return mObservableWrite2Soc;
    }

    // ---------------------------------------------------------------------------------------

    private String mIP;
    private int mPort;
    private InetSocketAddress mSocketAddress;

    private final static int TIME_OUT = 10 * 1000;
    private volatile Socket mSocket;

    public SocketBus2Con() {
    }

    public SocketBus2Con(String ip, int port) throws SocketException {
        if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
            throw new SocketException(SocketException.IP_EXCEPTION, "IP error!");
        } else if (1023 > port || port > 65535) {
            throw new SocketException(SocketException.PORT_EXCEPTION, "Prot error!");
        }
        mIP = ip;
        mPort = port;
        // mSocketAddress = new InetSocketAddress(mIP, mPort);
    }

    public String getIP() {
        return mIP;
    }

    public int getPort() {
        return mPort;
    }

    public void setIP(String IP) throws SocketException {
        if (!Patterns.IP_ADDRESS.matcher(mIP).matches()) {
            throw new SocketException(SocketException.IP_EXCEPTION, "IP error!");
        }
        mIP = IP;
    }

    public void setPort(int port) throws SocketException {
        if (1023 > mPort || mPort > 65535) {
            throw new SocketException(SocketException.PORT_EXCEPTION, "Prot error!");
        }
        mPort = port;
    }

    /**
     * @return soocket建联是否成功
     * @throws SocketException
     */
    private synchronized void connect() throws SocketException {
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
        mSubscriptionHeart.unsubscribe();
        mSubscriptionRead42Soc.unsubscribe();
        mSubscriptionConnect.unsubscribe();
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
                e.printStackTrace();
            } finally {
                mSocket = null;
            }
        }
    }

    // ---------------------------------------------------------------------------------------

    private static final String HEART_ACTION = "lang_h";

    private String getHeartRequest() {
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
