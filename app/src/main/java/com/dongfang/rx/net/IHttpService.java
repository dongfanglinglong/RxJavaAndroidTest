package com.dongfang.rx.net;

import com.dongfang.rx.Bean.SocketMsgBean;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by dongfang on 2016/4/14.
 */
public interface IHttpService {

    @GET("/socketmsg")
    Observable<SocketMsgBean> getSocketMsg(@Query("ids") long[] ids);














}
