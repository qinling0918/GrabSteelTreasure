package com.zgw.qgb.net.download1;

import java.io.File;

/**
 * Name:DownloadInfo
 * Created by Tsinling on 2018/2/9 15:21.
 * description:
 */

public class DownloadInfo {
    private File file;
    private String url;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    private Integer status;

    public DownloadInfo(File file, Integer status) {
        this.file = file;
        this.status = status;
    }

    public DownloadInfo(File file, String url) {
        this.file = file;
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
