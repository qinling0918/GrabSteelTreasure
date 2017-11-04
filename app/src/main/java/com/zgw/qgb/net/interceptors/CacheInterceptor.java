package com.zgw.qgb.net.interceptors;

import com.zgw.qgb.App;
import com.zgw.qgb.helper.utils.NetUtils;
import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Tsinling on 2017/10/16 14:42.
 * description:
 */

public class CacheInterceptor implements Interceptor {
    public static final int CACHE_NO_NET_TIME = 3600;
    public static final int CACHE_HAVE_NET_TIME = 90;
    int noNetCacheTime = CACHE_NO_NET_TIME;
    int cacheTime = CACHE_HAVE_NET_TIME;

    /**
     *
     * @param noNetCacheTime 没有网络时,从缓存中读取时间
     * @param cacheTime 有网络,在多久时间内获取缓存
     */
    public CacheInterceptor(int noNetCacheTime, int cacheTime) {
        this.noNetCacheTime = noNetCacheTime;
        this.cacheTime = cacheTime;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        boolean connected = NetUtils.isConnected(App.getContext());
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
                    .header("Cache-Control", "public, max-age=" + noNetCacheTime)
                    .build();
        } else {
            //没有网络
            return originalResponse.newBuilder()
                    //这里的设置的是我们的没有网络的缓存时间，想设置多少就是多少。
                    .header("Cache-Control", "public, max-age=" + cacheTime)
                    .removeHeader("Pragma")
                    .build();
        }
    }
}