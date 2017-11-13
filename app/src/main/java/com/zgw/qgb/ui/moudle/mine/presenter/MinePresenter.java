package com.zgw.qgb.ui.moudle.mine.presenter;


import com.zgw.qgb.R;
import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.helper.rx.RxProgress;
import com.zgw.qgb.model.MainBean;
import com.zgw.qgb.net.RetrofitProvider;
import com.zgw.qgb.net.extension.BaseObserver;
import com.zgw.qgb.ui.moudle.main.MainService;
import com.zgw.qgb.ui.moudle.mine.contract.MineContract;

/**
 * Name:MinePresenter
 * Created by Tsinling on 2017/11/1 15:33.
 * description:
 */

public class MinePresenter extends BasePresenter<MineContract.IMineView> implements MineContract.IMinePresenter{
    public MinePresenter(MineContract.IMineView view) {
        super(view);
        RetrofitProvider.getService(MainService.class)
                .getNotification(1,22,10,73740)
                .compose(RxProgress.bindToLifecycle(getView(), R.string.message))
                .compose(getView().bind2Lifecycle())
                .toObservable()
                .subscribe(new BaseObserver<MainBean>() {
                    @Override
                    public void onNext(MainBean mainBean) {
                    }
                });
    }
}
