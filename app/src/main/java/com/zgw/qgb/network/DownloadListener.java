package com.zgw.qgb.network;

/**
 * Name:DownloadListener
 * Created by Tsinling on 2017/12/21 16:59.
 * description:
 */

public interface DownloadListener {
    /**
     * 通知当前的下载进度
     * @param progress
     */
    void onProgress(float progress);

    /**
     * 通知下载成功
     * @param filePath
     */
    void onSuccess(String filePath);

    /**
     * 通知下载失败
     */
    void onFailed( String errorMsg);

    /**
     * 通知下载暂停
     */
    void onPaused();

    /**
     * 通知下载取消事件
     */
    void onCanceled();


}
