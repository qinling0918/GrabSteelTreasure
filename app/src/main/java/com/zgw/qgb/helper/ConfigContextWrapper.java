package com.zgw.qgb.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import timber.log.Timber;

import static android.content.res.Configuration.DENSITY_DPI_UNDEFINED;

/**
 * Created by qinling on 2020/7/21 10:17
 * Description: 设备配置项上下文扩展类  此处含字体大小，显示大小重写
 * 在 Applcation 以及 Activity的attachBaseContext()使用
 *
 * @Override protected void attachBaseContext(Context base) {
 * // super.attachBaseContext(new ConfigContextWrapper(base,480)); // or
 * // super.attachBaseContext(new ConfigContextWrapper(base));// or
 * // super.attachBaseContext(ConfigContextWrapper.create(base,NOT_ADJUST_CONFIG));// or
 * // super.attachBaseContext(ConfigContextWrapper.create(base,540,DEFAULT_CONFIG));// or
 * DEFAULT_CONFIG see {@link IConfig}
 * }
 */
public class ConfigContextWrapper extends ContextWrapper {
    private final float width;
    private Integer defaultDensityDpi;

    /**
     * @param base
     */
    public ConfigContextWrapper(Context base) {
        super(base);

        // 此处用系统初始值，该值可能会相对默认值偏大。
        // 在测试机（华为 STL-Al00,2340*1080,6.59inch）系统设置中默认为480，而getDefaultDisplayDensity值为540
        // 若是版本号小于23 无需重置显示大小
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.width = 0;
            return;
        }
        DisplayMetrics dm = base.getResources().getDisplayMetrics();
        this.width = dm.widthPixels / (getDefaultDisplayDensity() / 160f);
        setDensity(base, width);
    }

    /**
     * @param base  上下文
     * @param width 设计图对应的宽，单位 px   例如手机宽为1080 px ，而设计图宽为 720px，在此设置之后,便可以根据设计图在布局文件中分为720dp
     *              则在ui布局中 设置ui布局单位为 720dp 便是满屏效果。设置为 360dp 便是半屏效果
     *              系统原来的displayDensity值参见 @{{@link #getDefaultDisplayDensity()}}
     *              若 下面@{@link #getDensityDpiScale()} 设置的为随着系统设置变化而变化,则无法保证360dp 是一半的效果
     *              例如某手机(宽1080px,其系统中显示大小分为 小(408)  默认(480)  大(540) )
     *              则正常情况显示大小为默认值 480 ,若在系统设置中设置了显示大小,例如设置为小 408,则满屏效果为 720*(408/480)
     *              故若是需要严格按照设计稿进行UI 布局绘制,请关闭随系统显示大小的配置,关闭方法参见@{@link #NOT_ADJUST_CONFIG}
     */
    public ConfigContextWrapper(Context base, float width) {
        super(base);
        this.width = width;
        setDensity(base, width);

        try {
            test();
            Log.i("declareField: ", "getDisplayContentLocked: " + getDisplayContentLocked());

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 重新设置屏幕显示配置
     *
     * @param base  上下文
     * @param width 设计图对应的宽，单位 px   例如手机宽为1080 px ，而设计图宽为 540px，
     *              则在ui布局中 设置ui布局单位为 540dp 便是满屏效果。设置为 270dp 便是半屏效果
     *              系统原来的displayDensity值参见 @{{@link #getDefaultDisplayDensity()}}
     */
    public void setDensity(Context base, float width) {
        if (width <= 0) {
            return;
        }

        // 密度 名称           LDPI   MDPI     HDPI     XHDPI     XXHDPI   XXXHDPI
        // 密度值 densityDpi   120     160      240      320       480      540
        // 缩放比例 density    0.75     1       1.5       2         3        4
        // 代表分辨率        240*320 320*480 480*800/854 720*1280 1080*1920 2160*3840
        // 屏幕                QVGA    HVGA   WVGA/FWVGA  720P     1080p     4K
        DisplayMetrics dm = base.getResources().getDisplayMetrics();
        // 将该上下文对应的Resources的density、scaledDensity、densityDpi重置
        dm.density = dm.widthPixels / width;
        dm.scaledDensity = dm.density * (dm.scaledDensity / dm.density);
        dm.densityDpi = (int) (dm.density * 160);

    }

    /**
     * // 計算出設置中系統 顯示大小与设备初始显示大小的比例,为后面的显示进行配置
     * // 例如手机自带的 densityDpi 默认大小为480,而若在系统设置中将显示大小(通常由 408 480 ,540 三档)进行调整,
     * // 计算出 设置后的缩放比
     *
     * @param base
     * @return
     */
/*
    private float getScale(Context base) {

        int defaultDensityDpi = getBaseDisplayDensity();
        Timber.d("setDensity getScale densityDpi %s : %s", base.getResources().getConfiguration().densityDpi,defaultDensityDpi);
        if (defaultDensityDpi <= 0) {
            return 1;
        }
        // if (DENSITY_DPI_UNDEFINED == getDensityDpi() || densityDpi < 0)
        return base.getResources().getConfiguration().densityDpi / (defaultDensityDpi * 1f);
    }
*/


    /**
     * API 17 在getResource时会调用该方法，在这里进行重置字体缩放大小以及显示缩放大小。
     *
     * @param overrideConfiguration
     * @return
     */
    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        resetFontScale(overrideConfiguration);
        resetDensityDpi(overrideConfiguration);
        return super.createConfigurationContext(overrideConfiguration);
    }

    /**
     * 重写 getResource类，在此处将缩放大小重置
     *
     * @return
     */
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


    /**
     * 对Configuration 显示大小 重新赋值，
     *
     * @param configuration
     * @return true 将会调用 updateConfiguration 进行更新，false 无需调用
     */
    private boolean resetDensityDpi(Configuration configuration) {
        // 若是版本号小于23 无需重置显示大小
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        int densityDpi = getScaleDensityDpi();

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
     * @param configuration 系统配置
     * @return 是否需要调用 res.updateConfiguration（）； true为需要，false 为不需要。
     */
    private boolean resetFontScale(Configuration configuration) {
        float fontScale = getFontScale();
        if (fontScale <= 0) {
            return false;
        }
        if (configuration.fontScale == fontScale) {
            return false;
        }
        configuration.fontScale = fontScale;
        return true;
    }

    /**
     * 若是重写，设置为1f 则表示不再随系统设置变化而变化
     * 若仍想随系统设置的字体大小变化 而变化便设置为 小于等于0的值
     *
     * @return 字体缩放尺寸
     */

    protected float getFontScale() {
        return null != config ? config.getFontScale() : 1;
    }


    /**
     * @return
     */
    protected float getDensityDpiScale() {
        return null != config ? config.getDensityDpiScale() : -1;
    }

    /**
     * 根据缩放比例获取 DensityDpi
     *
     * @return
     */
    private int getScaleDensityDpi() {
        float densityDpiScale = getDensityDpiScale();
        densityDpiScale = densityDpiScale <= 0 ? 1 : densityDpiScale;
        // if ( densityDpiScale <= 0) return DENSITY_DPI_UNDEFINED;
        setDensity(getBaseContext(), width);
        DisplayMetrics dm = getBaseContext().getResources().getDisplayMetrics();
        return (int) (densityDpiScale * dm.densityDpi);
    }


    /**
     * 获取手机出厂时默认的densityDpi
     * 1、当调节手机系统"显示大小"【调大】的时候，相应的dpi会变大【dp = (dpi/160) * px】,此时dp就会变大，所以相应的UI布局就会变大。
     * 2、当调节手机系统"分辨率"【调小】的时候，相应的dpi会变小【比如由480-->320】。如果此时使用技术手段使dpi保持不变，那么相同的dp就会占用更多的px，所以UI布局就会变大。
     */
    public int getDefaultDisplayDensity() {
        return defaultDensityDpi = defaultDensityDpi == null ? getInitialDisplayDensity() : defaultDensityDpi;
    }

    @SuppressLint("PrivateApi")
    public static int getInitialDisplayDensity() {
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

    @SuppressLint("PrivateApi")
    public static String getDisplayContentLocked() {
        try {
            Class aClass = Class.forName("android.view.WindowManagerGlobal");
            Method method = aClass.getMethod("getWindowManagerService");
            method.setAccessible(true);
            Object iwm = method.invoke(aClass);
            Method getInitialDisplayDensity = iwm.getClass().getMethod("getDisplayContentLocked", int.class);
            getInitialDisplayDensity.setAccessible(true);
            Object densityDpi = getInitialDisplayDensity.invoke(iwm, Display.DEFAULT_DISPLAY);
            return densityDpi.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    private void test() throws Throwable {

        //  Log.i("declareField: ", "getBaseDisplayDensity: " +  getBaseDisplayDensity());

        Class<?> aClass = Class.forName("android.view.WindowManagerGlobal");
        Method method = aClass.getMethod("getWindowManagerService");
        method.setAccessible(true);
        Object iwm = method.invoke(aClass);
        Class<?> hclass = iwm.getClass();
        Field[] declaredFields = hclass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Log.i("declareField: ", "declareField: " + declaredField);
        }
        Method[] declaredMethods = hclass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            Log.i("declareField: ", "declaredMethod: " + declaredMethod);
        }
    }

    public static ConfigContextWrapper create(Context base) {
        return new ConfigContextWrapper(base);
    }

    public static ConfigContextWrapper create(Context base, IConfig config) {
        return new ConfigContextWrapper(base).setConfig(config);
    }

    public static ConfigContextWrapper create(Context base, float displayDensity) {
        return new ConfigContextWrapper(base, displayDensity);
    }

    public static ConfigContextWrapper create(Context base, float displayDensity, IConfig config) {
        return new ConfigContextWrapper(base, displayDensity).setConfig(config);
    }

    private IConfig config;

    public ConfigContextWrapper setConfig(IConfig config) {
        this.config = config;
        return this;
    }

    public interface IConfig {
        float getFontScale();

        float getDensityDpiScale();
    }

    /**
     * 默认的 随系统设置的显示大小以及字体大小的变化而变化
     */
    public final static IConfig DEFAULT_CONFIG = new IConfig() {
        @Override
        public float getFontScale() {
            return -1;
        }

        @Override
        public float getDensityDpiScale() {
            return -1;
        }
    };
    /**
     * 不随系统设置的字体大小、显示大小变化而变化
     */
    public final static IConfig NOT_ADJUST_CONFIG = new IConfig() {
        @Override
        public float getFontScale() {
            return 1f;
        }

        @Override
        public float getDensityDpiScale() {
            return 1f;
        }
    };

}
