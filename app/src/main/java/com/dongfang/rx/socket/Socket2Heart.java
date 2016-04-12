package com.dongfang.rx.socket;

import android.support.annotation.Size;

import com.dongfang.rx.Bean.BaseBean;
import com.dongfang.rx.Bean.HeartMsgBean;
import com.dongfang.rx.exception.SocketException;
import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    public static final int HEART_ERROR_LIMIT = 3; //3
    /** 心跳数据出错次数 */
    private int heartErroCount = 0;

    /** 第几次心跳记录 */
    private long heartCount = 0;
    /** 心跳间隔时间 */
    private int mHeartInterval = HEART_INTERVAL_DEFAULT;

    /** 注册心跳逻辑的 subscription */
    private Subscription mSubscriptionHeart;

    public static Socket2Heart getInstance() {
        return Socket2HeartLoader.INSTANCE;
    }

    private Socket2Heart() {}

    /** zip {@code mOBSHeart} 和 {@code mOBSHeartWriter} 得到的新的Observable */
    private Observable<HeartMsgBean> mOBSHeart = null;
    /** 向socket写数据 */
    private Observable<Long> mOBSHeartWriter = null;
    /** 向socket读数据 */
    private Observable<HeartMsgBean> mOBSHeartReader = null;


    /** 停止心跳逻辑 */
    public void stopHeart() {
        if (mSubscriptionHeart != null)
            mSubscriptionHeart.unsubscribe();
        mOBSHeartReader = null;
        mOBSHeartWriter = null;
    }

    /**
     * 开始心跳逻辑；会重置之前的心跳逻辑
     *
     * @param obsHeartWriter 写入心跳信息
     * @param obsHeartReader 读取心跳反馈
     * @param subscriber     注册逻辑
     * @param time           心跳间隔时间
     * @return Observable<HeartMsgBean>
     */
    public Observable<HeartMsgBean> startHeart(Observable<PrintStream> obsHeartWriter,
                                               Observable<String> obsHeartReader,
                                               Subscriber subscriber,
                                               @Size(min = 1) int time) {
        mHeartInterval = time;
        heartErroCount = 0;

        if (null != mSubscriptionHeart) {
            mSubscriptionHeart.unsubscribe();
        }

        mOBSHeart = creatHeartWriter(obsHeartWriter)
                .zipWith(creatHeartReader(obsHeartReader), new Func2<Long, HeartMsgBean, HeartMsgBean>() {
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

        mSubscriptionHeart = mOBSHeart.subscribe(subscriber);
        return mOBSHeart;
    }

    /** {@link Socket2Heart#startHeart(Observable, Observable, Subscriber, int)} */
    public Observable<HeartMsgBean> startHeart(Observable<PrintStream> obsW,
                                               Observable<String> obsR,
                                               Subscriber subscriber) {
        return startHeart(obsW, obsR, subscriber, HEART_INTERVAL_DEFAULT);
    }


    /** 创建写入心跳信息的Observable */
    private synchronized Observable<Long> creatHeartWriter(Observable<PrintStream> observable) {
        if (observable != null) {
            mOBSHeartWriter = observable
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
                            if (heartErroCount > HEART_ERROR_LIMIT) {
                                ULog.d(" --- heart write error [" + heartErroCount + "]");
                                return Observable.error(new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION,
                                        "Socket outputStream error!"));
                            } else if (heartErroCount > 0) {
                                ULog.d(" --- heart write delay[" + (mHeartInterval * heartErroCount) + "]");
                                return Observable.just(aLong).delay(mHeartInterval * heartErroCount, TimeUnit.SECONDS);
                            }
                            return Observable.just(aLong).delay(mHeartInterval, TimeUnit.SECONDS);
                        }
                    })
                    .repeat()
                    .share();
        }
        return mOBSHeartWriter;
    }


    /** 创建读取心跳信息的Observable */
    private synchronized Observable<HeartMsgBean> creatHeartReader(final Observable<String> observable) {
        if (null != observable) {
            mOBSHeartReader = observable
                    .flatMap(new Func1<String, Observable<HeartMsgBean>>() {
                        @Override
                        public Observable<HeartMsgBean> call(String str) {
                            HeartMsgBean msgBean = null;
                            if (can2JsonBean(str, BaseBean.class)) {
                                heartErroCount = 0;
                                try {
                                    msgBean = new Gson().fromJson(str, HeartMsgBean.class);
                                } catch (JsonSyntaxException e) {
                                    ULog.e(e.getMessage());
                                    msgBean = null;
                                }
                            } else {
                                heartErroCount++;
                            }

                            if (heartErroCount > HEART_ERROR_LIMIT) {
                                return Observable.error(new SocketException(SocketException.SOCKET_CONNECT_EXCEPTION,
                                        "Socket inputStream error!"));
                            } else if (heartErroCount > 0) {
                                return Observable.just(msgBean).delay(mHeartInterval * heartErroCount, TimeUnit.SECONDS);
                            }

                            return Observable.just(msgBean);
                        }
                    })
                    .filter(new Func1<HeartMsgBean, Boolean>() {
                        @Override
                        public Boolean call(HeartMsgBean msgBean) {
                            return null != msgBean;
                        }
                    })
                    .repeat()
                    .share();
        }

        return mOBSHeartReader;
    }

    /**
     * 检测aJson是否能转换成aClass对象
     *
     * @param sJson
     * @param aClass
     * @return 转换成功且为空返回true，反正 false；
     */
    private boolean can2JsonBean(String sJson, Class aClass) {
        if (null == sJson || sJson.length() == 0)
            return false;
        try {
            return null != (new Gson().fromJson(sJson, aClass));
        } catch (Throwable e) {}

        return false;
    }

    /** 获取心跳间隔 */
    public int getHeartInterval() {
        return mHeartInterval;
    }


    /** 除心跳外，{@code mOBSHeartReader} 允许出错的上限 */
    public int getMaxNullLimit() {
        return HEART_ERROR_LIMIT * mHeartInterval + 2;
    }


    public static class Socket2HeartLoader {
        private static final Socket2Heart INSTANCE = new Socket2Heart();
    }
}
