package com.zgw.qgb.mvc_common.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;


import java.util.Observable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Created by qinling on 2020/8/11 11:35
 * Description:
 */
public abstract class IViewFragment extends BaseFragment implements IView {
    protected IView callback;


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IView) {
            callback = (IView) context;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void showProgress() {
        callback.showProgress();
    }

    @Override
    public void showProgress(int resId) {
        callback.showProgress(resId);
    }

    @Override
    public void showProgress(int resId, boolean cancelable) {
        callback.showProgress(resId, cancelable);
    }

    @Override
    public void showProgress(CharSequence msg, boolean cancelable) {
        callback.showProgress(msg, cancelable);
    }

    @Override
    public void hideProgress() {
        callback.hideProgress();
    }

    @Override
    public void showMessage(int msgRes) {
        callback.showMessage(msgRes);
    }

    @Override
    public void showMessage(@NonNull CharSequence msg) {
        callback.showMessage(msg);
    }

    @Override
    public void update(Observable o, Object arg) {
    }
}
