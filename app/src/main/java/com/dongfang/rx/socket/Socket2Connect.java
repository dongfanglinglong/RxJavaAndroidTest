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


    private Observable<SocketMsgBean> mObservableRead42Soc = null;
    /** socket链接单独建立了一个Observable,可以方便心跳改变等其他不需要重连的操作 */
    private Observable<Socket> mObservableConnect = null;
    /** merge(mObservableWriter,mObservableRead42Soc) */
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


//    /**
//     * 获取Socket全局的Observable，可以获取到 {@code SocketMsgBean} 对象数据
//     * <br/>
//     * <p>
//     * 注意:调用该方法时,sokcet已经开始连接
//     *
//     * @return mObservableSocket
//     */
//    public Observable<SocketMsgBean> getObservableSocket() {
//        if (mObservableSocket != null) {
//            return mObservableSocket;
//        }
//
//
//        initObservableConnect().flatMap(new Func1<Socket, Observable<HeartMsgBean>>() {
//            @Override
//            public Observable<HeartMsgBean> call(Socket socket) {
//                Socket2Heart heart = Socket2Heart.getInstance();
//
//                heart.getHeartObservable(getObservaleWriter(), getObservableReader());
//
//
//                return
//
//                        ;
//            }
//        });
//
//
//        mObservableSocket = initObservableConnect().flatMap(new Func1<Socket, Observable<SocketMsgBean>>() {
//            @Override
//            public Observable<SocketMsgBean> call(Socket socket) {
//                return Observable.zip(getObservableWrite2SocHeart(), getObservableRead42Soc(), new Func2<SocketMsgBean, SocketMsgBean, SocketMsgBean>() {
//                    @Override
//                    public SocketMsgBean call(SocketMsgBean socketMsgBean, SocketMsgBean socketMsgBean2) {
//                        return socketMsgBean2;
//                    }
//                }).filter(new Func1<SocketMsgBean, Boolean>() {
//                    @Override
//                    public Boolean call(SocketMsgBean bean) {
//                        return null != bean;
//                    }
//                })
//                        .share();
//
//
//                // if (null != getObservableWrite2SocHeart() && null != getObservableRead42Soc()) {
//                // return Observable.merge(getObservableWrite2SocHeart(), getObservableRead42Soc())
//                //         .filter(new Func1<SocketMsgBean, Boolean>() {
//                //             @Override
//                //             public Boolean call(SocketMsgBean bean) {
//                //                 return null != bean;
//                //             }
//                //         })
//                //         .share()
//                //         ;
//                // }
//                // return Observable.empty();
//            }
//        }).share();
//        return mObservableSocket;
//    }


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
                                Socket2Heart.getInstance()
                                        .getHeartObservable(mObservableWriter, mObservableReader)
                                        .subscribe();

                            }


                            return socket;
                        }
                    })
                    .flatMap(new Func1<Socket, Observable<Socket>>() {
                        @Override
                        public Observable<Socket> call(Socket socket) {
                            return null;
                        }
                    })
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

//    /**
//     * 用于读取Socket数据的Observable，使用时需判空
//     *
//     * @return mObservableRead42Soc
//     */
//    private Observable<SocketMsgBean> getObservableRead42Soc() {
//        if (mObservableRead42Soc == null && mBufferedReader != null) {
//            mObservableRead42Soc = Observable.just(mBufferedReader)
//                    .subscribeOn(Schedulers.io())
//                    .map(new Func1<BufferedReader, SocketMsgBean>() {
//                        @Override
//                        public SocketMsgBean call(BufferedReader bufferedReader) {
//                            try {
//                                ULog.d(" --- bufferedReader.readLine");
//                                String str = bufferedReader.readLine();
//                                ULog.d(" --- bufferedReader.readLined = " + str);
//                                if (null != str && str.length() > 0) {
//                                    return new Gson().fromJson(str, SocketMsgBean.class);
//                                }
//                            } catch (IOException e) {
//                                // e.printStackTrace();
//                                // throw new SocketException(SocketException.SOCKET_READER_EXCEPTION, e.getMessage());
//                            }
//                            return null;
//                        }
//                    })
//                    .doOnNext(new Action1<SocketMsgBean>() { // doOnNext 只适用于debug，看看而已
//                        @Override
//                        public void call(SocketMsgBean socketMegBean) {
//                            if (null == socketMegBean)
//                                ULog.e("null == socketMegBean");
//                            else
//                                ULog.d(socketMegBean.toString());
//                        }
//                    })
//                    .repeat()
//                    .share();
//            mSubscriptionRead42Soc = mObservableRead42Soc.subscribe();
//        }
//        return mObservableRead42Soc;
//    }


//    /** 获取心跳专用Observable */
//    private Observable<SocketMsgBean> getObservableWrite2SocHeart() {
//        if (null == getObservaleWriter())
//            return null;
//
//        Observable observable = getObservaleWriter()
//                .map(new Func1<PrintStream, SocketMsgBean>() {
//                    @Override
//                    public SocketMsgBean call(PrintStream out) {
//                        ULog.d(" --- heart");
//                        out.println(SocketUtils.getHeartRequest());
//                        return null;
//                    }
//                })
//                .share()
//                .debounce(2, TimeUnit.SECONDS)
//                .delay(5, TimeUnit.SECONDS)
//                .repeat();
//
//        mSubscriptionHeart = observable.subscribe();
//        return observable;
//    }

    public String getIP() {
        return mIP;
    }

    public int getPort() {
        return mPort;
    }


    public void startConnect() {
        mSubscriptionConnect = mObservableConnect.subscribe();
    }


    /** 断链 */
    public void disConnect() {
        ULog.d(" --- ---------");
        mSubscriptionHeart.unsubscribe();
        mSubscriptionRead42Soc.unsubscribe();
        mSubscriptionConnect.unsubscribe();

        mObservableWriter = null;
        mObservableRead42Soc = null;
        mObservableConnect = null;
        mObservableSocket = null;

        if (mSConnet != null) {
            mSConnet.disConnect();
        }
    }

}
