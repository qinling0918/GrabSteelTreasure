package com.zgw.qgb.mvc_common.download;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 下载管理器
 */
public class DownloadManager  {
    public static final int MAXIMUM_POOL_SIZE = 5;
    public static String TAG = "DownloadManager";

    protected Map<String, DownloadTask> mDownloadTasks = new HashMap<>();

    private final Deque<DownloadTask> runningTasks = new ArrayDeque<>();
    private final Deque<DownloadTask> readyTasks = new ArrayDeque<>();

    private int maxTaskCount = MAXIMUM_POOL_SIZE;

    public void recyclerTask(DownloadTask downloadTask) {
        Log.e(TAG, "downloadTask: " + downloadTask.hashCode());
        synchronized (this) {
            runningTasks.remove(downloadTask);
            // throw new AssertionError("Call wasn't in-flight!");
            //   runningTasks.remove(downloadTask);
            promoteCalls();
        }
        //  runningTasks.remove(downloadTask);
    }

    private void promoteCalls() {
        if (runningTasks.size() >= maxTaskCount) return; // Already running max capacity.
        if (readyTasks.isEmpty()) return; // No ready calls to promote.

        for (Iterator<DownloadTask> i = readyTasks.iterator(); i.hasNext(); ) {
            DownloadTask task = i.next();
            i.remove();
            runningTasks.add(task);
            if (runningTasks.size() >= maxTaskCount) break; // Reached max capacity.
        }

        resetTaskMaxRequestCount();

    }

    private void resetTaskMaxRequestCount() {
        if (runningTasks != null && !runningTasks.isEmpty()) {
            int maxCount = maxTaskCount / runningTasks.size();
            int remains = maxTaskCount % runningTasks.size();
            for (DownloadTask task : runningTasks) {
                task.setMaxTaskCounts(maxCount);
            }
            if (remains != 0) {
                DownloadTask toptask = runningTasks.getFirst();
                toptask.setMaxTaskCounts(toptask.getMaxTaskCounts() + remains);
            }
            for (DownloadTask task : readyTasks) {
                task.setMaxTaskCounts(0);
            }
        }

    }


    public static DownloadManager getInstance() {
        return SingleTon.INSTANCE;
    }

    public synchronized void setMaxTaskCount(int maxTaskCount) {
        if (maxTaskCount < 1) {
            throw new IllegalArgumentException("max < 1: " + maxTaskCount);
        }
        this.maxTaskCount = maxTaskCount;
    }

    private static class SingleTon {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }

    private String downloadPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    // TODO: 2019/4/1  建议改成 更换存储路径就把集合中的路径也更换成重新设置的这个。
    public DownloadManager setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    /**
     * 添加下载任务
     */

    public DownloadManager add(String url, String filePath, String fileName, long contentLength, long blockLength, DownloadListener listener) {
        //没有指定下载目录,使用默认目录
        filePath = TextUtils.isEmpty(filePath)
                ? downloadPath
                : filePath;
        fileName = TextUtils.isEmpty(fileName)
                ? new File(url).getName()
                : fileName;

        DownloadTask task = new DownloadTask(url, filePath, fileName, contentLength, blockLength, listener);
        if (!mDownloadTasks.containsKey(url)) {
            mDownloadTasks.put(url, task);
        }

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

    public static final int INIT = 0;
    public static final int PAUSE = 1;
    public static final int STOP = 2;
    public static final int DOWNLOADING = 3;

    public int getState() {
        return state;
    }

    private int state = INIT;

    public void setState(int state) {
        this.state = state;
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

            if (runningTasks.size() < maxTaskCount) {
                runningTasks.add(downloadTask);
            } else {
                readyTasks.add(downloadTask);
            }
            resetTaskMaxRequestCount();
            setState(DOWNLOADING);
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
        setState(PAUSE);
        runningTasks.clear();
        readyTasks.clear();
    }

    public void stop() {
        pauseAll();
        setState(STOP);
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

    public void cancelDownload(String url) {
        DownloadTask downloadTask = mDownloadTasks.get(url);
        if (downloadTask != null) {
            downloadTask.cancelDownload();
        }

    }

    /**
     * 这个没用
     */
    public void cancelAll() {
        if (taskIsNotEmpty()) {
            for (String url : mDownloadTasks.keySet()) {
                cancelDownload(url);
            }
        }
    }

    public void removeTask(String url) {
        if (taskIsNotEmpty()) {
            mDownloadTasks.remove(url);
        }

    }

    /**
     * 清除所有下载数据
     */
 /*   public void clearAllThreadAndInfo(){
        downloadDispacther.executorService().shutdownNow();
        new ThreadDaoImpl(SampleApplicationLike.getInstance()).deleteAllThreadInfo();
        mDownloadTasks.clear();
    }
*/


}
