package com.zgw.qgb.ui.moudle.mine.presenter;


import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.mine.contract.MineContract;

/**
 * Name:MinePresenter
 * Created by Tsinling on 2017/11/1 15:33.
 * description:
 */

public class MinePresenter extends BasePresenter<MineContract.IMineView> implements MineContract.IMinePresenter{
    public MinePresenter(MineContract.IMineView view) {
        super(view);

    }
}
