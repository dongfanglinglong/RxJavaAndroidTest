package com.dongfang.rx.net;

import com.dongfang.rx.entity.SocketMsgBean;
import com.dongfang.rx.entity.WeatherAPI;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by dongfang on 2016/4/14.
 */
public interface IHttpService {
    // 18de4eb4b63d4cb08a2bab2629c1d4b3
    @GET("/x3/weather")
    Observable<SocketMsgBean> getSocketMsg(@Query("city") String city,
                                           @Query("key") String key);

//    @Headers("Cache-Control:max-age=3600")
//    @Headers("Cache-Control:only-if-cached")
    @GET("/x3/weather")
    Observable<WeatherAPI> getWeather(@Query("city") String city,
                                      @Query("key") String key);


}
