package com.zgw.qgb;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.squareup.leakcanary.LeakCanary;
import com.zgw.qgb.helper.ActivityMgr;
import com.zgw.qgb.helper.DebugHelper;
import com.zgw.qgb.helper.RudenessScreenHelper;

import java.util.Locale;


/**
 */

public class App extends Application {
    private static App instance;

    @Override public void onCreate() {
        super.onCreate();
        //支持vector drawable
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        instance = this;
        init();
    }

    @NonNull
    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    private void init() {
        LeakCanary.install(this);
        //AppHelper.updateAppLanguage(this); 未完成
        DebugHelper.getInstance().syscIsDebug(this);
        ActivityMgr.getInstance().init(this);
        RudenessScreenHelper.getInstance().init(this,720)/*.activate()*/;
        //FabricHelper.getInstance().init(this);
        //Timber.plant(isDebug() ? new Timber.DebugTree() : new CrashlyticsTree());
    }

    public boolean isDebug() {
        return DebugHelper.getInstance().isDebug();
    }
    public static Locale getLocale() {
        return Locale.CHINA;
    }

}