package com.zgw.qgb.net.extension;

import android.util.Log;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import retrofit2.HttpException;

/**
 * 界面描述： BaseSubscriber基类，处理返回数据
 * <p>
 * Created by tianyang on 2017/9/27.
 */

public abstract class BaseObserver<T> implements Observer<T>, SingleObserver<T> {

    private static final String TAG = "BaseObserver";
    private Disposable d;
  /*  @Override
    public void onSubscribe(Disposable d) {

    }*/

    @Override
    public void onSubscribe(@NonNull Disposable d) {
     this.d = d;
    }


    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG,getErrorCode(e)+"");
    }

    @Override
    public void onComplete() {
        if (d!=null){
            d.dispose();
        }
    }

    public static int getErrorCode(Throwable throwable) {
        if (throwable instanceof HttpException) {
            return ((HttpException) throwable).code();

        }
        return -1;
    }
}

