package com.dongfang.rx.socket;

import android.util.Patterns;

import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.Bean.SocketMsgBean;
import com.dongfang.rx.BuildConfig;
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
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by dongfang on 2016/4/7.
 */
public final class Socket2Connect {


    /** 向socket写数据 */
    private Observable<PrintStream> mObservableWrite2Soc = null;


    /** 从socket读取数据（或消息） */
    private Observable mObservableReader;


    private Observable<SocketMsgBean> mObservableRead42Soc = null;
    /** socket链接单独建立了一个Observable,可以方便心跳改变等其他不需要重连的操作 */
    private Observable<Socket> mObservableConnect = null;
    /** merge(mObservableWrite2Soc,mObservableRead42Soc) */
    private Observable<SocketMsgBean> mObservableSocket = null;

    // ---- Subscription -- 是为了维持在没有用户订阅的情况下,系统仍旧保持长连接和心跳等动作
    /** 心跳的默认subscription */
    private Subscription mSubscriptionHeart;
    /** 默认读取socket消息的subscription */
    private Subscription mSubscriptionRead42Soc;
    /** sokcet 链接的Subscription */
    private Subscription mSubscriptionConnect;


    /** 输出流，发消息给socket */
    private PrintStream mPrintStream = null;
    /** 输入流，从socket读取消息（或消息） */
    private BufferedReader mBufferedReader;

    private static String mIP;
    private static int mPort;
    private InetSocketAddress mSocketAddress;

    /** socket链接失败时,重连次数的上限值 */
    private static final int RETRY_COUNT_LIMIT = 5;
    /** 重连间隔时间 */
    private static final int RETRY_CONNECT_TIME = 3;
    /** 重连的次数 */
    private int retryCount = 0;

    /** socket建链超时时间 */
    private final static int TIME_OUT = 15 * 1000;
    private volatile Socket mSocket;
    /** 单例 Socket2Connect */
    private static Socket2Connect mSocketBus2Con;


    private Socket2Connect() { }

    public static Socket2Connect getInstance() {
        if (mSocketBus2Con == null) {
            mSocketBus2Con = new Socket2Connect();
        }
        return mSocketBus2Con;
    }

    public static Socket2Connect getInstance(String ip, int port) throws SocketException {
        if (!Patterns.IP_ADDRESS.matcher(ip).matches()) {
            throw new SocketException(SocketException.IP_EXCEPTION, "IP error!");
        } else if (1023 > port || port > 65535) {
            throw new SocketException(SocketException.PORT_EXCEPTION, "Prot error!");
        }
        mIP = ip;
        mPort = port;

        if (mSocketBus2Con == null) {
            mSocketBus2Con = new Socket2Connect();
        }
        return mSocketBus2Con;
    }

    /**
     * 获取Socket全局的Observable，可以获取到 {@code SocketMsgBean} 对象数据
     * <br/>
     * <p>
     * 注意:调用该方法时,sokcet已经开始连接
     *
     * @return mObservableSocket
     */
    public Observable<SocketMsgBean> getObservableSocket() {
        if (mObservableSocket != null) {
            return mObservableSocket;
        }


        getObservableConnect().flatMap(new Func1<Socket, Observable<HeartMsgBean>>() {
            @Override
            public Observable<HeartMsgBean> call(Socket socket) {
                Socket2Heart heart = Socket2Heart.getInstance();

                heart.getHeartObservable(getObservaleWriter(),getObservableReader());


                return

                        ;
            }
        });


        mObservableSocket = getObservableConnect().flatMap(new Func1<Socket, Observable<SocketMsgBean>>() {
            @Override
            public Observable<SocketMsgBean> call(Socket socket) {
                return Observable.zip(getObservableWrite2SocHeart(), getObservableRead42Soc(), new Func2<SocketMsgBean, SocketMsgBean, SocketMsgBean>() {
                    @Override
                    public SocketMsgBean call(SocketMsgBean socketMsgBean, SocketMsgBean socketMsgBean2) {
                        return socketMsgBean2;
                    }
                }).filter(new Func1<SocketMsgBean, Boolean>() {
                    @Override
                    public Boolean call(SocketMsgBean bean) {
                        return null != bean;
                    }
                })
                        .share();


                // if (null != getObservableWrite2SocHeart() && null != getObservableRead42Soc()) {
                // return Observable.merge(getObservableWrite2SocHeart(), getObservableRead42Soc())
                //         .filter(new Func1<SocketMsgBean, Boolean>() {
                //             @Override
                //             public Boolean call(SocketMsgBean bean) {
                //                 return null != bean;
                //             }
                //         })
                //         .share()
                //         ;
                // }
                // return Observable.empty();
            }
        }).share();
        return mObservableSocket;
    }


    /**
     * 返回可以向Sokcet写数据的Observable
     * <br/>注意: * 使用时需判空 *
     * <br/> 由于心跳在维持输入,所以不需要注册
     *
     * @return mObservableWrite2Soc
     */
    public Observable<PrintStream> getObservaleWriter() {
        if (mObservableWrite2Soc == null || mPrintStream != null) {
            mObservableWrite2Soc = Observable.just(mPrintStream)
                    .subscribeOn(Schedulers.io())
                    .share();
        }
        return mObservableWrite2Soc;
    }

    public Observable<BufferedReader> getObservableReader() {
        if (mObservableReader == null && mBufferedReader != null) {
            mObservableReader = Observable.just(mBufferedReader).subscribeOn(Schedulers.io()).share();
        }

        return mObservableReader;
    }


    /** 获取链接socket的Observable */
    private Observable<Socket> getObservableConnect() {
        if (null != mObservableConnect && null != mSocket && mSocket.isConnected())
            return mObservableConnect;

        if (mSubscriptionConnect != null) {
            mSubscriptionConnect.unsubscribe();
        }
        retryCount = 0;
        mObservableConnect = Observable
                .create(new Observable.OnSubscribe<Socket>() {
                    @Override
                    public void call(Subscriber<? super Socket> subscriber) {
                        try {
                            ULog.d(" --- connect [" + System.currentTimeMillis() + "]");
                            connect();

                            mPrintStream = new PrintStream(mSocket.getOutputStream());
                            mPrintStream.println("{\"id\":10000}");
                            ULog.d(" --- new PrintStream and check");
                            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                            ULog.d(" --- nwe BufferedReader");

                            subscriber.onNext(mSocket);
                            subscriber.onCompleted();
                        } catch (SocketException e) {
                            //e.printStackTrace();
                            subscriber.onError(e);
                        } catch (IOException e) {
                            // TODO: 2016/4/8  有待考虑如何处理输入输出流,是否需要重新获取等
                            //e.printStackTrace();
                            mPrintStream = null;
                            mBufferedReader = null;
                            subscriber.onError(new SocketException(SocketException.SOCKET_PUTSTREAM_SHOTDOWN, e.getMessage()));
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        ULog.d("---------------retryWhen-----------------");
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                if (retryCount < RETRY_COUNT_LIMIT && throwable instanceof SocketException) {
                                    return Observable.just(null)
                                            .delay(++retryCount * RETRY_CONNECT_TIME, TimeUnit.SECONDS);
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ULog.e("doOnError " + throwable);
                    }
                })
                .share();

        //mSubscriptionConnect = mObservableConnect.subscribe();

        return mObservableConnect;
    }

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
                                ULog.d(" --- bufferedReader.readLine");
                                String str = bufferedReader.readLine();
                                ULog.d(" --- bufferedReader.readLined = " + str);
                                if (null != str && str.length() > 0) {
                                    return new Gson().fromJson(str, SocketMsgBean.class);
                                }
                            } catch (IOException e) {
                                // e.printStackTrace();
                                // throw new SocketException(SocketException.SOCKET_READER_EXCEPTION, e.getMessage());
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


    /** 获取心跳专用Observable */
    private Observable<SocketMsgBean> getObservableWrite2SocHeart() {
        if (null == getObservaleWriter())
            return null;

        Observable observable = getObservaleWriter()
                .map(new Func1<PrintStream, SocketMsgBean>() {
                    @Override
                    public SocketMsgBean call(PrintStream out) {
                        ULog.d(" --- heart");
                        out.println(SocketUtils.getHeartRequest());
                        return null;
                    }
                })
                .share()
                .debounce(2, TimeUnit.SECONDS)
                .delay(5, TimeUnit.SECONDS)
                .repeat();

        mSubscriptionHeart = observable.subscribe();
        return observable;
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
     * Socket 链接，如果之前已经有链接，会先断开
     *
     * @throws SocketException
     */
    private synchronized void connect() throws SocketException {
        if (!Patterns.IP_ADDRESS.matcher(mIP).matches()) {
            throw new SocketException(SocketException.IP_EXCEPTION, "IP error!");
        } else if (1023 > mPort || mPort > 65535) {
            throw new SocketException(SocketException.PORT_EXCEPTION, "Prot error!");
        }
        if (BuildConfig.DEBUG && 3 < retryCount)
            mSocketAddress = new InetSocketAddress(mIP, mPort + 1);
        else
            mSocketAddress = new InetSocketAddress(mIP, mPort);


        close();
        try {
            mSocket = new Socket();
            mSocket.connect(mSocketAddress, TIME_OUT);
        } catch (IOException e) {
            close();
            throw new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION, "Socket connect error [" + mIP + ":" + mPort + "]!");
        }
    }


    public void startConnect() {
        mSubscriptionConnect = mObservableConnect.subscribe();
    }


    /** 断链 */
    public synchronized void disConnect() {
        ULog.d(" --- ---------");
        mSubscriptionHeart.unsubscribe();
        mSubscriptionRead42Soc.unsubscribe();
        mSubscriptionConnect.unsubscribe();

        mObservableWrite2Soc = null;
        mObservableRead42Soc = null;
        mObservableConnect = null;
        mObservableSocket = null;
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
