package com.zgw.qgb.delete;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import com.zgw.qgb.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Name:DownloadsService
 * Created by Tsinling on 2018/2/27 17:14.
 * description:
 */

public class DownloadsService extends Service {
    private Map<String, DownloadTask> downloadTaskMap = new HashMap<>();

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Notification getNotification(String title, int progress) {
       /* Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);*/

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        //builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if(progress > 0){
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }


        return builder.build();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    class DownloadBinder extends Binder {
        public void startDownload(String url, int position, DownloadListener listener){
            if(!downloadTaskMap.containsKey(url)){
                DownloadTask downloadTask = new DownloadTask(listener);
                downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, position+"");
                downloadTaskMap.put(url, downloadTask);
                if(downloadTaskMap.size() == 1){
                    startForeground(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
                } else{
                    getNotificationManager().notify(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
                }
            }
        }

        public void updateDownload(String url, DownloadListener listener){
            if(downloadTaskMap.containsKey(url)){
                DownloadTask downloadTask = downloadTaskMap.get(url);
                if(downloadTask != null){
                    downloadTask.setDownloadListener(listener);
                }
            }

        }

        public void pauseDownload(String url){
            if(downloadTaskMap.containsKey(url)){
                DownloadTask downloadTask = downloadTaskMap.get(url);
                if(downloadTask != null){
                    downloadTask.pauseDownload();
                }

                downloadTaskMap.remove(url);

                if(downloadTaskMap.size() > 0){
                    getNotificationManager().notify(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
                } else {
                    stopForeground(true);
                    getNotificationManager().notify(1, getNotification("全部暂停下载", -1));
                }
            }
        }

        public void downloadSuccess(String url){
            if(downloadTaskMap.containsKey(url)){
                DownloadTask downloadTask = downloadTaskMap.get(url);
                downloadTaskMap.remove(url);
                if(downloadTask != null){
                    downloadTask = null;
                }

                if(downloadTaskMap.size() > 0){
                    getNotificationManager().notify(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
                } else {
                    stopForeground(true);
                    getNotificationManager().notify(1, getNotification("下载成功", -1));
                }

            }
        }

        public boolean isDownloading(String url){
            if(downloadTaskMap.containsKey(url)){
                return true;
            }

            return  false;
        }

        public void cancelDownload(String url){
            if(downloadTaskMap.containsKey(url)){
                DownloadTask downloadTask = downloadTaskMap.get(url);
                if(downloadTask != null){
                    downloadTask.cancelDownload();
                }
                downloadTaskMap.remove(url);

                if(downloadTaskMap.size() > 0){
                    getNotificationManager().notify(1, getNotification("正在下载" + downloadTaskMap.size(), -1));
                } else {
                    stopForeground(true);
                    getNotificationManager().notify(1, getNotification("全部取消下载", -1));
                }
            }

            if(url != null){
                String fileName = url.substring(url.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + fileName);

                if(file.exists()){
                    file.delete();
                    Toast.makeText(DownloadsService.this, "Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

