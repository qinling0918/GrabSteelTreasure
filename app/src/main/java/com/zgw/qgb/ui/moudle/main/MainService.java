package com.zgw.qgb.ui.moudle.main;

import com.zgw.qgb.model.MainBean;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Tsinling on 2017/10/17 11:06.
 * description:
 */

public interface MainService {
    //http://qgbtest.zgw.com/Notice/GetPushMessageList?page=1&MsgTypeId=22&pageSize=10&memberId=73740

    @GET("Notice/GetPushMessageList")
    Single<MainBean> getNotification(
            @Query("page") int page,
            @Query("MsgTypeId") int msgTypeId,
            @Query("pageSize") int pageSize,
            @Query("memberId") int memberId);

    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);

   /* @GET("/users")
    Observable<List<User>> getUsers(@Query("since") int lastIdQueried, @Query("per_page") int perPage);*/
}
