package com.zgw.qgb.net.download_native;

import android.util.Log;

import com.zgw.qgb.network.download.NumberConvert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicLong;

public class FileDownloadThread extends Thread {

    private static final String TAG = FileDownloadThread.class.getSimpleName();
    public static final int BUFFER = 1024;
    public static final String MAX_BLOCK = "FFFFFFFF";
    private static final String CACHE_FILE_SUFFIX = ".cache";
    private static final String TEMP_FILE_SUFFIX = ".temp";

    private final long startIndex;
    private final long endIndex;
    private final File tempFile;
    private AtomicLong currentBytes;

    /**
     * 是否完成标志
     */
    private boolean isCompleted = false;
    /**
     * 已下载长度
     */
    private int downloadLength = 0;
    /**
     * 生成文件路径
     */
    private File file;
    /**
     * 文件地址
     */
    private URL downloadUrl;
    /**
     * 下载线程编号
     */
    private int threadId;

    /**
     * 文件块大小
     */

    public FileDownloadThread(URL downloadUrl, File file, File tempFile, long startIndex, long endIndex,
                              int threadId, AtomicLong currentBytes) {
        this.downloadUrl = downloadUrl;
        this.file = file;
        this.tempFile = tempFile;
        this.threadId = threadId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.currentBytes = currentBytes;

        // THREAD_POOL_EXECUTOR.execute(this);
    }

    @Override
    public void run() {

        //缓冲输入流
        BufferedInputStream bis = null;
        RandomAccessFile raf = null;

        RandomAccessFile tempRaf = null;
        try {
            tempRaf = new RandomAccessFile(tempFile, "rwd");
            //移到所处该线程所应使用的位置
            tempRaf.seek(getBlockStartIndex(threadId));
            long finalStartIndex = startIndex;
            try {
                finalStartIndex = Long.parseLong(readMessage(tempRaf, MAX_BLOCK.length()), 16);
            } catch (NumberFormatException e) {
                finalStartIndex = startIndex;
            }
            // 若是记录的当前快信息的起始长度 +1 == 结束值，则认为该块已经下载过
            if (finalStartIndex + 1 == endIndex) {
                return;
            }
            Log.d(TAG, "startIndex:" + finalStartIndex);

          //  long finalStartIndex = startIndex;
            URLConnection conn = downloadUrl.openConnection();
            //如果为 true，则在允许用户交互
            conn.setAllowUserInteraction(true);

         /*   long startPos = blockSize * (threadId - 1);//该线程开始下载位置
            long endPos = blockSize * threadId - 1;//该线程结束下载位置*/

            //用来设置请求头文报属性，这里设置range范围
            conn.setRequestProperty("Range", "bytes=" + finalStartIndex + "-" + endIndex);
            System.out.println(Thread.currentThread().getName() + "  bytes="
                    + finalStartIndex + "-" + endIndex);

            byte[] buffer = new byte[BUFFER];
            bis = new BufferedInputStream(conn.getInputStream());
            //能对file进行随意读写
            raf = new RandomAccessFile(file, "rwd");
            //移到所处该线程所应使用的位置
            raf.seek(startIndex);
            int len;
            //每次最多读1024个byte
            while ((len = bis.read(buffer, 0, BUFFER)) != -1) {
                //在buffer中写len个字节到文件file中
                raf.write(buffer, 0, len);
                downloadLength += len;
                currentBytes.addAndGet(len);
                tempRaf.seek(getBlockStartIndex(threadId));
                writeMessage(tempRaf, NumberConvert.toHexStrWithAddZero(downloadLength,MAX_BLOCK.length()));
            }
            isCompleted = true;
            Log.d(TAG, "current thread task has finished,all size:"
                    + downloadLength);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    //关闭输入流
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 此处定一的 MAX_BLOCK  为 ffffffff
     * // 第一块的起始位置 第二块    第三块 //
     * // ffffffff    ffffffff    fffffffff //
     *
     * @param threadId 块号 从1开始的。
     * @return 起始位置， 例如  第1块  应该是0 ，第二块  8， 第三块 16
     */
    // 记录每块下载进度的临时文件，设置为4个字节， 0-0xffffffff  = 4g
    private long getBlockStartIndex(int threadId) {
        return (threadId - 1) * MAX_BLOCK.length();
    }

    private String readMessage(RandomAccessFile tempRas, int length) throws IOException {
        //  byte[] bytes = new byte[length];
        byte[] bytes = new byte[length];
        // read后，指针会默认往后移， 移动的长度与所读数据长度相同
        int len = tempRas.read(bytes);
        // return len == -1 ? "" : NumberConvert.bytesToHexString(bytes);
        return len == -1 ? "" : new String(bytes);
    }

    /**
     * 写入到文件
     *
     * @param tempRas
     * @param msgHex
     * @throws IOException
     */
    private void writeMessage(RandomAccessFile tempRas, String msgHex) throws IOException {
        // tempRas.writeChars(msg);
        tempRas.write(msgHex.getBytes());
        //tempRas.write(NumberConvert.hexStringToBytes(msg));
    }

    public int getStringByteSize(String hexStr) {
        if (null == hexStr) {
            return 0;
        }
        int length = hexStr.length() / 2;
        return hexStr.length() % 2 == 0 ? length : length + 1;
    }

    /**
     * 查询是否下载完成
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * 返回已下载长度
     */
    public int getDownloadLength() {
        return downloadLength;
    }

}
