package com.zgw.qgb.net.interceptors;

import java.io.IOException;
import java.io.InterruptedIOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 *  网络重连机制
 */
public class RetryInterceptor implements Interceptor {
    public int executionCount;      // 最大重试次数
    private long retryInterval;     // 重试的间隔

    RetryInterceptor(Builder builder) {
        this.executionCount = builder.executionCount;
        this.retryInterval = builder.retryInterval;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        int retryNum = 0;
        while ((response == null || !response.isSuccessful()) && retryNum <= executionCount) {
            final long nextInterval = getRetryInterval();
            try {
                Thread.sleep(nextInterval);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            }
            retryNum++;
            response = chain.proceed(request);
        }
        return response;
    }

    /**
     * retry间隔时间
     */
    public long getRetryInterval() {
        return this.retryInterval;
    }

    public static final class Builder {
        private int executionCount;
        private long retryInterval;

        public Builder() {
            executionCount = 3;
            retryInterval = 1000;
        }

        public Builder executionCount(int executionCount) {
            this.executionCount = executionCount;
            return this;
        }

        public Builder retryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        public RetryInterceptor build() {
            return new RetryInterceptor(this);
        }
    }
}
