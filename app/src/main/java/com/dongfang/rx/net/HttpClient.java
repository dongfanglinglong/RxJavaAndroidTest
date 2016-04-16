package com.dongfang.rx.net;

import com.dongfang.rx.config.AppConfig;
import com.dongfang.rx.config.Your;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dongfang on 2016/4/14.
 */
public class HttpClient {


    private OkHttpClient mOkHttpClient;
    private IHttpService iHttpService;

    private volatile static HttpClient sHttpClient;

    private HttpClient() {init();}

    public static HttpClient getSingleton() {
        if (sHttpClient == null) {
            synchronized (HttpClient.class) {
                if (sHttpClient == null) {
                    sHttpClient = new HttpClient();
                }
            }
        }
        return sHttpClient;
    }

    private void init() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addNetworkInterceptor(mTokenInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .client(mOkHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        iHttpService = retrofit.create(IHttpService.class);
    }

    Interceptor mTokenInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (Your.sToken == null || alreadyHasAuthorizationHeader(originalRequest)) {
                return chain.proceed(originalRequest);
            }
            Request authorised = originalRequest.newBuilder()
                    .header("Authorization", Your.sToken)
                    .build();
            return chain.proceed(authorised);
        }

        private boolean alreadyHasAuthorizationHeader(Request originalRequest) {
            for (String s : originalRequest.headers().names()) {
                if ("Authorization".equals(s)) {
                    return true;
                }
            }
            return false;
        }
    };


    public IHttpService getHttpService() {
        return iHttpService;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
