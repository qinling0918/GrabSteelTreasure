package com.zgw.qgb.ui.moudle.quote.presenter;

import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteMapContract;

/**
 * Name:QuoteMapPresenter
 * Created by Tsinling on 2017/11/1 15:33.
 * description:
 */

public class QuoteMapPresenter extends BasePresenter<QuoteMapContract.IQuoteMapView> implements QuoteMapContract.IQuoteMapPresenter{
    public QuoteMapPresenter(QuoteMapContract.IQuoteMapView view) {
        super(view);
    }
}
