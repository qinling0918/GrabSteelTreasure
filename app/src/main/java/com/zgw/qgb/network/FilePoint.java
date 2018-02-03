package com.zgw.qgb.network;

/**
 * Name:FilePoint
 * Created by Tsinling on 2018/1/23 8:51.
 * description:
 */

public class FilePoint {

    private String fileName;//文件名

    private String url;//文件url

    private String filePath;//文件下载路径



    public FilePoint(String url) {

        this.url = url;

    }



    public FilePoint(String filePath, String url) {

        this.filePath = filePath;

        this.url = url;

    }



    public FilePoint(String url, String filePath, String fileName) {

        this.url = url;

        this.filePath = filePath;

        this.fileName = fileName;

    }



    public String getFileName() {

        return fileName;

    }



    public void setFileName(String fileName) {

        this.fileName = fileName;

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



}