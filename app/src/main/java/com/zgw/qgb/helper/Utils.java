package com.zgw.qgb.helper;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;


import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Created by qinling on 2018/9/17 10:03
 * Description:
 */
public class Utils {
    public static Utils getInstance() {
        return SingleTon.sInstance;
    }

    private static class SingleTon {
        private static final Utils sInstance = new Utils();
    }

    private static Application sApplication;

    public static Context getContext() {
        return sApplication.getApplicationContext();
    }

    public  boolean isDebug() {
        return DebugHelper.getInstance().isDebug();
    }

    public static Locale getLocale() {
        return Locale.CHINA;
    }

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Init utils.
     * <p>Init it in the class of Application.</p>
     *
     * @param context context
     */
    public static void init(@NonNull final Context context) {
        init((Application) context.getApplicationContext());
    }

    /**
     * Init utils.
     * <p>Init it in the class of Application.</p>
     *
     * @param app application
     */
    public static void init(@NonNull final Application app) {
        sApplication = app;
        //Utils.sApplication.registerActivityLifecycleCallbacks(mCallbacks);
    }

    /**
     *
     * @return the context of Application object
     */
    public static Application getApp() {
        if (sApplication != null) return sApplication;
        throw new NullPointerException("u should init first ");
    }


}
