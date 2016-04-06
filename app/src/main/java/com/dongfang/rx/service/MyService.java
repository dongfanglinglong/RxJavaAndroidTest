package com.dongfang.rx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.dongfang.rx.socket.SocekBus;
import com.dongfang.rx.utils.ULog;


/**
 *
 */
public class MyService extends Service {


    //  private Handler mHandler;
    private SocekBus mSocekBus;

    @Override
    public void onCreate() {
        super.onCreate();

        ULog.d("------ ");
        //  mHandler = new Handler();
        mSocekBus = new SocekBus();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int iii = intent.getIntExtra("xxx", 0);
        if (iii == 1) {
            stopSelf();
        }

        ULog.d("------ iii = " + iii);

        int i = super.onStartCommand(intent, flags, startId);
        if (!mSocekBus.isConnect()) {
            mSocekBus.stat();
        }
        return i;
    }

    @Override
    public IBinder onBind(Intent intent) {
//        return mBinder;

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSocekBus == null) {
            mSocekBus.stop();
        }
    }
}
