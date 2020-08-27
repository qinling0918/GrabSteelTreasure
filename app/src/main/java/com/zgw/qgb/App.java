package com.zgw.qgb;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.zgw.qgb.helper.ConfigContextWrapper;
import com.zgw.qgb.helper.DebugHelper;
import com.zgw.qgb.helper.RudenessScreenHelper;
import com.zgw.qgb.helper.Utils;

import java.util.Locale;

import timber.log.Timber;


/**
 */

public class App extends Application {
    private static App instance;

 /*   public App() {
        //tinkerFlags, which types is supported
        //dex only, library only, all support
        super(ShareConstants.TINKER_ENABLE_ALL,"com.zgw.qgb.AppLike");
    }
*/
    @Override
    protected void attachBaseContext(Context base) {
        Timber.plant(new Timber.DebugTree() );
       /* DisplayMetrics dm =new DisplayMetrics();
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);

        Log.d("density1"  ,"before DisplayMetrics;"+ dm.toString());
        Log.d("density1"  ,"before Display;"+  manager.getDefaultDisplay().toString());*/
        Log.d("density1"  ,"before getDefaultDisplayDensity;"+ ConfigContextWrapper.getInitialDisplayDensity());
        Log.d("density1"  ,"before base.getResources();"+base.getResources().getDisplayMetrics().toString());
        Log.d("density1"  ,"before base.getConfiguration();"+base.getResources().getConfiguration().toString());
       // Log.d("density1"  ,"before getResources();"+getResources().getDisplayMetrics().toString());
      //  ConfigContextWrapper contextWrapper = new ConfigContextWrapper(base,200);
        ConfigContextWrapper contextWrapper = new ConfigContextWrapper(base);
        super.attachBaseContext(base);

    /*    Log.d("density1"  ,"after DisplayMetrics;"+ dm.toString());
        Log.d("density1"  ,"after Display;"+  manager.getDefaultDisplay().toString());*/
        Log.d("density1"  ,"after getDefaultDisplayDensity;"+ ConfigContextWrapper.getInitialDisplayDensity());
        Log.d("density1"  ,"after base.getResources();"+base.getResources().getDisplayMetrics().toString());
        Log.d("density1"  ,"after base.getConfiguration();"+contextWrapper.getBaseContext().getResources().getConfiguration().toString());
        Log.d("density1"  ,"after base.getConfiguration();"+contextWrapper.getResources().getConfiguration().toString());
        Log.d("density1"  ,"after getResources() ;"+getResources().getDisplayMetrics().toString());
      //  Log.d("density1"  ,"after getConfiguration() ;"+getResources().getConfiguration().toString());

        //支持vector drawable
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        MultiDex.install(this);

    }

    @Override public void onCreate() {
        super.onCreate();

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

        Utils.getInstance().init(this);

        DebugHelper.getInstance().syscIsDebug(this);
      //  LeakCanary.install(this);
        //AppHelper.updateAppLanguage(this); 未完成
        //ActivityMgr.getInstance().init(this);//出现了内存泄漏   不推荐
        RudenessScreenHelper.getInstance().init(this,720)/*.activate()*/;
        //FabricHelper.getInstance().init(this);
        //Timber.plant(isDebug() ? new Timber.DebugTree() : new CrashlyticsTree());

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

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
    }
}