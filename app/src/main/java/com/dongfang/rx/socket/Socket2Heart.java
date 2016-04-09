package com.dongfang.rx.socket;

import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
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

    public Observable<HeartMsgBean> getHeartObservable(Observable<PrintStream> obsW, Observable<BufferedReader> obsR) {
        if (mObservableHeart == null) {
            mObservableHeart = creatHeartWriter(obsW).zipWith(creatHeartReader(obsR), new Func2<Long, HeartMsgBean, HeartMsgBean>() {
                @Override
                public HeartMsgBean call(Long aLong, HeartMsgBean heartMsgBean) {
                    return heartMsgBean;
                }
            }).share();
        }

        return mObservableHeart;
    }

    private Observable<Long> mObservableWriter;
    /** 第几次心跳记录 */
    private long heartCount = 0;

    private synchronized Observable<Long> creatHeartWriter(Observable<PrintStream> observable) {
        if (mObservableWriter == null && observable != null) {
            mObservableWriter = observable
                    .map(new Func1<PrintStream, Long>() {
                        @Override
                        public Long call(PrintStream out) {
                            ULog.d(" --- heart  [" + heartCount + "]");
                            out.println(SocketUtils.getHeartRequest(heartCount));
                            return heartCount++;
                        }
                    })
                    .share()
                    // .debounce(1, TimeUnit.SECONDS)
                    .delay(5, TimeUnit.SECONDS)
                    .repeat();


        }
        return mObservableWriter;
    }


    private Observable<HeartMsgBean> mObservableReader;
    /** 心跳数据出错次数 */
    private int readerError = 0;

    private synchronized Observable<HeartMsgBean> creatHeartReader(final Observable<BufferedReader> obs) {
        if (mObservableReader == null && null != obs) {
            mObservableReader = obs
                    .map(new Func1<BufferedReader, HeartMsgBean>() {
                        @Override
                        public HeartMsgBean call(BufferedReader bufferedReader) {
                            try {
                                String str = bufferedReader.readLine();
                                ULog.d("  ---  HeartReader " + str);
                                if (null != str && str.length() > 0) {
                                    return new Gson().fromJson(str, HeartMsgBean.class);
                                }
                            } catch (Exception e) {
                                ULog.e(e.getMessage());
                            }
                            return null;
                        }
                    })
                    .doOnNext(new Action1<HeartMsgBean>() { // doOnNext 只适用于debug，看看而已
                        @Override
                        public void call(HeartMsgBean msgBean) {
                            if (null == msgBean) {
                                ULog.e("null == socketMegBean [" + readerError + "]");
                                readerError++;
                            } else {
                                readerError = 0;
                                ULog.i(msgBean.toString());
                            }
                        }
                    })
                    .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                        @Override
                        public Observable<?> call(Observable<? extends Void> observable) {
                            if (readerError > 5) {
                                return Observable.error(new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION,
                                        "HeartReader error!"));
                            } else if (readerError > 0) {
                                return observable.delay(5 * readerError, TimeUnit.SECONDS);
                            }
                            return observable;
                        }
                    })
                    .share();
        }

        return mObservableReader;
    }


}
