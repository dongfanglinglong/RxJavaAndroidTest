package com.dongfang.rx.net;

import com.dongfang.rx.config.AppConfig;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dongfang on 16/4/17.
 */
public class HttpBusTest {
    HttpBus mHttpBus;
    OkHttpClient mClient;

    @BeforeClass
    public void setUp() throws Exception {
        System.out.println("setUp");
        AppConfig.CACHE_DIR = new File("./build/cache");
        mHttpBus = HttpBus.getSingleton();

        mClient = mHttpBus.getOkHttpClient();
    }

    @Test
    public void testGetHttpService() throws Exception {

    }

    @Test
    public void testGetOkHttpClient() throws Exception {
        OkHttpClient client = mHttpBus.getOkHttpClient();
        System.out.println(client.cache().directory().toString());
        Request request = new Request.Builder()
//                .cacheControl(CacheControl.FORCE_CACHE)
                .url("http://publicobject.com/helloworld.txt")
//                .url("https://api.heweather.com/x3/weather?city=shanghai&key=18de4eb4b63d4cb08a2bab2629c1d4b3")
                .build();


        int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
        request.header("Cache-Control:public, only-if-cached, max-stale=" + maxStale);

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException("Unexpected code " + response);

//        Headers responseHeaders = response.headers();
//        for (int i = 0; i < responseHeaders.size(); i++) {
//            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//        }

        // System.out.println(response.body().string());
    }

    @Test
    public void testGetOkHttpClientNormal() throws Exception {

        // public, max-stale=3600 --> 能够缓存到
        // public, max-age=3600 --> 不能够缓存到

        Request request = new Request.Builder()
                .url("https://api.heweather.com/x3/weather?city=shanghai&key=18de4eb4b63d4cb08a2bab2629c1d4b3")
                .header("Cache-Control", "public, max-age=3600,max-stale=3600") //public, only-if-cached, max-stale=3600
                .build();
        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException("Unexpected code " + response);
    }

    public void testGetOkHttpClientRetrofit() throws Exception {}

    @Test
    public void testGetOkHttpClientObserver() throws Exception {
        mHttpBus.getHttpService().getWeather("shanghai", "18de4eb4b63d4cb08a2bab2629c1d4b3")
                .subscribe();
    }


}