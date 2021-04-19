package com.zgw.qgb.net;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;



/**
 * author: tisnling
 * created on: 2021/4/19 16:11
 * description: okhttp请求前以及请求后的处理.
 */
public interface RequestHandler {

    Request onBeforeRequest(Request request, Interceptor.Chain chain);

    Response onAfterRequest(Response response, Interceptor.Chain chain) throws IOException;

}
