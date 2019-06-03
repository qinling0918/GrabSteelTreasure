package com.zgw.qgb.download;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;

import com.zgw.qgb.R;
import com.zgw.qgb.download.bean.TaskInfo;
import com.zgw.qgb.helper.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * 专门用来下载大文件的服务  支持暂停,取消,失败,成功,下载中回调监听.  另外还有下载时显示在通知栏
 * 此处与ProgressManager连接,可以在其他位置根据url 来监听进度(onProgress与onError)
 */
public class DownloadManager {

    public static String TAG = "DownloadManager";

    DownloadDispacther downloadDispacther ;

    protected Map<String, DownloadTask> mDownloadTasks = new HashMap<>();
    //protected Map<String, TaskInfo> taskInfoMap = new HashMap<>();
    public static DownloadManager getInstance() {
        return SingleTon.INSTANCE;
    }

    private DownloadManager() {
        downloadDispacther = new DownloadDispacther();
    }

    private static class SingleTon {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }


    /**
     * 添加下载任务
     */

    public DownloadManager add(String url, String filePath, String fileName,long contentLength,long blockLength,DownloadListener listener) {
        //没有指定下载目录,使用默认目录
        filePath = TextUtils.isEmpty(filePath)
                ? Environment.getExternalStorageDirectory() + File.separator + Utils.getContext().getString(R.string.app_name)
                : filePath;
        fileName = TextUtils.isEmpty(fileName)
                ? new File(url).getName()
                : fileName;

        DownloadTask task = new DownloadTask(new TaskInfo(url,filePath,fileName,contentLength),blockLength,listener);
        mDownloadTasks.put(url,task);
        return this;
    }

    public void download(String... urls) {
        if (urlsIsEmpty(urls)) {
            downloadAll();
        } else {
            for (String url : urls) {
                if (mDownloadTasks.containsKey(url)) {
                    download(url);
                }
            }
        }

    }

    /**
     * @param urls
     * @return
     */
    private boolean urlsIsEmpty(String[] urls) {
        return urls == null || urls.length == 0;
    }

    /**
     * 下载所有路径文件
     */
    public void downloadAll() {
        if (taskIsNotEmpty()) {
            for (String url : mDownloadTasks.keySet()) {
                download(url);
            }
        }
    }

    private void download(String url) {

        DownloadTask downloadTask = mDownloadTasks.get(url);
        if (downloadTask != null) {
           /* if (executorService().isPasued()) {
                executorService().resume();
            }*/
            downloadTask.startDownload();
            //executorService().execute(downloadTask);
            // downloadTask.
          //  downloadTask.start();
        }
    }

    private boolean taskIsNotEmpty() {
        return mDownloadTasks != null && mDownloadTasks.size() != 0;
    }


    /**
     * 暂停
     */

    public void pauseDownload(String... urls) {
        if (urlsIsEmpty(urls)) {
            pauseAll();
        } else {
            for (String url : urls) {
                pauseDownload(url);
            }
        }
    }

    public void pauseAll() {
        if (taskIsNotEmpty()) {
            for (String url : mDownloadTasks.keySet()) {
                pauseDownload(url);
            }

        }
    }

    private void pauseDownload(String url) {
        DownloadTask downloadTask = mDownloadTasks.get(url);
        if (downloadTask != null) {
            downloadTask.pauseDownload();

        }

      //  mDownloadTasks.remove(url);

      /*  if (mDownloadTasks.size() == 0) {
            executorService().pause();
        }*/
    }

    /**
     * 取消下载
     */

    public void cancelDownload(String... urls) {
        if (urlsIsEmpty(urls)) {
            cancelAll();
        } else {
            for (String url : urls) {
                cancelDownload(url);
            }
        }
    }

    public void cancelAll() {
        if (taskIsNotEmpty()) {
            for (String url : mDownloadTasks.keySet()) {
                cancelDownload(url);
            }
        }

    }





}
