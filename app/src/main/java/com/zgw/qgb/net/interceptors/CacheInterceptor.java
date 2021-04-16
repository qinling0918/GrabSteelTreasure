package com.zgw.qgb.net.interceptors;

import com.zgw.qgb.helper.Utils;
import com.zgw.qgb.helper.utils.NetUtils;
import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Tsinling on 2017/10/16 14:42.
 * description:
 *
 * okhttp3 中Cache类包含的缓存策略
 * noCache ：不使用缓存，全部走网络
 * noStore ： 不使用缓存，也不存储缓存
 * onlyIfCached ： 只使用缓存
 * maxAge ：最长使用时间   设置最大失效时间，失效则不使用
 * maxStale ：最长过期时间值   设置最大失效时间，失效则不使用
 * minFresh ：设置最小有效时间，失效则不使用
 * FORCE_NETWORK ： 强制走网络
 * FORCE_CACHE ：强制走缓存
 */

public class CacheInterceptor implements Interceptor {
    // 有网络时 设置缓存超时时间1分钟
    public static final int DEFAULT_MAX_AGE = 60;
    // 无网络时 设置缓存超时时间1个小时
    public static final int DEFAULT_MAX_STALE = 3600;


    private final int maxAge ;
    private final int maxStale;

    /**
     *
     * @param maxAge 有网络,在多久时间内获取缓存,超出该值从网络重新获取
     * @param maxStale 无网络,早
     */
    public CacheInterceptor(int maxAge, int maxStale ) {
        this.maxAge = maxAge;
        this.maxStale  = maxStale;
    }
    public CacheInterceptor() {
        this.maxAge = DEFAULT_MAX_AGE;
        this.maxStale = DEFAULT_MAX_STALE;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        boolean connected = NetUtils.isConnected(Utils.getContext());
        if (!connected) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }
        Response originalResponse = chain.proceed(request);
        if (connected) {
            //有网络，缓存时间短
            String cacheControl = request.cacheControl().toString();
            return originalResponse.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        } else {
            //没有网络
            return originalResponse.newBuilder()
                    //这里的设置的是我们的没有网络的缓存时间，想设置多少就是多少。
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
        // max-age 为 设置最大失效时间，失效则不使用
        // 请求:强制响应缓存者，根据该值,校验新鲜性.即与自身的Age值,与请求时间做比较.
        // 如果超出max-age值,则强制去服务器端验证.以确保返回一个新鲜的响应.其功能本质
        // 上与传统的Expires类似,但区别在于Expires是根据某个特定日期值做比较.
        // 一但缓存者自身的时间不准确.则结果可能就是错误的.而max-age,显然无此问题. 
        //Max-age的优先级也是高于Expires的. 响应:同上类似,只不过发出方不一样.

        // // 设置单个请求的缓存时间
        //@Headers("Cache-Control: max-age=640000")
        //@GET("user/list")
        //Call<List<javaBean>> getList();
    }
}