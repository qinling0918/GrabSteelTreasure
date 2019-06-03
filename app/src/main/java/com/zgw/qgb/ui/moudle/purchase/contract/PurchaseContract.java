package com.zgw.qgb.ui.moudle.purchase.contract;

import android.support.v4.app.LoaderManager;

import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;

/**
 * Created by Tsinling on 2017/9/16 14:14.
 * description:
 */

public interface PurchaseContract {
    interface IPurchaseView extends IView/*,LoaderManager.LoaderCallbacks*/{
    }

    interface IPurchasePresenter extends IPresenter<IPurchaseView> {

    }
}
