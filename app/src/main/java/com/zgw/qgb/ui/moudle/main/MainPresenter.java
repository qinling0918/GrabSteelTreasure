package com.zgw.qgb.ui.moudle.main;

import android.util.Log;

import com.zgw.qgb.R;
import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.helper.rx.RxProgress;
import com.zgw.qgb.helper.rx.Rxdownload;
import com.zgw.qgb.model.MainBean;
import com.zgw.qgb.net.RetrofitProvider;
import com.zgw.qgb.net.extension.BaseObserver;
import com.zgw.qgb.ui.moudle.main.MainContract.IMainPresenter;
import com.zgw.qgb.ui.moudle.main.MainContract.IMainView;

import java.io.File;



/**
 * Created by Tsinling on 2017/9/8 17:11.
 * description:
 */

public class MainPresenter extends BasePresenter<IMainView> implements IMainPresenter {
     MainPresenter(IMainView view) {
        super(view);
    }

    @Override
    public void login() {
        /*http://192.168.1.18:8026/Notice/GetPushMessageList?page=1&MsgTypeId=22&pageSize=10&memberId=73740*/
        RetrofitProvider.getService(MainService.class)
                .getNotification(1,22,10,73740)
                .compose(RxProgress.bindToLifecycle(getView(), R.string.message))
                .compose(getView().bind2Lifecycle())
                .toObservable()
                .subscribe(new BaseObserver<MainBean>() {
                    @Override
                    public void onSuccess(MainBean mainBean) {
                        getView().setText(mainBean.toString());
                    }
                });

        Rxdownload.download("http://acj2.pc6.com/pc6_soure/2017-6/com.zgw.qgb_29.apk")
                .compose(RxProgress.bindToLifecycle(getView(), R.string.message))

                .subscribe(new BaseObserver<File>() {
                    @Override
                    public void onSuccess(File file) {
                        Log.d("Rxdownload", file.getAbsolutePath());
                    }
                });


    }

}
