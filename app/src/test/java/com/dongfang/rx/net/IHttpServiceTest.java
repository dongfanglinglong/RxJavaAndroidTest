package com.dongfang.rx.net;

import com.dongfang.rx.config.AppConfig;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import okhttp3.OkHttpClient;
import rx.Observer;

/**
 * Created by dongfang on 2016/4/18.
 */
public class IHttpServiceTest {


    HttpBus mHttpBus;
    OkHttpClient mClient;


    @Before
    public void setup() throws Exception {
        System.out.println("setUp");
        AppConfig.CACHE_DIR = new File("./build/cache");
        mHttpBus = HttpBus.getSingleton();
        mClient = mHttpBus.getOkHttpClient();
    }


    @Test
    public void testGetSocketMsg() throws Exception {
        mHttpBus.getHttpService().getSocketMsg()
                .subscribe(new Observer() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Object s) {
                        System.out.println(s);
                    }
                });

    }

    @Test
    public void testGetWeather() throws Exception {
        mHttpBus.getHttpService().getWeather("shanghai", "18de4eb4b63d4cb08a2bab2629c1d4b3")
                .subscribe();
    }
}