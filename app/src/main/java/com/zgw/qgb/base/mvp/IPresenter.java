package com.zgw.qgb.base.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by tsinling on 17/9/8
 */
public interface IPresenter<V extends IView>/*<V extends IView>*/ {

    void onSaveInstanceState(Bundle outState);
    void onRestoreInstanceState(Bundle savedInstanceState);
    void attachView(V view);
    void detachView();

}
