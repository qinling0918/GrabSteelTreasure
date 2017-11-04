package com.zgw.qgb.ui.moudle.main;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.interf.OnBadgeCountChangeListener;

/**
 * Created by Tsinling on 2017/9/16 14:14.
 * description:
 */

public interface MainContract {
    interface IMainView extends IView, BottomNavigationBar.OnTabSelectedListener, OnBadgeCountChangeListener {
        void setText(String str);
    }

    interface IMainPresenter extends IPresenter<IMainView> {
        void login();
    }
}
