package com.zgw.qgb.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zgw.qgb.helper.DebugHelper;
import com.zgw.qgb.helper.Utils;
import com.zgw.qgb.net.interceptors.CacheInterceptor;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Tsinling on 2017/10/18 13:18.
 * description:
 */

public abstract class OkHttpConfig {

    public abstract void configHttps(OkHttpClient.Builder builder);

    public Gson configGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }


    public static final OkHttpConfig DEFAULT_CONFIG = new OkHttpConfig() {
        @Override
        public void configHttps(OkHttpClient.Builder builder) {
        }
    };
    /**
     *  有okhttp Cache缓存策略 的配置
     */
    public static final OkHttpConfig CACHE_CONFIG = new OkHttpConfig() {
        @Override
        public void configHttps(OkHttpClient.Builder builder) {
            Cache cache = new Cache(new File(Utils.getContext().getCacheDir(), "httpCache"),
                    1024 * 1024 * 100);

            builder.cache(cache)
                    .addNetworkInterceptor(new CacheInterceptor());
        }
    };
}
