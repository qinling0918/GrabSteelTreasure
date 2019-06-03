package com.zgw.qgb.download;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zgw.qgb.download.bean.TaskInfo;
import com.zgw.qgb.download.bean.ThreadInfo;
import com.zgw.qgb.helper.utils.FileUtils;
import com.zgw.qgb.network.download.DownLoadInfoManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by qinling on 2018/12/1 12:48
 * Description:
 */
public class DownloadRunnable extends NamedRunnable {
    private AtomicLong currentBytes;
    private TaskInfo taskInfo;
    private ThreadInfo threaInfo;
    private DownloadCallBack callback;
    private Boolean isPause = false;
    public static int BUFFER = 1024;

    public DownloadRunnable(TaskInfo taskInfo, ThreadInfo threaInfo, AtomicLong currentBytes, DownloadCallBack callback) {
        super("DownloadRunnable Id:%s,start: %s ,end: %s", threaInfo.getId(), threaInfo.getStartIndex(), threaInfo.getEndIndex());
        this.taskInfo = taskInfo;
        this.threaInfo = threaInfo;
        this.callback = callback;
        this.currentBytes = currentBytes;
        this.currentBytes.addAndGet(threaInfo.getFinished());
    }

    public void setPause(Boolean pause) {
        isPause = pause;
    }


    @Override
    protected void execute() {
        HttpURLConnection connection = null;
        RandomAccessFile raf = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(threaInfo.getUrl());
            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            //设置下载起始位置
            long start = threaInfo.getStartIndex() + threaInfo.getFinished();
            connection.setRequestProperty("Range", "bytes=" + start + "-" + threaInfo.getEndIndex());
            //设置写入位置
            File file = new File(taskInfo.getFilePath(), taskInfo.getFileName());
            raf = new RandomAccessFile(file, "rwd");
            raf.seek(start);

            //开始下载
             if(connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
            Log.e("DownloadRunnable", Thread.currentThread().getName());
            inputStream = connection.getInputStream();
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(bytes)) != -1) {
                raf.write(bytes, 0, len);
                //将加载的进度回调出去
                currentBytes.addAndGet(len);
                callback.progressCallBack(len);
                //保存进度
                threaInfo.setFinished(threaInfo.getFinished() + len);
                Log.e("while", "" + threaInfo.getFinished() + len);


                //在下载暂停的时候将下载进度保存到数据库
                if (isPause) {
                    callback.pauseCallBack(threaInfo);
                  //  FileUtils.close(inputStream,raf,);
                    return;
                }
            }
            //下载完成

       //     DownloadManager.getInstance().downloadDispacther.finished(this);
            callback.threadDownLoadFinished(threaInfo);
            }else{
               Log.e("DownloadRunnable","无网络！");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                raf.close();
                connection.disconnect();
               // DownloadManager.getInstance().downloadDispacther.finished(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
