package com.zgw.qgb.net.interceptors;

import androidx.annotation.NonNull;

import com.zgw.qgb.net.RequestHandler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;



/**
 * author: tisnling
 * created on: 2021/4/19 16:10
 * description: 网络请求拦截者
 */
public class NetInterceptor implements Interceptor {
    private final RequestHandler handler;
    public NetInterceptor() {
        this(DEFAULT);
    }

    public NetInterceptor(RequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
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


    public static final RequestHandler DEFAULT = new RequestHandler(){
        @Override
        public Request onBeforeRequest(Request request, Interceptor.Chain chain) {
            return request.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("charset", "UTF-8")
                    .build();
        }
        @Override
        public Response onAfterRequest(Response response, Interceptor.Chain chain) throws IOException {
            return response;
        }
    };
}
