package com.zgw.qgb.ui.moudle.main;

import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.main.MainContract.*;
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
        getView().setText("login");
        getView().showProgress(0);
    }
}
