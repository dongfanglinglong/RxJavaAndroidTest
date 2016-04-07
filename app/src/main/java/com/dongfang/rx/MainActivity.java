package com.dongfang.rx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dongfang.rx.socket.SocketBus;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private TextView mTextView;
    private Button mBtnStart, mBtnStop;


    private SocketBus mSocketBus;
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textview);
        mBtnStart = (Button) findViewById(R.id.button_start);
        mBtnStop = (Button) findViewById(R.id.button_stop);

        mSocketBus = new SocketBus();


        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mSocketBus.start();
            }
        });


        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mSocketBus.stop();
                mSubscription.unsubscribe();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Observable observable = Observable.just(1)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        System.out.println("-- " + integer);
                        return integer;

                    }
                })
                .share();


        observable
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return integer * 10;
                    }
                })
                .delay(1, TimeUnit.SECONDS)
                .repeat()
                .subscribe(new Action1<Integer>() {
                               @Override
                               public void call(Integer integer) {
                                   System.out.println("+= " + integer);
                               }
                           }
                );

        observable
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return integer * 10000;
                    }
                })
                .delay(1, TimeUnit.SECONDS)
                .repeat()
                .subscribe(new Action1<Integer>() {
                               @Override
                               public void call(Integer integer) {
                                   System.out.println("+= " + integer);
                               }
                           }
                );


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
