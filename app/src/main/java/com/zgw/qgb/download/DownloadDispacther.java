package com.zgw.qgb.download;

import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by qinling on 2018/12/5 10:09
 * Description:
 */
public class DownloadDispacther {

    private int maxRequests = 5;
    private @Nullable
    Runnable idleCallback;
    private final Deque<DownloadRunnable> readyAsyncCalls = new ArrayDeque<>();

    private final Deque<DownloadRunnable> runningAsyncCalls = new ArrayDeque<>();

    public synchronized void setMaxRequests(int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        this.maxRequests = maxRequests;
        promoteCalls();
    }


    private @Nullable
    ExecutorService executorService;

    public DownloadDispacther(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public DownloadDispacther() {
    }

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "DownloadManager WorkerThread#" + mCount.getAndIncrement());
        }
    };

       public synchronized ExecutorService executorService() {
           if (executorService == null) {
               executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                       new SynchronousQueue<Runnable>(), THREAD_FACTORY);
           }
           return executorService;
       }
    private static final BlockingQueue<Runnable> SPOOL_WORK_QUEUE =
            new LinkedBlockingQueue<Runnable>();

 /*   public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(maxRequests, maxRequests, 60, TimeUnit.SECONDS,
                    SPOOL_WORK_QUEUE, THREAD_FACTORY);
        }
        return executorService;
    }*/

    public synchronized void setIdleCallback(@Nullable Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    public synchronized void remove(@Nullable DownloadRunnable runnable) {
        SPOOL_WORK_QUEUE.remove(runnable);
    }

    synchronized void enqueue(DownloadRunnable call) {
        executorService().execute(call);

        /*if (runningAsyncCalls.size() < maxRequests ) {
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            readyAsyncCalls.add(call);
        }*/
    }

    private void promoteCalls() {
        Log.e("DownloadDispacther", runningAsyncCalls.size() + "");
        if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<DownloadRunnable> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            DownloadRunnable runnable = i.next();
            executorService().execute(runnable);
            i.remove();
            runningAsyncCalls.add(runnable);
            if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
    }


    void finished(DownloadRunnable call) {
        finished(runningAsyncCalls, call, true);
        //  Executors.newCachedThreadPool()
        //   Executors.newFixedThreadPool(5)
    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {

        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
            Log.e("DownloadDispacther", "remove" + call.hashCode());
            for (T call1 : calls) {
                Log.e("DownloadDispacther", "exists" + call1.hashCode());
            }

            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            Log.e("DownloadDispacther", runningAsyncCalls.size() + "");
            if (promoteCalls) promoteCalls();
            runningCallsCount = runningCallsCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }

    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size();
    }
}
