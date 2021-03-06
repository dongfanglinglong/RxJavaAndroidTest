package com.dongfang.rx.socket;

import com.dongfang.rx.entity.BaseBean;
import com.dongfang.rx.entity.SocketMsgBean;
import com.dongfang.rx.exception.SocketException;
import com.dongfang.rx.net.HttpBus;
import com.dongfang.rx.utils.ULog;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Response;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by dongfang on 16/4/6.
 */
public final class SocketBus {
    public static final String TAG = "SocketBus";
    private static final long UI_ID = 777000;

    private SocketMsgBean mHttpMegBean;

    private BaseBean mBaseBean;

    private Observable<String> mObsMsgHttp;

    /** 通过注册，可以获取到服务端推送（或轮询）得到的BaseBean对象 */
    private Observable<BaseBean> mOBSPushMsg;
    /** 查看{@link Socket2Connect} 说明 */
    private Socket2Connect mSocket2Connect;
    /** 获取socket的Observable , 通过 {@link Socket2Connect#startConnect()} 返回得到 */
    private Observable<Socket> mOBSSocket;
    /** 通过方法{@code subscripMsg} 注册之后的subscription列表 */
    private ArrayList<Subscription> mSubscriptionList;
    /** 用于保存已经收到的推送消息的id */
    private HashMap mHashMap;

    public static SocketBus getInstance() {
        return SocketBusLoader.INSTANCE;
    }

    private SocketBus() { init(); }

    private void init() {
        mHttpMegBean = new SocketMsgBean();
        mHttpMegBean.msgId = UI_ID;
        mHttpMegBean.mstType = SocketMsgBean.MSG_TYPE_HTTP;
        mHttpMegBean.dataArrary = new String[]{"1", "2", "3", "4"};
        mBaseBean = new BaseBean();

        mBaseBean.id = UI_ID;
        mBaseBean.msg = "OK";
        mBaseBean.data = new Gson().toJson(mHttpMegBean);

        mSubscriptionList = new ArrayList<Subscription>();
        mHashMap = new HashMap();

        initObsHttp();
    }

    /** 触发socket链接，同时初始化{@code mOBSSokcet} 和 {@code mSocket2Connect} 等对象 */
    public synchronized void start() throws SocketException {
        if (null == mSocket2Connect) {
            mSocket2Connect = //Socket2Connect.getInstance("192.168.5.6", 20011);
                    Socket2Connect.getInstance("10.128.7.25", 20011);
            mOBSSocket = mSocket2Connect.startConnect();

            // TODO: 2016/4/12  测试用，需要删除
            // Observable.just(1)
            //         .subscribeOn(Schedulers.io())
            //         .delaySubscription(5, TimeUnit.SECONDS)
            //         .subscribe(new Action1<Integer>() {
            //             @Override
            //             public void call(Integer integer) {
            //                 testSocketMsg();
            //             }
            //         });
        }
    }

    public void stop() {
        if (mSUBSPSocketMsgTest != null)
            mSUBSPSocketMsgTest.unsubscribe();
        for (Subscription subscription : mSubscriptionList) {
            subscription.unsubscribe();
        }
        mHashMap.clear();
        mSubscriptionList.clear();
        mSocket2Connect.disConnect();

        mSocket2Connect = null;
    }

    /** 是否开始连接 */
    public boolean isStarted() {
        return null != mOBSSocket;
    }


    /**
     * 合并socket推送（{@link Socket2Connect#getObservableReader()} ）和http轮询逻辑，
     * 建立能够得到 {@link BaseBean} 对象的Observable
     * <p>
     * <p>1. 会过滤空数据; （当 {@code str == null || str.length() == 0 } , str由socket推送或http轮询获取）
     * <p>2. 会过滤 当 str 无法转换成 {@link BaseBean}时的消息
     *
     * @param needPoll 是否需要http轮询
     * @return Observable<BaseBean>
     */
    private Observable<BaseBean> getGrablePushMsgObservable(final boolean needPoll) {
        Observable<String> observable = needPoll
                ? Observable.merge(mSocket2Connect.getObservableReader().repeat(), mObsMsgHttp)
                : mSocket2Connect.getObservableReader();
        mOBSPushMsg = observable
                .map(new Func1<String, BaseBean>() {
                    @Override
                    public BaseBean call(String str) {
                        ULog.e(" -- " + str);
                        if (null != str && str.length() > 0) {
                            BaseBean bean = null;
                            try {
                                bean = new Gson().fromJson(str, BaseBean.class);
                            } catch (Throwable e) {
                                ULog.e(e.getMessage());
                            }
                            // ULog.i(" -- map BaseBean == " + bean);
                            return bean;
                        }
                        return null;
                    }
                })
                .filter(new Func1<BaseBean, Boolean>() {
                    @Override
                    public Boolean call(BaseBean bean) {
                        return null != bean;
                    }
                })
                .share();


        return mOBSPushMsg;
    }

    /** 轮询http 获取推送失败的信息 */
    private void initObsHttp() {
        if (mObsMsgHttp == null) {
            mObsMsgHttp = Observable.just(HttpBus.getSingleton().getSocketMsgCall())
                    .map(new Func1<Call, String>() {
                        @Override
                        public String call(Call call) {
                            try {
                                Response response = call.execute();
                                if (response.isSuccessful()) {
                                    return response.body().string();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    })
                    .filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            return null != s && s.length() > 0;
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            mBaseBean.id = UI_ID + Math.round(Math.random() * 100);
                            mHttpMegBean.msgId = mBaseBean.id;
                            mBaseBean.data = new Gson().toJson(mHttpMegBean);
                            return Observable.just(new Gson().toJson(mBaseBean)).delay(5, TimeUnit.SECONDS);
                        }
                    })
                    .delay(5, TimeUnit.SECONDS)
                    .repeat()
                    .share();
        }
    }

    /** {@link SocketBus#subscripMsg(Class, Subscriber, boolean, Func1, Scheduler)} */
    public Subscription subscripMsg(final Class aClass, final Subscriber subscriber) {
        return subscripMsg(aClass, subscriber, false, null, null);
    }

    /**
     * 1. 通过该方法，可以获取到由服务端推送的 {@code aClass} 对象信息，并让用户在通过参数 {@code subscriber}  实现自己的业务逻辑；<br/>
     * 2. 注册成功之后，会把返回的{@code Subscription} 对象放到{@code mSubscriptionList}列表中<br/>
     * 3. 收到消息之后，会对该消息进行反馈
     * 4. 如果出现{@link Observable#error(Throwable)}错误,则会中断，同时会让{@code mSubscriptionList}列表中全部对象
     * 调用{@link Subscription#unsubscribe()}，之后清空{@code mSubscriptionList}列表<br/>
     * <ul>
     * a. 通过实现参数 {@code filterFunc1 } ，可以对返回的信息进行过滤，得到真正需要的数据；否则设置 null <br/>
     * b. 通过设置参数 {@code observeOn} ,可以设置返回的结果在哪个线程上返回，默认在{@link Schedulers#io()}线程上
     * </ul>
     *
     * @param aClass      最终需要转换成的对象
     * @param subscriber  注册到Observable的逻辑
     * @param needPoll    是否需要轮询
     * @param filterFunc1 若需要过滤信息，实现该函数
     * @param observeOn   注册之后，在哪个线程上返回结果
     * @return {@code Subscription} 对象
     */
    public Subscription subscripMsg(final Class aClass, final Subscriber subscriber, final boolean needPoll,
                                    final Func1 filterFunc1, final Scheduler observeOn) {
        Observable observable = getGrablePushMsgObservable(needPoll)
                .flatMap(new Func1<BaseBean, Observable<BaseBean>>() {
                    @Override
                    public Observable<BaseBean> call(final BaseBean bean) {
                        return Observable.just(bean).mergeWith(mSocket2Connect.getObservaleWriter().map(new Func1<PrintStream, BaseBean>() {
                            @Override
                            public BaseBean call(PrintStream printStream) {
                                // TODO: 2016/4/14 消息反馈需要进行调整，id应该不对
                                printStream.println(SocketUtils.getSocketMsgFeedback(bean.id));
                                printStream.flush();
                                return bean;
                            }
                        }));
                    }
                })
                .map(new Func1<BaseBean, Object>() {
                    @Override
                    public Object call(BaseBean bean) {
                        ULog.i(bean.toString());
                        Object obj = null;
                        try {
                            obj = new Gson().fromJson(bean.data, aClass);
                        } catch (Throwable e) {}
                        return obj;
                    }
                }).doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        for (Subscription scrip : mSubscriptionList) {
                            scrip.unsubscribe();
                        }
                        mSubscriptionList.clear();
                    }
                });

        if (null != filterFunc1) {
            observable = observable.filter(filterFunc1);
        }
        if (null != observeOn) {
            observable = observable.observeOn(observeOn);
        }

        Subscription subscription = observable.subscribe(subscriber);
        mSubscriptionList.add(subscription);
        return subscription;
    }

    /**
     * 向服务端发送消息
     *
     * @param msg 消息内容
     * @return Subscription
     */
    public Subscription sendMsg(final String msg) {
        if (mSocket2Connect == null || mSocket2Connect.getObservaleWriter() == null) {
            return null;
        }

        return mSocket2Connect.getObservaleWriter().map(new Func1<PrintStream, Object>() {
            @Override
            public Object call(PrintStream printStream) {
                printStream.println(msg);
                printStream.flush();
                return null;
            }
        }).subscribe();
    }

    /**
     * 修改心跳时间
     *
     * @param time 秒
     */
    public void heartIntervalChange(int time) {
        mSocket2Connect.changeHeartInterval(time);
    }


    public static class SocketBusLoader {
        private static final SocketBus INSTANCE = new SocketBus();
    }


    private Subscription mSUBSPSocketMsgTest;

    private void testSocketMsg() {
        if (null != mSUBSPSocketMsgTest) {
            mSUBSPSocketMsgTest.unsubscribe();
        }

        // --------- METHOD 1 ---------
        mSUBSPSocketMsgTest = subscripMsg(SocketMsgBean.class, new Subscriber<SocketMsgBean>() {
            @Override
            public void onCompleted() {ULog.i("onCompleted"); }

            @Override
            public void onError(Throwable e) {
                ULog.e(e.getMessage());
                mSUBSPSocketMsgTest.unsubscribe();
            }

            @Override
            public void onNext(SocketMsgBean socketMsgBean) {
                ULog.i("onNext -->" + (null == socketMsgBean ? "socketMsgBean == null" : socketMsgBean.toString()));
            }
        });

        // --------- METHOD 2 ---------
//        mSUBSPSocketMsgTest = getGrablePushMsgObservable()
//                .map(new Func1<BaseBean, SocketMsgBean>() {
//                    @Override
//                    public SocketMsgBean call(BaseBean bean) {
//                        ULog.i(bean.toString());
//                        try {
//                            return new Gson().fromJson(bean.data, SocketMsgBean.class);
//                        } catch (Throwable e) {}
//                        return null;
//                    }
//                })
//                .filter(new Func1<SocketMsgBean, Boolean>() {
//                    @Override
//                    public Boolean call(SocketMsgBean socketMsgBean) {
//                        if (null != socketMsgBean && !mHashMap.containsKey(socketMsgBean.msgId)) {
//                            mHashMap.put(socketMsgBean.msgId, socketMsgBean.msgId);
//                            return true;
//                        }
//
//                        if (null != socketMsgBean) {
//                            ULog.i("filter [" + (null == socketMsgBean ? "null" : socketMsgBean.msgId) + "]");
//                        }
//
//                        return false;
//                    }
//                })
//                .subscribe(new Subscriber<SocketMsgBean>() {
//                    @Override
//                    public void onCompleted() {ULog.i("onCompleted"); }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        ULog.e(e.getMessage());
//                        mSUBSPSocketMsgTest.unsubscribe();
//                    }
//
//                    @Override
//                    public void onNext(SocketMsgBean socketMsgBean) {
//                        ULog.i("onNext -->" + (null == socketMsgBean ? "socketMsgBean == null" : socketMsgBean.toString()));
//                    }
//                });
    }


}
