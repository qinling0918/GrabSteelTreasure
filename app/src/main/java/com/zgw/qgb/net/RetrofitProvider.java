package com.zgw.qgb.net;

import com.google.gson.Gson;
import com.zgw.qgb.Constant;
import com.zgw.qgb.helper.DebugHelper;
import com.zgw.qgb.helper.InputHelper;
import com.zgw.qgb.helper.Utils;
import com.zgw.qgb.helper.utils.EmptyUtils;
import com.zgw.qgb.net.converters.StringConverterFactory;
import com.zgw.qgb.net.interceptors.NetInterceptor;
import com.zgw.qgb.net.progressmanager.ProgressManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

import static com.zgw.qgb.net.OkHttpConfig.CACHE_CONFIG;
import static com.zgw.qgb.net.OkHttpConfig.DEFAULT_CONFIG;

/**
 * Created by Tsinling on 2017/10/18 9:39.
 * description: Retrofit 提供方
 */

public class RetrofitProvider {

    public static String baseUrl = Utils.getInstance().isDebug()
            ? Constant.BaseUrl_debug
            : Constant.BaseUrl;
    private static OkHttpClient okHttpClient;

    public static OkHttpClient provideOkHttpWithConfig(OkHttpConfig config) {
        provideOkHttp();
        return configHttps(config);
    }

    public static OkHttpClient provideOkHttp() {
        if (okHttpClient == null ) {
            OkHttpClient.Builder client = ProgressManager.getInstance().with(new OkHttpClient.Builder());
            client.addInterceptor(new NetInterceptor());
            if (Utils.getInstance().isDebug()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                client.addInterceptor(loggingInterceptor);
            }
            okHttpClient = client.build();
        }
        return okHttpClient;
    }


    private static OkHttpClient configHttps(OkHttpConfig config) {
        if (null != config){
            OkHttpClient.Builder builder = okHttpClient.newBuilder();
            config.configHttps(builder);
            return builder.build();
        }
        return okHttpClient;
    }


    private static Retrofit provideRetrofit(String baseUrl, OkHttpConfig config) {
        checkBaseUrl(baseUrl);

        config = config == null ?DEFAULT_CONFIG :config;

        Gson gson = config.configGson();
        gson = EmptyUtils.isEmpty(gson) ?
                DEFAULT_CONFIG.configGson() : gson ;
        
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(provideOkHttpWithConfig(config))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(new StringConverterFactory(gson));

        return builder.build();
    }

    private static void checkBaseUrl(String baseUrl) {
        if (InputHelper.isEmpty(baseUrl)) {
            throw new IllegalStateException("baseUrl can not be null");
        }
    }


    public static <S> S getService(Class<S> service) {
        return provideRetrofit(baseUrl,null).create(service);
    }
    public static <S> S getServiceWithCache(Class<S> service) {
        return provideRetrofit(baseUrl,CACHE_CONFIG).create(service);
    }
    public static <S> S getService(Class<S> service, OkHttpConfig config) {
        return provideRetrofit(baseUrl,config).create(service);
    }

    public static <S> S getService(String baseUrl, Class<S> service) {
        return provideRetrofit(baseUrl,null).create(service);
    }

    public <S> S getService(String baseUrl, OkHttpConfig config, Class<S> service) {
        return provideRetrofit(baseUrl,config).create(service);
    }


}
