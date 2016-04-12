package com.dongfang.rx.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.dongfang.rx.exception.SocketException;
import com.dongfang.rx.socket.SocketBus;
import com.dongfang.rx.utils.ULog;


public class MyService extends Service {
    private SocketBus mSocketBus;

    @Override
    public void onCreate() {
        super.onCreate();
        ULog.d("------ ");

        mSocketBus = SocketBus.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("action");
        ULog.d("------ action = " + action);
        if ("stop".equals(action)) {
            stopSelf();
        } else {
            if (null != mSocketBus && !mSocketBus.isStarted()) {
                try {
                    mSocketBus.start();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSocketBus != null) {
            mSocketBus.stop();
        }
    }
}
