package com.zgw.qgb.download;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zgw.qgb.App;
import com.zgw.qgb.download.bean.TaskInfo;
import com.zgw.qgb.download.bean.ThreadInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.zgw.qgb.download.DownloadRunnable.BUFFER;

/**
 * Created by qinling on 2018/12/5 12:44
 * Description:
 */
public class DownloadTask implements DownloadCallBack {

    private final DownloadListener listener;
    private final Handler sHandler;
    private final long blockLength;
    private TaskInfo taskInfo;
    private ThreadDao dao;

    /**
     * 总下载完成进度
     */
    private int finishedProgress = 0;
    /**
     * 下载线程信息集合
     */
    private List<ThreadInfo> threadInfos;
    /**
     * 下载线程集合
     */
    private List<DownloadRunnable> downloadRunnables ;

    private AtomicLong currentBytes;
    private long[] blockFileSizeArr;

    /*  public DownloadTask(Context context, TaskInfo taskInfo, int downloadThreadCount) {
          this.taskInfo = taskInfo;
          dao = new ThreadDaoImpl(context);
          //初始化下载线程
          initDownThreads(downloadThreadCount);
      }*/
    public DownloadTask(TaskInfo taskInfo, long blockLength, DownloadListener listener) {
        this.taskInfo = taskInfo;
        this.listener = listener;
        this.blockLength = blockLength;
        this.sHandler = new android.os.Handler(App.getContext().getMainLooper());
        dao = new ThreadDaoImpl(App.getContext());
        //初始化下载线程
        // initDownThreads(blockLength);
    }
    int downloadThreadCount;
    private void initDownThreads(long blockLength) {
        downloadRunnables = new ArrayList<>();
        threadInfos = dao.getThreads(taskInfo.getUrl());
        if (threadInfos.size() == 0) {
            //将下载线程保存到数据库
            long contentLength = taskInfo.getContentLength();
            blockFileSizeArr = new long[2];


            // 若是传入的每块文件大小 > 总文件大小  ,则以总文件大小为准
            blockLength = blockLength > contentLength ? contentLength : blockLength;
            long lastBlockFileSize = getLastBlockFileSize(contentLength, blockLength);

            // 若是设置的每一块大小 小于一次写入时的缓冲区大小，则以缓冲区大小为主。
            blockFileSizeArr[0] = blockLength <= BUFFER || blockLength <= 0
                    ? BUFFER : blockLength;
            blockFileSizeArr[1] = lastBlockFileSize <= BUFFER || lastBlockFileSize <= 0
                    ? BUFFER : lastBlockFileSize;

            downloadThreadCount = getDefaultBlockCount(contentLength, blockFileSizeArr[0]);


            // countDownLatch = new CountDownLatch(blockCounts);
            for (int i = 0; i < downloadThreadCount; i++) {
                int blockNum = i;
                // 对应块号应该的开始位置。
                long startIndex = blockNum * blockFileSizeArr[0];
                // 获取对应块号现有的长度
                long endIndex = blockNum == downloadThreadCount - 1
                        ? startIndex + blockFileSizeArr[1]
                        : startIndex + blockFileSizeArr[0] - 1;
                //   Log.e("downloadTASK", "RANGE, 206: blockNum:" + blockNum + "startIndex= " + startIndex + "endIndex= " + endIndex);

                ThreadInfo threadInfo = new ThreadInfo(i, taskInfo.getUrl(), startIndex,
                        endIndex, 0);

                dao.insertThread(threadInfo);
                threadInfos.add(threadInfo);

            }
        }
        long current = taskInfo.getContentLength()- blockLength*threadInfos.size();
        current = current<0?0:current;
        currentBytes = new AtomicLong(current) ;
        for (ThreadInfo thread : threadInfos) {

            finishedProgress += thread.getFinished();
            DownloadRunnable downloadRunnable = new DownloadRunnable(taskInfo, thread, currentBytes,this);
            DownloadManager.getInstance().downloadDispacther.enqueue(downloadRunnable);
            downloadRunnables.add(downloadRunnable);
        }
    }


    /**
     * 根据文件总长度，以及定义的每块长度，获取最后一块的长度
     *
     * @param totalFileSize 文件总长度
     * @param blockFileSize 每块长度
     * @return 最后一块的长度
     */
    private long getLastBlockFileSize(long totalFileSize, long blockFileSize) {
        if (totalFileSize <= 0 || blockFileSize <= 0) {
            return 0;
        }
        long lastBlockFileSize = totalFileSize % blockFileSize;
        return lastBlockFileSize == 0 ? blockFileSize : lastBlockFileSize;
    }

    /**
     * 根据文件总长度，每块文件长度 获取块数
     *
     * @param totalFileSize 文件总长度
     * @param blockFileSize 每块文件长度
     * @return int 总块数
     */
    private int getDefaultBlockCount(long totalFileSize, long blockFileSize) {
        // 若所传数据均不合法，则默认为一块
        if (totalFileSize <= 0 || blockFileSize <= 0 || totalFileSize <= blockFileSize) {
            return 1;
        }
        // 是否有余数
        boolean hasRemainder = (totalFileSize % blockFileSize) != 0;
        int blockSize = (int) (totalFileSize / blockFileSize);
        return hasRemainder ? blockSize + 1 : blockSize;
    }


    private void initDownThreads(int downloadThreadCount) {
        //查询数据库中的下载线程信息
        threadInfos = dao.getThreads(taskInfo.getUrl());
        if (threadInfos.size() == 0) {//如果列表没有数据 则为第一次下载
            //根据下载的线程总数平分各自下载的文件长度
            long length = taskInfo.getContentLength() / downloadThreadCount;
            for (int i = 0; i < downloadThreadCount; i++) {
                ThreadInfo thread = new ThreadInfo(i, taskInfo.getUrl(), i * length,
                        (i + 1) * length - 1, 0);
                if (i == downloadThreadCount - 1) {
                    thread.setEndIndex(taskInfo.getContentLength());
                }
                //将下载线程保存到数据库
                dao.insertThread(thread);
                threadInfos.add(thread);
            }
        }
        long current = taskInfo.getContentLength()-blockFileSizeArr[0]*threadInfos.size();
        currentBytes = new AtomicLong(current) ;
        //创建下载线程开始下载
        for (ThreadInfo thread : threadInfos) {
            finishedProgress += thread.getFinished();
            DownloadRunnable downloadThread = new DownloadRunnable(taskInfo, thread, currentBytes,this);
            //   DownloadManager.getInstance().downloadDispacther.enqueue(downloadThread);
            // DownloadService.executorService.execute(downloadThread);
            downloadRunnables.add(downloadThread);
        }
    }

    /**
     * 暂停下载
     */
    public void pauseDownload() {
        for (DownloadRunnable downloadThread : downloadRunnables) {
            if (downloadThread != null) {
                downloadThread.setPause(true);
                DownloadManager.getInstance().downloadDispacther.remove(downloadThread);
              //  DownloadManager.getInstance().downloadDispacther.finished(downloadThread);
            }
        }
    }

    @Override
    public void pauseCallBack(ThreadInfo threadBean) {
        dao.updateThread(threadBean.getUrl(), threadBean.getId(), threadBean.getFinished());
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onPaused();
                }
            }
        });


    }

    private long curTime = 0;

    @Override
    public void progressCallBack(int length) {
       // currentBytes.addAndGet(length);
        finishedProgress += length;
        //每500毫秒发送刷新进度事件
        if (System.currentTimeMillis() - curTime > 500 || finishedProgress == taskInfo.getContentLength()) {
            taskInfo.setFinished(finishedProgress);
            Log.e("progressCallBack", "progress"+finishedProgress );
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                       // listener.onProgress((int) ((finishedProgress*100)/taskInfo.getContentLength()));
                       // listener.onProgress((int) (finishedProgress * 100 / taskInfo.getContentLength()));
                        listener.onProgress((int) ((currentBytes.get() * 100) / taskInfo.getContentLength()));
                       // listener.onProgress((int) ((currentBytes.get() * 100) / ));
                    }
                }
            });


            curTime = System.currentTimeMillis();
        }
    }

    @NonNull
    private android.os.Handler getHandler() {
        return sHandler;
    }


    int finishedCount = 0;
    @Override
    public synchronized void threadDownLoadFinished(ThreadInfo threadBean) {
        finishedCount ++;
     /*   for(ThreadInfo bean:threadInfos){
            if(bean.getId() == threadBean.getId()){
                //从列表中将已下载完成的线程信息移除
                threadInfos.remove(bean);
                break;
            }
        }*/
        dao.deleteThread(taskInfo.getUrl(),threadBean.getId());
        Iterator<ThreadInfo> iterable = threadInfos.iterator();
        while (iterable.hasNext()) {
            if (iterable.next().getId() == threadBean.getId()) {
                //从列表中将已下载完成的线程信息移除
                iterable.remove();
                break;
            }
        }

        //如果列表size为0 则所有线程已下载完成
        if (threadInfos.size() == 0) {
            //删除数据库中的信息

            dao.deleteTask(taskInfo.getUrl());
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onSuccess(taskInfo.getFilePath() + File.separator + taskInfo.getFileName());
                    }
                }
            });


         /*   //发送下载完成事件
            EventMessage message = new EventMessage(2,taskInfo);
            EventBus.getDefault().post(message);*/
        }
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void startDownload() {

        //创建下载线程开始下载
        initDownThreads(blockLength);
    }
}

