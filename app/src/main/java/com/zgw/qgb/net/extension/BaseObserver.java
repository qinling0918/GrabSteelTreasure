package com.zgw.qgb.net.extension;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

/**
 * 界面描述： BaseSubscriber基类，处理返回数据
 * <p>
 * Created by tianyang on 2017/9/27.
 */

public abstract class BaseObserver<T> implements Observer<T> {

    private static final String TAG = "BaseObserver";

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG,getErrorCode(e)+"");
    }

    @Override
    public void onComplete() {

    }

    public static int getErrorCode(Throwable throwable) {
        if (throwable instanceof HttpException) {
            return ((HttpException) throwable).code();

        }
        return -1;
    }
}

