package com.zgw.qgb.network.download;

/**
 * Created by qinling on 2018/11/25 19:43
 * Description:
 */
public enum DownloadMode {
    /*** 单线程*/
    SINGLE_THREAD(1),
    /*** 多线程，自定义块数为5*/
    MULTI_THREAD_BLOCK_COUNT(5),
    /*** 多线程，每一块大小为512k */
    MULTI_THREAD_BLOCK_LENGTH(512 * 1024);

    public DownloadMode setValue(long value) {
        this.value = value;
        return this;
    }

    public long getValue() {
        return value;
    }

    private long value;

    DownloadMode(long value) {
        this.value = value;
    }

}
