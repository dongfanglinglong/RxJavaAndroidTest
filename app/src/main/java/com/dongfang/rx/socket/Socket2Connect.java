package com.dongfang.rx.socket;

import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.Bean.SocketMsgBean;
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
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * 初始化建链Observable ,获取输入输出流的Observable
 * <p/>
 * Created by dongfang on 2016/4/7.
 */
public final class Socket2Connect {


    /** 向socket写数据 */
    private Observable<PrintStream> mObservableWriter = null;
    /** 从socket读取数据（或消息） */
    private Observable mObservableReader;


    /** socket链接单独建立了一个Observable,可以方便心跳改变等其他不需要重连的操作 */
    private Observable<Socket> mObservableConnect = null;

    // ---- Subscription -- 是为了维持在没有用户订阅的情况下,系统仍旧保持长连接和心跳等动作
    /** 心跳的默认subscription */
    private Subscription mSubscriptionHeart;
    /** sokcet 链接的Subscription */
    private Subscription mSubscriptionConnect;


    /** 输出流，发消息给socket */
    private PrintStream mPrintStream = null;
    /** 输入流，从socket读取消息（或消息） */
    private BufferedReader mBufferedReader;


    /** socket链接失败时,重连次数的上限值 */
    private static final int RETRY_COUNT_LIMIT = 5;
    /** 重连间隔时间 */
    private static final int RETRY_CONNECT_TIME = 3;
    /** 重连的次数 */
    private int retryCount = 0;

    private static String mIP;
    private static int mPort;
    private SConnect mSConnet;
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
     * 返回可以向Sokcet写数据的Observable
     * <br/>注意: * 使用时需判空 *
     * <br/> 由于心跳在维持输入,所以不需要注册
     *
     * @return mObservableWriter
     */
    public Observable<PrintStream> getObservaleWriter() {
//        if (mBufferedReader == null) {
//            try {
//                initObservableConnect();
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
//
//            if (mPrintStream != null) {
//                mObservableWriter = Observable.just(mPrintStream).subscribeOn(Schedulers.io()).share();
//            }
//        }

        return mObservableWriter;
    }

    public Observable<BufferedReader> getObservableReader() {
//        if (mBufferedReader == null) {
//            try {
//                initObservableConnect();
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
//
//            if (mSConnet != null && mSConnet.getSocket() != null) {
//                mObservableReader = Observable.just(mBufferedReader).subscribeOn(Schedulers.io()).share();
//            }
//        }

        return mObservableReader;
    }


    private Socket2Heart mSocet2Heart;

    /**
     * 链接socket,返回Observable
     *
     * @return mObservableConnect
     * @throws SocketException
     */
    public synchronized Observable<Socket> initObservableConnect() throws SocketException {
        if (mObservableConnect == null || null == mSConnet) {
            if (mSubscriptionConnect != null) {
                mSubscriptionConnect.unsubscribe();
            }
            retryCount = 0;

            if (mSConnet != null) {
                mSConnet.disConnect();
            }
            mSConnet = new SConnect(mIP, mPort);
            mObservableConnect = Observable
                    .create(new Observable.OnSubscribe<Socket>() {
                        @Override
                        public void call(Subscriber<? super Socket> subscriber) {
                            try {
                                ULog.d(" --- connect [" + System.currentTimeMillis() + "]");
                                mSConnet.connect();

                                mPrintStream = new PrintStream(mSConnet.getSocket().getOutputStream());
                                // mPrintStream.println("{\"id\":10000}");
                                ULog.d(" --- new PrintStream and check");
                                mBufferedReader = new BufferedReader(new InputStreamReader(mSConnet.getSocket().getInputStream()));
                                ULog.d(" --- nwe BufferedReader");

                                subscriber.onNext(mSConnet.getSocket());
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
                    .map(new Func1<Socket, Socket>() {
                        @Override
                        public Socket call(Socket socket) {
                            ULog.d("  ---  doOnCompleted");
                            if (mObservableWriter == null && null != mPrintStream) {
                                mObservableWriter = Observable.just(mPrintStream).subscribeOn(Schedulers.io()).share();
                            }

                            if (mObservableReader == null && null != mBufferedReader) {
                                mObservableReader = Observable.just(mBufferedReader).subscribeOn(Schedulers.io()).share();
                            }

                            if (mObservableReader != null && mObservableWriter != null) {
                                // TODO: 16/4/10 心跳出错的error的处理
                                mSubscriptionHeart = Socket2Heart.getInstance()
                                        .getHeartObservable(mObservableWriter, mObservableReader)
                                        .subscribe();
                            }

                            return socket;
                        }
                    })
                    // .flatMap(new Func1<Socket, Observable<Socket>>() {
                    //     @Override
                    //     public Observable<Socket> call(Socket socket) {
                    //         return null;
                    //     }
                    // })
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
            mSubscriptionConnect = mObservableConnect.subscribe();
        }

        return mObservableConnect;
    }


    public String getIP() {
        return mIP;
    }

    public int getPort() {
        return mPort;
    }

    /** 断链 */
    public void disConnect() {
        ULog.d(" --- ---------");
        mSubscriptionHeart.unsubscribe();
        mSubscriptionConnect.unsubscribe();

        mObservableReader = null;
        mObservableWriter = null;
        mObservableConnect = null;

        if (mSConnet != null) {
            mSConnet.disConnect();
        }
    }

}
