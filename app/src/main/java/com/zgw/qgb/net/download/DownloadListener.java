package com.zgw.qgb.net.download;

import java.io.File;

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
    void onProgress(int progress);

    /**
     * 通知下载成功
     */
    void onSuccess(File file);

    /**
     * 通知下载失败
     */
    void onFailed(int errorCode, String errorMsg);

    /**
     * 通知下载暂停
     */
    void onPaused(File file);

    /**
     * 通知下载取消事件
     */
    void onCanceled(File file);

}
