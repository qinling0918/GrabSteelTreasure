package com.zgw.qgb.net.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.zgw.qgb.App;
import com.zgw.qgb.R;
import com.zgw.qgb.helper.utils.FileUtils;

import java.io.File;

/**
 * 专门用来下载大文件的服务  支持暂停,取消,失败,成功,下载中回调监听.  另外还有下载时显示在通知栏
 * 此处与ProgressManager连接,可以在其他位置根据url 来监听进度(onProgress与onError)
 *
 */
public class DownloadService extends Service implements DownloadListener {
    private DownloadTask downloadTask;

    private String downloadUrl;
    private File file;
    private boolean show_notification = true;
    private PendingIntent mPendingIntent ;
    /**
     * 更新进度的回调接口
     */
    private DownloadListener onDownloadListener;
    
    
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    /**
     * 为了要让DownloadService可以和活动进行通信，我们创建了一个DownloadBinder对象
     */
    public class DownloadBinder extends Binder {
        
        /**
         * 设置PendingIntent
         * @param pendingIntent
         */
        public void setPendingIntent(PendingIntent pendingIntent) {
            mPendingIntent = pendingIntent;

        }

        /**
         * 注册回调接口的方法，供外部调用
         * @param listener
         */
        public void setOnDownloadListener(DownloadListener listener) {
            onDownloadListener = listener;
        }

        /**
         * 开始下载
         * @param url
         */
        public void  startDownload(String url){
            startDownload(url,true);
        }

        public void  startDownload(String url ,boolean showNotification){
            startDownload(url,null,null, showNotification);
        }
        public void  startDownload(String mDownloadUrl, String mfilePath, String mfileName, boolean showNotification){
            if(downloadTask==null){
                downloadUrl= mDownloadUrl;
                file = FileUtils.getFile(mDownloadUrl,mfilePath,mfileName);
                downloadTask=new DownloadTask(DownloadService.this);
                //启动下载任务
                downloadTask.execute(new DownloadInfo(file,mDownloadUrl));

                show_notification = showNotification;
                if (show_notification){
                    startForeground(1,getNotification("Downloading...",0));
                    //Toast.makeText(DownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
                }

            }
        }

        /**
         * 暂停下载
         */
        public void pauseDownload(){
            if(downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }

        /**
         * 取消下载
         */
        public void cancelDownload(){
            if(downloadTask!=null){
                downloadTask.cancelDownload();
            }else {
                if(downloadUrl!=null){
                    //取消下载时需要将文件删除，并将通知关闭

                    FileUtils.deleteFile(file);

                    if (show_notification){
                        getNotificationManager().cancel(1);
                        stopForeground(true);
                        //Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }



    }

    /**
     * 获取NotificationManager的实例，对通知进行管理
     * @return
     */
    private NotificationManager getNotificationManager(){
        return (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     *
     * @param title
     * @param progress
     * @return
     */
    private Notification getNotification(String title, int progress){
        /*Intent intent=new Intent(this,MainActivity.class);
        //PendingIntent是等待的Intent,这是跳转到一个Activity组件。当用户点击通知时，会跳转到MainActivity
        PendingIntent pi= PendingIntent.getActivity(this,0,intent,0);*/
        /**
         * 几乎Android系统的每一个版本都会对通知这部分功能进行获多或少的修改，API不稳定性问题在通知上面凸显的尤其严重。
         * 解决方案是：用support库中提供的兼容API。support-v4库中提供了一个NotificationCompat类，使用它可以保证我们的
         * 程序在所有的Android系统版本中都能正常工作。
         */
        NotificationCompat.Builder builder=new NotificationCompat.Builder(App.getContext(),downloadUrl);
        //设置通知的小图标
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //设置通知的大图标，当下拉系统状态栏时，就可以看到设置的大图标
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        //当通知被点击的时候，跳转到MainActivity中

        if (null != mPendingIntent){
            builder.setContentIntent(mPendingIntent);
        }

        //设置通知的标题
        builder.setContentTitle(title);
        if(progress>0){
            //当progress大于或等于0时，才需要显示下载进度
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }

   

    



    /***************           下载各种状态             *********************************/

    /**
     * 构建了一个用于显示下载进度的通知
     * @param progress
     */
    @Override
    public void onProgress(int progress) {

        //NotificationManager的notify()可以让通知显示出来。
        //notify(),接收两个参数，第一个参数是id:每个通知所指定的id都是不同的。第二个参数是Notification对象。
        if (show_notification){
            getNotificationManager().notify(1,getNotification("Downloading...",progress));
        }

        if (null == onDownloadListener) return;
        onDownloadListener.onProgress(progress);
    }


    /**
     * 观察小米下载,是在不同状态都有一个通知,多个下载任务进行时,同在下载中则显示一个
     * 有结束的则又开了一个通知,当全部结束又都整合到一个通知上,只是下载数量的变化
     */
    /**
     * 创建了一个新的通知用于告诉用户下载成功啦
     */
    @Override
    public void onSuccess(File file) {
        downloadTask=null;

        if (null != onDownloadListener) {
            onDownloadListener.onSuccess(file);
        }

        //下载成功时将前台服务通知关闭，并创建一个下载成功的通知
        if (show_notification){
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Success",-1));
        }




        //Toast.makeText(DownloadService.this,"Download Success",Toast.LENGTH_SHORT).show();
    }

    /**
     *用户下载失败
     */
    @Override
    public void onFailed(int errorCode, String errorMsg) {
        downloadTask=null;

        if (null != onDownloadListener) {
            onDownloadListener.onFailed(errorCode, errorMsg);
        }
        //下载失败时，将前台服务通知关闭，并创建一个下载失败的通知

        if (show_notification){
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1));
        }


        //Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_SHORT).show();
    }

    /**
     * 用户暂停
     */
    @Override
    public void onPaused(File file) {
        downloadTask=null;

        if (null != onDownloadListener) {
            onDownloadListener.onPaused(file);
        }
        //Toast.makeText(DownloadService.this,"Download Paused",Toast.LENGTH_SHORT).show();
    }

    /**
     * 用户取消
     */
    @Override
    public void onCanceled(File file) {
        downloadTask=null;
        FileUtils.deleteFile(file);

        if (null != onDownloadListener) {
            onDownloadListener.onCanceled(file);
        }

        if (show_notification){
            //取消下载，将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
        }


        //Toast.makeText(DownloadService.this,"Download Canceled",Toast.LENGTH_SHORT).show();
    }


}
