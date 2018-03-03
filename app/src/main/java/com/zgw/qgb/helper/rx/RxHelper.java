package com.zgw.qgb.helper.rx;

import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Kosh on 11 Nov 2016, 11:53 AM
 */

public class RxHelper {
    public static <U> SingleTransformer<U, U> scheduler_single() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
    public static <U> ObservableTransformer<U, U> scheduler_observable() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
