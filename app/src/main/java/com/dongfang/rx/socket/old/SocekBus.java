package com.dongfang.rx.socket.old;

import com.dongfang.rx.entity.SocketMsgBean;
import com.dongfang.rx.exception.SocketException;
import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;


/**
 * Created by dongfang on 2016/3/30.
 *
 * @deprecated
 */
public class SocekBus {

    private SocketConnection mConnection;
    private BufferedReader buf;
    private PrintStream out;
    private Subscription mSubscription;


    private SocketMsgBean mSocketMegBean, mBeanRec;
    private int id = 0;
    private boolean close = false;
    private static final long UI_ID = 9000l;

    public SocekBus() {
        init();
    }

    private void init() {
        mSocketMegBean = new SocketMsgBean();
        mSocketMegBean.dataArrary = new String[]{"1", "2", "3", "4"};
    }

    /**
     * @return 是否已建链
     */
    public boolean isConnect() {
        return null != mConnection && mConnection.getSocket() != null;
    }

    public void stat() {
        ULog.d("------------start------------------");
        startObservable();
    }

    public void stop() {
        ULog.d("------------stop------------------");
        mConnection.disConnect();
        mSubscription.unsubscribe();
    }


    private void startObservable() {
        mSubscription = Observable
                .create(new Observable.OnSubscribe<SocketConnection>() {
                    @Override
                    public void call(Subscriber<? super SocketConnection> subscriber) {
                        mConnection = new SocketConnection("10.128.4.102", 20011);
                        try {
                            mConnection.connect();
                        } catch (SocketException e) {
                            e.printStackTrace();
                            mConnection = null;
                        }

                        subscriber.onNext(mConnection);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .map(new Func1<SocketConnection, SocketConnection>() {
                         @Override
                         public SocketConnection call(SocketConnection socketConnection) {
                             if (null == socketConnection || null == socketConnection.getSocket())
                                 return socketConnection;
                             try {
                                 out = new PrintStream(mConnection.getSocket().getOutputStream());
                                 out.println("{\"id\":10000}");
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                             return socketConnection;
                         }
                     }
                )
                .flatMap(new Func1<SocketConnection, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(SocketConnection socketConnection) {
                        if (null != socketConnection && null != socketConnection.getSocket()) {
                            return Observable.zip(
                                    Observable.interval(100, 5000, TimeUnit.MILLISECONDS)
                                            .just(socketConnection.getSocket())
                                            .map(new Func1<Socket, Boolean>() {
                                                @Override
                                                public Boolean call(Socket socket) {
                                                    ULog.i("zip1");
                                                    // mSocketMegBean.id = UI_ID + id++;
                                                    ULog.d(mSocketMegBean.toString());

                                                    String msg = new Gson().toJson(mSocketMegBean);
                                                    out.println(msg);
                                                    return true;
                                                }
                                            })
                                            .map(new Func1<Boolean, Boolean>() {
                                                @Override
                                                public Boolean call(Boolean aBoolean) {
                                                    out.println("close");
                                                    return aBoolean;
                                                }
                                            })
                                    ,
                                    Observable
                                            .just(socketConnection.getSocket())
                                            .map(new Func1<Socket, Boolean>() {
                                                @Override
                                                public Boolean call(Socket socket) {
                                                    ULog.i("zip2");
                                                    try {
                                                        buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                                        String echo = buf.readLine();
                                                        echo = null == echo || "".equals(echo) ? "null" : echo;
                                                        ULog.d("s2c:" + echo);
                                                        if (!echo.equals("null")) {
                                                            mBeanRec = new Gson().fromJson(echo, SocketMsgBean.class);
                                                            ULog.d("rec = " + mBeanRec.toString());
                                                            //  HttpHeaderIds.addId(mBeanRec.id);
                                                            out.println("null");
                                                        } else if ("close".equals(echo)) {
                                                            close = true;
                                                            return false;
                                                        }

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    return true;
                                                }
                                            })
                                            .repeat(10000)
//                                            .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
//                                                @Override
//                                                public Observable<?> call(Observable<? extends Void> observable) {
//                                                    return close ? Observable.empty() : observable;
//                                                }
//                                            })
                                    ,
                                    new Func2<Boolean, Boolean, Boolean>() {
                                        @Override
                                        public Boolean call(Boolean aBoolean, Boolean aBoolean2) {
                                            return aBoolean & aBoolean2;
                                        }
                                    });
                        }
                        return null;
                    }
                })
                .subscribe();
    }


}