package com.zgw.qgb.helper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.zgw.qgb.helper.utils.EmptyUtils;
import com.zgw.qgb.interf.ActivityLifecycleCallbacks;

import java.lang.reflect.Field;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Caodongyao on 2017/8/13.
 */

public class RudenessScreenHelper {

    private RudenessScreenHelper() {
    }

    private static class SingletonHolder {
        private static final RudenessScreenHelper mInstance = new RudenessScreenHelper();
    }

    public static RudenessScreenHelper getInstance() {
        return SingletonHolder.mInstance;
    }

    /**
     * 重新计算displayMetrics.xhdpi, 使单位pt重定义为设计稿的相对长度
     *
     * @param context
     * @param designWidth 设计稿的宽度
     * @see #activate()
     */
    private static void resetDensity(Context context, float designWidth) {
        if (context == null)
            return;

        Point size = new Point();
        ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        Resources resources = context.getResources();
        resources.getDisplayMetrics().xdpi = size.x / designWidth * 72f;
        DisplayMetrics metrics = getMetricsOnMiui(context.getResources());
        if (metrics != null)
            metrics.xdpi = size.x / designWidth * 72f;
    }

    /**
     * 恢复displayMetrics为系统原生状态，单位pt恢复为长度单位磅
     *
     * @param context
     * @see #inactivate()
     */
    private static void restoreDensity(Context context) {
        getDisplayMetrics().setToDefaults();

        DisplayMetrics metrics = getMetricsOnMiui(context.getResources());
        if (metrics != null)
            metrics.setToDefaults();
    }

    //解决MIUI更改框架导致的MIUI7+Android5.1.1上出现的失效问题(以及极少数基于这部分miui去掉art然后置入xposed的手机)
    private static DisplayMetrics getMetricsOnMiui(Resources resources) {
        if ("MiuiResources".equals(resources.getClass().getSimpleName()) || "XResources".equals(resources.getClass().getSimpleName())) {
            try {
                Field field = Resources.class.getDeclaredField("mTmpMetrics");
                field.setAccessible(true);
                return (DisplayMetrics) field.get(resources);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    private ActivityLifecycleCallbacks activityLifecycleCallbacks;
    private Application mApplication;
    private float designWidth;

    /**
     * @param application application
     * @param width       设计稿宽度
     *
     * 返回值为自己,不推荐这么用,此处只是为了实现链式编程
     */
    public RudenessScreenHelper init(Application application, float width) {
        mApplication = application;
        designWidth = width;

        activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                resetDensity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                resetDensity(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                resetDensity(activity);
            }

        };
        return getInstance();
    }

    private void resetDensity(Activity activity) {
        resetDensity(mApplication, designWidth);
        resetDensity(activity, designWidth);
    }

    /**
     * 激活本方案
     */
    public void activate() {
        EmptyUtils.checkNotNull(mApplication,"Please initialize in application");
        EmptyUtils.checkNotNull(designWidth,"designWidth should be greater than 0");

        resetDensity(mApplication, designWidth);
        mApplication.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    /**
     * 恢复系统原生方案
     */
    public void inactivate() {
        EmptyUtils.checkNotNull(mApplication,"Please initialize in application");
        restoreDensity(mApplication);
        mApplication.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }


}
