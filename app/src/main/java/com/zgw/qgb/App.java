package com.zgw.qgb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.TinkerSoLoader;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.zgw.qgb.helper.DebugHelper;
import com.zgw.qgb.helper.RudenessScreenHelper;
import com.zgw.qgb.helper.utils.ScreenUtils;

import java.lang.reflect.Field;
import java.util.Locale;



/**
 */

public class App extends TinkerApplication {
    private static App instance;

    public App() {
        //tinkerFlags, which types is supported
        //dex only, library only, all support
        super(ShareConstants.TINKER_ENABLE_ALL,"com.zgw.qgb.AppLike");
    }
    @Override public void onCreate() {
        super.onCreate();
        //支持vector drawable
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //Glide.with(this).applyDefaultRequestOptions(new RequestOptions()).
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


        MultiDex.install(this);
        DebugHelper.getInstance().syscIsDebug(this);
        LeakCanary.install(this);
        //AppHelper.updateAppLanguage(this); 未完成
        //ActivityMgr.getInstance().init(this);//出现了内存泄漏   不推荐
        RudenessScreenHelper.getInstance().init(this,720)/*.activate()*/;
        //FabricHelper.getInstance().init(this);
        //Timber.plant(isDebug() ? new Timber.DebugTree() : new CrashlyticsTree());
        Log.d("density1"  ,";"+ScreenUtils.getDisplayMetrics(this).toString());
        Log.d("density2"  ,":"+ScreenUtils.getDisplayMetrics(this).densityDpi);
        Log.d("density3"  ,";"+ScreenUtils.getDisplayMetrics(this).scaledDensity);
        Log.d("density4"  ,";"+ScreenUtils.getDisplayMetrics(this).scaledDensity);
        Log.d("density5"  ,";"+ScreenUtils.getDisplayMetrics(this).scaledDensity);
        initARouter();



}

    private void initARouter() {
        if (isDebug()) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(instance); // 尽可能早，推荐在Application中初始化
    }

    public boolean isDebug() {
        return DebugHelper.getInstance().isDebug();
    }
    public static Locale getLocale() {
        return Locale.CHINA;
    }

}