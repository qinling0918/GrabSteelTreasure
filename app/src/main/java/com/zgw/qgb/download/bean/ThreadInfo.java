package com.zgw.qgb.download.bean;

/**
 * Created by qinling on 2018/12/5 11:35
 * Description:
 */
public class ThreadInfo {
    private String url;
    private long startIndex;
    private long endIndex;
    private int id;
    private long finished;

    public ThreadInfo() {
    }

    public ThreadInfo(int id, String url, long startIndex, long endIndex, long finished) {
        this.url = url;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.id = id;
        this.finished = finished;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(long endIndex) {
        this.endIndex = endIndex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }
}
