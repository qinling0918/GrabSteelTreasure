package com.zgw.qgb.net.interceptors;

import androidx.annotation.NonNull;

import com.zgw.qgb.net.RequestHandler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 界面描述：
 * <p>
 * Created by tianyang on 2017/9/27.
 */

public class NetInterceptor implements Interceptor {
    private final RequestHandler handler;
    public NetInterceptor() {
        this(null);
    }

    public NetInterceptor(RequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("charset", "UTF-8")
                .build();
        if (handler != null) {
            request = handler.onBeforeRequest(request, chain);
        }
        Response response = chain.proceed(request);
        if (handler != null) {
            Response tmp = handler.onAfterRequest(response, chain);
            if (tmp != null) {
                return tmp;
            }
        }
        return response;
    }
}
