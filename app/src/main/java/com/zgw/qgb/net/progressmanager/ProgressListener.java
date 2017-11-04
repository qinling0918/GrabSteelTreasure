package com.zgw.qgb.net.progressmanager;


import com.zgw.qgb.net.progressmanager.body.ProgressInfo;

/**
 * Created by jess on 02/06/2017 18:23
 * Contact with jess.yan.effort@gmail.com
 */

public interface ProgressListener {
    /**
     * 进度监听
     *
     * @param progressInfo
     */
    void onProgress(ProgressInfo progressInfo);

    void onError(long id, Exception e);
}
