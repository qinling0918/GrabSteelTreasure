package com.zgw.qgb.base;

/**
 * Name:BaseLazyFragment
 * Created by Tsinling on 2017/11/1 14:11.
 * description:
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;

import com.zgw.qgb.base.mvp.IPresenter;

import icepick.State;

/**
 * 懒加载fragment基类
 * Created by wanny on 16/7/18.
 */
public abstract class BaseLazyFragment<P extends IPresenter> extends BaseFragment<P> {
    /**
     * 懒加载过
     */
    @State boolean isLazyLoaded;
    @State boolean isPrepared;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isPrepared = true;

        lazyLoad(getUserVisibleHint());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        lazyLoad(getUserVisibleHint());
    }

    /**
     * 调用懒加载
     */

    private void lazyLoad(boolean visiable) {
        if (visiable && isPrepared && !isLazyLoaded) {
            onLazyLoad();
            isLazyLoaded = true;
        }
    }

    @UiThread
    public abstract void onLazyLoad();


}

