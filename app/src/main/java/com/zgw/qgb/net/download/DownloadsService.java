package com.zgw.qgb.net.download;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.zgw.qgb.helper.utils.EmptyUtils;
import com.zgw.qgb.helper.utils.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 专门用来下载大文件的服务  支持暂停,取消,失败,成功,下载中回调监听.  另外还有下载时显示在通知栏
 * 此处与ProgressManager连接,可以在其他位置根据url 来监听进度(onProgress与onError)
 */
public class DownloadsService extends Service implements DownloadListener {
    private Map<String, DownloadTask> downloadTaskMap = new HashMap<>();
    private Map<String, File> fileMap = new HashMap<>();
    private Map<String, DownloadListener> listenerMap = new HashMap<>();
    //private Map<String, PendingIntent> pendingIntentMap = new HashMap<>();
    private String[] downloadUrlArr;

    //private DownloadTask downloadTask;
    //private String downloadUrl;
    //private File file;
    /**
     * 是否显示 Notification 默认显示
     */
    private PendingIntent mPendingIntent ;

    @Override
    public void onDestroy() {
        unregisterOnDownloadListeners();
        super.onDestroy();
    }

    public void unregisterOnDownloadListeners() {
        setOnDownloadListener(null);

        for (String key : listenerMap.keySet()) {
            DownloadListener listener = listenerMap.get(key);
            listener = null;
        }

        listenerMap.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    /**
     * 为了要让DownloadService可以和活动进行通信，我们创建了一个DownloadBinder对象
     */
    public class DownloadBinder extends Binder {
        public DownloadsService getService() {
            // Return this instance of DownloadService so clients can call public methods
            return DownloadsService.this;
        }
    }

    /**
     * 设置PendingIntent
     *
     * @param pendingIntent
     */
    public void setPendingIntent(PendingIntent pendingIntent) {
        //pendingIntentMap.put(url, pendingIntent);
        mPendingIntent = pendingIntent;

    }


    /**
     * 开始下载
     *
     * @param url
     */
    public void startDownload(String... url) {
        startDownloadWithPath(null, null, url);
    }



    public void startDownloadWithPath(String mfilePath, String mfileName, String... downloadUrl) {
        downloadUrlArr = downloadUrl;
        for (String url : downloadUrl) {
            startDownload(mfilePath, mfileName, url);
        }


    }

   /* private void showstartDownloadNotification() {

        if (show_notification) {
            if (downloadTaskMap.size() == 1) {
                startForeground(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
            } else {
                getNotificationManager().notify(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
            }
        }
    }*/

    private void startDownload(String mfilePath, String mfileName, String url) {
        if (!downloadTaskMap.containsKey(url)) {
            DownloadTask downloadTask = new DownloadTask(DownloadsService.this);
            File file = FileUtils.getFile(url, mfilePath, mfileName);
            downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new DownloadInfo(file, url));
            downloadTaskMap.put(url, downloadTask);
            fileMap.put(url, file);
        }
    }

    /**
     * 暂停下载
     */
    public void pauseDownload(String... downloadUrl) {
        if (EmptyUtils.isEmpty(downloadUrl)){
            downloadUrl = downloadUrlArr;
        }

        for (String url : downloadUrl) {
            pauseDownload(downloadUrl);
        }
    }




    public void pauseDownload(String url) {
        if(downloadTaskMap.containsKey(url)){
            DownloadTask downloadTask = downloadTaskMap.get(url);
            if(downloadTask != null){
                downloadTask.pauseDownload();
            }
        }
    }
    /**
     * 取消下载
     */
    public void cancelDownload(String... downloadUrl) {
        if (EmptyUtils.isEmpty(downloadUrl)){
            downloadUrl = downloadUrlArr;
        }

        for (String url : downloadUrl) {
            cancelDownload(url);
        }

    }

    public void cancelDownload(String url) {
        if(downloadTaskMap.containsKey(url)){
            DownloadTask downloadTask = downloadTaskMap.get(url);
            if(downloadTask != null){
                downloadTask.cancelDownload();
            }
        }else {
            deleteFileAfterCancel(url);
        }

    }


    //取消下载时需要将文件删除，并将通知关闭
    public void deleteFileAfterCancel(String url) {
        if (url != null && fileMap.containsKey(url)) {
            File file = fileMap.get(url);
            if (null != file)
            FileUtils.deleteFile(file);
        }
    }







    /**
     * 使用 下载链接的url 的哈希值作为 notification 的id
     * @return
     */
    protected int getNotificationId(String url) {
        return url.hashCode();
    }






    protected void removeTask(String url) {
        if (downloadTaskMap.containsKey(url)) {
            DownloadTask downloadTask = downloadTaskMap.get(url);
            if(downloadTask != null){
                downloadTask = null;
            }
            downloadTaskMap.remove(url);
        }
    }


    /***************           下载各种状态             *********************************/

    /**
     * 构建了一个用于显示下载进度的通知
     *
     * @param progress
     */
    @Override
    public void onProgress(String url, int progress,long contentLength, long currentBytes) {
        if (null != onDownloadListener) {
            onDownloadListener.onProgress(url, progress,contentLength,currentBytes);
        }

        if (listenerMap.containsKey(url)) {
            DownloadListener listener = listenerMap.get(url);
            if (null != listener) {
                listener.onProgress(url, progress,contentLength,currentBytes);
            }
        }
    }


    /**
     * 观察小米下载,是在不同状态都有一个通知,多个下载任务进行时,同在下载中则显示一个
     * 有结束的则又开了一个通知,当全部结束又都整合到一个通知上,只是下载数量的变化
     */
    /**
     * 创建了一个新的通知用于告诉用户下载成功啦
     */
    @Override
    public void onSuccess(String url, File file) {
        removeTask(url);

        if (null != onDownloadListener) {
            onDownloadListener.onSuccess(url, file);
        }
        if (listenerMap.containsKey(url)) {
            DownloadListener listener = listenerMap.get(url);
            if (null != listener) {
                listener.onSuccess(url, file);
            }
        }
    }


    /**
     * 用户下载失败
     */
    @Override
    public void onFailed(String url, int errorCode, String errorMsg) {
        removeTask(url);

        if (null != onDownloadListener) {
            onDownloadListener.onFailed(url, errorCode, errorMsg);
        }

        if (listenerMap.containsKey(url)) {
            DownloadListener listener = listenerMap.get(url);
            if (null != listener) {
                listener.onFailed(url, errorCode, errorMsg);
            }
        }
        //下载失败时，将前台服务通知关闭，并创建一个下载失败的通知
        //Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_SHORT).show();
    }

    /**
     * 用户暂停时,仅仅停止了下载任务,没有对notification 做处理,
     * 在downloadListener 的onpause 里面设置pendingintent 不会有效果
     */
    @Override
    public void onPaused(String url, File file) {
        removeTask(url);

        if (null != onDownloadListener) {
            onDownloadListener.onPaused(url, file);
        }
        if (listenerMap.containsKey(url)) {
            DownloadListener listener = listenerMap.get(url);
            if (null != listener) {
                listener.onPaused(url, file);
            }
        }
    }

    /**
     * 用户取消
     */
    @Override
    public void onCanceled(String url, File file) {
        removeTask(url);

        FileUtils.deleteFile(file);
        fileMap.remove(url);

        if (null != onDownloadListener) {
            onDownloadListener.onCanceled(url, file);
        }

        if (listenerMap.containsKey(url)) {
            DownloadListener listener = listenerMap.get(url);
            if (null != listener) {
                listener.onCanceled(url, file);
            }
        }

        /*if (show_notification) {
            //取消下载，将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
        }*/
        //Toast.makeText(DownloadService.this,"Download Canceled",Toast.LENGTH_SHORT).show();
    }


    /**
     * 更新进度的回调接口
     */
    private DownloadListener onDownloadListener;

    /**
     * 注册回调接口的方法，供外部调用
     *
     * @param listener
     */
    public void setOnDownloadListener(DownloadListener listener) {
        onDownloadListener = listener;
    }

    public void setOnDownloadListener(String url, DownloadListener listener) {
        listenerMap.put(url,listener);
        //onDownloadListener = listener;
    }

}
