package com.zgw.qgb.helper.rx;

import android.content.Context;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.trello.rxlifecycle2.LifecycleTransformer;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public final class RxProgress {

    private RxProgress() {
        throw new AssertionError("No instances.");
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
                   /* .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())*/
                    .doOnSubscribe(disposable -> progressDialog.show())
                    .doOnSuccess(u -> progressDialog.dismiss())
                    .doOnError(throwable -> progressDialog.dismiss());
        };
    }


   /* public static <U> ObservableTransformer<U, U> bindToLifecycle_observable(Context context, @StringRes int stringRes) {
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
*/
}
