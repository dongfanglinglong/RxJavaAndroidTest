package com.dongfang.rx.socket;

import com.dongfang.rx.utils.ULog;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by dongfang on 16/4/6.
 */
public class SocketBus {

    public static final String TAG = "SocketBus";
    private static final String UI_ID = "769U";
    private SocketMegBean mSocketMegBean, mHttpMegBean;


    private Observable<SocketMegBean> mObservableHttp, mObservableSocket, mObservableGrable;


    private Subscription mSubscription;


    private HashMap mHashMap;


    public SocketBus() {
        init();
    }

    private void init() {
        mSocketMegBean = new SocketMegBean();
        mSocketMegBean.id = UI_ID;
        mSocketMegBean.mstType = SocketMegBean.MSG_TYPE_SOCKET;
        mSocketMegBean.msg = "OK";
        mSocketMegBean.data = "{\"a\":10}";
        mSocketMegBean.dataArrary = new String[]{"1", "2", "3", "4"};

        mHttpMegBean = new SocketMegBean();
        mHttpMegBean.id = UI_ID;
        mHttpMegBean.mstType = SocketMegBean.MSG_TYPE_HTTP;
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
                .flatMap(new Func1<Long, Observable<SocketMegBean>>() {
                    @Override
                    public Observable<SocketMegBean> call(Long aLong) {
                        mHttpMegBean.id = UI_ID + aLong;
                        return Observable.just(mHttpMegBean);
                    }
                })
        ;

        mObservableSocket = Observable
                .interval(5, TimeUnit.SECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return System.currentTimeMillis() % 10;
                    }
                })
                .flatMap(new Func1<Long, Observable<SocketMegBean>>() {
                    @Override
                    public Observable<SocketMegBean> call(Long aLong) {
                        mSocketMegBean.id = UI_ID + aLong;
                        return Observable.just(mSocketMegBean);
                    }
                })
        ;


    }


    public void start() {

        if (mSubscription.isUnsubscribed())
            return;


        mObservableGrable = Observable.merge(mObservableHttp, mObservableSocket)
                .filter(new Func1<SocketMegBean, Boolean>() {
                    @Override
                    public Boolean call(SocketMegBean socketMegBean) {
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
        mSubscription = mObservableGrable.subscribe(new Action1<SocketMegBean>() {
            @Override
            public void call(SocketMegBean socketMegBean) {
                ULog.d(socketMegBean.toString());
            }
        });
    }

    public void stop() {
        mSubscription.unsubscribe();
        mHashMap.clear();
    }

}
