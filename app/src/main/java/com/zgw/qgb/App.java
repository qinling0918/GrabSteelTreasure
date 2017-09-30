package com.zgw.qgb;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.zgw.qgb.helper.ActivityMgr;
import com.zgw.qgb.helper.CrashlyticsTree;
import com.zgw.qgb.helper.DebugHelper;
import com.zgw.qgb.helper.RudenessScreenHelper;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;


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

    private void init() {
        DebugHelper.getInstance().syscIsDebug(this);
        ActivityMgr.getInstance().init(this);
        RudenessScreenHelper.getInstance().init(this,720);
        initFabric();
        initTimber();
    }

    private boolean isDebug() {
        return DebugHelper.getInstance().isDebug();
    }

    private void initFabric() {
        Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics.Builder()
                        .core(new CrashlyticsCore.Builder().disabled(isDebug()).build())
                        .build())
                .debuggable(isDebug())
                .build();
        Fabric.with(fabric);
       // Fabric.with(this, new Crashlytics());
    }

    private void initTimber() {
        if (isDebug()) {
            //使用时将Timber 进行再次封装
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree());
        }
    }



}