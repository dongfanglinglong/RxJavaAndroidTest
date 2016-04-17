package com.dongfang.rx.net;

import android.net.http.HttpResponseCache;
import android.util.Log;

import com.dongfang.rx.entity.WeatherAPI;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import rx.Observer;

/**
 * Created by dongfang on 2016/4/15.
 */
public class HttpClientTest {


    HttpBus mHttpClient;


    @Before
    public void setUp() throws Exception {
        mHttpClient = HttpBus.getSingleton();
        System.out.println("setUp");
    }

    @Test
    public void testGetSingleton() throws Exception {
        final Request request = new Request.Builder()
                .addHeader("dongfang", "linglong")
                .addHeader("dongfang", "linglong1111111111111111")
                .url("http://www.publicobject.com/helloworld.txt")
                .header("User-Agent", "OkHttp Example")
                .header("dongfang", "linglong111111111")
                .build();
        System.out.println("---------request--------\n " + request.toString());

        try {
            Response response = mHttpClient.getOkHttpClient().newCall(request).execute();
            System.out.println("---------Response--------\n " +
                    response.body().string());
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }


    @Test
    public void testGetHttpService() throws Exception {
        mHttpClient.getHttpService().getWeather("shanghai", "18de4eb4b63d4cb08a2bab2629c1d4b3")
                .subscribe(new Observer<WeatherAPI>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("----------onCompleted-------");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("----------onError-------");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(WeatherAPI s) {
                        System.out.println(s.toString());
                    }
                });
    }
}