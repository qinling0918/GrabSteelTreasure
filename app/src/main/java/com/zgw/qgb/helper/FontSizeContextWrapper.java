package com.zgw.qgb.helper;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.Display;

import java.lang.reflect.Method;

import timber.log.Timber;

import static android.content.res.Configuration.DENSITY_DPI_UNDEFINED;

/**
 * Created by qinling on 2020/7/21 10:17
 * Description: 字体大小上下文
 */
public class FontSizeContextWrapper extends ContextWrapper {
    public FontSizeContextWrapper(Context base) {
        super(base);
       /* DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        //初始化赋值操作
        float appDensity = displayMetrics.density;
        float appScaleDensity = displayMetrics.scaledDensity;
        //计算目标值density,scaleDensity,densityDpi
        float targetDensity = displayMetrics.widthPixels / WIDTH;//1080/360=3.0
        float targetScaleDensity = targetDensity * (appScaleDensity / appDensity);
        int targetDensityDpi = (int) (targetDensity * 160);*/
    }


    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        resetFontScale(overrideConfiguration);
         resetDensityDpi(overrideConfiguration);
        return super.createConfigurationContext(overrideConfiguration);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration configuration = res.getConfiguration();
        boolean needUpdate = resetFontScale(configuration);
        needUpdate = resetDensityDpi(configuration) || needUpdate;
        if (needUpdate) {
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }
        return res;
    }

    // 重置显示大小，
    private boolean resetDensityDpi(Configuration configuration) {
        // 若是版本号小于23 无需重置显示大小
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        int densityDpi = getDensityDpi();
        Timber.d("getResources densityDpi :%s", densityDpi);
        // 若是获取不到需要设置的 值，不再重置
        if (DENSITY_DPI_UNDEFINED == densityDpi || densityDpi < 0) {
            return false;
        }
        // 若是需要设置的与 现有的值一致，不再重置
        if (configuration.densityDpi == densityDpi) {
            return false;
        }
        configuration.densityDpi = densityDpi;
        return true;
    }

    /**
     * 重置字体大小
     *
     * @param configuration
     * @return 是否需要调用 res.updateConfiguration（）； true为需要，false 为不需要。
     */
    private boolean resetFontScale(Configuration configuration) {

        float fontScaleSize = getFontScaleSize();
        if (configuration.fontScale != fontScaleSize) {//fontScale要缩放的比例
            configuration.fontScale = fontScaleSize;
            return true;
        }
        return false;
    }

    /**
     *  若是重写，设置为1f 则表示不再随系统设置变化而变化
     * @return 字体缩放尺寸
     */
    protected float getFontScaleSize() {
        return 1f;
       // return PrefGetter.getFontScaleSize();
    }
    //

    /**
     * 若是想随着系统设置中的显示大小 则返回  DENSITY_DPI_UNDEFINED，
     *     // 若是不想随着变化，则使用getDefaultDisplayDensity
     * @return
     */
    protected int getDensityDpi() {
        //return DENSITY_DPI_UNDEFINED;
        return getDefaultDisplayDensity();
    }



    /**
     * 获取手机出厂时默认的densityDpi
     */
    public static int getDefaultDisplayDensity() {
        try {
            Class aClass = Class.forName("android.view.WindowManagerGlobal");
            Method method = aClass.getMethod("getWindowManagerService");
            method.setAccessible(true);
            Object iwm = method.invoke(aClass);
            Method getInitialDisplayDensity = iwm.getClass().getMethod("getInitialDisplayDensity", int.class);
            getInitialDisplayDensity.setAccessible(true);
            Object densityDpi = getInitialDisplayDensity.invoke(iwm, Display.DEFAULT_DISPLAY);
            return (int) densityDpi;
        } catch (Exception e) {
            e.printStackTrace();
            return DENSITY_DPI_UNDEFINED;
        }
    }

}
