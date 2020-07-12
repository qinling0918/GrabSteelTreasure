package com.zgw.qgb.network.download;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseLongArray;


import com.zgw.qgb.network.download.listener.DownloadListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.CANCELED;
import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.FAILED;
import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.PAUSED;
import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.PROGRESS;
import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.SUCCESS;
import static com.zgw.qgb.network.download.DownloadMode.SINGLE_THREAD;


/**
 * Created by Tsinling on 2017/12/21 17:09.
 */
public class DownloadTask implements/* Runnable,*/ Handler.Callback, Callback {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30000;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "DownloadManager WorkerThread#" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>();




    /**
     * 设置线程池最大任务数
     *
     * @param maxPoolSize
     */
    public void setMaxPoolSize(int maxPoolSize) {
        if (this.maxPoolSize != maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            // 重置为null ，方便根据新传入的值重新生成executorService
            executorService = null;
        }

    }

    /**
     * 设置每个线程保活时间
     *
     * @param aliveTime 单位为 毫秒
     */
    public void setAliveTime(long aliveTime) {
        if (this.aliveTime != aliveTime) {
            this.aliveTime = aliveTime;
            executorService = null;
        }
    }
    private ThreadPoolExecutor executorService;

    public synchronized ThreadPoolExecutor executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(maxPoolSize, maxPoolSize, aliveTime, TimeUnit.MILLISECONDS,
                    sPoolWorkQueue, sThreadFactory);
        }

        return executorService;
    }
    private int maxPoolSize = MAXIMUM_POOL_SIZE;
    private long aliveTime = KEEP_ALIVE_SECONDS;
    /**
     * 判断是否是最后一个任务
     */
    protected boolean isTaskEnd() {
        return executorService.getActiveCount() == 0;
    }

    /**
     * 关闭线程池，不在接受新的任务，会把已接受的任务执行玩
     */
    private void shutdown() {
        executorService().shutdown();
        executorService = null;
    }




    private static final String TAG = "DownloadTask";
    private DownloadListener listener;
    private ArrayList<Call> calls;
    // private boolean isCanceled = false;
    //  private boolean isPaused = false;

    //private int lastProgress;
    //private long blockLength;
    //private int blockCount;
    private String downloadUrl;
    private String mfilePath;
    private String mfileName;
    private DownloadMode downloadMode;
    private boolean supportRanage = true;
    private CountDownLatch countDownLatch;
    private long contentLength;
    private final ArrayList<DownloadRunnable> mRunnables;

    public DownloadTask(DownloadManager downloadManager, String url, String mfilePath, String mfileName,long contentLength) {
        this.downloadUrl = url;
        // manager = new DownLoadInfoManager(mfilePath, mfileName);
        this.mfilePath = mfilePath;
        this.mfileName = mfileName;
        sHandler = getMainHandler();
        mRunnables = new ArrayList<>();
        this.contentLength = contentLength;
        Log.e("downloadTASK", "DownloadTask: " + toString());

    }

    @Override
    public String toString() {
        return "DownloadTask{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", mfilePath='" + mfilePath + '\'' +
                ", mfileName='" + mfileName + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    // 错误信息
    private String errorMsg;

    private DownLoadInfoManager manager;


    /**
     * 检查url的合法性
     *
     * @param url url链接
     * @return true ：合法
     */
    private boolean isUrl(String url) {
        return true;
       /* if (url == null) {
            return false;
        }
        String URL_REGEX = "^(http|https|ftp)//://([a-zA-Z0-9//.//-]+(//:[a-zA-"
                + "Z0-9//.&%//$//-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{"
                + "2}|[1-9]{1}[0-9]{1}|[1-9])//.(25[0-5]|2[0-4][0-9]|[0-1]{1}"
                + "[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)//.(25[0-5]|2[0-4][0-9]|"
                + "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)//.(25[0-5]|2[0-"
                + "4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0"
                + "-9//-]+//.)*[a-zA-Z0-9//-]+//.[a-zA-Z]{2,4})(//:[0-9]+)?(/" +
                "[^/][a-zA-Z0-9//.//,//?//'///////+&%//$//=~_//-@]*)*$";
        Pattern pattern = Pattern.compile(URL_REGEX);
        return pattern.matcher(url).matches();*/
    }

    /**
     * 根据现在每一块的信息进度，然后再按照现有进度下载
     * @param reminsBlocks
     */
    @SuppressWarnings("uncheck")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void downloads(SparseLongArray reminsBlocks) {
        long[] oneBlockLength = manager.getBlockFileSizeArr();
        Log.e("downloadTASK", "oneBlockLength: " + oneBlockLength[0]);
        int blockCounts = manager.getTotalBlockCount();
       // countDownLatch = new CountDownLatch(blockCounts);
        for (int i = 0; i < reminsBlocks.size(); i++) {
            countDownLatch = new CountDownLatch(1);
            int blockNum = reminsBlocks.keyAt(i);
            // 对应块号应该的开始位置。
            long startIndex = blockNum * oneBlockLength[0];
            // 获取对应块号现有的长度
            long currentLength = reminsBlocks.get(blockNum, 0);
            long finalStartIndex = currentLength + startIndex;
            long endIndex = blockNum == blockCounts - 1
                    ? startIndex + oneBlockLength[1]
                    : startIndex + oneBlockLength[0] - 1;
            Log.e("downloadTASK", "RANGE, 206: blockNum:" + blockNum + "startIndex= " + startIndex + "finalStartIndex= " + finalStartIndex + "endIndex= " + endIndex);


            download(finalStartIndex, endIndex, countDownLatch);
            waitCountDown();
        }

       // waitCountDown();
    }

    private void waitCountDown() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 全量下载
     */
    private void download() {
        OkHttpClient client = getClient();
        Call call = client.newCall(getRequest());
        calls = new ArrayList<>();
        calls.add(call);
        call.enqueue(this);
    }

    /**
     * 获取请求信息
     * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
     * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
     * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
     * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
     *
     * @param startIndex 起始请求值
     * @param endIndex   结束值
     * @return 请求头信息
     */
    private Request getRequest(long startIndex, long endIndex) {

        if (supportRanage) {
            return new Request.Builder().header("RANGE", "bytes=" + startIndex + "-" + endIndex)
                    .url(downloadUrl)
                    .build();
        } else {
            return new Request.Builder().url(downloadUrl).build();
        }
    }

    private Request getRequest() {
        return new Request.Builder().url(downloadUrl).build();
    }


    /***
     * @param startIndex 此次下载的开始位置
     * @param endIndex  此次下载的文件结束位置
     */
    private void download(long startIndex, long endIndex) {
        download(startIndex, endIndex, null);
    }

    private void download(final long startIndex, final long endIndex, final CountDownLatch countDownLatch) {
        Log.e("downloadTASK", "download, 206: startIndex= " + startIndex + "endIndex= " + endIndex);

        OkHttpClient client = getClient();
        /*
        okhttp 已经有了连接池， 多余的将会添加到连接池的队列中
        默认单个链接 5个，同时最大请求为64.若需要修改，则可以直接调用
        client.dispatcher().setMaxRequestsPerHost(5);
        client.dispatcher().setMaxRequests(64);*/

        Call call = client.newCall(getRequest(startIndex, endIndex));
        calls = new ArrayList<>();
        calls.add(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendErrorMessage("连接服务器失败，请重试。 " + e.getMessage());
                closeCountDownLacth(countDownLatch);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 请求部分资源成功码 206 ，表示服务器支持断点续传
                // 200 为成功响应码
                Log.e("downloadTASK", "onResponse, 206: " + response.code());
                printlnHeaders(response);
                // 正常来说应该是206，可是此处很奇葩。
                if (response.code() != 206) {
                    String responseMsg = response.message();
                    responseMsg = responseMsg == null ? "" : " " + responseMsg;
                    sendErrorMessage("服务器断点续传失败" + responseMsg + "RANGE bytes=" + startIndex + "-" + endIndex);
                    Log.e("downloadTASK", "onResponse, 206: " + errorMsg);
                    closeCountDownLacth(countDownLatch);
                }

                InputStream is = response.body().byteStream();
                if (is == null) {
                    errorMsg = "服务器返回值为空";
                    sendMessage(FAILED);
                    closeCountDownLacth(countDownLatch);
                }
                manager.writeToCacheFile(is, startIndex, endIndex);

                if (null != countDownLatch) {
                    countDownLatch.countDown();
                }
            }
        });


    }

    @NonNull
    private OkHttpClient getClient() {
       return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
               .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }


    private void closeCountDownLacth(CountDownLatch countDownLatch) {
        if (null != countDownLatch) {
            for (int i = 0; i < countDownLatch.getCount(); i++) {
                countDownLatch.countDown();
            }
        }
    }

    public void pauseDownload() {
        manager.setStatus(PAUSED);
        cancelCall();
    }

    public void cancelDownload() {
        manager.setStatus(CANCELED);
        cancelCall();

    }

    private void cancelCall() {
        for (DownloadRunnable runnable : mRunnables) {
            runnable.cancelCall();
            manager.setStatus(CANCELED);
        }
    }

  /*  private void cancelCall() {
        if (calls != null) {
            for (Call call : calls) {
                call.cancel();
            }
        }
    }*/
/*
    public void cancel(){
        for (DownloadRunnable runnable : mRunnables) {
            runnable.cancelCall();
            manager.setStatus(CANCELED);
        }
    }
*/

    private void getContentLengthAsync(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .method("HEAD", null).build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    manager.setStatus(FAILED);
                    sendErrorMessage(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response != null && response.isSuccessful()) {
                        // printlnHeaders(response);
                         contentLength = Long.valueOf(response.header("Content-Length"));
                        // bytes 表示支持， none 不支持
//                supportRanage = "bytes".equals(acceptRanges);
                        //   if (!supportRanage) {
                        // sendErrorMessage("服务器不支持断点续传功能");
                        //   }
                        response.close();
                        doDownload();
                    }
                }
            });

    }
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .method("HEAD", null).build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                // printlnHeaders(response);
                long contentLength = Long.valueOf(response.header("Content-Length"));
                String acceptRanges = response.header("Accept-Ranges");
                // bytes 表示支持， none 不支持
//                supportRanage = "bytes".equals(acceptRanges);
                //   if (!supportRanage) {
                // sendErrorMessage("服务器不支持断点续传功能");
                //   }
                response.close();
                return contentLength;
            }
        } catch (IOException e) {
            String errorMsg = TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "服务器连接失败，请重试！";
            Log.e(TAG, "getContentLength", e);
            sendErrorMessage(errorMsg);
            e.printStackTrace();
        }
        return 0;
    }
    /**
     * 打印响应头信息
     *
     * @param response okhttp 响应
     */
    private void printlnHeaders(Response response) {
        Headers responseHeaders = response.headers();
        int responseHeadersLength = responseHeaders.size();
        for (int i = 0; i < responseHeadersLength; i++) {
            String headerName = responseHeaders.name(i);
            String headerValue = responseHeaders.get(headerName);
            Log.e(TAG, "downloadTASK:  headerName" + headerName + " headerValue: " + headerValue);
        }
    }


    private static Handler sHandler;

    private Handler getMainHandler() {
        synchronized (AsyncTask.class) {
            if (sHandler == null) {
                sHandler = new Handler(Looper.getMainLooper(), this);
            }
            return sHandler;
        }
    }




    public void setOnDownloadListener(DownloadListener listener) {
        this.listener = listener;
    }

    public void setOnDownloadMode(DownloadMode downloadMode) {
        this.downloadMode = downloadMode;
    }

    public void start() {

        if (!isUrl(downloadUrl)) {
            sendErrorMessage("URL不合法,url: " + downloadUrl);
            return;
        }

        downloadMode = downloadMode==null ?SINGLE_THREAD:downloadMode;
        switch (downloadMode) {
            case MULTI_THREAD_BLOCK_COUNT:
                manager = new DownLoadInfoManager(mfilePath, mfileName, contentLength, (int) downloadMode.getValue());
                break;
            case MULTI_THREAD_BLOCK_LENGTH:
                manager = new DownLoadInfoManager(mfilePath, mfileName, contentLength, downloadMode.getValue());
                break;
            case SINGLE_THREAD:
            default:
                manager = new DownLoadInfoManager(mfilePath, mfileName);
                break;
        }

        manager.setHandler(sHandler);
        manager.setStatus(PROGRESS);

      // contentLength = getContentLength(downloadUrl);
        if (contentLength <= 0) {
             getContentLengthAsync(downloadUrl);
        }else{
            doDownload();
        }

        Log.e("contentLength", contentLength + "");


    }

    private void doDownload() {


        // 文件下载状态 为已完成
        if (manager.isFileExists()) {
            //setErrorMsg("远程文件(服务器端目标文件)不存在");
            // return SUCCESS;
            sendMessage(SUCCESS);
            return;
        }

        long totalFileSize = manager.getTotalFileSize();

        // 远程文件与临时文件记录的文件长度不一致.则或不支持断点续传则单线程下载
        if (totalFileSize != contentLength || !supportRanage) {
            // 删除临时文件。
            manager.deleteTempFile();
            // 不再多线程下载 直接全部下载，
            Log.e("contentLength", "contentLength read from temp file: " + totalFileSize);
            download();
            return;
            // return manager.getStatus();
        }
        // 单线程（且支持断点续传） ，文件已经下载了一部分。则按照缓存文件目前已下载的长度作为起始位置下载
        if (manager.getTotalBlockCount() == 1) {
            File downloadFile = manager.getDownloadCacheFile();
            long startIndex = null == downloadFile ? 0 : downloadFile.length();
            download(startIndex, contentLength);
            return;
        }

        // 此处文件不存在，
        // 需要下载的剩余块数为0 ，则意味着获取剩余块数失败
        SparseLongArray reminsBlocks = manager.getRemainingBlocks();
        // 若是获取剩余块数失败，或者文件不存在，则重新开始下载。
        if (null == reminsBlocks || reminsBlocks.size() == 0) {
            //  return DownloadStatus.FAILED.setMsg("从临时记录文件中读取信息失败");
            manager.deleteCacheFile();
            download();
            return;
        }
        // 多线程下载
        downloads(reminsBlocks);
       // downloads();
        Log.e("DownloadTask", "doInBackground: " + errorMsg);
    }

    /**
     *  重新下载每一块
     */
    private void downloads() {

        long[] oneBlockLength = manager.getBlockFileSizeArr();
        Log.e("downloadTASK", "oneBlockLength: " + oneBlockLength[0]);
        int blockCounts = manager.getTotalBlockCount();
       // countDownLatch = new CountDownLatch(blockCounts);
        for (int i = 0; i < blockCounts; i++) {
            int blockNum = i;
            // 对应块号应该的开始位置。
            long startIndex = blockNum * oneBlockLength[0];
            // 获取对应块号现有的长度
            long endIndex = blockNum == blockCounts - 1
                    ? startIndex + oneBlockLength[1]
                    : startIndex + oneBlockLength[0] - 1;
            Log.e("downloadTASK", "RANGE, 206: blockNum:" + blockNum + "startIndex= " + startIndex +  "endIndex= " + endIndex);

            DownloadRunnable downloadRunnable = new DownloadRunnable(downloadUrl,startIndex,endIndex,i,manager);
            executorService().execute(downloadRunnable);
            mRunnables.add(downloadRunnable);

            //  download(startIndex, endIndex, countDownLatch);
        }
    }
    /**
     * 此时在主线程了
     *
     * @param msg Message
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        int status = msg.what;
        if (null != listener) {
            switch (status) {
                case SUCCESS:
                  /*  File file = manager.getDownloadCacheFile();
                    String filePath =file == null ? mfilePath + File.separator + mfileName : file
                            .getAbsolutePath();*/
                    listener.onSuccess( mfilePath + File.separator + mfileName);
                    setOnDownloadListener(null);
                    break;
                case FAILED:
                    listener.onFailed( errorMsg);
                    setOnDownloadListener(null);
                    break;
                case PROGRESS:
                    long currentLength = manager.currentFileSize.get();
                    int progress = (int) ((currentLength * 100) / contentLength);
                    listener.onProgress( progress);
                    break;
                case PAUSED:
                    // listener.onPaused(downloadUrl);
                    setOnDownloadListener(null);
                    break;
                case CANCELED:
                    // listener.onCanceled(downloadUrl);
                    setOnDownloadListener(null);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void sendErrorMessage(String msg) {

        errorMsg = msg;
        sendMessage(FAILED);
    }

    private void sendMessage(int status) {
        //发送暂停消息
        sHandler.sendEmptyMessage(status);
    }


    @Override
    public void onFailure(Call call, IOException e) {
        Log.e("downloadTASK", "onFailure, 200: " + e);
        sendErrorMessage("连接服务器失败，请重试。 " + e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 200) {
            String responseMsg = response.message();
            responseMsg = responseMsg == null ? "" : " " + responseMsg;
            sendErrorMessage("服务器响应错误" + responseMsg);
            Log.e("downloadTASK", "onResponse, 200: " + responseMsg);
        }

        InputStream is = response.body().byteStream();
        if (is == null) {
            errorMsg = "服务器返回值为空";
            sendMessage(FAILED);
            closeCountDownLacth(countDownLatch);
        }
        manager.writeToCacheFile(is, 0, contentLength);

        if (null != countDownLatch) {
            countDownLatch.countDown();
        }
    }

  /*  public void startDownload() {
        manager.setStatus(PROGRESS);
    }*/
}