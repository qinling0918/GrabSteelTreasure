package com.zgw.qgb.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.trello.rxlifecycle2.components.support.RxAppCompatDialogFragment;
import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.helper.AnimHelper;

import butterknife.Unbinder;
import icepick.Icepick;

import static com.trello.rxlifecycle2.internal.Preconditions.checkNotNull;

/**
 * Created by Tsinling on 2017/9/9 15:28.
 * description:
 */

public abstract class BaseDialogFragment<P extends IPresenter> extends RxAppCompatDialogFragment implements IView {
    protected final String TAG = this.getClass().getSimpleName();
    protected P mPresenter;
    protected IView callback;
    @Nullable private Unbinder unbinder;

    @LayoutRes
    protected abstract int fragmentLayout();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IView) {
            callback = (IView) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = getPresenter();
        checkNotNull(mPresenter,"presenter can't be null");
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
             Icepick.restoreInstanceState(this, savedInstanceState);
            mPresenter.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
        mPresenter.onSaveInstanceState(outState);
    }

    @Override public void dismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimHelper.dismissDialog(this, getResources().getInteger(android.R.integer.config_shortAnimTime), new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    BaseDialogFragment.super.dismiss();
                }
            });
        }else{
             BaseDialogFragment.super.dismiss();
        }

    }



    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.setOnShowListener(dialogInterface -> AnimHelper.revealDialog(dialog,
                  getResources().getInteger(android.R.integer.config_longAnimTime)));
        }
        return dialog;
    }

    /**
     * 获取注入Activity的Presenter对象
     */
    protected abstract P getPresenter();



    @Override
    public void showProgress(int resId) {
        callback.showProgress(resId);
    }

    @Override
    public void hideProgress() {
        callback.hideProgress();
    }

    @Override
    public void showErrorMessage(@NonNull String msgRes) {
        callback.showErrorMessage(msgRes);
    }

    @Override
    public void showMessage(int titleRes, int msgRes) {
        callback.showMessage(titleRes,msgRes);
    }

    @Override
    public void showMessage(@NonNull String titleRes, @NonNull String msgRes) {
        callback.showMessage(titleRes,msgRes);
    }

    @Override
    public boolean isLoggedIn() {
        return callback.isLoggedIn();
    }
}
