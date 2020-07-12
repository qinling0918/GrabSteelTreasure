package com.zgw.qgb.ui.moudle.purchase.presenter;


import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.purchase.contract.PurchaseContract;

/**
 * Name:PurchasePresenter
 * Created by Tsinling on 2017/11/1 15:33.
 * description:
 */

public class PurchasePresenter extends BasePresenter<PurchaseContract.IPurchaseView> implements PurchaseContract.IPurchasePresenter{
    public PurchasePresenter(PurchaseContract.IPurchaseView view) {
        super(view);


    }

   // createAdapter();


}
