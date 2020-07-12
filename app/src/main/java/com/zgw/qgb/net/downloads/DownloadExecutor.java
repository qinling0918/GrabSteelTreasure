package com.zgw.qgb.net.downloads;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * created by tsinling on: 2018/12/16 13:43
 * description:
 */
public class DownloadExecutor implements Executor {

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "DownloadExecutor WorkerThread#" + mCount.getAndIncrement());
        }
    };
    private static final Executor DOWNLOAD_POOL_EXECUTOR; //= AsyncTask.THREAD_POOL_EXECUTOR ;

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), THREAD_FACTORY);

        threadPoolExecutor.allowCoreThreadTimeOut(true);
        DOWNLOAD_POOL_EXECUTOR = threadPoolExecutor;
    }





    private @Nullable
    Runnable idleCallback;



    final ArrayDeque<Runnable> runningTasks = new ArrayDeque<Runnable>();
    final ArrayDeque<Runnable> readyTasks = new ArrayDeque<Runnable>();
    private int maxRequests = 1;


    @Override
    public synchronized void execute(final Runnable r) {

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    r.run();
                } finally {
                    finished(runningTasks, this, true);
                }
            }
        };
        if (runningTasks.size() < maxRequests) {
            runningTasks.offer(runnable);
            DOWNLOAD_POOL_EXECUTOR.execute(runnable);

        } else {
            readyTasks.add(runnable);
        }

    }


    private void promoteCalls() {
        Log.e("MainActivity", "promoteCalls:  " + maxRequests + "runningtask : "+ runningTasksCount() + "ready： "+ readyTasks.size());
        if (runningTasks.size() >= maxRequests) return; // Already running max capacity.
        if (readyTasks.isEmpty()) return; // No ready calls to promote.

        for (Iterator<Runnable> i = readyTasks.iterator(); i.hasNext(); ) {
            Runnable runnable = i.next();
            DOWNLOAD_POOL_EXECUTOR.execute(runnable);
            i.remove();
            runningTasks.add(runnable);
            if (runningTasks.size() >= maxRequests) return; // Reached max capacity.
        }

    }
    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {

        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {

           // if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            calls.remove(call);

            if (promoteCalls) promoteCalls();
            runningCallsCount = runningTasksCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }

    public synchronized int runningTasksCount() {
        return runningTasks.size() ;
    }

    public synchronized int readyTasksCount() {
        return readyTasks.size() ;
    }


    @Nullable
    public Runnable getIdleCallback() {
        return idleCallback;
    }

    public void setIdleCallback(@Nullable Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public synchronized void setMaxRequests(int maxRequests) {
        /*if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }*/
        this.maxRequests = maxRequests;
         promoteCalls();
    }
}
