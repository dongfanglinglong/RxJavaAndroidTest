package com.dongfang.rx.net;

import com.dongfang.rx.config.AppConfig;
import com.dongfang.rx.config.Your;
import com.dongfang.rx.utils.ULog;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Headers;
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
public class HttpBus {


    private OkHttpClient mOkHttpClient;
    private IHttpService iHttpService;

    private volatile static HttpBus sHttpBus;

    private HttpBus() {
        init();
    }


    public static HttpBus getSingleton() {
        if (sHttpBus == null) {
            synchronized (HttpBus.class) {
                if (sHttpBus == null) {
                    sHttpBus = new HttpBus();
                }
            }
        }
        return sHttpBus;
    }

    private void init() {
        mOkHttpClient = new OkHttpClient
                .Builder()
                .cache(new Cache(AppConfig.CACHE_DIR, 10 * 1024 * 1024)) // 10MB
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                 .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                // .writeTimeout(15, TimeUnit.SECONDS)
                // .readTimeout(30, TimeUnit.SECONDS)
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


    /** Dangerous interceptor that rewrites the server's cache-control header. */
    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();
            if (request.url().toString().contains("heweather")){
                request.newBuilder().addHeader("Cache-Control", "max-age=3600").build();
            }
            return chain.proceed(request);
        }
    };


    Interceptor mCacheInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            request.newBuilder().cacheControl(CacheControl.FORCE_CACHE);
            return chain.proceed(request);
        }
    };


    Interceptor mTokenInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            // System.out.println("----------mTokenInterceptor---------");
            Request originalRequest = chain.request();


            if (Your.sToken == null || alreadyHasAuthorizationHeader(originalRequest)) {
                return chain.proceed(originalRequest);
            }


            Request authorised = originalRequest.newBuilder()
                    .header("Authorization", Your.sToken)
                    .build();

//            Headers headers = authorised.headers();
//            for (int i = 0, count = headers.size(); i < count; i++) {
//                System.out.println(headers.name(i) + ": " + headers.value(i));
//            }
            return chain.proceed(authorised);
        }

        private boolean alreadyHasAuthorizationHeader(Request originalRequest) {
            String key;
            for (int i = 0, l = originalRequest.headers().size(); i < l; i++) {
                key = originalRequest.headers().name(i);
                ULog.d(key + ":" + originalRequest.headers().value(i));
                if ("Authorization".equals(key)) {
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
