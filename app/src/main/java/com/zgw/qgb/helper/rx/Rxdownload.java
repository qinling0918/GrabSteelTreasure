package com.zgw.qgb.helper.rx;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zgw.qgb.App;
import com.zgw.qgb.R;
import com.zgw.qgb.helper.utils.NetUtils;
import com.zgw.qgb.net.RetrofitProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public final class Rxdownload {

    private Rxdownload() {
        throw new AssertionError("No instances.");
    }

    private interface DownLoadService {
        @Streaming
        @GET
        Single<ResponseBody> download(@Url String url);

        //downParam下载参数，传下载区间使用
        //url 下载链接
        @Streaming
        @GET
        Single<ResponseBody> download(@Header("RANGE") String downParam, @Url String url);

}

    public static Single<File> download(String mDownloadUrl) {
        return download(mDownloadUrl, null, null);
    }

    public static Single<File> download(String mDownloadUrl, String filePath, String fileName) {
        return RetrofitProvider.getService(DownLoadService.class)
                .download(mDownloadUrl)
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .doOnSubscribe(disposable -> checkNet())
                .observeOn(Schedulers.io()) //指定线程保存文件
                .map(responseBody -> saveFile(responseBody,mDownloadUrl,filePath,fileName));
    }

    public static Single<File> downloadBigFile(String mDownloadUrl,@NonNull final int threadCount, String filePath, String fileName) {
        filePath = TextUtils.isEmpty(filePath)
                ?Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name)
                :filePath;
        fileName = TextUtils.isEmpty(fileName)
                ?new File(mDownloadUrl).getName()
                :fileName;
        String finalFilePath = filePath;
        String finalFileName = fileName;

        if (threadCount < 1) throw new IllegalArgumentException("线程数不能少于一条");
        return RetrofitProvider.getService(DownLoadService.class)
                .download(mDownloadUrl)
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .doOnSubscribe(disposable -> checkNet())
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnSuccess(responseBody -> fileSeparateDownload(responseBody,mDownloadUrl, threadCount, finalFilePath, finalFileName))
                .map(responseBody -> saveFile(responseBody,mDownloadUrl, finalFilePath, finalFileName));
    }
/*    public static Single<File> downloadBigFile(String mDownloadUrl,@NonNull final long start, @NonNull final long end, String filePath, String fileName) {
        String endStr  = end == -1 ? "" : String.valueOf(end);
        return RetrofitProvider.getService(DownLoadService.class)
                .download("bytes=" + start + "-" + endStr, mDownloadUrl)
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .doOnSubscribe(disposable -> checkNet())
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnSuccess(responseBody -> fileSeparateDownload(responseBody, end))
                .map(responseBody -> saveFile(responseBody,mDownloadUrl,filePath,fileName));
    }*/

    private static void fileSeparateDownload(ResponseBody responseBody, String mDownloadUrl, int threadCount, String filePath, String fileName) {
        File mTmpFile = new File(filePath, fileName + ".tmp");
        RandomAccessFile tmpAccessFile = null;
        try {
            tmpAccessFile = new RandomAccessFile(mTmpFile, "rw");
            tmpAccessFile.setLength(responseBody.contentLength());
            responseBody.close();
            long onceLenth = responseBody.contentLength() / threadCount;

            DownLoadService service = RetrofitProvider.getService(DownLoadService.class);

            List<Observable<ResponseBodyAndPoint>> observableList = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                long end = i == threadCount-1 ? responseBody.contentLength() : onceLenth*(i+1);
                long start = onceLenth*i;
                Observable<ResponseBodyAndPoint> observe = (service.download("bytes=" + start + "-" + end , mDownloadUrl))
                        .toObservable()
                        .map(responseBody1 -> new ResponseBodyAndPoint(responseBody1,start,end));
                observableList.add(observe);
            }

            RandomAccessFile finalTmpAccessFile = tmpAccessFile;
            Observable.merge(observableList)
                    .observeOn(Schedulers.io())
                    .doOnNext(responseBody12 -> finalTmpAccessFile.seek(responseBody12.startPoint))

            ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkNet() {
        if (!NetUtils.isConnected(App.getContext())){
            Toast.makeText(App.getContext(),App.getContext().getText(R.string.please_check_network),Toast.LENGTH_SHORT).show();
        }
    }



    private static File saveFile(ResponseBody responseBody,String mDownloadUrl, String destFileDir, String fileName) {
     /*   destFileDir = TextUtils.isEmpty(destFileDir)
                ?Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name)
                :destFileDir;
        fileName = TextUtils.isEmpty(fileName)
                ?new File(mDownloadUrl).getName()
                :fileName;*/
        InputStream is = responseBody.byteStream();

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

        return file;
    }


/*    public Observable download(@NonNull final long start, @NonNull final long end, @NonNull final String url, final File file, final Subscriber subscriber) {
        String str = "";
        if (end == -1) {
            str = "";
        } else {
            str = end + "";
        }
        return RetrofitProvider.getService(DownLoadService.class).download("bytes=" + start + "-" + str, url).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).map(new Func1<ResponseBody, ResponseBody>() {
            @Override
            public ResponseBody call(ResponseBody responseBody) {
                return responseBody;
            }
        }).observeOn(Schedulers.computation()).doOnNext(new Action1<ResponseBody>() {
            @Override
            public void call(ResponseBody responseBody) {
                //第一次请求全部文件长度
                if (end == -1) {
                    try {
                        RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
                        randomFile.setLength(responseBody.contentLength());
                        long one = responseBody.contentLength() / 3;
                        download(0, one, url, file, subscriber).mergeWith(download(one, one * 2, url, file, subscriber)).mergeWith(download(one * 2, responseBody.contentLength(), url, file, subscriber)).subscribe(subscriber);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    FileUtils fileUtils = new FileUtils();
                    fileUtils.writeFile(start, end, responseBody.byteStream(), file);
                }

            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }*/


    private static class ResponseBodyAndPoint {
        ResponseBody responseBody;
        long startPoint;
        long endPoint;

        public ResponseBodyAndPoint(ResponseBody responseBody, long startPoint, long endPoint) {
            this.responseBody = responseBody;
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }
    }
}
