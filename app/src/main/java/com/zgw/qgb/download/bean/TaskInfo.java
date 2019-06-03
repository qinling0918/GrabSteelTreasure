package com.zgw.qgb.download.bean;

/**
 * Created by qinling on 2018/12/5 11:29
 * Description:
 */
public class TaskInfo {
    private String url;
    private String filePath;
    private String fileName;
    private long contentLength;
    private long finished;

    public TaskInfo(String url, String filePath, String fileName) {
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public TaskInfo(String url, String filePath, String fileName, long contentLength) {
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
        this.contentLength = contentLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }
}
