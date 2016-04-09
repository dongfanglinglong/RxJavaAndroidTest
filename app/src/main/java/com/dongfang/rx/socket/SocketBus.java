package com.dongfang.rx.socket;

import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.Bean.SocketMsgBean;
import com.dongfang.rx.utils.ULog;

import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by dongfang on 16/4/6.
 */
public final class SocketBus {
    public static final String TAG = "SocketBus";
    private static final String UI_ID = "769U";
    private SocketMsgBean mSocketMegBean, mHttpMegBean;


    private Observable<SocketMsgBean> mObservableHttp, mObservableSocket, mObservableGrable;

    private Observable<HeartMsgBean> mObservabelHeart;


    private Subscription mSubscription;


    private HashMap mHashMap;


    public SocketBus() {
        init();
    }

    private void init() {
        // mSocketMegBean = new SocketMsgBean();
        // mSocketMegBean.id = UI_ID;
        // mSocketMegBean.mstType = SocketMsgBean.MSG_TYPE_SOCKET;
        // mSocketMegBean.msg = "OK";
        // mSocketMegBean.data = "{\"a\":10}";
        // mSocketMegBean.dataArrary = new String[]{"1", "2", "3", "4"};

        mHttpMegBean = new SocketMsgBean();
        mHttpMegBean.id = UI_ID;
        mHttpMegBean.mstType = SocketMsgBean.MSG_TYPE_HTTP;
        mHttpMegBean.msg = "OK";
        mHttpMegBean.data = "{\"a\":10}";
        mHttpMegBean.dataArrary = new String[]{"1", "2", "3", "4"};


        mHashMap = new HashMap();


        mObservableHttp = Observable
                .interval(4, TimeUnit.SECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return System.currentTimeMillis() % 10;
                    }
                })
                .flatMap(new Func1<Long, Observable<SocketMsgBean>>() {
                    @Override
                    public Observable<SocketMsgBean> call(Long aLong) {
                        mHttpMegBean.id = UI_ID + aLong;
                        return Observable.just(mHttpMegBean);
                    }
                })
        ;

        // mObservableSocket = Observable
        //         .interval(5, TimeUnit.SECONDS)
        //         .map(new Func1<Long, Long>() {
        //             @Override
        //             public Long call(Long aLong) {
        //                 return System.currentTimeMillis() % 10;
        //             }
        //         })
        //         .flatMap(new Func1<Long, Observable<SocketMsgBean>>() {
        //             @Override
        //             public Observable<SocketMsgBean> call(Long aLong) {
        //                 mSocketMegBean.id = UI_ID + aLong;
        //                 return Observable.just(mSocketMegBean);
        //             }
        //         })
        // ;

        try {
            final Socket2Connect socket2Connect = Socket2Connect.getInstance("192.168.5.6", 20011);
//            Socket2Connect socket2Connect = Socket2Connect.getInstance("10.128.7.25", 20011);
            socket2Connect.initObservableConnect()
                    .map(new Func1<Socket, Socket>() {
                        @Override
                        public Socket call(Socket socket) {
                            mObservabelHeart = Socket2Heart.getInstance()
                                    .getHeartObservable(socket2Connect.getObservaleWriter(), socket2Connect.getObservableReader());


                            mObservabelHeart.subscribe(new Action1<HeartMsgBean>() {
                                                           @Override
                                                           public void call(HeartMsgBean heartMsgBean) {

                                                           }
                                                       }, new Action1<Throwable>() {
                                                           @Override
                                                           public void call(Throwable throwable) {
                                                               ULog.e(throwable.toString());
                                                           }
                                                       }


                            );
                            return null;
                        }
                    });

//            mObservabelHeart = Socket2Heart.getInstance()
//                    .getHeartObservable(socket2Connect.getObservaleWriter(), socket2Connect.getObservableReader());
//            mObservabelHeart.subscribe();

            // mObservableSocket = Socket2Connect.getInstance("10.128.7.25", 20011).getObservableSocket();
            // mObservableSocket.subscribe();
        } catch (SocketException e) {
            // TODO: 心跳断了之后重连,有可能是一直重连

            e.printStackTrace();
            // mObservableSocket = null;
        }
    }

    public void start() {
        if (null != mSubscription && !mSubscription.isUnsubscribed())
            return;

//        if (null != mObservableSocket)
//            Socket2Connect.getInstance().startConnect();


        mObservableGrable = Observable.merge(mObservableHttp, mObservableSocket)
                .subscribeOn(Schedulers.io())
                .filter(new Func1<SocketMsgBean, Boolean>() {
                    @Override
                    public Boolean call(SocketMsgBean socketMegBean) {
                        if (socketMegBean.mstType != SocketMsgBean.MSG_TYPE_HTTP)
                            ULog.i(socketMegBean.toString());
                        if (mHashMap.containsKey(socketMegBean.id)) {
                            ULog.i("containsKey [" + socketMegBean.id + "]");
                            return false;
                        }
                        mHashMap.put(socketMegBean.id, socketMegBean.id);
                        return true;
                    }
                })
                .share();
        mSubscription = mObservableGrable.subscribe(
                new Action1<SocketMsgBean>() {
                    @Override
                    public void call(SocketMsgBean socketMegBean) {
                        if (socketMegBean.mstType != SocketMsgBean.MSG_TYPE_HTTP)
                            ULog.d(socketMegBean.toString());
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ULog.e(throwable.toString());
                    }
                });
    }

    public void stop() {
        mSubscription.unsubscribe();
        mHashMap.clear();
    }


    /**
     * 获取推送消息的Observable
     */
    public Observable<SocketMsgBean> getObservable4Msg() {
        return mObservableGrable;
    }


}
