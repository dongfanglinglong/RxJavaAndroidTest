package com.dongfang.rx.socket;

import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by dongfang on 2016/4/8.
 */
public final class Socket2Heart {

    private static Socket2Heart sSocket2Heart;

    private Socket2Heart() { }

    public static synchronized Socket2Heart getInstance() {
        if (null == sSocket2Heart) {
            sSocket2Heart = new Socket2Heart();
        }
        return sSocket2Heart;
    }


    /** 向socket写数据 */
    private Observable<HeartMsgBean> mObservableHeart = null;

    private Subscription mSubscriptionHeart;

    public Observable<HeartMsgBean> getHeartObservable(Observable<PrintStream> obsW, Observable<BufferedReader> obsR) {
        return creatHeartWriter(obsW).zipWith(creatHeartReader(obsR), new Func2() {
            @Override
            public Object call(Object o, Object o2) {
                return null;
            }
        });
    }


    public Observable getHeartObservable(Socket2Connect connect) {
        if (connect != null && null != connect.getObservaleWriter() && null != connect.getObservableReader()) {
            // Observable.zip(creatHeartWriter(connect.getObservaleWriter()), creatHeartReader(connect.getObservableReader()), new Func2() {
            //     @Override
            //     public Object call(Object o, Object o2) {
            //         return null;
            //     }
            // });
            //
            // creatHeartWriter(connect.getObservaleWriter());
            // creatHeartReader(connect.getObservableReader());

            mObservableHeart = creatHeartWriter(connect.getObservaleWriter())
                    .zipWith(creatHeartReader(connect.getObservableReader()), new Func2<Object, HeartMsgBean, HeartMsgBean>() {
                        @Override
                        public HeartMsgBean call(Object o, HeartMsgBean msgBean) {
                            return null;
                        }
                    })
                    .filter(new Func1<HeartMsgBean, Boolean>() {
                        @Override
                        public Boolean call(HeartMsgBean bean) {
                            return null != bean;
                        }
                    })
                    .share();

            mSubscriptionHeart = mObservableHeart.subscribe();
        }
        return mObservableHeart;
    }


    private Observable mObservableWriter;

    private synchronized Observable creatHeartWriter(Observable<PrintStream> obs) {
        if (mObservableWriter == null) {
            mObservableWriter = obs
                    .map(new Func1<PrintStream, HeartMsgBean>() {
                        @Override
                        public HeartMsgBean call(PrintStream out) {
                            ULog.d(" --- heart");
                            out.println(SocketUtils.getHeartRequest());
                            return null;
                        }
                    })
                    .share()
                    .debounce(1, TimeUnit.SECONDS)
                    .delay(5, TimeUnit.SECONDS)
                    .repeat();


        }
        return mObservableWriter;
    }


    private Observable<HeartMsgBean> mObservableReader;

    private synchronized Observable<HeartMsgBean> creatHeartReader(Observable<BufferedReader> obs) {
        if (mObservableReader == null) {
            mObservableReader = obs
                    .map(new Func1<BufferedReader, HeartMsgBean>() {
                        @Override
                        public HeartMsgBean call(BufferedReader bufferedReader) {
                            try {
                                ULog.d(" --- bufferedReader.readLine");
                                String str = bufferedReader.readLine();
                                ULog.d(" --- bufferedReader.readLined = " + str);
                                if (null != str && str.length() > 0) {
                                    return new Gson().fromJson(str, HeartMsgBean.class);
                                }
                            } catch (IOException e) {
                                // e.printStackTrace();
                            }
                            return null;
                        }
                    })
                    .doOnNext(new Action1<HeartMsgBean>() { // doOnNext 只适用于debug，看看而已
                        @Override
                        public void call(HeartMsgBean msgBean) {
                            if (null == msgBean)
                                ULog.e("null == socketMegBean");
                            else
                                ULog.d(msgBean.toString());
                        }
                    })
                    .repeat()
//                    .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
//                        @Override
//                        public Observable<?> call(Observable<? extends Void> observable) {
//                            return observable.flatMap(new Func1<Void, Observable<?>>() {
//                                @Override
//                                public Observable<?> call(Void aVoid) {
//                                    return null;
//                                }
//                            });
//                        }
//                    })
                    .share();
        }

        return mObservableReader;
    }


}
