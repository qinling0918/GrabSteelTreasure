package com.zgw.qgb.helper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by tsinling on 16/11/7.
 */
public class ScreenUtils {


    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }
    /**
     * 转换dp为px
     * @param context context
     * @param dpValue 需要转换的dp值
     * @return px值
     */
    public static float dp2px(Context context, float dpValue){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getDisplayMetrics(context));
    }
    /**
     * 转换px为dp
     * @param context context
     * @param pxValue 需要转换的px值
     * @return px值
     */
    public static float px2dp(Context context, float pxValue){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pxValue, getDisplayMetrics(context));
    }
    /**
     * 转换pt为px
     * @param ptValue 需要转换的pt值，若context.resources.displayMetrics经过resetDensity()的修改则得到修正的相对长度，否则得到原生的磅
     */
    public static float pt2px(Context context, float ptValue){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, ptValue, getDisplayMetrics(context));
    }

    public static int dp2sp(Context context, float dipValue) {
        float pxValue = dp2px(context, dipValue);
        return px2sp(context, pxValue);
    }

    public static int sp2dp(@NonNull Context context, float spValue) {
        float pxValue = sp2px(context, spValue);
        return (int) px2dp(context, pxValue);
    }
    public static int px2sp(@NonNull Context context, float pxValue) {
        final float fontScale = getDisplayMetrics(context).scaledDensity;
        return (int) (pxValue / fontScale);
    }

    public static int sp2px(@NonNull Context context, float spValue) {
        final float fontScale = getDisplayMetrics(context).scaledDensity;
        return (int) (spValue * fontScale);
    }

    /**
     * 获取屏幕的宽
     * @return 屏幕宽
     */
    public static int getScreenWidthPix(Context context) {
        return  getScreenPix(context).widthPixels;
    }

    /**
     * 获取屏幕的高
     * @return 屏幕高
     */
    public static int getScreenHeightPix(Context context) {
        return  getScreenPix(context).heightPixels;
    }

    /**
     * 获取屏幕的大小
     * @return 屏幕尺寸对象
     */
    public static Screen getScreenPix(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return new Screen(dm.widthPixels, dm.heightPixels);
    }
    /**
     * 屏幕信息
     *
     * @author wangyang
     *
     */
    public static class Screen {
        // 屏幕宽
        int widthPixels;
        // 屏幕高
        int heightPixels;

        Screen(int widthPixels, int heightPixels) {
            this.widthPixels = widthPixels;
            this.heightPixels = heightPixels;
        }
    }

    /**
     * 截屏
     *
     * @param activity activity
     * @param  isDeleteStatusBar 是否去掉状态栏
     * @return Bitmap
     */
    public static Bitmap screenShot(@NonNull final Activity activity, boolean isDeleteStatusBar) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setDrawingCacheEnabled(true);
        decorView.buildDrawingCache();
        Bitmap bmp = decorView.getDrawingCache();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int statusBarHeight = isDeleteStatusBar ? getStatusBarHeight() : 0 ;
        Bitmap ret = Bitmap.createBitmap(bmp, 0, statusBarHeight, dm.widthPixels, dm.heightPixels - statusBarHeight);
        decorView.destroyDrawingCache();
        return ret;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     * @return int
     * @throws
     */
    public static int getStatusBarHeight() {
        // Resources.getSystem() 可以在任何地方进行使用，但是有一个局限，只能获取系统本身的资源
        return Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }

}
