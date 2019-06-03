package com.zgw.qgb.net.download_native;


import android.content.Context;
import android.util.Log;

import com.zgw.qgb.helper.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.zgw.qgb.net.download_native.FileDownloadThread.BUFFER;


public class DownLoadSingleFile {
    private ThreadPoolExecutor taskPool;

    public static DownLoadSingleFile getInstance() {
        return SingleTon.sInstance;
    }

    private DownLoadSingleFile() {
    }

    private static class SingleTon {
        private static final DownLoadSingleFile sInstance = new DownLoadSingleFile();
    }

    private static final String TAG = "DownLoadSingleFile";
  //  private Map<String, List<DownloadCallBack>> callBacks = new WeakHashMap<>();

    protected Map<String, DownloadTask> mDownloadTasks = new HashMap<>();

/*    public void addCallBacks(String downloadUrl, DownloadCallBack callBack) {
        List<DownloadCallBack> progressListeners;
        synchronized (DownloadCallBack.class) {
            progressListeners = callBacks.get(downloadUrl);
            if (progressListeners == null) {
                progressListeners = new LinkedList<>();
                callBacks.put(downloadUrl, progressListeners);
            }
        }
        progressListeners.add(callBack);
    }*/

    public void cancelAll() {

        if (mDownloadTasks != null) {
            for (String key : mDownloadTasks.keySet()) {
                DownloadTask downloadTask = mDownloadTasks.get(key);
                if (downloadTask != null) {
                    // callBacks.clear();
                    downloadTask.cancel();
                    downloadTask.interrupt();
                    // downloadTask = null;
                    // downloadTask = null;
                    // mDownloadTasks.put(key,downloadTask);
                }
            }
        }
        if (taskPool != null) {
            taskPool.shutdownNow();
        }

    }

    /**
     * 下载文件
     */
  /*  public void doDownload(Context context, String filePath, String fileName, String downloadUrl, int contentLength, String updType, int installVersion, String verTypeCode, DownloadCallBack callBack) {
        //创建文件
        File file = new File(filePath);
        // 如果文件不存在，则按path路径创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
        int threadNum;
        if (TextUtils.isEmpty(updType) || updType.equals("1") || installVersion < Integer.parseInt(verTypeCode)) {
            threadNum = 5;
        } else {
            threadNum = 1;
        }

        //下载后的文件所处的绝对文件路径
        String filepath = filePath + fileName;
        Log.d(TAG, "download file  path:" + filepath);
        //启动下载任务线程
        task = new DownloadTask(context, downloadUrl, threadNum, contentLength, filepath, callBack);
        executorService().execute(task);
        // task.start();
    }*/
    public void doDownload(Context context, String packageName, String filePath, String fileName, String downloadUrl, int threadNum, long contentLength, DownloadCallBack callBack) {
        Log.d(TAG, "download file  downloadUrl:" + downloadUrl);
        //创建文件
        File file = new File(filePath);
        // 如果文件不存在，则按path路径创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }


        //下载后的文件所处的绝对文件路径
        String filepath = filePath + fileName;
        Log.d(TAG, "download file  path:" + filepath);
        DownloadTask task = mDownloadTasks.get(downloadUrl);
        if (task == null) {
            //启动下载任务线程
            task = new DownloadTask(context, downloadUrl, threadNum, contentLength, filepath,  callBack);
            mDownloadTasks.put(downloadUrl, task);
            //executorService().execute(task);
        }
        new Thread(task).start();
       // taskPool = new ThreadPoolExecutor(5, 5, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
       // taskPool.execute(task);
       // taskPool.shutdown();
        // taskPool.awaitTermination(1,TimeUnit.DAYS);
    }


    public void doDownload(Context context, String packageName, String filePath, String fileName, String downloadUrl, long blockLength, long contentLength, DownloadCallBack callBack) {
        Log.d(TAG, "download file  downloadUrl:" + downloadUrl);
        //创建文件
        File file = new File(filePath);
        // 如果文件不存在，则按path路径创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
        //下载后的文件所处的绝对文件路径
        String filepath = filePath + fileName;
        Log.d(TAG, "download file  path:" + filepath);


        DownloadTask task = mDownloadTasks.get(downloadUrl);
        if (task == null) {
            //启动下载任务线程
            task = new DownloadTask(context, downloadUrl, blockLength, contentLength, filepath,  callBack);
            mDownloadTasks.put(downloadUrl, task);
            //executorService().execute(task);
        }
    /*    taskPool = new ThreadPoolExecutor(5, 5, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        taskPool.execute(task);
        taskPool.shutdown();*/
        new Thread(task).start();
      /*  //启动下载任务线程
        task = new DownloadTask(context,downloadUrl, blockLength, contentLength, filepath, callBacks.get(packageName));
        //executorService().execute(task);
         task.start();*/
    }

    /**
     * 文件下载线程
     */
    class DownloadTask extends Thread {
        private static final String CACHE_FILE_SUFFIX = ".cache";
        private static final String TEMP_FILE_SUFFIX = ".temp";
        private long[] blockFileSizeArr = new long[2];
        private String downloadUrl;//文件下载路径ַ
        private int threadNum;// 分成段数
        private String filePath;// 存储路径ַ
        private int blockSize;// 每一文件块大小
        private DownloadCallBack callBack;//
        private Context context;//
        private long contentLength;// 文件总长度
        private AtomicLong currentBytes;
        private File tempFile;
        private ThreadPoolExecutor executorService;
        private File cacheFile;
        private boolean isfinished;

        public DownloadTask(Context context, String downloadUrl, int threadNum, long contentLength, String filePath, DownloadCallBack callBack) {
            this.downloadUrl = downloadUrl;
            this.threadNum = threadNum;
            this.filePath = filePath;
            this.callBack = callBack;
            this.context = context;
            this.contentLength = contentLength;

            // 以缓冲池大小为每一块最小值，计算出最多有多少块
            long maxBlockCount = getDefaultBlockCount(contentLength, BUFFER);
            // 若是块数为 0x7fffffff，可能就是类型转换溢出
            threadNum = threadNum > maxBlockCount ? (int) maxBlockCount : threadNum;
            // 文件总大小 小于设置的缓冲区的值，或者传入的块数小于1
            this.threadNum = (contentLength <= BUFFER || threadNum <= 1)
                    ? 1 : threadNum;

            this.blockFileSizeArr = getDefaultBlockFileSize();
            Log.d(TAG, "DownloadSingleFile:" + toString());
        }

        private long[] getDefaultBlockFileSize() {

            if (contentLength <= 0 || contentLength <= 0) {
                return new long[]{BUFFER, BUFFER};
            }
            long[] blockFileSize = new long[2];
            long remains = contentLength % threadNum;
            if (remains == 0) {
                blockFileSize[0] = blockFileSize[1] = contentLength / threadNum;
            } else {
                blockFileSize[0] = (contentLength / threadNum) + 1;
                blockFileSize[1] = contentLength % blockFileSize[0];
            }
            return blockFileSize;
        }

        public DownloadTask(Context context, String downloadUrl, long blockLength, long contentLength, String filePath, DownloadCallBack callBacks) {
            this.downloadUrl = downloadUrl;
            this.filePath = filePath;
            this.callBack = callBacks;
            this.context = context;
            this.contentLength = contentLength;


            // 若是传入的每块文件大小 > 总文件大小  ,则以总文件大小为准
            blockLength = blockLength > contentLength ? contentLength : blockLength;
            long lastBlockFileSize = getLastBlockFileSize(contentLength, blockLength);

            // 若是设置的每一块大小 小于一次写入时的缓冲区大小，则以缓冲区大小为主。
            this.blockFileSizeArr[0] = blockLength <= BUFFER || blockLength <= 0
                    ? BUFFER : blockLength;
            this.blockFileSizeArr[1] = lastBlockFileSize <= BUFFER || lastBlockFileSize <= 0
                    ? BUFFER : lastBlockFileSize;

            this.threadNum = getDefaultBlockCount(contentLength, this.blockFileSizeArr[0]);
            Log.d(TAG, "DownloadSingleFile:" + toString());
        }

        @Override
        public String toString() {
            return "DownloadTask{" +
                    "blockFileSizeArr=" + Arrays.toString(blockFileSizeArr) +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    ", threadNum=" + threadNum +
                    ", filePath='" + filePath + '\'' +
                    ", blockSize=" + blockSize +
                    ", contentLength=" + contentLength +
                    '}';
        }

        private long getLastBlockFileSize(long totalFileSize, long blockFileSize) {
            if (totalFileSize <= 0 || blockFileSize <= 0) {
                return 0;
            }
            long lastBlockFileSize = totalFileSize % blockFileSize;
            return lastBlockFileSize == 0 ? blockFileSize : lastBlockFileSize;
        }

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

        @Override
        public void run() {
            // ThreadPoolExecutor executor = new ThreadPoolExecutor()

            try {
                URL url = new URL(downloadUrl);
                Log.d(TAG, "download file http path:" + downloadUrl);
                //打开网络连接
                URLConnection conn = url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                if (contentLength <= 0) {
                    falied("无法获得文件信息");
                    return;
                }
                // 设置进度条的最大值
                //  mProgressbar.setMax(fileSize);
                //每一个文件块的长度
          /*      long blockSize = (contentLength % threadNum) == 0 ? contentLength / threadNum
                        : contentLength / threadNum + 1;
                Log.d(TAG, "fileSize:" + contentLength + "  blockSize:" + blockSize);
                File file = new File(filePath);
                for (int i = 0; i < threads.length; i++) {
                    //让每一个FileDownloadThread线程分段下载
                    threads[i] = new FileDownloadThread(url, file, blockSize,
                            (i + 1));
                    //为线程设置一个名字
                    threads[i].setName("Thread:" + i);
                    threads[i].start();
                }*/

                CommonUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != callBack) {
                            // cacheFile.renameTo(new File(filePath));
                            callBack.onStart();
                            // 删除掉临时文件
                            //  FileUtils.deleteFile(tempFile);
                            // callBack = null;
                            //isfinished = true;
                        }
                    }
                });
                currentBytes = new AtomicLong(0);
                executorService = new ThreadPoolExecutor(5, 5, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
                 //  File file = new File(filePath);
                   File cacheFile = new File(filePath+CACHE_FILE_SUFFIX);
               // cacheFile = new File(filePath);
                 tempFile = new File(filePath + TEMP_FILE_SUFFIX);
                FileUtils.createOrExistsFile(cacheFile);
                  FileUtils.createOrExistsFile(tempFile);
                FileDownloadThread[] threads = new FileDownloadThread[threadNum];

                for (int i = 0; i < threadNum; i++) {
                    //  countDownLatch = new CountDownLatch(1);
                    int blockNum = i;
                    // 对应块号应该的开始位置。
                    long startIndex = blockNum * blockFileSizeArr[0];
                    // 获取对应块号现有的长度
                    long endIndex = blockNum == threadNum - 1
                            ? startIndex + blockFileSizeArr[1]
                            : startIndex + blockFileSizeArr[0] - 1;
                    Log.e("downloadTASK", "RANGE, 206: blockNum:" + blockNum + "startIndex= " + startIndex + "endIndex= " + endIndex);
                    threads[i] = new FileDownloadThread(url, cacheFile, tempFile, startIndex, endIndex,
                            (i + 1), currentBytes);
                    //为线程设置一个名字
                    threads[i].setName("Thread:" + i);
                    //executorService().execute(threads[i]);
                    executorService.execute(threads[i]);
                    //threads[i].start();
                }

                //是否下载完成标志
                isfinished = false;
                int downloadedAllSize = 0;

                while (!isfinished) {
                  /*  if (!SharepreferenceUtil.getCommUsbEnable(context) && !NetWorkUtil.getInstance().isConnected(context)) {
                        CommonUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {

                                        if (null != callBack) {

                                            callBack.onFail("网络连接失败");

                                           // DownLoadSingleFile.getInstance().cancelAll();
                                        }

                            }
                        });
                        interrupt();
                        return;
                    }*/
                    isfinished = true;
                    // downloadedAllSize为已下载总长度
                    downloadedAllSize = 0;
                    for (int i = 0; i < threads.length; i++) {
                        // downloadedAllSize += threads[i].getDownloadLength();
                        // LogUtil.e("downloadedAllSize", downloadedAllSize + "");
                        if (!threads[i].isCompleted()) {
                            isfinished = false;
                        }

                    }
                    final boolean finalIsfinished = isfinished;

                    CommonUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (currentBytes != null && currentBytes.get() != 0 && !finalIsfinished) {
                                        if (null != callBack) {
                                            callBack.onProgress((int) (currentBytes.get() * 100 / contentLength));
                                }
                            }
                                 /*if (null!=callBack ){
                                     callBack.onProgress((int) (finalDownloadedAllSize * 100 / contentLength));
                                 }*/
                        }
                    });
                // final long finalDownloadedAllSize = currentBytes.get();
            }
            // 循环检测网络状态，以及下载状态  任务结束后关闭线程
            interrupt();
            //网络原因的结束，
            CommonUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                            if (null != callBack) {
                                // cacheFile.renameTo(new File(filePath));
                                callBack.onCompleted(filePath);
                                // 删除掉临时文件
                                //  FileUtils.deleteFile(tempFile);
                               // callBack = null;
                                //isfinished = true;
                    }


                }
            });


            // 等待线程全部执行完毕，然后关闭线程池
            executorService.shutdown();
            // 若超出2小时，仍未下载完成，也将关闭线程池
            executorService.awaitTermination(2, TimeUnit.HOURS);


        } catch(
        final MalformedURLException e)

        {
            Log.e(TAG, e.getMessage());
            falied(e.toString());
            e.printStackTrace();
        } catch(
        final IOException e)

        {
            Log.e(TAG, e.getMessage());
            falied(e.toString());
            e.printStackTrace();
        } catch(
        InterruptedException e)

        {
            Log.e(TAG, e.getMessage());
            falied(e.toString());
        }

    }

    /**
     * 非网络原因导致的失败。
     *
     * @param s
     */
    private void falied(final String s) {
        // if (currentBytes.get() != contentLength){
        interrupt();
        CommonUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                        if (null != callBack) {
                            FileUtils.deleteFile(tempFile);
                            callBack.onFail(s);
                            isfinished = true;
                           // callBack = null;
                }
            }
        });

    }

    //  }

    public void cancel() {
        executorService.shutdownNow();
        FileUtils.deleteFile(cacheFile);
        interrupt();

        isfinished = true;
        currentBytes = null;
                if (callBack != null) {
                    callBack.onCancel();
                }
    }


}


public interface DownloadCallBack {

    void onCompleted(String filePath);

    void onProgress(int progress);

    void onFail(String error);
    void onCancel();
    void onStart();
}


}
