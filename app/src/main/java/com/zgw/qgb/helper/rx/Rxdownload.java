package com.zgw.qgb.helper.rx;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.zgw.qgb.App;
import com.zgw.qgb.R;
import com.zgw.qgb.helper.utils.NetUtils;
import com.zgw.qgb.net.RetrofitProvider;
import com.zgw.qgb.net.progressmanager.body.ProgressInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public final class Rxdownload {

    private Rxdownload() {
        throw new AssertionError("No instances.");
    }

    private interface DownLoadService {
        @Streaming
        @GET
        Observable<ResponseBody> download(@Url String url);
    }

    public static Observable<String> download(String mDownloadUrl) {
        return RetrofitProvider.getService(DownLoadService.class)
                .download(mDownloadUrl)
                //.compose(RxProgress.bindToLifecycle(getContext(),"jiazaihong "))
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .doOnSubscribe(disposable -> checkNet())
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnNext(responseBody -> saveFile(responseBody,mDownloadUrl))
                .flatMap(responseBody -> Observable.just(responseBody.contentLength()+""))

        ;
    }

    private static void checkNet() {
        if (!NetUtils.isConnected(App.getContext())){
            Toast.makeText(App.getContext(),App.getContext().getText(R.string.please_check_network),Toast.LENGTH_SHORT).show();
        }
    }


    private static void saveFile(ResponseBody responseBody, String mDownloadUrl) {
        String destFileDir = Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name);
        String fileName = new File(mDownloadUrl).getName();
        saveFile(responseBody.byteStream(),destFileDir,fileName);
    }

    private static void getFilePath(ResponseBody responseBody, String mDownloadUrl) {
        String destFileDir = Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name);
        String fileName = new File(mDownloadUrl).getName();
        saveFile(responseBody.byteStream(),destFileDir,fileName);
    }


    private static void saveFile(InputStream is, String destFileDir, String fileName) {

        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(dir, fileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            //onCompleted();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e("saveFile", e.getMessage());
            }
        }


    }





}
