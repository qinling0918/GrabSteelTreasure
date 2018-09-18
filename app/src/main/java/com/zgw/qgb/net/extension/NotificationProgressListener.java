package com.zgw.qgb.net.extension;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Utils;
import com.zgw.qgb.net.progressmanager.ProgressListener;
import com.zgw.qgb.net.progressmanager.ProgressManager;
import com.zgw.qgb.net.progressmanager.body.ProgressInfo;

import java.io.File;

/**
 * Name:NotificationProgressListener
 * Created by Tsinling on 2017/10/21 15:16.
 * description:
 */

/**
 * 需要放在另一个进程的service 保证App退出,也能下载
 */
public class NotificationProgressListener implements ProgressListener {
    private static final String TAG = "DownloadService";
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private String url;

    public NotificationProgressListener(String url) {
        this.url = url ;
        String fileName = new File(url).getName();
        notificationManager = (NotificationManager) Utils.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(Utils.getContext(),url)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(fileName)
                .setAutoCancel(true);

        notificationManager.notify(url,url.hashCode(), notificationBuilder.build());//两个对象相等则它们的hashCode值必然相等；
    }

    @Override
    public void onProgress(ProgressInfo progressInfo) {
        sendNotification(progressInfo);
    }

    @Override
    public void onError(long id, Exception e) {
        sendNotificationError();
    }


    private void sendNotificationError() {
        sendNotification(0,0,"下载失败",0,0);
    }


    private void sendNotification(ProgressInfo progressInfo) {
        sendNotification(100
                ,progressInfo.getPercent()
                ,"下载完成"
                ,progressInfo.getContentLength()
                ,progressInfo.getCurrentbytes());
    }

    int lastProgress = 0 ;
    private void sendNotification(int max, int progress, CharSequence content, long totalLength, long currentLength) {

        int mRefreshTime = ProgressManager.getInstance().getmRefreshTime();//若不调用setRefreshTime 获取的时间是默认值 ProgressManager.DEFAULT_REFRESH_TIME
        long speed = ((progress - lastProgress) * totalLength) / mRefreshTime ; //ms >s 比例 100  progress > 百分比 比例100  ,此时单位为  b/s
        long timeLeft = speed == 0 ? 0 : (totalLength - currentLength) / speed ;
        max = progress == max ? 0 : max; //当完成或者出现错误,则隐藏进度条
        CharSequence contentInfo = max == 0 ? null : progress +"%";
        notificationBuilder.setProgress(max, progress, false);

        content = max == 0 ? content
                : totalLength == 0 ? content
                : String.format(Utils.getLocale(),Utils.getContext().getString(R.string.leftTime),formatSeconds(timeLeft));
        notificationBuilder.setContentText(content);

        notificationBuilder.setContentInfo(contentInfo);
        notificationManager.notify(url,url.hashCode(), notificationBuilder.build());

        lastProgress = progress;
    }

    /**
     * 秒转化为天小时分秒字符串
     *
     * @param seconds
     * @return String
     */
    public static String formatSeconds(long seconds) {
        String timeStr = seconds + "秒";
        if (seconds > 60) {
            long second = seconds % 60;
            long min = seconds / 60;
            timeStr = min + "分" + second + "秒";
            if (min > 60) {
                min = (seconds / 60) % 60;
                long hour = (seconds / 60) / 60;
                timeStr = hour + "小时" + min + "分" + second + "秒";
                if (hour > 24) {
                    hour = ((seconds / 60) / 60) % 24;
                    long day = (((seconds / 60) / 60) / 24);
                    timeStr = day + "天" + hour + "小时" + min + "分" + second + "秒";
                }
            }
        }
        return timeStr;
    }
}
