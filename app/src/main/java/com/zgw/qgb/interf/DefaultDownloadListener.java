package com.zgw.qgb.interf;

import com.zgw.qgb.net.download.DownloadListener;

import java.io.File;

/**
 * Name:DefaultDownloadListener
 * Created by Tsinling on 2018/2/27 11:57.
 * description:
 */

public class DefaultDownloadListener implements DownloadListener {
    @Override
    public void onProgress(int progress) {

    }

    @Override
    public void onSuccess(File file) {
    }

    @Override
    public void onFailed(int errorCode, String errorMsg) {

    }

    @Override
    public void onPaused(File file) {

    }

    @Override
    public void onCanceled(File file) {

    }
}
