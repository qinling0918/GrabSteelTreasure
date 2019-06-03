package com.zgw.qgb.download;

import com.zgw.qgb.download.bean.ThreadInfo;

/**
 * Created by qinling on 2018/12/5 12:47
 * Description:
 */
public interface DownloadCallBack {
    /**
     * 暂停回调
     * @param threadInfo
     */
    void pauseCallBack(ThreadInfo threadInfo);
    /**
     * 下载进度
     * @param length
     */
    void progressCallBack(int length);

    /**
     * 线程下载完毕
     * @param threadInfo
     */
    void threadDownLoadFinished(ThreadInfo threadInfo);

}
