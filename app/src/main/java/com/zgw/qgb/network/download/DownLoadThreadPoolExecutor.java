package com.zgw.qgb.network.download;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by qinling on 2018/11/21 15:05
 * Description:
 */
public class DownLoadThreadPoolExecutor extends ThreadPoolExecutor {

    public DownLoadThreadPoolExecutor DEFAULT = new DownLoadThreadPoolExecutor(5,20,30);
    public static String TAG = "LinkedBqThreadPool";

    /**
     * 正在执行任务数量
     */
    private AtomicInteger taskNum = new AtomicInteger(0);

    /**
     * 构建线程池
     * @param corePoolSize    池中所保存的核心线程数
     * @param maximumPoolSize    池中允许的最大线程数
     * @param keepActiveTime    非核心线程空闲等待新任务的最长时间
     * @param timeunit    keepActiveTime参数的时间单位
     * @param blockingqueue    任务队列
     */
    public DownLoadThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepActiveTime, TimeUnit timeunit,
                                      BlockingQueue<Runnable> blockingqueue) {
        super(corePoolSize, maximumPoolSize, keepActiveTime, timeunit, blockingqueue);
        allowCoreThreadTimeOut(true);
    }

    /**
     * 构建线程池
     * @param corePoolSize    池中所保存的核心线程数
     * @param maximumPoolSize    池中允许的最大线程数
     * @param keepActiveTime    非核心线程空闲等待新任务的最长时间(单位：秒)
     * @param blockingqueue    任务队列
     */
    public DownLoadThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepActiveTime,
                                      BlockingQueue<Runnable> blockingqueue) {
        this(corePoolSize, maximumPoolSize, keepActiveTime, TimeUnit.SECONDS, blockingqueue);
    }

    /**
     * 构建线程池
     * @param corePoolSize    池中所保存的核心线程数
     * @param maximumPoolSize    池中允许的最大线程数
     * @param keepActiveTime    非核心线程空闲等待新任务的最长时间(单位：秒)
     */
    public DownLoadThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepActiveTime) {
        this(corePoolSize, maximumPoolSize, keepActiveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 构建单线程的线程池
     */
    public DownLoadThreadPoolExecutor() {
        this(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 任务执行，以原子方式将当前值加 1
     */
    @Override
    public void execute(Runnable task) {
        taskNum.getAndIncrement();
        super.execute(task);
    }

    /**
     * 任务执行之后
     */
    @Override
    public void afterExecute(Runnable task, Throwable throwable) {
        taskNum.decrementAndGet();
        Log.d(TAG,"task : " + task.getClass().getSimpleName()
                +  " completed,Throwable:" + throwable + ",taskNum:" + getTaskNum());
        synchronized(this) {
            notifyAll();
        }
    }

    /**
     * 挂起当前线程，直到所有任务执行完成
     */
    public void waitComplete() {
        try {
            synchronized(this){
                while(getTaskNum() > 0){
                    wait(500);
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "taskNum:" + getTaskNum(),e);
        }
    }

    /**
     * @return    未执行的任务数
     */
    public int getTaskNum() {
        return taskNum.get();
    }

    /**
     * @param time    非核心线程空闲等待新任务的最长时间(单位：秒)
     */
    public void setKeepAliveTime(int time) {
        super.setKeepAliveTime(time, TimeUnit.SECONDS);
    }

    /**
     * @param size    池中所保存的核心线程数
     */
    public void setCorePoolSize(int size) {
        super.setCorePoolSize(size);
    }

    /**
     * @param size    池中允许的最大线程数
     */
    public void setMaximumPoolSize(int size) {
        super.setMaximumPoolSize(size);
    }
}
