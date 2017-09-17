package com.zgw.qgb.base.mvp;


import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Created by tsinling on 17/9/8
 */
public interface IView {
    /**
     * 显示加载
     */
    void showProgress(@StringRes int resId);

    /**
     * 隐藏加载
     */
    void hideProgress();

    void showErrorMessage(@NonNull String msgRes);

    void showMessage(@StringRes int titleRes, @StringRes int msgRes);

    void showMessage(@NonNull String titleRes, @NonNull String msgRes);

    boolean isLoggedIn();

 /*   void onRequireLogin();

    void onThemeChanged();*/

}
