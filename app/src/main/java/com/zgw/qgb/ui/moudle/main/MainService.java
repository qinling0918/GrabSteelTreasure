package com.zgw.qgb.ui.moudle.main;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Tsinling on 2017/10/17 11:06.
 * description:
 */

public interface MainService {
    //http://qgbtest.zgw.com/Notice/GetPushMessageList?page=1&MsgTypeId=22&pageSize=10&memberId=73740

   /* @GET("Notice/GetPushMessageList")
    Single<MainBean> getNotification(
            @Query("page") int page,
            @Query("MsgTypeId") int msgTypeId,
            @Query("pageSize") int pageSize,
            @Query("memberId") int memberId);*/
    @POST("api/User/SendVcode")
    @FormUrlEncoded
    Observable<BaseBean> sendVcode(
            @Field("Phone") String phone,
            @Field("ValidateType") int validateType,
            @Field("Source") int source);

    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("api/User/Register")
    Observable<BaseBean> Register(@Body Object requestBody);//注册


    @POST("api/User/Login")
    Observable<BaseBean> Login(//登录
                                @Body Object requestBody
    );
/*    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);*/

   /* @GET("/users")
    Observable<List<User>> getUsers(@Query("since") int lastIdQueried, @Query("per_page") int perPage);*/
}
