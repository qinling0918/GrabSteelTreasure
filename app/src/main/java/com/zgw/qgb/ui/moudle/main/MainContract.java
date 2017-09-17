package com.zgw.qgb.ui.moudle.main;

import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;

/**
 * Created by Tsinling on 2017/9/16 14:14.
 * description:
 */

public interface MainContract {
    interface IMainView extends IView {
        void setText(String str);
    }

    interface IMainPresenter extends IPresenter<IMainView> {
        void login();
    }
}
