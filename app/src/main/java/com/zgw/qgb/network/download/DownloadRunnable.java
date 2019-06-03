package com.zgw.qgb.network.download;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by qinling on 2018/12/1 12:48
 * Description:
 */
public class DownloadRunnable implements Runnable {
    private String url;
    private long startIndex;
    private long endIndex;
    private int threadNum;
    private DownLoadInfoManager manager;
    private Call call;

    public DownloadRunnable(String url, long startIndex, long endIndex, int threadNum, DownLoadInfoManager manager) {
        this.url = url;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.threadNum = threadNum;
        this.manager = manager;
    }

    private Request getRequest(long startIndex, long endIndex) {
        return new Request.Builder().header("RANGE", "bytes=" + startIndex + "-" + endIndex)
                .url(url)
                .build();
    }

    @Override
    public void run() {
        /*
        okhttp 已经有了连接池， 多余的将会添加到连接池的队列中
        默认单个链接 5个，同时最大请求为64.若需要修改，则可以直接调用
        client.dispatcher().setMaxRequestsPerHost(5);
        client.dispatcher().setMaxRequests(64);*/
        Request request = getRequest(startIndex, endIndex);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS).
                        build();
        try {
             call = client.newCall(request);
            Response response = call.execute();
            if (response != null && response.isSuccessful()) {
                InputStream is = response.body().byteStream();
                manager.writeToCacheFile(is,startIndex,endIndex);
                // printlnHeaders(response);
             /*   long contentLength = Long.valueOf(response.header("Content-Length"));
                String acceptRanges = response.header("Accept-Ranges");*/
                // bytes 表示支持， none 不支持
//                supportRanage = "bytes".equals(acceptRanges);
                //   if (!supportRanage) {
                // sendErrorMessage("服务器不支持断点续传功能");
                //   }
                response.close();
            }
        } catch (IOException e) {
            String errorMsg = TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "服务器连接失败，请重试！";
            e.printStackTrace();
        }
    }

    public void cancelCall() {
        call.cancel();
    }
}
