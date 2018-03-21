package com.zgw.qgb.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;

/**
 * Created by Tsinling on 2017/10/18 13:18.
 * description:
 */

public abstract class OkHttpConfig {

    public abstract void  configHttps(OkHttpClient.Builder builder);

    public Gson configGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }


    public static final OkHttpConfig DEFAULT_CONFIG = new OkHttpConfig() {
        @Override
        public void  configHttps(OkHttpClient.Builder builder) {
        }
    };


}
