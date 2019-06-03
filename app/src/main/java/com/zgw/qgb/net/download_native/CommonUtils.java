package com.zgw.qgb.net.download_native;

import android.os.Handler;

import com.zgw.qgb.App;

public class CommonUtils {
    /**
     * 在主线程执行runnable
     * @param runnable
     */
    public static void runOnUIThread(Runnable runnable){
        new Handler(App.getContext().getMainLooper()).post(runnable);
    }
    public static void runOnUIThread(Runnable runnable, long delayMillis){
        new Handler(App.getContext().getMainLooper()).postDelayed(runnable,delayMillis);
    }
}
