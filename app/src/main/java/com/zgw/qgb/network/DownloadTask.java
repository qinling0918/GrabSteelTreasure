package com.zgw.qgb.network;

import android.os.Handler;

import android.os.Message;
import android.util.Log;

import java.io.Closeable;

import java.io.File;

import java.io.IOException;

import java.io.InputStream;

import java.io.RandomAccessFile;

import okhttp3.Call;

import okhttp3.Response;



/**

 * 多线程下载任务

 * Created by Cheny on 2017/05/03.

 */

//http://blog.csdn.net/seu_calvin/article/details/52415337

public class DownloadTask extends Handler {


    private static final int NET_FAILURE = 0;  //网络请求失败
    private static final int IOEXCEPTION = 1;  //下载过程中,文件读写出现异常


    private final int THREAD_COUNT = 4;//下载线程数量

    private FilePoint mPoint;

    private long mFileLength;//文件大小



    private boolean isDownloading = false;//是否正在下载

    private int childCanleCount;//子线程取消数量

    private int childPauseCount;//子线程暂停数量

    private int childFinishCount;//子线程完成下载数量

    private HttpUtil mHttpUtil;//http网络通信工具

    private long[] mProgress;//各个子线程下载进度集合

    private File[] mCacheFiles;//各个子线程下载缓存数据文件

    private File mTmpFile;//临时占位文件

    private boolean pause;//是否暂停

    private boolean cancel;//是否取消下载



    private final int MSG_PROGRESS = 1;//进度

    private final int MSG_FINISH = 2;//完成下载

    private final int MSG_PAUSE = 3;//暂停

    private final int MSG_CANCEL = 4;//取消

    private final int MSG_FAILED = 5;//失败

    private DownloadListener mListner;//下载回调监听



    DownloadTask(FilePoint point, DownloadListener l) {

        this.mPoint = point;

        this.mListner = l;

        this.mProgress = new long[THREAD_COUNT];

        this.mCacheFiles = new File[THREAD_COUNT];

        this.mHttpUtil = HttpUtil.getInstance();

    }



    /**

     * 开始下载

     */

    public synchronized void start() {

       /* try {*/

            if (isDownloading) return;

            isDownloading = true;

        try {
            mHttpUtil.getContentLength(mPoint.getUrl(), new okhttp3.Callback() {

                @Override

                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() != 200) {

                        close(response.body());
                        //resetStutus();
                        sendErrorMessage(response.code(), response.message());
                        //sendMessage();
                        return;

                    }

                    // 获取资源大小
                    Log.d("onResponse", "onResponse: "+ response.body().contentType()+  "  "+response.body().contentLength());
                    mFileLength = response.body().contentLength();

                    close(response.body());

                    // 在本地创建一个与资源同样大小的文件来占位
                    if (mFileLength > 0){
                        mTmpFile = new File(mPoint.getFilePath(), mPoint.getFileName() + ".tmp");

                        if (!mTmpFile.getParentFile().exists()) mTmpFile.getParentFile().mkdirs();

                        RandomAccessFile tmpAccessFile = new RandomAccessFile(mTmpFile, "rw");

                        tmpAccessFile.setLength(mFileLength);

                    /*将下载任务分配给每个线程*/

                        long blockSize = mFileLength / THREAD_COUNT;// 计算每个线程理论上下载的数量.



                    /*为每个线程配置并分配任务*/

                        for (int threadId = 0; threadId < THREAD_COUNT; threadId++) {

                            long startIndex = threadId * blockSize; // 线程开始下载的位置

                            long endIndex = (threadId + 1) * blockSize - 1; // 线程结束下载的位置

                            if (threadId == (THREAD_COUNT - 1)) { // 如果是最后一个线程,将剩下的文件全部交给这个线程完成

                                endIndex = mFileLength - 1;

                            }

                            download(startIndex, endIndex, threadId);// 开启线程下载

                        }
                    }else{
                        sendErrorMessage(-1, " 返回文件长度小于0");
                    }


                }



                @Override

                public void onFailure(Call call, IOException e) {

                    //resetStutus(NET_FAILURE, e.getMessage());
                    //resetStutus();
                    sendErrorMessage(NET_FAILURE, e.getMessage());

                }

            });
        } catch (IOException e) {
            e.printStackTrace();

            sendErrorMessage(IOEXCEPTION, e.getMessage());
        }



    }

    private void sendErrorMessage(int code, String msg) {

        Message message = new Message();
        message.arg1 = code;
        message.obj = msg;
        message.what = MSG_FAILED;
        sendMessage(message);

    }


    /**

     * 下载

     * @param startIndex 下载起始位置

     * @param endIndex  下载结束位置

     * @param threadId 线程id

     * @throws IOException

     */

    public void download(final long startIndex, final long endIndex, final int threadId) throws IOException {

        long newStartIndex = startIndex;

        // 分段请求网络连接,分段将文件保存到本地.

        // 加载下载位置缓存数据文件

        final File cacheFile = new File(mPoint.getFilePath(), "thread" + threadId + "_" + mPoint.getFileName() + ".cache");

        mCacheFiles[threadId] = cacheFile;

        final RandomAccessFile cacheAccessFile = new RandomAccessFile(cacheFile, "rwd");

        if (cacheFile.exists()) {// 如果文件存在

            String startIndexStr = cacheAccessFile.readLine();

            try {

                newStartIndex = Integer.parseInt(startIndexStr);//重新设置下载起点

            } catch (NumberFormatException e) {

                e.printStackTrace();

            }

        }

        final long finalStartIndex = newStartIndex;

        mHttpUtil.downloadFileByRange(mPoint.getUrl(), finalStartIndex, endIndex, new okhttp3.Callback() {

            @Override

            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() != 206) {// 206：请求部分资源成功码，表示服务器支持断点续传

                    //resetStutus();
                    sendErrorMessage(response.code(), response.message());
                    return;

                }

                InputStream is = response.body().byteStream();// 获取流

                RandomAccessFile tmpAccessFile = new RandomAccessFile(mTmpFile, "rw");// 获取前面已创建的文件.

                tmpAccessFile.seek(finalStartIndex);// 文件写入的开始位置.

                  /*  将网络流中的文件写入本地*/

                byte[] buffer = new byte[1024 << 2];

                int length = -1;

                int total = 0;// 记录本次下载文件的大小

                long progress = 0;

                while ((length = is.read(buffer)) > 0) {//读取流

                    if (cancel) {

                        close(cacheAccessFile, is, response.body());//关闭资源

                        cleanFile(cacheFile);//删除对应缓存文件

                        sendMessage(MSG_CANCEL);

                        return;

                    }

                    if (pause) {

                        //关闭资源

                        close(cacheAccessFile, is, response.body());

                        //发送暂停消息

                        sendMessage(MSG_PAUSE);

                        return;

                    }

                    tmpAccessFile.write(buffer, 0, length);

                    total += length;

                    progress = finalStartIndex + total;



                    //将该线程最新完成下载的位置记录并保存到缓存数据文件中

                    //建议转成Base64码，防止数据被修改，导致下载文件出错（若真有这样的情况，这样的朋友可真是无聊透顶啊）

                    cacheAccessFile.seek(0);

                    cacheAccessFile.write((progress + "").getBytes("UTF-8"));

                    //发送进度消息

                    mProgress[threadId] = progress - startIndex;

                    sendMessage(MSG_PROGRESS);

                }

                //关闭资源

                close(cacheAccessFile, is, response.body());

                // 删除临时文件

                cleanFile(cacheFile);

                //发送完成消息

                sendMessage(MSG_FINISH);

            }



            @Override

            public void onFailure(Call call, IOException e) {


                isDownloading = false;


                //关闭资源
                close(cacheAccessFile);
                // 删除临时文件
                cleanFile(cacheFile);
                sendErrorMessage(NET_FAILURE,e.getMessage());

            }

        });

    }

    /**

     * 轮回消息回调

     *

     * @param msg

     */

    @Override

    public void handleMessage(Message msg) {

        super.handleMessage(msg);

        if (null == mListner) {

            return;

        }

        switch (msg.what) {

            case MSG_PROGRESS://进度

                long progress = 0;

                for (int i = 0, length = mProgress.length; i < length; i++) {

                    progress += mProgress[i];

                }

                mListner.onProgress(progress * 1.0f / mFileLength);

                break;

            case MSG_PAUSE://暂停

                childPauseCount++;

                if (childPauseCount % THREAD_COUNT != 0) return;//等待所有的线程完成暂停，真正意义的暂停，以下同理

                resetStutus();

                mListner.onPaused();

                break;

            case MSG_FINISH://完成

                childFinishCount++;

                if (childFinishCount % THREAD_COUNT != 0) return;

                mTmpFile.renameTo(new File(mPoint.getFilePath(), mPoint.getFileName()));//下载完毕后，重命名目标文件名

                resetStutus();

                mListner.onSuccess();

                break;

            case MSG_CANCEL://取消

                childCanleCount++;

                if (childCanleCount % THREAD_COUNT != 0) return;

                resetStutus();

                mProgress = new long[THREAD_COUNT];

                mListner.onCanceled();

                break;

            case MSG_FAILED://失败


                resetStutus();
                mListner.onFailed(msg.arg1, (String) msg.obj);

                break;
        }

    }



    /**

     * 发送消息到轮回器

     *

     * @param what

     */

    private void sendMessage(int what) {

        //发送暂停消息

        Message message = new Message();

        message.what = what;

        sendMessage(message);

    }





    /**

     * 关闭资源

     *

     * @param closeables

     */

    private void close(Closeable... closeables) {

        int length = closeables.length;

        try {

            for (int i = 0; i < length; i++) {

                Closeable closeable = closeables[i];

                if (null != closeable)

                    closeables[i].close();

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            for (int i = 0; i < length; i++) {

                closeables[i] = null;

            }

        }

    }



    /**

     * 暂停

     */

    public void pause() {

        pause = true;

    }



    /**

     * 取消

     */

    public void cancel() {

        cancel = true;

        cleanFile(mTmpFile);

        if (!isDownloading) {//针对非下载状态的取消，如暂停

            if (null != mListner) {

                cleanFile(mCacheFiles);

                resetStutus();

                mListner.onCanceled();

            }

        }

    }




    /**

     * 重置下载状态


     */

    private void resetStutus() {

        pause = false;

        cancel = false;

        isDownloading = false;

    }



    /**

     * 删除临时文件

     */

    private void cleanFile(File... files) {

        for (int i = 0, length = files.length; i < length; i++) {

            if (null != files[i])

                files[i].delete();

        }

    }



    /**

     * 获取下载状态

     * @return boolean

     */

    public boolean isDownloading() {

        return isDownloading;

    }

}


