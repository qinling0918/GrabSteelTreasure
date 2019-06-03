package com.zgw.qgb.download;

import com.zgw.qgb.download.bean.ThreadInfo;

import java.util.List;

/**
 * Created by qinling on 2018/12/5 11:51
 * Description:
 */
public interface  ThreadDao {
    /**
     * 插入下载线程信息
     * @param threadBean
     */
    void insertThread(ThreadInfo threadBean);

    /**
     * 更新下载线程信息
     * @param url
     * @param thread_id
     * @param finished
     */
    void updateThread(String url ,int thread_id,long finished);

    /**
     * 删除下载线程
     * @param url
     */
    void deleteThread(String url,int thread_id);

    void deleteTask(String url);
    /**
     * 获取下载线程
     * @param url
     * @return
     */
    List<ThreadInfo> getThreads(String url);

    /**
     * 判断下载线程是否存在
     * @param url
     * @param thread_id
     * @return
     */
    boolean isExists(String url ,int thread_id);

}
