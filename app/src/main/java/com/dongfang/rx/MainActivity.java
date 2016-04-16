package com.dongfang.rx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dongfang.rx.Bean.SocketMsgBean;
import com.dongfang.rx.net.HttpClient;
import com.dongfang.rx.service.MyService;
import com.dongfang.rx.socket.SocketBus;
import com.dongfang.rx.utils.ULog;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private TextView mTextView;
    private Button mBtnStart, mBtnStop, mBtnHeartChange, mBtnSubc, mBtnUnSubc;


    private SocketBus mSocketBus;
    private Subscription mSubscription;


    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        mTextView = (TextView) findViewById(R.id.textview);
        mBtnStart = (Button) findViewById(R.id.button_start);
        mBtnStop = (Button) findViewById(R.id.button_stop);
        mBtnHeartChange = (Button) findViewById(R.id.button_heart_change);
        mBtnSubc = (Button) findViewById(R.id.button_subscribe);
        mBtnUnSubc = (Button) findViewById(R.id.button_unsubscribe);


        mSocketBus = SocketBus.getInstance();


        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(mContext, MyService.class));
                mTextView.setText(mBtnStart.getText());
            }
        });
        mBtnHeartChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocketBus.heartIntervalChange(15);
                mTextView.setText("Change heart interval to 15s !");
            }
        });
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MyService.class);
                intent.putExtra("action", "stop");
                startService(intent);
                mTextView.setText(mBtnStop.getText());
            }
        });

        mBtnSubc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubscription = mSocketBus.subscripMsg(SocketMsgBean.class, new Subscriber() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {
                        ULog.e("onNext -->" + o);
                    }
                });
            }
        });

        mBtnUnSubc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mSubscription) {
                    mSubscription.unsubscribe();
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

        ULog.i("onResume");

        HttpClient.getSingleton().getHttpService().getSocketMsg(new long[]{1, 2, 3, 4})
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<SocketMsgBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e.toString());
                    }

                    @Override
                    public void onNext(SocketMsgBean socketMsgBean) {
                        System.out.println(socketMsgBean.toString());
                    }
                });


//        Observable observable = Observable.just(1)
//                .subscribeOn(Schedulers.io())
//                .map(new Func1<Integer, Integer>() {
//                    @Override
//                    public Integer call(Integer integer) {
//                        System.out.println("-- " + integer);
//                        return integer;
//
//                    }
//                })
//                .share();

//
//        observable
//                .map(new Func1<Integer, Integer>() {
//                    @Override
//                    public Integer call(Integer integer) {
//                        return integer * 10;
//                    }
//                })
//                .delay(1, TimeUnit.SECONDS)
//                .repeat()
//                .subscribe(new Action1<Integer>() {
//                               @Override
//                               public void call(Integer integer) {
//                                   System.out.println("+= " + integer);
//                               }
//                           }
//                );
//
//        observable
//                .map(new Func1<Integer, Integer>() {
//                    @Override
//                    public Integer call(Integer integer) {
//                        return integer * 10000;
//                    }
//                })
//                .delay(1, TimeUnit.SECONDS)
//                .repeat()
//                .subscribe(new Action1<Integer>() {
//                               @Override
//                               public void call(Integer integer) {
//                                   System.out.println("+= " + integer);
//                               }
//                           }
//                );


//        Observable.just(1)
//                .flatMap(new Func1<Integer, Observable<Long>>() {
//                    @Override
//                    public Observable<Long> call(Integer integer) {
//                        return Observable.interval(1, TimeUnit.SECONDS);
//                    }
//                })
//                .subscribe(new Action1<Long>() {
//                    @Override
//                    public void call(Long aLong) {
//                        System.out.println("--- " + aLong);
//                    }
//                });

    }
}
