package com.zgw.qgb.network.download;

import android.os.Environment;
import android.text.TextUtils;


import com.zgw.qgb.R;
import com.zgw.qgb.helper.Utils;
import com.zgw.qgb.network.download.listener.DownloadListener;
import com.zgw.qgb.network.download.listener.DownloadsListener;


import java.io.File;
import java.util.HashMap;
import java.util.Map;



/**
 * 专门用来下载大文件的服务  支持暂停,取消,失败,成功,下载中回调监听.  另外还有下载时显示在通知栏
 * 此处与ProgressManager连接,可以在其他位置根据url 来监听进度(onProgress与onError)
 */
public class DownloadManager {

    public static String TAG = "DownloadManager";


    protected Map<String, DownloadTask> mDownloadTasks = new HashMap<>();
    protected Map<String, DownloadTask> mPausedTasks = new HashMap<>();
    // protected Map<String, DownloadTask> mPausedDownloadTasks = new HashMap<>();
    protected String[] downloadUrlArr;

    public static DownloadManager getInstance() {
        return SingleTon.INSTANCE;
    }

    private DownloadManager() {
    }



    private static class SingleTon {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }


    public DownloadManager setBlockCounts(int blockCount) {
        setDownloadMode(DownloadMode.MULTI_THREAD_BLOCK_COUNT.setValue(blockCount));
        return this;
    }

    public DownloadManager setOneBlockLength(long blockLength) {
        setDownloadMode(DownloadMode.MULTI_THREAD_BLOCK_LENGTH.setValue(blockLength));
        return this;
    }

    private DownloadMode downloadMode = DownloadMode.SINGLE_THREAD;


    public void unregisterOnDownloadListeners() {
        setOnDownloadListener(null);
    }

    public DownloadManager add(String url) {
        return add(url, null);
    }
    public DownloadManager add(String url, DownloadListener listener) {
        return add(url, null);
    }


  /*  public DownloadManager add(String url, String filePath) {
        return add(url, filePath, null,n);
    }*/
    public DownloadManager add(String url, String filePath,DownloadListener listener) {
        return add(url, filePath, null);
    }
    public DownloadManager add(String url, String filePath, String fileName,DownloadListener listener) {
        //没有指定下载目录,使用默认目录

        return add(url,filePath,fileName,-1,listener);
    }
    /**
     * 添加下载任务
     */

    public DownloadManager add(String url, String filePath, String fileName,long contentLength,DownloadListener listener) {
        //没有指定下载目录,使用默认目录
        filePath = TextUtils.isEmpty(filePath)
                ? Environment.getExternalStorageDirectory() + File.separator + Utils.getContext().getString(R.string.app_name)
                : filePath;
        fileName = TextUtils.isEmpty(fileName)
                ? new File(url).getName()
                : fileName;
        DownloadTask downloadTask = new DownloadTask(
                this, url, filePath, fileName,contentLength);
        downloadTask.setOnDownloadMode(downloadMode);
        downloadTask.setOnDownloadListener(listener);
        mDownloadTasks.put(url, downloadTask);
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
           // downloadTask.startDownload();
            //executorService().execute(downloadTask);
            downloadTask.start();
        }
    }

    private boolean taskIsNotEmpty() {
        return mDownloadTasks != null && mDownloadTasks.size() != 0;
    }

 /*   public void reStartAll() {
        if (taskIsNotEmpty()) {
            for (String url : mPausedTasks.keySet()) {
                reStart(url);
            }
        }
    }*/

/*    private void reStart(String url) {

        DownloadTask downloadTask = mPausedTasks.get(url);
        if (downloadTask != null) {
            if (executorService().isPasued()) {
                executorService().resume();
            }
            executorService().execute(downloadTask);
            //downloadTask.start();
        }
    }*/

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
            mPausedTasks.put(url, downloadTask);
        }

        mDownloadTasks.remove(url);

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

    private void cancelDownload(String url) {
        DownloadTask downloadTask = mDownloadTasks.get(url);
        if (downloadTask != null) {
            downloadTask.cancelDownload();
        }
        mDownloadTasks.remove(url);
        // 若是该集合为空，则关闭线程池
       /* if (mDownloadTasks.size() == 0) {
            shutdown();
        }*/
    }

    /**
     * 更新进度的回调接口
     */
    protected DownloadsListener onDownloadListener;

    /**
     * 注册回调接口的方法，供外部调用
     *
     * @param listener
     */
    private DownloadManager setOnDownloadListener(DownloadListener listener) {
        if (taskIsNotEmpty()) {
            for (String url : mDownloadTasks.keySet()) {
                DownloadTask downloadTask = mDownloadTasks.get(url);
                if (downloadTask != null) {
                    downloadTask.setOnDownloadListener(listener);
                }
            }
        }
        return this;
    }

    public void setDownloadMode(DownloadMode downloadMode) {
        if (taskIsNotEmpty()) {
            for (String url : mDownloadTasks.keySet()) {
                DownloadTask downloadTask = mDownloadTasks.get(url);
                if (downloadTask != null) {
                    downloadTask.setOnDownloadMode(downloadMode);
                }
            }
        }
    }
}
