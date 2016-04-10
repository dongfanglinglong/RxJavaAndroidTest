package com.dongfang.rx.socket;

import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * 负责维持心跳
 * <p/>
 * Created by dongfang on 2016/4/8.
 */
public final class Socket2Heart {


    /** 心跳时长, 秒 */
    private static final int HEART_INTERVAL_DEFAULT = 2; // 5
    /** 心跳错误阀值 */
    private static final int HEART_ERROR_CONFINE = 3; //3
    /** 心跳数据出错次数 */
    private int heartErroCount = 0;

    /** 第几次心跳记录 */
    private long heartCount = 0;


    private int heartInterval = HEART_INTERVAL_DEFAULT;

    private static Socket2Heart sSocket2Heart;

    private Socket2Heart() { }

    public static synchronized Socket2Heart getInstance() {
        if (null == sSocket2Heart) {
            sSocket2Heart = new Socket2Heart();
        }
        return sSocket2Heart;
    }

    private Subscription mSubscriptionHeart;

    /** 向socket写数据 */
    private Observable<HeartMsgBean> mObservableHeart = null;


    public void stopHeart() {
        if (mSubscriptionHeart != null)
            mSubscriptionHeart.unsubscribe();

        mObservableReader = null;
        mObservableWriter = null;

    }

    public Observable<HeartMsgBean> startHeart(Observable<PrintStream> obsW,
                                               Observable<BufferedReader> obsR,
                                               Subscriber subscriber,
                                               int time) {
        heartInterval = time;
        heartErroCount = 0;

        if (null != mSubscriptionHeart) {
            mSubscriptionHeart.unsubscribe();
        }

        mObservableHeart = creatHeartWriter(obsW)
                .zipWith(creatHeartReader(obsR), new Func2<Long, HeartMsgBean, HeartMsgBean>() {
                    @Override
                    public HeartMsgBean call(Long aLong, HeartMsgBean heartMsgBean) {
                        return heartMsgBean;
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        //throwable.printStackTrace();
                        ULog.e(throwable.getMessage());
                    }
                })
                .share();

        mSubscriptionHeart = mObservableHeart.subscribe(subscriber);
        return mObservableHeart;
    }


    public Observable<HeartMsgBean> startHeart(Observable<PrintStream> obsW,
                                               Observable<BufferedReader> obsR,
                                               Subscriber subscriber) {
        return startHeart(obsW, obsR, subscriber, HEART_INTERVAL_DEFAULT);
    }

    private Observable<Long> mObservableWriter;


    private synchronized Observable<Long> creatHeartWriter(Observable<PrintStream> observable) {
        if (observable != null) {
            mObservableWriter = observable
                    .map(new Func1<PrintStream, Long>() {
                        @Override
                        public Long call(PrintStream out) {
                            ULog.d(" --- heart  [" + heartCount + "]");
                            out.println(SocketUtils.getHeartRequest(heartCount));
                            out.flush();
                            return heartCount++;
                        }
                    })
                    .flatMap(new Func1<Long, Observable<Long>>() {
                        @Override
                        public Observable<Long> call(Long aLong) {
                            if (heartErroCount > HEART_ERROR_CONFINE) {
                                ULog.d(" --- heart write error [" + heartErroCount + "]");
                                return Observable.error(new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION,
                                        "HeartWriter error!"));
                            } else if (heartErroCount > 0) {
                                ULog.d(" --- heart write delay[" + (heartInterval * heartErroCount) + "]");
                                return Observable.just(aLong).delay(heartInterval * heartErroCount, TimeUnit.SECONDS);
                            }
                            return Observable.just(aLong).delay(heartInterval, TimeUnit.SECONDS);
                        }
                    })
                    .repeat()
                    .share();
        }
        return mObservableWriter;
    }


    private Observable<HeartMsgBean> mObservableReader;

    private synchronized Observable<HeartMsgBean> creatHeartReader(final Observable<BufferedReader> obs) {
        if (null != obs) {
            mObservableReader = obs
                    .map(new Func1<BufferedReader, HeartMsgBean>() {
                        @Override
                        public HeartMsgBean call(BufferedReader bufferedReader) {
                            try {
                                String str = bufferedReader.readLine();
                                ULog.d(" --- Heart FeedBack --> " + str);
                                if (null != str && str.length() > 0) {
                                    return new Gson().fromJson(str, HeartMsgBean.class);
                                }
                            } catch (Exception e) {
                                ULog.e(e.getMessage());
                            }
                            return null;
                        }
                    })
                    .flatMap(new Func1<HeartMsgBean, Observable<HeartMsgBean>>() {
                        @Override
                        public Observable<HeartMsgBean> call(HeartMsgBean heartMsgBean) {
                            if (null == heartMsgBean) {
                                ULog.e("null == socketMegBean [" + heartErroCount + "]");
                                heartErroCount++;
                            } else {
                                heartErroCount = 0;
                                ULog.i(" --- GSON fromJson --> " + heartMsgBean.toString());
                            }

                            if (heartErroCount > HEART_ERROR_CONFINE) {
                                return Observable.error(new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION,
                                        "HeartReader error!"));
                            } else if (heartErroCount > 0) {
                                return Observable.just(heartMsgBean).delay(heartInterval * heartErroCount, TimeUnit.SECONDS);
                            }

                            return Observable.just(heartMsgBean);
                        }
                    })
                    .repeat()
                    .share();
        }

        return mObservableReader;
    }


}
