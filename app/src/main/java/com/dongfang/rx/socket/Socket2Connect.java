package com.dongfang.rx.socket;

import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.exception.SocketException;
import com.dongfang.rx.utils.ULog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 初始化建链Observable ,获取输入输出流的Observable
 * <p/>
 * Created by dongfang on 2016/4/7.
 */
public final class Socket2Connect {

    /** socket链接失败时,重连次数的上限值 */
    private static final int RETRY_COUNT_LIMIT = 2; //5
    /** 重连间隔时间[3,6,9,12,15] */
    private static final int RETRY_CONNECT_TIME_INTERVAL = 1; //3


    /** 向socket写数据 */
    private Observable<PrintStream> mObservableWriter = null;
    /** 从socket读取数据（或消息） */
    private Observable<String> mObservableReader;

    /** socket链接单独建立了一个Observable,可以方便心跳改变等其他不需要重连的操作 */
    private Observable<Socket> mObservableConnect = null;
    /** sokcet 链接的Subscription */
    private Subscription mSubscriptionConnect = null;


    /** 输出流，发消息给socket */
    private PrintStream mPrintStream = null;
    /** 输入流，从socket读取消息（或消息） */
    private BufferedReader mBufferedReader = null;


    /** 重连的次数 */
    private int retryCount = 0;

    private static String mIP;
    private static int mPort;

    /** 用于socket连接和断链 */
    private SConnect mSConnet;
    /** 心跳逻辑控制 */
    private Socket2Heart mSocet2Heart;

    /** 单例 Socket2Connect */
    private static Socket2Connect mSocketBus2Con;

    private Socket2Connect() { }

    public static Socket2Connect getInstance(String ip, int port) {
        if (mSocketBus2Con == null) {
            mSocketBus2Con = new Socket2Connect();
            mIP = ip;
            mPort = port;
        }
        return mSocketBus2Con;
    }

    /**
     * 会重置mObservableConnect,使用请注意
     *
     * @return mObservableConnect
     * @throws SocketException
     */
    private Observable<Socket> initConnect() throws SocketException {
        mObservableConnect = null;

        if (null != mSConnet) {
            mSConnet.disConnect();
        }
        mSConnet = new SConnect(mIP, mPort);
        mObservableConnect = Observable
                .create(new Observable.OnSubscribe<Socket>() {
                    @Override
                    public void call(Subscriber<? super Socket> subscriber) {
                        try {
                            ULog.d(" --- connect [" + retryCount + "][" + System.currentTimeMillis() + "]");
                            mSConnet.connect();
                            subscriber.onNext(mSConnet.getSocket());
                            subscriber.onCompleted();
                        } catch (SocketException e) {
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .map(new Func1<Socket, Socket>() {
                    @Override
                    public Socket call(Socket socket) {
                        initHeart(socket);
                        return socket;
                    }
                })
                .share()
        ;
        return mObservableConnect;
    }

    /**
     * 链接socket,返回Observable，连接失败会自动重连，重连次数上限 {@code RETRY_COUNT_LIMIT}
     *
     * @return mObservableConnect
     * @throws SocketException ip和port 异常会抛出错误
     */
    public synchronized Observable<Socket> startConnect() throws SocketException {
        if (mObservableConnect == null || null == mSConnet) {
            if (mSubscriptionConnect != null) {
                mSubscriptionConnect.unsubscribe();
            }
            retryCount = 0;

            if (mSConnet != null) {
                mSConnet.disConnect();
            }
            mObservableConnect = initConnect()
                    .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                        @Override
                        public Observable<?> call(Observable<? extends Throwable> observable) {
                            ULog.d("---------------retryWhen-----------------");
                            return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                                @Override
                                public Observable<?> call(Throwable throwable) {
                                    // TODO: 16/4/10 socket重连是否需要次数限制,有待考虑
                                    if (retryCount < RETRY_COUNT_LIMIT && throwable instanceof SocketException) {
                                        return Observable.just(null)
                                                .delay(++retryCount * RETRY_CONNECT_TIME_INTERVAL, TimeUnit.SECONDS);
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
            mSubscriptionConnect = mObservableConnect.subscribe();
        }
        return mObservableConnect;
    }

    /** 断链 */
    public void disConnect() {
        ULog.d(" --- ---------");
        mSubscriptionConnect.unsubscribe();

        mObservableReader = null;
        mObservableWriter = null;
        mObservableConnect = null;

        if (mPrintStream != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mBufferedReader = null;
            }
        }

        if (mPrintStream != null) {
            mPrintStream.flush();
            mPrintStream.close();
            mPrintStream = null;
        }

        if (null != mSocet2Heart)
            mSocet2Heart.stopHeart();

        if (mSConnet != null) {
            mSConnet.disConnect();
            mSConnet = null;
        }
    }

    /** 延迟重连,无限制 **/
    private void reconnect() {
        ULog.d("============= reconnect ============");
        if (mSubscriptionConnect != null) {
            mSubscriptionConnect.unsubscribe();
        }
        try {
            mObservableConnect = initConnect()
                    .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                        @Override
                        public Observable<?> call(Observable<? extends Throwable> observable) {
                            ULog.d("---------------retryWhen-----------------");
                            return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                                @Override
                                public Observable<?> call(Throwable throwable) {
                                    // if (retryCount < RETRY_COUNT_LIMIT &&
                                    if (throwable instanceof SocketException) {
                                        retryCount++;
                                        return Observable.just(null)
                                                .delay(retryCount < RETRY_COUNT_LIMIT
                                                                ? retryCount * RETRY_CONNECT_TIME_INTERVAL
                                                                : RETRY_COUNT_LIMIT * RETRY_CONNECT_TIME_INTERVAL,
                                                        TimeUnit.SECONDS);
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

            mSubscriptionConnect = mObservableConnect.delaySubscription(5, TimeUnit.SECONDS).subscribe();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /** 初始化心跳服务,如果心跳异常,走心跳重置操作 */
    private void initHeart(final Socket socket) {
        if (null != socket && socket.isConnected()) {
            try {
                initReaderAndWriter(socket);
                mSocet2Heart = null == mSocet2Heart ? Socket2Heart.getInstance() : mSocet2Heart;
                mSocet2Heart.startHeart(mObservableWriter, mObservableReader,
                        new Subscriber() {
                            @Override
                            public void onCompleted() {
                                ULog.d("onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                ULog.e(e.getMessage());
                                reconnect();
                            }

                            @Override
                            public void onNext(Object o) {
                                ULog.d(o.toString());
                            }
                        });

            } catch (IOException e) {
                ULog.e(e.getMessage());
                mPrintStream = null;
                mBufferedReader = null;
                mObservableWriter = null;
                mObservableReader = null;
            }
        }
    }


    private int mReaderErrorCount = 0;


    /**
     * 初始化输入输出流
     *
     * @param socket
     * @throws IOException
     */
    private void initReaderAndWriter(final Socket socket) throws IOException {
        mPrintStream = new PrintStream(socket.getOutputStream());
        //mPrintStream.println(SocketUtils.getHeartRequest(10000));
        // mPrintStream.flush();
        // mPrintStream.println("{\"id\":10000}");
        ULog.d(" --- new PrintStream and check");
        mBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ULog.d(" --- nwe BufferedReader");

        mObservableWriter = Observable.just(mPrintStream).subscribeOn(Schedulers.io()).share();
        mObservableReader = Observable.just(mBufferedReader)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<BufferedReader, Observable<String>>() {
                    @Override
                    public Observable<String> call(BufferedReader reader) {
                        String str = null;
                        try {
                            str = reader.readLine();
                        } catch (IOException e) {
                            ULog.e(e.getMessage());
                        }
                        ULog.d(" ---  header reader [" + mReaderErrorCount + "]-->" + str);
                        if (null == str) {
                            mReaderErrorCount++;
                        } else {
                            mReaderErrorCount = 0;
                        }

                        if (mReaderErrorCount > mSocet2Heart.getMaxNullLimit()) {
                            mReaderErrorCount = 0;
                            return Observable.error(new SocketException(SocketException.SOCKET_READER_EXCEPTION, "Socket inputStream error! "));
                        }
                        return Observable.just(str);
                    }
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .share();
    }

    /**
     * * 修改心跳时间，返回心跳的Observable ,初始化失败会返回null
     *
     * @param time 秒
     * @return Observable<HeartMsgBean> or null
     */
    public Observable<HeartMsgBean> changeHeartInterval(int time) {

        if (null == mSocet2Heart || null == mObservableWriter || null == mObservableReader)
            return null;

        return mSocet2Heart.startHeart(mObservableWriter, mObservableReader,
                new Subscriber() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        ULog.e(e.getMessage());
                        reconnect();
                    }

                    @Override
                    public void onNext(Object o) {}
                },
                time
        );
    }


    /**
     * 返回可以向Sokcet写数据的Observable
     * <br/>注意: * 使用时需判空 *
     * <br/> 由于心跳在维持输入,所以不需要注册
     *
     * @return mObservableWriter
     */
    public Observable<PrintStream> getObservaleWriter() {
        return mObservableWriter;
    }


    public Observable<String> getObservableReader() {
        return mObservableReader
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return null != s && s.length() > 0;
                    }
                }).share();
    }


    public Socket2Heart getSocet2Heart() {
        return mSocet2Heart;
    }

    public String getIP() {
        return mIP;
    }

    public int getPort() {
        return mPort;
    }

//    PrintStream getPrintStream() {
//        return mPrintStream;
//    }

}
