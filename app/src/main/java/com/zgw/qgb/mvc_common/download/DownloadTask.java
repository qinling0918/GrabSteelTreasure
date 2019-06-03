package com.zgw.qgb.mvc_common.download;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.zgw.qgb.mvc_common.download.DownloadTask.Status.CANCELED;
import static com.zgw.qgb.mvc_common.download.DownloadTask.Status.PAUSED;
import static com.zgw.qgb.mvc_common.download.DownloadTask.Status.RUNNING;

/**
 * created by tsinling on: 2018/12/16 18:17
 * description:
 */
public class DownloadTask {
    private static final String TAG = "DownloadTask";
    private static final String CACHE_FILE_SUFFIX = ".cache";
    private static final String TEMP_FILE_SUFFIX = ".temp";
    private static final int BUFFER = 1024;

    private static final String BLOCK_DEFAULT = "00000000";

    private volatile Status mStatus = Status.PENDING;
    private final DownloadExecutor executor = new DownloadExecutor();

    private final String downloadUrl;
    private final String filePath;
    private final String fileName;
    private final long contentLength;
    private long blockLength;
    private final DownloadListener listener;

    private long[] blockFileSizeArr = new long[2];
    private int blockCounts;
    private File cacheFile;
    private File tempFile;
    private long curTime;

    public DownloadTask(String url, String filePath, String fileName, long contentLength, long blockLength, DownloadListener listener) {
        this.downloadUrl = url;
        this.filePath = filePath;
        this.fileName = fileName;
        this.contentLength = contentLength;
        this.blockLength = blockLength;
        this.listener = listener;
        Log.e("Range1", "bytes=" + contentLength + "-" + blockLength);
        initDownloadTasks();
        initCacheFile();
        initTempFile();


    }

    // SparseArray<DownloadAsyncTask> tasks = new SparseArray<>();
    ArrayList<DownloadAsyncTask> tasks = new ArrayList<>();

    private void initDownloadTasks() {

        blockFileSizeArr = new long[2];
        // 若是传入的每块文件大小 > 总文件大小  ,则以总文件大小为准
        blockLength = blockLength > contentLength ? contentLength : blockLength;
        long lastBlockFileSize = getLastBlockFileSize(contentLength, blockLength);
        Log.e("Range1", "bytes=" + lastBlockFileSize + "-" + blockLength);
        // 若是设置的每一块大小 小于一次写入时的缓冲区大小，则以缓冲区大小为主。
        blockFileSizeArr[0] = blockLength <= BUFFER || blockLength <= 0 ? BUFFER : blockLength;
        blockFileSizeArr[1] = lastBlockFileSize;
        // lastBlockFileSize <= BUFFER || lastBlockFileSize <= 0 ? BUFFER : lastBlockFileSize;

        blockCounts = getDefaultBlockCount(contentLength, blockFileSizeArr[0]);


        // countDownLatch = new CountDownLatch(blockCounts);

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

    private void initTempFile() {
        tempFile = new File(filePath, fileName + TEMP_FILE_SUFFIX);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(cacheFile, "rwd");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < blockCounts; i++) {
                stringBuilder.append(BLOCK_DEFAULT);
            }
            // 前面 blockCounts 个字节 记录块下载的信息， 后面才是每块已经下载的大小
            raf.seek(blockCounts);
            raf.writeBytes(stringBuilder.toString());
            raf.close();
            raf = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileUtils.createOrExistsFile(tempFile);
    }

    private void initCacheFile() {
        cacheFile = new File(filePath, fileName + CACHE_FILE_SUFFIX);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(cacheFile, "rwd");
            raf.setLength(contentLength);
            raf.close();
            raf = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setMaxTaskCounts(int maxTaskCounts) {
        executor.setMaxRequests(maxTaskCounts);
    }

    public int getMaxTaskCounts() {
        return executor.getMaxRequests();
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 超时时间  默认 3分钟
     */
    private long DEFAULT_TIME_OUT = 1000 * 60 * 3;
    private long timeout = DEFAULT_TIME_OUT;
    private volatile AtomicInteger retryTimes = new AtomicInteger(10);
    private volatile AtomicInteger finishedBlockCount = new AtomicInteger(0);
    private volatile AtomicInteger successBlockCount = new AtomicInteger(0);
    private volatile AtomicLong currentLength = new AtomicLong(0);

    public void startDownload() {

        isSuccess.set(true);
       // isFailed.set(false);
        if (null != listener && mStatus != RUNNING) {
            listener.onStart();
        }
        mStatus = RUNNING;

        // Log.e("DownloadExecutor ", " blockCounts: "+blockCounts+ " finishedBlockCount: "+finishedBlockCount.get() );
        finishedBlockCount.set(0);
        successBlockCount.set(0);
        currentLength.set(0);
        currentLength.addAndGet(readCurrentLength());
        for (int i = 0; i < blockCounts; i++) {
            boolean isAlreadyDownload = isAlreadyDownload(i);
            long blockLength = i == blockCounts - 1 ? blockFileSizeArr[1] : blockFileSizeArr[0];

            if (!isAlreadyDownload(i)) {

                int blockNum = i;
                long current = getCurrentLengthByBlockNum(blockNum);
                Log.e("DownloadExecutor ", "blockNum: " + blockNum + " alreadyDownload: " + current);
                // 对应块号应该的开始位置。
                long startIndex = blockNum * blockFileSizeArr[0] + current;
                // 获取对应块号现有的长度
                long endIndex = blockNum == blockCounts - 1 ? startIndex + blockFileSizeArr[1] : startIndex + blockFileSizeArr[0] - 1;
                //   Log.e("downloadTASK", "RANGE, 206: blockNum:" + blockNum + "startIndex= " + startIndex + "endIndex= " + endIndex);

                DownloadAsyncTask task = new DownloadAsyncTask(i, startIndex, endIndex);
                // tasks.put(i, task);
                tasks.add(task);
                task.executeOnExecutor(executor);
                //task.executeOnExecutor(Executors.newSingleThreadExecutor());
                // task.execute();
                Log.e("Range1", "bytes=" + startIndex + "-" + endIndex);


                // Log.e("DownloadExecutor ", " blockCounts: " + blockCounts + " tasks: " + tasks.size() + " blockId: " + i);
            } else {
                finishedBlockCount.incrementAndGet();
                successBlockCount.incrementAndGet();
                if (finishedBlockCount.get() == blockCounts){

                    downloadSuccess();
                }
                // currentLength.addAndGet(blockLength);
            }


        }

    }

    /**
     * 将第0块作为 总进度 ，其后后面记录的是每一块的当前长度
     *
     * @param blockNum
     * @return
     */
    private long getCurrentLengthByBlockNum(int blockNum) {
        blockNum = blockNum + 1;
        RandomAccessFile tempRaf = null;
        int blockBufferLength = BLOCK_DEFAULT.length();
        try {
            tempRaf = new RandomAccessFile(tempFile, "rwd");
            tempRaf.seek(blockCounts + blockNum * blockBufferLength);
            byte[] bytes = new byte[blockBufferLength];
            // read后，指针会默认往后移， 移动的长度与所读数据长度相同
            int len;
            len = tempRaf.read(bytes);
            //String hex = NumberConvert.bytesToHexString(bytes);
            String hex = new String(bytes);
            Log.e("download ", " isAlreadyDownload: " + hex);
            return len == -1 ? 0 : Integer.parseInt(hex, 16);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private synchronized long writeCurrentLengthByBlockNum(int blockNum, long currentBlockLength) {
        // 预留出第一块的空间，记录当前所有任务下载的总长度
        blockNum = blockNum + 1;
        RandomAccessFile tempRaf = null;
        int blockBufferLength = BLOCK_DEFAULT.length();
        try {
            tempRaf = new RandomAccessFile(tempFile, "rwd");
            tempRaf.seek(blockCounts + blockNum * blockBufferLength);
            byte[] bytes = new byte[blockBufferLength];
            // read后，指针会默认往后移， 移动的长度与所读数据长度相同
            int len;
            String currentLengthHexStr = NumberConvert.toHexStrWithAddZero(currentBlockLength, blockBufferLength);
            Log.e("DownloadExecutor  id:", blockNum + "  current: " + currentLengthHexStr);
            tempRaf.write(currentLengthHexStr.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long writeCurrentLength(long currentTotalBlockLength) {
        return writeCurrentLengthByBlockNum(-1, currentTotalBlockLength);
    }

    private long readCurrentLength() {
        return getCurrentLengthByBlockNum(-1);
    }

    private boolean isAlreadyDownload(int blockId) {
        RandomAccessFile tempRaf = null;
        try {
            tempRaf = new RandomAccessFile(tempFile, "rwd");
            tempRaf.seek(blockId);
            byte[] bytes = new byte[1];
            // read后，指针会默认往后移， 移动的长度与所读数据长度相同
            int len = 0;
            len = tempRaf.read(bytes);
            Log.e("DownloadExecutor ", " isAlreadyDownload: " + new String(bytes) + " " + ((byte) (1 & 0xff) == bytes[0]));
            return len != -1 && (byte) (1 & 0xff) == bytes[0];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


  /*  private boolean retryDownload(DownloadAsyncTask task, long timeout, TimeUnit unit) {
        try {
            if (timeout > 0) {
                boolean isSuccess = task.get(timeout, unit);
                task.cancel(true);
                return isSuccess;
            } else {
                return task.get();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return false;
    }*/

    public void pauseDownload() {
        // mStatus = Status.FINISHED;
        mStatus = PAUSED;
        for (DownloadAsyncTask task : tasks) {
            if (null != task) {
                task.cancel(true);
            }
        }


        finishedBlockCount.set(0);
        currentLength.set(0);
        executor.readyTasks.clear();
        executor.runningTasks.clear();
    }

    public void cancelDownload() {
        pauseDownload();

        mStatus = CANCELED;
        FileUtils.deleteFile(cacheFile);
        FileUtils.deleteFile(tempFile);
    }


    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that {@link AsyncTask#onPostExecute} has finished.
         */
        FINISHED,

        PAUSED,
        CANCELED,
    }

    /**
     * Returns the current status of this task.
     *
     * @return The current status.
     */
    public final Status getStatus() {
        return mStatus;
    }

    private volatile String errorMsg;
    // 表示全部成功
    private volatile AtomicBoolean isSuccess = new AtomicBoolean(true);
    // 表示全部失败
    private volatile AtomicBoolean isFailed = new AtomicBoolean(false);

    class DownloadAsyncTask extends AsyncTask<String, Long, Boolean> {
        private long startIndex;
        private long endIndex;
        private int bloclId;

        DownloadAsyncTask(int bloclId, long startIndex, long endIndex) {
            this.bloclId = bloclId;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

           /* if (!isSuccess && retryTimes <= retryTimes.get()) {
                DownloadAsyncTask newTask = new DownloadAsyncTask(bloclId, startIndex, endIndex);
                newTask.addRetryTimes();
                newTask.executeOnExecutor(executor);
                retryDownload(newTask, timeout, TimeUnit.SECONDS);
            }*/

            Log.e("download", "onPostExecute=" + result);
            isSuccess.set(isSuccess.get() && result);
            // 只要有一块能下载下来，便不认为失败，只是可能有的下不下来。
            isFailed.set(isFailed.get() || result);
            finishedBlockCount.incrementAndGet();

            if (result) {
                successBlockCount.incrementAndGet();
            }

            if (null != listener && mStatus == RUNNING) {
                // int notFinished = executor.readyTasksCount()+executor.runningTasksCount();
                listener.onProgress(successBlockCount.get() * 100 / blockCounts);
            }

            if (finishedBlockCount.get() == blockCounts) {
                if (null != listener) {
                    // 所有的块都下载成功
                    if (isSuccess.get()) {
                        downloadSuccess();
                    }

                    // 所有的块都结束了，然而没有一块成功，则认为失败
                    if (!isFailed.get()) {
                       /* while (timeout>){
                            startDownload();
                        }*/
                        failed();
                    }
                    // 并不是完全下载成功 且 并未完全失败
                    if (!isSuccess.get() && isFailed.get() && mStatus != DownloadTask.Status.FINISHED) {
                        // 进行多次尝试
                        if (retryTimes.decrementAndGet() >= 0) {
                            startDownload();
                        } else {
                            // 暂停下载。
                            // startDownload();
                          /*  pauseDownload();
                            // 释放线程给其他下载任务。
                             DownloadManager.getInstance().recyclerTask(DownloadTask.this);*/
                            failed();
                        }
                    }
                }

            }
        }

        private void failed() {
            FileUtils.deleteFile(cacheFile);
            FileUtils.deleteFile(tempFile);
            DownloadManager.getInstance().recyclerTask(DownloadTask.this);
            DownloadManager.getInstance().removeTask(downloadUrl);
            listener.onFailed(errorMsg);
            mStatus = DownloadTask.Status.FINISHED;
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // 只有  主动暂停和取消状态 才会由子线程的cancel回调结果，进入结束状态
            // 重试策略 中的调用task,cancel(true) 进行取消时，  状态为 Running
            if (null != listener) {
                if (mStatus != DownloadTask.Status.FINISHED) {
                    if (mStatus == PAUSED) {
                        listener.onPaused();
                        mStatus = DownloadTask.Status.FINISHED;
                        DownloadManager.getInstance().recyclerTask(DownloadTask.this);
                    }
                } else {
                    if (mStatus == CANCELED) {
                        listener.onCanceled();
                        mStatus = DownloadTask.Status.FINISHED;
                        DownloadManager.getInstance().recyclerTask(DownloadTask.this);
                    }
                }
            }

        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);

            //   Log.e("download",Thread.currentThread().getName());
            if (System.currentTimeMillis() - curTime > 500) {
                if (null != listener && mStatus == RUNNING) {
                    // int notFinished = executor.readyTasksCount()+executor.runningTasksCount();
                    listener.onProgress((int) (currentLength.get() * 100 / contentLength));
                }
                curTime = System.currentTimeMillis();
            }


        }


        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            RandomAccessFile tempRaf = null;
            InputStream inputStream = null;


            try {
                tempRaf = new RandomAccessFile(tempFile, "rwd");
                raf = new RandomAccessFile(cacheFile, "rwd");

                  /*  byte[] bytes = new byte[1];
                    // read后，指针会默认往后移， 移动的长度与所读数据长度相同
                    int len = tempRaf.read(bytes);
                    // 该块已经写入
                    if (len != -1 && "1".equals(new String(bytes))) {
                        return true;
                    }*/

                //  StringBuilder sb = getDownloadUrl();
                URL url = new URL(downloadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);
                // connection.setReadTimeout(30000);
                connection.setRequestMethod("GET");

                //设置下载起始位置
                /*  long start = threaInfo.getStartIndex() + threaInfo.getFinished();*/
                connection.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
                Log.e("download", "bytes=" + startIndex + "-" + endIndex);
                //设置写入位置
                // File file = new File(taskInfo.getFilePath(), taskInfo.getFileName());


                raf.seek(startIndex);
                tempRaf.seek(bloclId);

                //  Log.e("downlaod", "inputStream  contentLength  " + (connection.getContentLength()));


                //开始下载
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                     return writeByBlock(raf, tempRaf, inputStream);
                   // return writeByBreakPoint(raf, tempRaf, inputStream, connection);

                } else {

                    errorMsg = "服务器响应错误！url：" + url + "response：" + connection.toString();
                    Log.e("download", "无网络！");
                    return false;

                }

            } catch (IOException e) {
                errorMsg = e.getMessage();
                Log.e("download", "Exception！ 块号： " + bloclId + " bytes=" + startIndex + "-" + endIndex, e);
                e.printStackTrace();
                return false;
            } finally {
                try {
                    inputStream.close();
                    raf.close();
                    tempRaf.close();
                    connection.disconnect();
                    // DownloadManager.getInstance().downloadDispacther.finished(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // }
            // return false;

    /*    private void printlnHeaders(Response response) {
            okhttp3.Headers responseHeaders = response.headers();
            int responseHeadersLength = responseHeaders.size();
            for (int i = 0; i < responseHeadersLength; i++) {
                String headerName = responseHeaders.name(i);
                String headerValue = responseHeaders.get(headerName);
                Log.e(TAG, "downloadTASK:  headerName" + headerName + " headerValue: " + headerValue);
            }
        }


        }*/


            //  private String URL = "";


        }







        private synchronized Boolean writeByBreakPoint(RandomAccessFile raf, RandomAccessFile tempRaf, InputStream inputStream, HttpURLConnection connection) throws IOException {
            Log.e("download", "writeByBreakPoint！ 块号： " + bloclId + " inputStream=" + inputStream.available());


            int total = 0;
            int len;
            byte[] buffer = new byte[BUFFER];
            // 读取流
            while ((len = inputStream.read(buffer)) != -1) {

                if (mStatus == PAUSED || mStatus == CANCELED) {
                    writeCurrentLengthByBlockNum(bloclId, total);
                    FileUtils.close(inputStream, raf, tempRaf);

                    if (null != connection) {
                        connection.disconnect();
                    }

                    cancel(true);
                    return false;
                }
                raf.write(buffer, 0, len);
                total += len;
                currentLength.addAndGet(len);
                // 网络状况不好，丢包率高的时候，会出现 currentLength > contentLength 的情况.
                // 怀疑有重复写入
                // publishProgress(currentLength.get());
            }

            Log.e("download", "writeByBreakPoint！ 块号： " + bloclId + " total=" + total);
            writeCurrentLength(currentLength.get());
            writeCurrentLengthByBlockNum(bloclId, total);
            tempRaf.seek(bloclId);
            tempRaf.writeByte(1);
            return true;
        }

        @NonNull
        private Boolean writeByBlock(RandomAccessFile raf, RandomAccessFile tempRaf, InputStream inputStream) throws IOException {
            Log.e("download", "inputStream    " + (inputStream == null) + "   length：" + inputStream.available());
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[BUFFER];
            int rc = -1;
            while ((rc = inputStream.read(buff)) != -1) {
                swapStream.write(buff, 0, rc);
                if (isCancelled()) {
                    return false;
                }
                 /*   if (mStatus == DownloadTask.Status.PAUSED) {
                        inputStream.close();
                        raf.close();
                        tempRaf.close();
                        connection.disconnect();
                        cancel(true);
                        return false;
                    }*/
            }
            byte[] in2b = swapStream.toByteArray();
            swapStream.close();

            Log.e("download", bloclId + "inputStream length   " + in2b.length);

            int length = in2b.length;
            long blocklength = endIndex == contentLength
                    ? endIndex - startIndex
                    : endIndex - startIndex + 1;
            // 是不是所期望的每一段下载的长度。
            boolean isExpectedLength = length == blocklength;

            Log.e("download", bloclId + "isExpectedLength length   " + blocklength);
            if (isExpectedLength) {
                try {
                    raf.write(in2b, 0, length);
                    tempRaf.writeByte(1);
                    return true;
                } catch (IOException e) {
                    errorMsg = "写入文件时出错！块号：" + bloclId;
                    Log.e("download", "block length IOException   " + (endIndex - startIndex + 1), e);
                    return false;
                    // callback.threadDownLoadFailedd(e.getMessage());
                }
            } else {
                errorMsg = "不是所期望的数据块长度！块号：" + bloclId + "，该块长度应为：" + blocklength + " ，实际下载长度：" + length;
                return false;

            }
        }

     /*   private StringBuilder getDownloadUrl(String url, long start, long endIndex) {
            StringBuilder requestParms = new StringBuilder(url);
            requestParms
                    .append("&rangeKey=")
                    .append("bytes:" + start + "-" + endIndex);

            return new StringBuilder()
                    .append("下载路径");
        }*/




    }

    private void downloadSuccess() {
        File file = new File(filePath, fileName);
        cacheFile.renameTo(file);
        FileUtils.deleteFile(tempFile);
        listener.onSuccess(file.getAbsolutePath());
        DownloadManager.getInstance().recyclerTask(DownloadTask.this);
        DownloadManager.getInstance().removeTask(downloadUrl);
        mStatus = Status.FINISHED;
    }
}



