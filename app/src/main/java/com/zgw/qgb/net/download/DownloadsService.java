package com.zgw.qgb.net.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Utils;
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
    private static final String NOTIFICATION_GROUP = "com.zgw.qgb.notifications";

    protected Map<String, DownloadTask> downloadTaskMap = new HashMap<>();
    protected Map<String, File> fileMap = new HashMap<>();
    protected Map<String, DownloadListener> listenerMap = new HashMap<>();
    protected String[] downloadUrlArr;

    /**
     * 是否显示 Notification 默认显示
     */
    protected PendingIntent mPendingIntent;


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
            return DownloadsService.this;
        }
    }

    /**
     * 设置PendingIntent
     *
     * @param pendingIntent
     */
    public void setPendingIntent(PendingIntent pendingIntent) {
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


    protected void startDownload(String mfilePath, String mfileName, String url) {
        if (!downloadTaskMap.containsKey(url)) {
            DownloadTask downloadTask = new DownloadTask(DownloadsService.this);
            //DownloadTask downloadTask = new DownloadTask(listener);
            File file = FileUtils.getFile(url, mfilePath, mfileName);
            downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new DownloadInfo(file, url));
            downloadTaskMap.put(url, downloadTask);
            fileMap.put(url, file);
            showstartDownloadNotification(url);


        }
    }

    private void showstartDownloadNotification(String url) {
        if (downloadTaskMap.size() == 1) {
            startForeground(getNotificationId(url), getNotification("正在下载" + downloadTaskMap.size(), -1));
        } else {
            getNotificationManager().notify(getNotificationId(url), getNotification(downloadTaskMap.size() + "个任务正在下载", -1));
        }
    }

    /**
     * 暂停下载
     */
    public void pauseDownload(String... downloadUrl) {
        if (EmptyUtils.isEmpty(downloadUrl)) {
            downloadUrl = downloadUrlArr;
        }

        for (String url : downloadUrl) {
            pauseDownload(url);
        }
    }


    public void pauseDownload(String url) {
        if (downloadTaskMap.containsKey(url)) {
            DownloadTask downloadTask = downloadTaskMap.get(url);
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }
    }

    /**
     * 取消下载
     */
    public void cancelDownload(String... downloadUrl) {
        if (EmptyUtils.isEmpty(downloadUrl)) {
            downloadUrl = downloadUrlArr;
        }

        for (String url : downloadUrl) {
            cancelDownload(url);
        }

    }

    public void cancelDownload(String url) {
        if (downloadTaskMap.containsKey(url)) {
            DownloadTask downloadTask = downloadTaskMap.get(url);
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            }
        } else {
            deleteFileAfterCancel(url);
        }

    }


    //取消下载时需要将文件删除，并将通知关闭
    private void deleteFileAfterCancel(String url) {
        if (url != null && fileMap.containsKey(url)) {
            File file = fileMap.get(url);
            if (null != file){
                FileUtils.deleteFile(file);
            }


        }
    }


    /**
     * 获取NotificationManager的实例，对通知进行管理
     *
     * @return
     */
    private NotificationManager getNotificationManager() {
        return (NotificationManager) Utils.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * @param title
     * @param progress
     * @return
     */
    private Notification getNotification(String title, int progress) {
        /*Intent intent=new Intent(this,MainActivity.class);
        //PendingIntent是等待的Intent,这是跳转到一个Activity组件。当用户点击通知时，会跳转到MainActivity
        PendingIntent pi= PendingIntent.getActivity(this,0,intent,0);*/
        /**
         * 几乎Android系统的每一个版本都会对通知这部分功能进行获多或少的修改，API不稳定性问题在通知上面凸显的尤其严重。
         * 解决方案是：用support库中提供的兼容API。support-v4库中提供了一个NotificationCompat类，使用它可以保证我们的
         * 程序在所有的Android系统版本中都能正常工作。
         */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Utils.getContext(), NOTIFICATION_GROUP);
        //设置通知的小图标
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //设置通知的大图标，当下拉系统状态栏时，就可以看到设置的大图标
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        //当通知被点击的时候，跳转到MainActivity中

        if (null != mPendingIntent) {
            builder.setContentIntent(mPendingIntent);
        }

        //设置通知的标题
        builder.setContentTitle(title);
        if (progress > 0) {
            //当progress大于或等于0时，才需要显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }

        builder.setStyle(new NotificationCompat.BigTextStyle()
                .setSummaryText(title))
                .setGroupSummary(true); //这句话必须和上面那句一起调用，否则不起作用
        return builder.build();
    }


    /**
     * 使用 下载链接的url 的哈希值作为 notification 的id
     *
     * @return
     */

    protected int getNotificationId() {
        return  getNotificationId(NOTIFICATION_GROUP);
    }

    protected int getNotificationId(String url) {
        return  url.hashCode();
    }


    protected void removeTask(String url) {
        if (downloadTaskMap.containsKey(url)) {
            DownloadTask downloadTask = downloadTaskMap.get(url);
            if (downloadTask != null) {
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
    public void onProgress(String url, int progress, long contentLength, long currentBytes) {
        if (null != onDownloadListener) {
            onDownloadListener.onProgress(url, progress, contentLength, currentBytes);
        }

        if (listenerMap.containsKey(url)) {
            DownloadListener listener = listenerMap.get(url);
            if (null != listener) {
                listener.onProgress(url, progress, contentLength, currentBytes);
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


        showNotification(url, getNotification("下载成功", -1));


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


        showNotification(url, getNotification("全部失败下载", -1));
    }

    /**
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


        showNotification(url, getNotification("全部暂停下载", -1));


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


        showNotification(url, getNotification("全部取消下载", -1));


        /*if (show_notification) {
            //取消下载，将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
        }*/
        //Toast.makeText(DownloadService.this,"Download Canceled",Toast.LENGTH_SHORT).show();
    }

    private void showNotification(String url, Notification status) {
        if (downloadTaskMap.size() > 0) {
            getNotificationManager().notify(getNotificationId(), getNotification("正在下载" + downloadTaskMap.size(), -1));
        } else {
            stopForeground(true);
            getNotificationManager().notify(getNotificationId(), status);
        }
    }


    /**
     * 更新进度的回调接口
     */
    protected DownloadListener onDownloadListener;

    /**
     * 注册回调接口的方法，供外部调用
     *
     * @param listener
     */
    public void setOnDownloadListener(DownloadListener listener) {
        onDownloadListener = listener;
    }

    public void setOnDownloadListener(String url, DownloadListener listener) {
        listenerMap.put(url, listener);
        //onDownloadListener = listener;
    }

}
