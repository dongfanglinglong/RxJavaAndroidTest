package com.dongfang.rx.net;

import com.dongfang.rx.Bean.SocketMsgBean;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import rx.Observer;

/**
 * Created by dongfang on 2016/4/15.
 */
public class HttpClientTest {


    HttpClient mHttpClient;


    @Before
    public void setUp() throws Exception {
        mHttpClient = HttpClient.getSingleton();
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
        System.out.println("---------request--------\n " +request.toString());

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
        mHttpClient.getHttpService().getSocketMsg(new long[]{1, 2, 3})
                .subscribe(new Observer<SocketMsgBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SocketMsgBean socketMsgBean) {
                        System.out.println(socketMsgBean.toString());
                    }
                });
    }
}