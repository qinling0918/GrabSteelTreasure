package com.zgw.qgb.helper.rx;

import android.content.Context;
import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zgw.qgb.base.BaseActivity;
import com.zgw.qgb.base.mvp.IView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public final class RxProgress {

    private RxProgress() {
        throw new AssertionError("No instances.");
    }

   /* public static <U> SingleTransformer<U, U> bindToLifecycle(IView view, @StringRes int stringRes) {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> view.showProgress(stringRes))
                .doOnSuccess(u -> view.hideProgress())
                .doOnError(throwable -> view.hideProgress());

    }
    public static <U> SingleTransformer<U, U> bindToLifecycle(Context context, @StringRes int stringRes) {

        return bindToLifecycle(context, context.getString(stringRes));
    }

    public static <U> SingleTransformer<U, U> bindToLifecycle(Context context, CharSequence message) {
        return upstream -> {
            final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                    .content(message)
                    .progress(true, 0)
                    .build();

            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> progressDialog.show())
                    .doOnSuccess(u -> progressDialog.dismiss())
                    .doOnError(throwable -> progressDialog.dismiss());
        };
    }*/
  /*  public static <U> SingleTransformer<U, U> bindToLifecycle(Context context, CharSequence message) {
       return new SingleTransformer<U, U>() {
           @Override
           public SingleSource<U> apply(Single<U> upstream) {
               upstream

                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(((BaseActivity)context).showProgress(message))
                       .doOnSuccess(u -> progressDialog.dismiss())
                       .doOnError(throwable -> progressDialog.dismiss());
               return null;
           }
       };
    }*/
  public static <U> ObservableTransformer<U, U> bindToLifecycle_observable(IView view, @StringRes int stringRes) {
      return upstream -> upstream
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .onTerminateDetach()//解决内存泄漏
              .doOnSubscribe(disposable -> view.showProgress(stringRes))
              .doOnNext(u -> view.hideProgress())
              .doOnError(throwable -> view.hideProgress())
              .onTerminateDetach();
  }

    public static <U> ObservableTransformer<U, U> bindToLifecycle_observable(Context context, @StringRes int stringRes) {
        return bindToLifecycle_observable(context, context.getString(stringRes));
    }

    public static <U> ObservableTransformer<U, U> bindToLifecycle_observable(Context context, CharSequence message) {
        return upstream -> {
            final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                    .content(message)
                    .progress(true, 0)
                    .build();

            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> progressDialog.show())
                    .doOnNext(accept -> progressDialog.dismiss())
                    //.doOnSuccess(u -> progressDialog.dismiss())
                    .doOnError(throwable -> progressDialog.dismiss());
        };
    }

    
    public static <U> ObservableTransformer<U, U> bindToLifecycle(BaseActivity activity, @StringRes int stringRes) {

        return bindToLifecycle(activity, activity.getString(stringRes), true);
    }

    public static <U> ObservableTransformer<U, U> bindToLifecycle(BaseActivity activity, @StringRes int stringRes, boolean cancelable) {

        return bindToLifecycle(activity, activity.getString(stringRes), cancelable);
    }
    public static <U> ObservableTransformer<U, U> bindToLifecycle(BaseActivity activity, CharSequence message) {

        return bindToLifecycle(activity, message, true);
    }
    public static <U> ObservableTransformer<U, U> bindToLifecycle(BaseActivity activity, CharSequence message, boolean cancelable) {
        return upstream -> {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onTerminateDetach()//解决内存泄漏
                    .doOnSubscribe(disposable -> activity.showProgress(message, cancelable))
                    .doOnNext(u -> activity.hideProgress())
                    .doOnError(throwable -> activity.hideProgress())
                    .onTerminateDetach();
        };

    }
}
