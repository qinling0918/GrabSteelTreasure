package com.zgw.qgb.net.download;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zgw.qgb.App;
import com.zgw.qgb.R;
import com.zgw.qgb.helper.utils.FileUtils;
import com.zgw.qgb.net.RetrofitProvider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.zgw.qgb.net.download.DownloadInfo.Status.CANCELED;
import static com.zgw.qgb.net.download.DownloadInfo.Status.FAILED;
import static com.zgw.qgb.net.download.DownloadInfo.Status.PAUSED;
import static com.zgw.qgb.net.download.DownloadInfo.Status.SUCCESS;


/**
 * Created by Tsinling on 2017/12/21 17:09.
 *
 * DownloadInfo 在执行AsyncTask时需要传入的参数，可用于在后台任务中使用。
 * Integer 后台任务执行时，如果需要在界面上显示当前的进度，则使用这里指定的泛型作为进度单位。
 * Integer 当任务执行完毕后，如果需要对结果进行返回，则使用这里指定的泛型作为返回值类型。
 */
public class DownloadTask extends AsyncTask<DownloadInfo,DownloadInfo,DownloadInfo> {

    private DownloadListener listener;

    private boolean isCanceled=false;

    private boolean isPaused=false;

    private int lastProgress;


    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    /**
     * 这个方法中的所有代码都会在子线程中运行，我们应该在这里处理所有的耗时任务。
     * @param params
     * @return
     */
    @Override
    protected DownloadInfo doInBackground(DownloadInfo... params) {
        InputStream is=null;
        DownloadInfo downloadInfo = params[0];
        downloadInfo.setStatus(FAILED,-1,"未知错误");

        String downloadUrl=downloadInfo.getUrl();
        File file= downloadInfo.getFile();
        if(!FileUtils.createOrExistsFile(file)){
            return downloadInfo.setStatus(FAILED,-1,"无该文件路径");
        }

        long downloadLength= file.length();   //记录已经下载的文件长度

        long contentLength=getContentLength(downloadUrl);
        downloadInfo.setContentLength(contentLength);

        if(contentLength==0){
            return downloadInfo.setStatus(FAILED,-1,"远程文件(服务器端目标文件)不存在");
        }else if(contentLength==downloadLength){
            //已下载字节和文件总字节相等，说明已经下载完成了
            return downloadInfo.setStatus(SUCCESS);
        }

        //OkHttpClient client=new OkHttpClient();

        //提供progressManager  进度支持
        OkHttpClient client= RetrofitProvider.provideOkHttp();
        /**
         * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
         * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
         * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
         * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
         */

        Request request=new Request.Builder()
                .addHeader("RANGE","bytes="+downloadLength+"-")  //断点续传要用到的，指示下载的区间
                .url(downloadUrl)
                .build();
        try {
            Response response=client.newCall(request).execute();
               if(response!=null && response.body()!= null){
                is= response.body().byteStream();

                if ( is == null) {
                    return downloadInfo.setStatus(FAILED,-1,"服务器返回值为空");
                }
                OutputStream os = null;
                try {
                    os = new BufferedOutputStream(new FileOutputStream(file, true));
                    byte[] buf = new byte[1024 << 2];
                    long total=0;
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        if(isCanceled){
                            FileUtils.deleteFile(file);
                            return downloadInfo.setStatus(CANCELED);
                        }else if(isPaused){
                            return downloadInfo.setStatus(PAUSED);
                        }else {
                            total+=len;
                            os.write(buf,0,len);
                            //计算已经下载的百分比
                            int progress=(int)((total+downloadLength)*100/contentLength);
                            //注意：在doInBackground()中是不可以进行UI操作的，如果需要更新UI,比如说反馈当前任务的执行进度，
                            //可以调用publishProgress()方法完成。
                            downloadInfo.setProgress(progress);
                            downloadInfo.setCurrentBytes(file.length());
                            publishProgress(downloadInfo);
                        }
                    }

                    FileUtils.close(response);
                    return downloadInfo.setStatus(SUCCESS);
                } catch (IOException e) {

                    FileUtils.deleteFile(file);
                    e.printStackTrace();
                    return downloadInfo.setStatus(FAILED,-1,"文件写入失败");
                } finally {

                    FileUtils.close(is,os);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(is!=null){
                    is.close();
                }
                if(isCanceled&&file!=null){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return downloadInfo.setStatus(FAILED,-1,"okhttp's execute() IOEXCEPTION");
    }


    /**
     * 当在后台任务中调用了publishProgress(Progress...)方法之后，onProgressUpdate()方法
     * 就会很快被调用，该方法中携带的参数就是在后台任务中传递过来的。在这个方法中可以对UI进行操作，利用参数中的数值就可以
     * 对界面进行相应的更新。
     * @param values
     */
    protected void onProgressUpdate(DownloadInfo...values){
        int progress = values[0].getProgress();
        String url = values[0].getUrl();
        long contentLength = values[0].getContentLength();
        long currentBytes = values[0].getCurrentBytes();
        if(progress>lastProgress){
            listener.onProgress(url, progress, contentLength, currentBytes);
            lastProgress=progress;
        }
    }

    /**
     * 当后台任务执行完毕并通过Return语句进行返回时，这个方法就很快被调用。返回的数据会作为参数
     * 传递到此方法中，可以利用返回的数据来进行一些UI操作。
     * @param info
     */
    @Override
    protected void onPostExecute(DownloadInfo info) {
        DownloadInfo.Status status = info.getStatus();
        String url  = info.getUrl();
        switch (status){
            case SUCCESS:
                listener.onSuccess(url, info.getFile());
                break;
            case FAILED:
                listener.onFailed(url, status.getCode(), status.getMsg());
                break;
            case PAUSED:
                listener.onPaused(url, info.getFile());
                break;
            case CANCELED:
                listener.onCanceled(url, info.getFile());
                break;
            default:
                break;
        }
    }

    public void  pauseDownload(){
        isPaused=true;
    }

    public void cancelDownload(){
        isCanceled=true;
    }

    /**
     * 得到下载内容的大小
     * @param downloadUrl
     * @return
     */
   /* private long getContentLength(String downloadUrl){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(downloadUrl).build();
        try {
            Response response=client.newCall(request).execute();
            if(response!=null&&response.isSuccessful()){
                long contentLength=response.body().contentLength();
                response.body().close();
                return contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  0;
    }*/
    private static long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .method("HEAD", null).build();
        OkHttpClient client=new OkHttpClient();
        try {
            Response response=client.newCall(request).execute();
            if(response!=null&&response.isSuccessful()){
                long contentLength = Long.valueOf(response.header("Content-Length"));
                response.close();
                return contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  0;
    }


    @NonNull
    private static File getFile(String mDownloadUrl, String filePath, String fileName) {
        //String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        filePath = TextUtils.isEmpty(filePath)
                ? Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name)
                :filePath;
        fileName = TextUtils.isEmpty(fileName)
                ?new File(mDownloadUrl).getName()
                :fileName;

        File fileParent = new File(filePath);
        if(!fileParent.exists()){
            fileParent.mkdirs();
        }
        File file = new File(fileParent, fileName );
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}