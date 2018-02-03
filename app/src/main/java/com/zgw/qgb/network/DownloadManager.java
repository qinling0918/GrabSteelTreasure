package com.zgw.qgb.network;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



/**

 * 下载管理器，断点续传 

 *

 * @author Cheny

 */

public class DownloadManager {



    private String DEFAULT_FILE_DIR;//默认下载目录  

    private Map<String, DownloadTask> mDownloadTasks;//文件下载任务索引，String为url,用来唯一区别并操作下载的文件  

    private static DownloadManager mInstance;



    /**

     * 下载文件 

     */

    public void download(String... urls) {

        for (int i = 0, length = urls.length; i < length; i++) {

            String url = urls[i];

            if (mDownloadTasks.containsKey(url)) {

                mDownloadTasks.get(url).start();

            }

        }

    }



    /**

     * 通过url获取下载文件的名称 

     */

    public String getFileName(String url) {
        return new File(url).getName();

    }



    /**

     * 暂停 

     */

    public void pause(String... urls) {

        for (int i = 0, length = urls.length; i < length; i++) {

            String url = urls[i];

            if (mDownloadTasks.containsKey(url)) {

                mDownloadTasks.get(url).pause();

            }

        }

    }







    /**

     * 添加下载任务 

     */

    public DownloadManager add(String url, DownloadListener l) {

        return add(url, null, null, l);
    }



    /**

     * 添加下载任务 

     */

    public DownloadManager add(String url, String filePath, DownloadListener l) {

        return add(url, filePath, null, l);

    }



    /**

     * 添加下载任务 

     */

    public DownloadManager add(String url, String filePath, String fileName, DownloadListener l) {

        if (TextUtils.isEmpty(filePath)) {//没有指定下载目录,使用默认目录  

            filePath = getDefaultDirectory();

        }

        if (TextUtils.isEmpty(fileName)) {
            fileName = getFileName(url);

        }

        mDownloadTasks.put(url, new DownloadTask(new FilePoint(url, filePath, fileName), l));

        return this;
    }



    /**

     * 获取默认下载目录 

     *

     * @return

     */

    private String getDefaultDirectory() {

        if (TextUtils.isEmpty(DEFAULT_FILE_DIR)) {

            DEFAULT_FILE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+ File.separator;
            //DEFAULT_FILE_DIR =Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name)+ File.separator;
            /*DEFAULT_FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()

                    + File.separator + "icheny" + File.separator;*/

        }

        return DEFAULT_FILE_DIR;

    }



    /**

     * 是否正在下载 

     * @param urls

     * @return boolean

     */

    public boolean isDownloading(String... urls) {

        boolean result = false;

        for (int i = 0, length = urls.length; i < length; i++) {

            String url = urls[i];

            if (mDownloadTasks.containsKey(url)) {

                result = mDownloadTasks.get(url).isDownloading();

            }

        }

        return result;

    }



    public static DownloadManager getInstance() {

        if (mInstance == null) {

            synchronized (DownloadManager.class) {

                if (mInstance == null) {

                    mInstance = new DownloadManager();

                }

            }

        }

        return mInstance;

    }

    /**

     * 初始化下载管理器 

     */

    private DownloadManager() {

        mDownloadTasks = new HashMap<>();

    }

}  