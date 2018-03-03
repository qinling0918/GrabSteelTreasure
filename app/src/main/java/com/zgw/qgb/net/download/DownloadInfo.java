package com.zgw.qgb.net.download;

import java.io.File;

/**
 * Name:DownloadInfo
 * Created by Tsinling on 2018/2/9 15:21.
 * description:
 */

public class DownloadInfo {
    private File file;
    private String url;
    private Status status;
    private Integer progress;
    private long contentLength;

    public long getCurrentBytes() {
        return currentBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    private long currentBytes; //当前已上传或下载的总长度

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }



    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public DownloadInfo(File file, Status status) {
        this.file = file;
        this.status = status;
    }

    public DownloadInfo(File file, String url) {
        this.file = file;
        this.url = url;
    }

    public Status getStatus() {
        return status;
    }

    public DownloadInfo setStatus(Status status) {
        this.status = status;
        return this;
    }

    public DownloadInfo setStatus(Status status, int code, String msg) {
        this.status = status;
        this.status.setCode(code);
        this.status.setMsg(msg);
        return this;
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


    public enum Status {
        SUCCESS(0),
        FAILED(1),
        PAUSED(2),
        CANCELED(3),
        PROGRESS(4),;

        private final int state;

        public int getState() {
            return state;
        }

        Status(int state) {
            this.state = state;
        }

        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

}
