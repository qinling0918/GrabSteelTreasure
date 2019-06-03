package com.zgw.qgb.network.download.listener;

import java.io.File;

/**
 * Name:DownloadListener
 * Created by Tsinling on 2017/12/21 16:59.
 * description: 由于会涉及多个url 同时下载。
 * 则这里给出url，方便判断是哪一个url的回调
 */

public interface DownloadsListener {
    /**
     * 通知当前的下载进度
     * @param progress
     */
    void onProgress(String url, int progress, long contentLength, long currentBytes);

    /**
     * 通知下载成功
     */
    void onSuccess(String url, String filePath);

    /**
     * 通知下载失败
     */
    void onFailed(String url, String errorMsg);

    /**
     * 通知下载暂停
     */
   // void onPaused(String url);

    /**
     * 通知下载取消事件
     */
   // void onCanceled(String url);

}
