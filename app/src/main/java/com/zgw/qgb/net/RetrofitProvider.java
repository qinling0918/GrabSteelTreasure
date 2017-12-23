package com.zgw.qgb.net;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.zgw.qgb.App;
import com.zgw.qgb.Constant;
import com.zgw.qgb.R;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.helper.InputHelper;
import com.zgw.qgb.helper.utils.EmptyUtils;
import com.zgw.qgb.net.converters.StringConverterFactory;
import com.zgw.qgb.net.progressmanager.ProgressManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static com.zgw.qgb.net.OkHttpConfig.DEFAULT_CONFIG;

/**
 * Created by Tsinling on 2017/10/18 9:39.
 * description:
 */

public class RetrofitProvider {
    public static String baseUrl = App.getInstance().isDebug()
            ? Constant.BaseUrl_local
            : Constant.BaseUrl;

    private static Map<String, OkHttpConfig> configMap = new HashMap<>();
    private static Map<String, Retrofit> retrofitMap = new HashMap<>();
    private static Map<String, OkHttpClient> clientMap = new HashMap<>();

    private static OkHttpClient provideOkHttpClient(String baseUrl, OkHttpConfig config) {
        checkBaseUrl(baseUrl);

        if (clientMap.get(baseUrl) != null) {
            return clientMap.get(baseUrl);
        }

        OkHttpClient.Builder builder = provideOkHttp().newBuilder();

        if (null != config){
            config.configHttps(new OkHttpClient.Builder());
        }

        if (App.getInstance().isDebug()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        OkHttpClient client = builder.build();
        clientMap.put(baseUrl, client);
        configMap.put(baseUrl, config);

        return client;
    }



    private static Retrofit provideRetrofit(String baseUrl, OkHttpConfig config) {
        checkBaseUrl(baseUrl);

        if (retrofitMap.get(baseUrl) != null) {
            return retrofitMap.get(baseUrl);
        }

        if (config == null) {
            config = configMap.get(baseUrl);
            if (config == null) {
                config = DEFAULT_CONFIG;
            }
        }
        Gson gson = config.configGson();
        gson = EmptyUtils.isEmpty(gson) ?
                DEFAULT_CONFIG.configGson() : gson ;
        
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(provideOkHttpClient(baseUrl, config))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(new StringConverterFactory(gson));

        Retrofit retrofit = builder.build();
        retrofitMap.put(baseUrl, retrofit);
        configMap.put(baseUrl, config);

        return retrofit;
    }

    /**
     *
     * @param context
     * @param url
     * @return downloadId
     */
    public static long downloadFile(@NonNull Context context, @NonNull String url) {
        if (InputHelper.isEmpty(url)) return -1;
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        File direct = new File(Environment.getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name));
        if (!direct.isDirectory() || !direct.exists()) {
            boolean isCreated = direct.mkdirs();
            if (!isCreated) {
                ((IView)context).showMessage(R.string.error,R.string.create_directory_error);
                //Toast.makeText(App.getInstance(), "Unable to create directory to download file", Toast.LENGTH_SHORT).show();
                return -1;
            }
        }
        String fileName = new File(url).getName();

        request.setDestinationInExternalPublicDir(context.getString(R.string.app_name), fileName);
        request.setTitle(fileName);
        request.setDescription(context.getString(R.string.downloading_file));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        return downloadManager.enqueue(request);




    }

    /**
     *
     * @param context
     * @param downloadId
     * @return 根据downloadId  获取文件路径
     */
    private static String findFileByDownloadId(@NonNull Context context, long downloadId) {
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = dm.query(query);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
            }
            c.close();
        }
        return "";
    }
    /**
     *
     * @param context
     * @param downloadId
     * @return
     *      STATUS_PENDING
     *      STATUS_PAUSED
     *      STATUS_RUNNING
     *      STATUS_SUCCESSFUL
     *      STATUS_FAILED
     */
    private static int getFileDownloadStatusById(@NonNull Context context, long downloadId) {
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = dm.query(query);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            }
            c.close();
        }
         return DownloadManager.STATUS_FAILED;

    }

    private static void checkBaseUrl(String baseUrl) {
        if (InputHelper.isEmpty(baseUrl)) {
            throw new IllegalStateException("baseUrl can not be null");
        }
    }



    public static <S> S getService(Class<S> service) {
        return provideRetrofit(baseUrl,null).create(service);
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


    public static void clearCache() {
        retrofitMap.clear();
        clientMap.clear();
    }

    public static Map<String, Retrofit> getRetrofitMap() {
        return retrofitMap;
    }

    public static Map<String, OkHttpClient> getClientMap() {
        return clientMap;
    }

    public static OkHttpClient provideOkHttp() {
        OkHttpClient.Builder builder = ProgressManager.getInstance().with(new OkHttpClient.Builder());
        return builder.build();
    }
}
