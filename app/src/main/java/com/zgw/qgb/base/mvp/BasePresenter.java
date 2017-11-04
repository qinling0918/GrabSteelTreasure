package com.zgw.qgb.base.mvp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by Tsinling on 2017/8/30 15:58.
 * description:
 */

public  class BasePresenter<V extends IView> implements IPresenter<V>{
    protected String TAG = this.getClass().getSimpleName();
    private WeakReference<V> viewRef;
    protected Context mContext;

    private BasePresenter() {
    }

    public BasePresenter(V view) {
        attachView(view);
    }


    @UiThread
    @Override
    public void attachView(V view) {
        viewRef = new WeakReference<V>(view);
        mContext = getContext();
        Log.d("onDestroy", "attachView"+Thread.currentThread().getName() + isViewAttached());
    }


    /**
     * Checks if a view is attached to this presenter. You should always call this method before
     * calling {@link #getView()} to get the view instance.
     */
    @UiThread
    public boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }


    @UiThread
    @NonNull
    public V getView() {

        Log.d("onDestroy", "getView"+Thread.currentThread().getName());
        if (!isViewAttached()) throw new NullPointerException("view is not attached");
        return viewRef.get();
    }

    @UiThread
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
        Log.d("onDestroy","detachView"+Thread.currentThread().getName());
    }

    protected Context getContext(){
        if(getView() instanceof Fragment){
            return ((Fragment)getView()).getActivity();
        }else if(getView() instanceof Activity){
            return (Context) getView();
        }else if(getView() instanceof View){
            return ((View)getView()).getContext();
        }
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }
}
