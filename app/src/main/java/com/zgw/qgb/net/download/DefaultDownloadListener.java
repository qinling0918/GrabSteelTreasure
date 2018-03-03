package com.zgw.qgb.net.download;

import java.io.File;

/**
 * Name:DefaultDownloadListener
 * Created by Tsinling on 2018/2/27 11:57.
 * description:
 */

public class DefaultDownloadListener implements DownloadListener {
    @Override
    public void onProgress(String url, int progress,long contentLength, long currentBytes) {

    }

    @Override
    public void onSuccess(String url, File file) {
    }

    @Override
    public void onFailed(String url, int errorCode, String errorMsg) {

    }

    @Override
    public void onPaused(String url, File file) {

    }

    @Override
    public void onCanceled(String url, File file) {

    }
}
