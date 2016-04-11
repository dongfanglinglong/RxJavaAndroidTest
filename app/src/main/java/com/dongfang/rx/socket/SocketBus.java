package com.dongfang.rx.socket;

import com.dongfang.rx.Bean.BaseBean;
import com.dongfang.rx.Bean.SocketMsgBean;
import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;

import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by dongfang on 16/4/6.
 */
public final class SocketBus {
    public static final String TAG = "SocketBus";
    private static final long UI_ID = 777000;

    private SocketMsgBean mSocketMegBean, mHttpMegBean;

    private BaseBean<SocketMsgBean> mBaseBean;

    private Observable<String> mObsMsgHttp, mObsMsgSocket;


    private Observable<BaseBean> mObservableGrable;
    private Observable<Socket> mObservableSocket;
    private Subscription mSubscription;


    private Socket2Connect mSocket2Connect;


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
        mHttpMegBean.msgId = UI_ID;
        mHttpMegBean.mstType = SocketMsgBean.MSG_TYPE_HTTP;
        mHttpMegBean.dataArrary = new String[]{"1", "2", "3", "4"};
        mBaseBean = new BaseBean<SocketMsgBean>();


        mBaseBean.id = UI_ID;
        mBaseBean.msg = "OK";
        mBaseBean.data = new Gson().toJson(mHttpMegBean);

        mHashMap = new HashMap();
        try {
            mSocket2Connect =
                    //Socket2Connect.getInstance("192.168.5.6", 20011);
                    Socket2Connect.getInstance("10.128.7.25", 20011);
            mObservableSocket = mSocket2Connect.startConnect();

        } catch (SocketException e) {
            e.printStackTrace();
        }

        initObsHttp();

    }

    /** 全局获取 推送的Observable */
    private Observable<BaseBean> getObsGrable() {
        initObsHttp();
        mObservableGrable = Observable.merge(mSocket2Connect.getObservableReader().repeat(), mObsMsgHttp)
//        mObservableGrable = mSocket2Connect.getObservableReader()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        ULog.e(" -- " + s);
                        // 过滤空的数据
                        return null != s && s.length() > 0;
                    }
                })
                .map(new Func1<String, BaseBean>() {
                    @Override
                    public BaseBean call(String s) {
                        BaseBean bean = null;
                        try {
                            bean = new Gson().fromJson(s, BaseBean.class);
                        } catch (Throwable e) {
                            ULog.e(e.getMessage());
                        }
                        // ULog.i(" -- map BaseBean == " + bean);
                        return bean;
                    }
                })
                .filter(new Func1<BaseBean, Boolean>() {
                    @Override
                    public Boolean call(BaseBean bean) {
//                        ULog.i(" --filter  BaseBean == " + bean);
                        return true;
                    }
                })
                .share();


        return mObservableGrable;
    }

    /** 轮询http 获取推送失败的信息 */
    private void initObsHttp() {
        if (mObsMsgHttp == null) {
            mObsMsgHttp = Observable.just(1l)
//                    .interval(4, TimeUnit.SECONDS)
                    .map(new Func1<Long, Long>() {
                        @Override
                        public Long call(Long aLong) {
                            return System.currentTimeMillis() % 10;
                        }
                    })
                    .flatMap(new Func1<Long, Observable<String>>() {
                        @Override
                        public Observable<String> call(Long aLong) {
                            mBaseBean.id = UI_ID + aLong;
                            mHttpMegBean.msgId = mBaseBean.id;
                            mBaseBean.data = new Gson().toJson(mHttpMegBean);
                            return Observable.just(new Gson().toJson(mBaseBean)).delay(5, TimeUnit.SECONDS);
                        }
                    })
                    .repeat()
                    .share();
        }


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
    }


    public Subscription subscripMsg(final Class aClass, Subscriber subscriber) {
        return getObsGrable()
                .map(new Func1<BaseBean, Object>() {
                    @Override
                    public Object call(BaseBean bean) {
                        ULog.i(bean.toString());
                        try {
                            return new Gson().fromJson(bean.data, aClass);
                        } catch (Throwable e) {}
                        return null;
                    }
                })
                .subscribe(subscriber);
    }


    public void start() {
        if (null != mSubscription) {
            mSubscription.unsubscribe();
        }
        mSubscription = getObsGrable()
                .map(new Func1<BaseBean, SocketMsgBean>() {
                    @Override
                    public SocketMsgBean call(BaseBean bean) {
                        ULog.i(bean.toString());
                        try {
                            return new Gson().fromJson(bean.data, SocketMsgBean.class);
                        } catch (Throwable e) {}
                        return null;
                    }
                })
                .filter(new Func1<SocketMsgBean, Boolean>() {
                    @Override
                    public Boolean call(SocketMsgBean socketMsgBean) {
                        if (null != socketMsgBean && !mHashMap.containsKey(socketMsgBean.msgId)) {
                            mHashMap.put(socketMsgBean.msgId, socketMsgBean.msgId);
                            return true;
                        }

                        if (null != socketMsgBean) {
                            ULog.i("filter [" + (null == socketMsgBean ? "null" : socketMsgBean.msgId) + "]");
                        }

                        return false;
                    }
                })
                .subscribe(new Subscriber<SocketMsgBean>() {
                    @Override
                    public void onCompleted() {ULog.i("onCompleted"); }

                    @Override
                    public void onError(Throwable e) {
                        ULog.e(e.getMessage());
                        mSubscription.unsubscribe();
                    }

                    @Override
                    public void onNext(SocketMsgBean socketMsgBean) {
                        ULog.i("onNext -->" + (null == socketMsgBean ? "socketMsgBean == null" : socketMsgBean.toString()));
                    }
                });
    }

    public void stop() {
        mSubscription.unsubscribe();
        mHashMap.clear();
//        mSocket2Connect.disConnect();
    }

    /**
     * 修改心跳时间
     *
     * @param time 秒
     */
    public void heartIntervalChange(int time) {
        mSocket2Connect.changeHeartInterval(time);
    }
}
