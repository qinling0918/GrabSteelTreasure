package com.zgw.qgb;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.zgw.qgb.helper.DebugHelper;
import com.zgw.qgb.helper.RudenessScreenHelper;
import com.zgw.qgb.helper.Utils;
import com.zgw.qgb.helper.utils.ScreenUtils;

import java.util.Locale;

/**
 * Created by qinling on 2018/9/14 18:51
 * Description:
 */

/*@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.zgw.qgb.App",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)*/
public class AppLike extends DefaultApplicationLike  {
    private static final String TAG = "AppLike";

    public AppLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //Glide.with(this).applyDefaultRequestOptions(new RequestOptions()).

        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
        Utils.getInstance().init(getApplication());
        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerInstaller.install(this);
        DebugHelper.getInstance().syscIsDebug(base);
       // LeakCanary.install(getApplication());
        //AppHelper.updateAppLanguage(this); 未完成
        //ActivityMgr.getInstance().init(this);//出现了内存泄漏   不推荐
        RudenessScreenHelper.getInstance().init(getApplication(), 720)
.activate()
;
        //FabricHelper.getInstance().init(this);
        //Timber.plant(isDebug() ? new Timber.DebugTree() : new CrashlyticsTree());

        initARouter();
    }

    private void initARouter() {
        if (isDebug()) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(getApplication()); // 尽可能早，推荐在Application中初始化
    }

    public boolean isDebug() {
        return DebugHelper.getInstance().isDebug();
    }

    public static Locale getLocale() {
        return Locale.CHINA;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }

}
