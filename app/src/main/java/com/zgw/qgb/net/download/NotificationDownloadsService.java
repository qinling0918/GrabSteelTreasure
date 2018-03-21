package com.zgw.qgb.net.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.zgw.qgb.App;
import com.zgw.qgb.R;

import java.io.File;

/**
 * 专门用来下载大文件的服务  支持暂停,取消,失败,成功,下载中回调监听.  另外还有下载时显示在通知栏
 * 此处与ProgressManager连接,可以在其他位置根据url 来监听进度(onProgress与onError)
 */
public class NotificationDownloadsService extends DownloadsService  {
    private static final String NOTIFICATION_GROUP = "com.zgw.qgb.notifications";

    /**
     * 是否显示 多个Notification 默认不显示,多个下载任务只显示一个notification
     */
    private boolean showGroupNotification = true;
    private PendingIntent mPendingIntent ;

    @Override
    public void startDownload(String mfilePath, String mfileName, String downloadUrl) {
        super.startDownload(mfilePath, mfileName, downloadUrl);
        showstartDownloadNotification(downloadUrl);
    }

    private void showstartDownloadNotification(String url) {
        if (showGroupNotification) {
            if (downloadTaskMap.size() == 1) {
                startForeground(getNotificationId(url), getNotification("正在下载" + downloadTaskMap.size(), -1));
            } else {
                getNotificationManager().notify(getNotificationId(url), getNotification(downloadTaskMap.size()+"个任务正在下载", -1));
            }
        }else{
                startForeground(getNotificationId(url), getNotification("下载中" + downloadTaskMap.size(), -1));

        }
    }

    @Override
    protected int getNotificationId(String url) {
        return showGroupNotification
                ? super.getNotificationId(NOTIFICATION_GROUP)
                : super.getNotificationId(url);
    }

    /**
     * 获取NotificationManager的实例，对通知进行管理
     *
     * @return
     */
    private NotificationManager getNotificationManager() {
        return (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getContext(), NOTIFICATION_GROUP);
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
                .setGroup(NOTIFICATION_GROUP) //设置类组key，说明此条通知归属于哪一个归类
                .setGroupSummary(true); //这句话必须和上面那句一起调用，否则不起作用
        return builder.build();
    }

/*if (show_notification){
        getNotificationManager().cancel(getNotificationId());
        stopForeground(true);
        //Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
    }*/
    private void showNotification(String url,String downloadingstr, String statusStr) {
            if (downloadTaskMap.size() > 0) {
                getNotificationManager().notify(getNotificationId(url), getNotification(downloadingstr + downloadTaskMap.size(), -1));
            } else {
                stopForeground(true);
                getNotificationManager().notify(getNotificationId(url), getNotification(statusStr, -1));
            }

    }

 


    private void pauseDownloadNotification(String url) {
            if (downloadTaskMap.size() > 0) {
                getNotificationManager().notify(getNotificationId(url), getNotification("正在下载" + downloadTaskMap.size(), -1));
            } else {
                stopForeground(true);
                getNotificationManager().notify(getNotificationId(url), getNotification("全部暂停下载", -1));
            }
    }
    private void cancelDownloadNotification() {
            if(downloadTaskMap.size() > 0){
                getNotificationManager().notify(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
            } else {
                stopForeground(true);
                getNotificationManager().notify(1, getNotification("全部取消下载", -1));
            }
    }
    private void successDownloadNotification() {
            if(downloadTaskMap.size() > 0){
                getNotificationManager().notify(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
            } else {
                stopForeground(true);
                getNotificationManager().notify(1, getNotification("下载成功", -1));
            }
    }
    private void failedDownloadNotification() {
            if(downloadTaskMap.size() > 0){
                getNotificationManager().notify(1, getNotification("有任务失败,下载中" + downloadTaskMap.size(), -1));
            } else {
                stopForeground(true);
                getNotificationManager().notify(1, getNotification("下载成功", -1));
            }
    }

    public boolean isDownloading(String url){
        if(downloadTaskMap.containsKey(url)){
            return true;
        }

        return  false;
    }



    /***************           下载各种状态             *********************************/
/*
    *//**
     * 构建了一个用于显示下载进度的通知
     *
     * @param progress
     *//*
    @Override
    public void onProgress(String url, int progress,long contentLength, long currentBytes) {
        if (null != onDownloadListener) {
            onDownloadListener.onProgress(url, progress,contentLength,currentBytes);
        }

        //NotificationManager的notify()可以让通知显示出来。
        //notify(),接收两个参数，第一个参数是id:每个通知所指定的id都是不同的。第二个参数是Notification对象。

      *//*  if (show_notification) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }*//*


    }*/

    @Override
    public void onProgress(String url, int progress, long contentLength, long currentBytes) {
        super.onProgress(url, progress, contentLength, currentBytes);
        if (showGroupNotification){
            getNotificationManager().notify(getNotificationId(url), getNotification("Downloading...", progress));
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
        super.onSuccess(url, file);
        //successDownloadNotification();
        showNotification(url,"成功下载","下载完成");
        /*if (showGroupNotification){
            showNotification(url);
        }else{
            stopForeground(true);
            getNotificationManager().notify(getNotificationId(url),getNotification("Download Success",-1));
        }*/



    }


    /**
     * 用户下载失败
     */
    @Override
    public void onFailed(String url, int errorCode, String errorMsg) {
        super.onFailed(url, errorCode,errorMsg);

        //下载失败时，将前台服务通知关闭，并创建一个下载失败的通知
        showNotification(url,"有任务失败,下载中" ,"任务完成");


 /*       stopForeground(true);
        getNotificationManager().notify(getNotificationId(url),getNotification("Download Failed",-1));*/
        //failedDownloadNotification();
        //Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_SHORT).show();
    }

    /**
     * 用户暂停时,仅仅停止了下载任务,没有对notification 做处理,
     * 在downloadListener 的onpause 里面设置pendingintent 不会有效果
     */
    @Override
    public void onPaused(String url, File file) {
        super.onPaused(url, file);
        //pauseDownloadNotification();
        //showNotification(url,"下载中" ,"全部暂停下载");
    }

    /**
     * 用户取消
     */
    @Override
    public void onCanceled(String url, File file) {
        super.onCanceled(url, file);
        //cancelDownloadNotification();
        showNotification(url,"下载中" ,"全部取消下载");
/*

        getNotificationManager().cancel(getNotificationId(url));
        stopForeground(true);*/
    }


}
