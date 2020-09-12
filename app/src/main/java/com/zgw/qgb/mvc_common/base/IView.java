package com.zgw.qgb.mvc_common.base;

import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Created by tsinling on 17/9/8
 */
public interface IView extends Observer {
    /**
     * 显示加载
     */
    void showProgress();

    void showProgress(@StringRes int resId);

    void showProgress(@StringRes int resId, boolean cancelable);

    void showProgress(CharSequence msg, boolean cancelable);

    /**
     * 隐藏加载
     */
    void hideProgress();

    void showMessage(@StringRes int msgRes);

    void showMessage(@NonNull CharSequence msg);


}
